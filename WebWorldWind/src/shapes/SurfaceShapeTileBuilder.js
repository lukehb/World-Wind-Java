/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports SurfaceShapeTileBuilder
 * @version $Id$
 */
define([
        '../error/ArgumentError',
        '../render/DrawContext',
        '../globe/Globe',
        '../shaders/GpuProgram',
        '../util/Level',
        '../util/LevelSet',
        '../geom/Location',
        '../util/Logger',
        '../geom/Matrix',
        '../cache/MemoryCache',
        '../navigate/NavigatorState',
        '../error/NotYetImplementedError',
        '../geom/Rectangle',
        '../geom/Sector',
        '../shapes/SurfaceShape',
        '../shapes/SurfaceShapeTile',
        '../globe/Terrain',
        '../globe/TerrainTile',
        '../globe/TerrainTileList',
        '../render/TextureTile',
        '../util/Tile'
    ],
    function (ArgumentError,
              DrawContext,
              Globe,
              GpuProgram,
              Level,
              LevelSet,
              Location,
              Logger,
              Matrix,
              MemoryCache,
              NavigatorState,
              NotYetImplementedError,
              Rectangle,
              Sector,
              SurfaceShape,
              SurfaceShapeTile,
              Terrain,
              TerrainTile,
              TerrainTileList,
              TextureTile,
              Tile) {
        "use strict";

        var SurfaceShapeTileBuilder = function() {
            // Parameterize top level subdivision in one place.

            // TilesInTopLevel describes the most coarse tile structure.
            this.numRowsTilesInTopLevel = 4; // baseline: 4
            this.numColumnsTilesInTopLevel = 8; // baseline: 8

            // The maximum number of levels that will ever be tessellated.
            this.maximumSubdivisionDepth = 15; // baseline: 15

            // tileWidth, tileHeight - the number of subdivisions a single tile has; this determines the sampling grid.
            this.tileWidth = 32; // baseline: 32
            this.tileHeight = 32; // baseline: 32

            // detailHintOrigin - a parameter that describes the size of the sampling grid when fully zoomed in.
            // The size of the tile sampling grid when fully zoomed in is related to the logarithm base 10 of this parameter.
            // A parameter of 2 will have a sampling size approximately 10 times finer than a parameter of 1.
            // Parameters which result in changes of a factor of two are: 1.1, 1.4, 1.7, 2.0.
            this.detailHintOrigin = 1.1; // baseline: 1.1
            this.detailHint = 0;

            /**
             * The collection of levels.
             * @type {LevelSet}
             */
            this.levels = new LevelSet(
                Sector.FULL_SPHERE,
                new Location(
                    180 / this.numRowsTilesInTopLevel,
                    360 / this.numColumnsTilesInTopLevel),
                this.maximumSubdivisionDepth,
                this.tileWidth,
                this.tileHeight);

            /**
             * The collection of surface shapes processed by this class.
             * @type {SurfaceShape[]}
             */
            this.surfaceShapes = [];

            /**
             * The collection of surface shape tiles that actually contain surface shapes.
             * @type {SurfaceShapeTile[]}
             */
            this.surfaceShapeTiles = [];

            /**
             * The collection of top level surface shape tiles, from which actual tiles are derived.
             * @type {SurfaceShapeTile[]}}
             */
            this.topLevelTiles = null;

            /**
             * Accumulator of all sectors for surface shapes
             * @type {Sector}
             */
            this.sector = new Sector(-90, 90, -180, 180);

            /**
             * The default split scale. The split scale 2.9 has been empirically determined to render sharp lines and edges with
             * the SurfaceShapes such as SurfacePolyline and SurfacePolygon.
             *
             * @type {number}
             */
            this.SPLIT_SCALE = 1.9; // JWW uses 2.9;
        };

        /**
         * Clear all transient state from the surface shape tile builder.
         */
        SurfaceShapeTileBuilder.prototype.clear = function() {
            this.surfaceShapeTiles.splice(0);
            this.surfaceShapes.splice(0);
        };

        /**
         * Insert a surface shape to be rendered into the surface shape tile builder.
         * 
         * @param {SurfaceShape} surfaceShape A surfave shape to be processed.
         */
        SurfaceShapeTileBuilder.prototype.insertSurfaceShape = function(surfaceShape) {
            this.surfaceShapes.push(surfaceShape);
        };

        /**
         * Perform the rendering of any accumulated surface shapes by building the surface shape tiles that contain these
         * shapes and then rendering those tiles.
         * 
         * @param {DrawContext} dc The drawing context.
         */
        SurfaceShapeTileBuilder.prototype.doRender = function(dc) {
            this.buildTiles(dc);

            dc.surfaceTileRenderer.renderTiles(dc, this.surfaceShapeTiles, 1);
        };

        /**
         * Assembles the surface tiles and draws any surface shapes that have been accumulated into those offscreen tiles. The
         * surface tiles are assembled to meet the necessary resolution of to the draw context's. 
         * <p/>
         * This does nothing if there are no surface shapes associated with this builder.
         *
         * @param {DrawContext} dc The draw context to build tiles for.
         *
         * @throws {ArgumentError} If the draw context is null.
         */
        SurfaceShapeTileBuilder.prototype.buildTiles = function(dc) {
            if (!dc) {
                throw new ArgumentError(
                    Logger.logMessage(Logger.LEVEL_SEVERE, "SurfaceShapeTileBuilder", "buildTiles", "missingDc"));
            }

            if (!this.surfaceShapes || this.surfaceShapes.length < 1) {
                return;
            }

            // Determine if we can assemble and update the tiles. If not, we're done.
            if (!this.canAssembleTiles(dc))
                return;

            // Assemble the current visible tiles and update their associated textures if necessary.
            this.assembleTiles(dc);
            this.updateTiles(dc);

            // Clean up references to all surface shapes to avoid dangling references. The surface shape list is no
            // longer needed, now that the shapes are held by each tile.
            this.surfaceShapes.splice(0);
            for (var idx = 0, len = this.surfaceShapeTiles.length; idx < len; idx += 1) {
                var tile = this.surfaceShapeTiles[idx];
                tile.clearShapes();
            }
        };

        /**
         * Returns true if the draw context's viewport width and height are greater than zero.
         *
         * @param {DrawContext} dc The DrawContext to test.
         *
         * @return {boolean} true if the DrawContext's has a non-zero viewport; false otherwise.
         */
        SurfaceShapeTileBuilder.prototype.canAssembleTiles = function(dc) {
            var viewport = dc.navigatorState.viewport;
            return viewport.width > 0 && viewport.height > 0;
        };

        /**
         * Assembles a set of surface tiles that are visible in the specified DrawContext and meet the tile builder's
         * resolution criteria. Tiles are culled against the current surface shape list, against the DrawContext's view
         * frustum during rendering mode, and against the DrawContext's pick frustums during picking mode. If a tile does
         * not meet the tile builder's resolution criteria, it's split into four sub-tiles and the process recursively
         * repeated on the sub-tiles.
         * <p/>
         * During assembly, each surface shape in {@link #surfaceShapes} is sorted into the tiles they
         * intersect. The top level tiles are used as an index to quickly determine which tiles each shape intersects.
         * Surface shapes are sorted into sub-tiles by simple intersection tests, and are added to each tile's surface
         * renderable list at most once. See {@link SurfaceShapeTileBuilder.SurfaceShapeTile#addSurfaceShape(SurfaceShape,
         * gov.nasa.worldwind.geom.Sector)}. Tiles that don't intersect any surface shapes are discarded.
         *
         * @param {DrawContext} dc The DrawContext to assemble tiles for.
         */
        SurfaceShapeTileBuilder.prototype.assembleTiles = function(dc) {
            var tile;

            this.createTopLevelTiles();

            // Store the top level tiles in a set to ensure that each top level tile is added only once. Store the tiles
            // that intersect each surface shape in a set to ensure that each object is added to a tile at most once.
            var /* Set<Object> */ intersectingTiles = {}; //new HashSet<Object>();

            // Iterate over the current surface shapes, adding each surface shape to the top level tiles that it
            // intersects. This produces a set of top level tiles containing the surface shapes that intersect each
            // tile. We use the tile structure as an index to quickly determine the tiles a surface shape intersects,
            // and add object to those tiles. This has the effect of quickly sorting the objects into the top level tiles.
            // We collect the top level tiles in a HashSet to ensure there are no duplicates when multiple objects intersect
            // the same top level tiles.
            for (var idxShape = 0, lenShapes = this.surfaceShapes.length; idxShape < lenShapes; idxShape += 1) {
                var surfaceShape = this.surfaceShapes[idxShape];

                var sectors = surfaceShape.computeSectors(dc);
                if (!sectors) {
                    continue;
                }

                for (var idxSector = 0, lenSectors = sectors.length; idxSector < lenSectors; idxSector += 1) {
                    var sector = sectors[idxSector];

                    for (var idxTile = 0, lenTiles = this.topLevelTiles.length; idxTile < lenTiles; idxTile += 1) {
                        tile = this.topLevelTiles[idxTile];

                        if (tile.sector.intersects(sector)) {
                            var cacheKey = tile.tileKey;
                            intersectingTiles[cacheKey] = tile;
                            tile.addSurfaceShape(surfaceShape);
                        }
                    }
                }
            }

            // Add each top level tile or its descendants to the current tile list.
            //for (var idxTile = 0, lenTiles = this.topLevelTiles.length; idxTile < lenTiles; idxTile += 1) {
            for (var key in intersectingTiles) {
                if (intersectingTiles.hasOwnProperty(key)) {
                    tile = intersectingTiles[key];

                    this.addTileOrDescendants(dc, this.levels, null, tile);
                }
            }
        };

        /**
         * Potentially adds the specified tile or its descendants to the tile builder's surface shape tile collection.
         * The tile and its descendants are discarded if the tile is not visible or does not intersect any surface shapes in the
         * parent's surface shape list.
         * <p/>
         * If the tile meet the tile builder's resolution criteria it's added to the tile builder's
         * <code>currentTiles</code> list. Otherwise, it's split into four sub-tiles and each tile is recursively processed.
         *
         * @param {DrawContext} dc              The current DrawContext.
         * @param {LevelSet} levels             The tile's LevelSet.
         * @param {SurfaceShapeTile} parent     The tile's parent, or null if the tile is a top level tile.
         * @param {SurfaceShapeTile} tile       The tile to add.
         */
         SurfaceShapeTileBuilder.prototype.addTileOrDescendants = function(dc, levels, parent, tile) {
            // Ignore this tile if it falls completely outside the frustum. This may be the viewing frustum or the pick
            // frustum, depending on the implementation.
            if (!this.intersectsFrustum(dc, tile)) {
                // This tile is not added to the current tile list, so we clear it's object list to prepare it for use
                // during the next frame.
                tile.clearShapes();
                return;
            }

            // If the parent tile is not null, add any parent surface shapes that intersect this tile.
            if (parent != null) {
                this.addIntersectingShapes(dc, parent, tile);
            }

            // Ignore tiles that do not intersect any surface shapes.
            if (!tile.hasShapes()) {
                return;
            }

            // If this tile meets the current rendering criteria, add it to the current tile list. This tile's object list
            // is cleared after the tile update operation.
            if (this.meetsRenderCriteria(dc, levels, tile)) {
                this.addTile(tile);
                return;
            }

            var nextLevel = levels.level(tile.level.levelNumber + 1);
            var subTiles = tile.subdivide(nextLevel, this);
            for (var idxTile = 0, lenTiles = subTiles.length; idxTile < lenTiles; idxTile += 1) {
                var subTile = subTiles[idxTile];
                this.addTileOrDescendants(dc, levels, tile, subTile);
            }

            // This tile is not added to the current tile list, so we clear it's object list to prepare it for use during
            // the next frame.
            tile.clearShapes();
        };

        /**
         * Adds surface shapes from the parent's object list to the specified tile's object list. If the tile's sector
         * does not intersect the sector bounding the parent's object list, this does nothing. Otherwise, this adds any of
         * the parent's surface shapes that intersect the tile's sector to the tile's object list.
         *
         * @param {DrawContext} dc              The current DrawContext.
         * @param {SurfaceShapeTile} parent     The tile's parent.
         * @param {SurfaceShapeTile} tile       The tile to add intersecting surface shapes to.
         */
        SurfaceShapeTileBuilder.prototype.addIntersectingShapes = function(dc, parent, tile) {
            // If the parent has no objects, then there's nothing to add to this tile and we exit immediately.
            if (!parent.hasShapes())
                return;

            // If this tile does not intersect the parent's object bounding sector, then none of the parent's objects
            // intersect this tile. Therefore we exit immediately, and do not add any objects to this tile.
            if (!tile.sector.intersects(parent.sector))
                return;

            // If this tile contains the parent's object bounding sector, then all of the parent's objects intersect this
            // tile. Therefore we just add all of the parent's objects to this tile. Additionally, the parent's object
            // bounding sector becomes this tile's object bounding sector.
            if (tile.getSector().contains(parent.sector)) {
                tile.addAllSurfaceShapes(parent.getShapes());
            }
            // Otherwise, the tile may intersect some of the parent's object list. Compute which objects intersect this
            // tile, and compute this tile's bounding sector as the union of those object's sectors.
            else {
                var shapes = parent.getShapes();
                for (var idxShape = 0, lenShapes = shapes.length; idxShape < lenShapes; idxShape += 1) {
                    var shape = shapes[idxShape];

                    var sectors = shape.computeSectors(dc);
                    if (!sectors) {
                        continue;
                    }

                    // Test intersection against each of the surface shape's sectors. We break after finding an
                    // intersection to avoid adding the same object to the tile more than once.
                    for (var idxSector = 0, lenSectors = sectors.length; idxSector < lenSectors; idxSector += 1) {
                        var sector = sectors[idxSector];

                        if (tile.getSector().intersects(sector)) {
                            tile.addSurfaceShape(shape);
                            break;
                        }
                    }
                }
            }
        };

        /**
         * Adds the specified tile to this tile builder's surface tile collection.
         *
         * @param {SurfaceShapeTile} tile The tile to add.
         */
        SurfaceShapeTileBuilder.prototype.addTile = function(tile) {
            this.surfaceShapeTiles.push(tile);
        };

        /**
         * Returns a new SurfaceObjectTile corresponding to the specified {@code sector}, {@code level}, {@code row},
         * and {@code column}.
         *
         * @param {Sector} sector       The tile's Sector.
         * @param {Level} level         The tile's Level in a {@link LevelSet}.
         * @param {number} row          The tile's row in the Level, starting from 0 and increasing to the right.
         * @param {number} column       The tile's column in the Level, starting from 0 and increasing upward.
         *
         * @return {SurfaceShapeTile} a new SurfaceShapeTile.
         */
        SurfaceShapeTileBuilder.prototype.createTile = function(sector, level, row, column) {
            return new SurfaceShapeTile(sector, level, row, column);
        };

        SurfaceShapeTileBuilder.prototype.createTopLevelTiles = function() {
            this.topLevelTiles = [];
            Tile.createTilesForLevel(this.levels.firstLevel(), this, this.topLevelTiles);
        };

        /**
         * Test if the tile intersects the specified draw context's frustum. During picking mode, this tests intersection
         * against all of the draw context's pick frustums. During rendering mode, this tests intersection against the draw
         * context's viewing frustum.
         *
         * @param {DrawContext} dc   The draw context the surface shape is related to.
         * @param {SurfaceShapeTile} tile The tile to test for intersection.
         *
         * @return {boolean} true if the tile intersects the draw context's frustum; false otherwise.
         */
        SurfaceShapeTileBuilder.prototype.intersectsFrustum = function(dc, tile) {
            if (dc.globe.projectionLimits && !tile.sector.overlaps(dc.globe.projectionLimits)) {
                return false;
            }

            tile.update(dc);

            return tile.extent.intersectsFrustum(dc.navigatorState.frustumInModelCoordinates);
        };

        /**
         * Tests if the specified tile meets the rendering criteria on the specified draw context. This returns true if the
         * tile is from the level set's final level, or if the tile achieves the desired resolution on the draw context.
         *
         * @param {DrawContext} dc          The current draw context.
         * @param {LevelSet} levels         The level set the tile belongs to.
         * @param {SurfaceShapeTile} tile   The tile to test.
         *
         * @return {boolean} true if the tile meets the rendering criteria; false otherwise.
         */
        SurfaceShapeTileBuilder.prototype.meetsRenderCriteria = function(dc, levels, tile) {
            return tile.level.levelNumber == levels.lastLevel().levelNumber || !tile.mustSubdivide(dc, this.SPLIT_SCALE);
        };

        /**
         * Updates each surface shape tile in the surface shape tile collection. This is typically
         * called after {@link #assembleTiles(DrawContext)} to update the assembled tiles.
         * <p/>
         * This method does nothing if <code>currentTiles</code> is empty.
         *
         * @param {DrawContext} dc the draw context the tiles relate to.
         */
        SurfaceShapeTileBuilder.prototype.updateTiles = function(dc) {
            if (this.surfaceShapeTiles.length < 1) {
                return;
            }

            // The tile drawing rectangle has the same dimension as the current tile viewport, but it's lower left corner
            // is placed at the origin. This is because the orthographic projection setup by OGLRenderToTextureSupport
            // maps (0, 0) to the lower left corner of the drawing region, therefore we can drop the (x, y) offset when
            // drawing pixels to the texture, as (0, 0) is automatically mapped to (x, y). Since we've created the tiles
            // from a LevelSet where each level has equivalent dimension, we assume that tiles in the current tile list
            // have equivalent dimension.

            for (var idx = 0, len = this.surfaceShapeTiles.length; idx < len; idx += 1) {
                var tile = this.surfaceShapeTiles[idx];
                tile.updateTexture(dc);
            }
        };

        return SurfaceShapeTileBuilder;
    }
);