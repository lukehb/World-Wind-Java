/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports WorldWindow
 * @version $Id$
 */
define([
        './error/ArgumentError',
        './render/DrawContext',
        './util/FrameStatistics',
        './globe/Globe',
        './cache/GpuResourceCache',
        './layer/LayerList',
        './util/Logger',
        './navigate/LookAtNavigator',
        './navigate/NavigatorState',
        './geom/Rectangle',
        './geom/Sector',
        './globe/Terrain',
        './globe/Tessellator',
        './globe/ZeroElevationModel'],
    function (ArgumentError,
              DrawContext,
              FrameStatistics,
              Globe,
              GpuResourceCache,
              LayerList,
              Logger,
              LookAtNavigator,
              NavigatorState,
              Rectangle,
              Sector,
              Terrain,
              Tessellator,
              ZeroElevationModel) {
        "use strict";

        /**
         * Constructs a World Wind window for an HTML canvas.
         * @alias WorldWindow
         * @constructor
         * @classdesc Represents a World Wind window for an HTML canvas.
         * @param {String} canvasName The name assigned to the canvas in the HTML page.
         */
        var WorldWindow = function (canvasName) {
            if (!(window.WebGLRenderingContext)) {
                throw new ArgumentError(
                    Logger.logMessage(Logger.LEVEL_SEVERE, "WorldWindow", "constructor",
                        "The specified canvas does not support WebGL."));
            }

            this.canvas = document.getElementById(canvasName);

            this.canvas.addEventListener("webglcontextlost", handleContextLost, false);
            this.canvas.addEventListener("webglcontextrestored", handleContextRestored, false);

            function handleContextLost(event) {
                event.preventDefault();
                this.gpuResourceCache.clear();
            }

            function handleContextRestored(event) {
            }

            var gl = this.canvas.getContext("webgl");

            /**
             * The number of bits in the depth buffer associated with this World Window.
             * @type {number}
             */
            this.depthBits = gl.getParameter(WebGLRenderingContext.DEPTH_BITS);

            /**
             * The current viewport of this World Window.
             * @type {Rectangle}
             */
            this.viewport = new Rectangle(0, 0, this.canvas.width, this.canvas.height);

            /**
             * The globe displayed.
             * @type {Globe}
             */
            this.globe = new Globe(new ZeroElevationModel());

            /**
             * The layers to display.
             * @type {LayerList}
             */
            this.layers = new LayerList();

            /**
             * The navigator used to manipulate the globe.
             * @type {LookAtNavigator}
             * @default [LookAtNavigator]{@link LookAtNavigator}
             */
            this.navigator = new LookAtNavigator(this);

            /**
             * The tessellator used to create the globe's terrain.
             * @type {Tessellator}
             */
            this.tessellator = new Tessellator();

            /**
             * The vertical exaggeration to apply to the terrain.
             * @type {Number}
             */
            this.verticalExaggeration = 1;

            /**
             * Performance statistics for this WorldWindow.
             * @type {FrameStatistics}
             */
            this.frameStatistics = new FrameStatistics();

            // Internal. Intentionally not documented.
            this.gpuResourceCache = new GpuResourceCache();

            // Internal. Intentionally not documented.
            this.drawContext = new DrawContext();
            this.drawContext.canvas = this.canvas;

            // Set up to handle redraw events.
            var thisWindow = this;
            this.canvas.addEventListener(WorldWind.REDRAW_EVENT_TYPE, function (event) {
                thisWindow.redraw();
            }, false);
        };

        /**
         * Redraws the window.
         */
        WorldWindow.prototype.redraw = function () {
            try {
                this.resetDrawContext();
                this.drawFrame();
            } catch (e) {
                Logger.logMessage(Logger.LEVEL_SEVERE, "WorldWindow", "redraw",
                    "Exception occurred during rendering: " + e.toString());
            }
        };

        // Internal. Intentionally not documented.
        WorldWindow.prototype.resetDrawContext = function () {
            var dc = this.drawContext;

            dc.reset();
            dc.globe = this.globe;
            dc.layers = this.layers;
            dc.navigatorState = this.navigator.currentState();
            dc.verticalExaggeration = this.verticalExaggeration;
            dc.frameStatistics = this.frameStatistics;
            dc.update();
        };

        /* useful stuff to debug WebGL */
        /*
        function logGLCall(functionName, args) {
            console.log("gl." + functionName + "(" +
            WebGLDebugUtils.glFunctionArgsToString(functionName, args) + ")");
        };

        function validateNoneOfTheArgsAreUndefined(functionName, args) {
            for (var ii = 0; ii < args.length; ++ii) {
                if (args[ii] === undefined) {
                    console.error("undefined passed to gl." + functionName + "(" +
                    WebGLDebugUtils.glFunctionArgsToString(functionName, args) + ")");
                }
            }
        };

        WorldWindow.prototype.logAndValidate = function logAndValidate(functionName, args) {
            logGLCall(functionName, args);
            validateNoneOfTheArgsAreUndefined (functionName, args);
        };

        WorldWindow.prototype.throwOnGLError = function throwOnGLError(err, funcName, args) {
            throw WebGLDebugUtils.glEnumToString(err) + " was caused by call to: " + funcName;
        };
        */

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.drawFrame = function () {
            this.drawContext.frameStatistics.beginFrame();

            var gl = this.canvas.getContext("webgl");

            // uncomment to debug WebGL
            //var gl = WebGLDebugUtils.makeDebugContext(this.canvas.getContext("webgl"),
            //        this.throwOnGLError,
            //        this.logAndValidate
            //);

            this.drawContext.currentGlContext = gl;

            this.viewport = new Rectangle(0, 0, this.canvas.width, this.canvas.height);

            try {
                this.beginFrame(this.drawContext, this.viewport);
                this.createTerrain(this.drawContext);
                this.clearFrame(this.drawContext);
                this.doDraw(this.drawContext);
            } finally {
                this.endFrame(this.drawContext);
                this.drawContext.frameStatistics.endFrame();
            }
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.beginFrame = function (dc, viewport) {
            var gl = dc.currentGlContext;

            gl.viewport(viewport.x, viewport.y, viewport.width, viewport.height);
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.endFrame = function (dc) {
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.clearFrame = function (dc) {
            var gl = dc.currentGlContext;

            gl.clearColor(dc.clearColor.red, dc.clearColor.green, dc.clearColor.blue, dc.clearColor.alpha);
            gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.doDraw = function (dc) {
            this.drawLayers();
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.createTerrain = function (dc) {
            // TODO: Implement Tessellator to return a Terrain rather than synthesizing this copy here.
            dc.terrain = new Terrain(); // TODO: have Tessellator.tessellate() return a filled out one of these
            dc.terrain.surfaceGeometry = this.tessellator.tessellate(dc).tileArray;
            dc.terrain.globe = dc.globe;
            dc.terrain.tessellator = this.tessellator;
            dc.terrain.verticalExaggeration = dc.verticalExaggeration;
            dc.terrain.sector = Sector.FULL_SPHERE;

            dc.frameStatistics.setTerrainTileCount(
                this.drawContext.terrain && this.drawContext.terrain.surfaceGeometry ?
                    this.drawContext.terrain.surfaceGeometry.length : 0);
        };

        // Internal function. Intentionally not documented.
        WorldWindow.prototype.drawLayers = function () {
            // Draw all the layers attached to this WorldWindow.

            var beginTime = new Date().getTime(),
                dc = this.drawContext,
                layers = this.drawContext.layers.layers,
                layer;

            for (var i = 0, len = layers.length; i < len; i++) {
                layer = layers[i];
                if (layer) {
                    dc.currentLayer = layer;
                    try {
                        layer.render(dc);
                    } catch (e) {
                        Logger.log(Logger.LEVEL_SEVERE, "Error while rendering layer " + layer.displayName + ".");
                        // Keep going. Render the rest of the layers.
                    }
                }
            }

            var now = new Date().getTime();
            dc.frameStatistics.layerRenderingTime = now - beginTime;
        };

        return WorldWindow;
    }
)
;