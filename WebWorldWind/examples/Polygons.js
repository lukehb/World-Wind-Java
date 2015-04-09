/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * Illustrates how to display and pick Polygons.
 *
 * @version $Id$
 */

requirejs(['../src/WorldWind',
        './LayerManager/LayerManager'],
    function (ww,
              LayerManager) {
        "use strict";

        // Tell World Wind to log only warnings and errors.
        WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);

        // Create the World Window.
        var wwd = new WorldWind.WorldWindow("canvasOne");

        /**
         * Added imagery layers.
         */
        var layers = [
            {layer: new WorldWind.BMNGLayer(), enabled: true},
            {layer: new WorldWind.BMNGLandsatLayer(), enabled: false},
            {layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true},
            {layer: new WorldWind.CompassLayer(), enabled: true},
            {layer: new WorldWind.ViewControlsLayer(wwd), enabled: true}
        ];

        for (var l = 0; l < layers.length; l++) {
            layers[l].layer.enabled = layers[l].enabled;
            wwd.addLayer(layers[l].layer);
        }

        // Define an outer and an inner boundary to make a polygon with a hole.
        var boundaries = [];
        boundaries[0] = []; // outer boundary
        boundaries[0].push(new WorldWind.Position(40, -100, 1e5));
        boundaries[0].push(new WorldWind.Position(45, -110, 1e5));
        boundaries[0].push(new WorldWind.Position(40, -120, 1e5));
        boundaries[1] = []; // inner boundary
        boundaries[1].push(new WorldWind.Position(41, -103, 1e5));
        boundaries[1].push(new WorldWind.Position(44, -110, 1e5));
        boundaries[1].push(new WorldWind.Position(41, -117, 1e5));

        // Create the polygon and assign its attributes.

        var polygon = new WorldWind.Polygon(boundaries);
        polygon.altitudeMode = WorldWind.ABSOLUTE;
        polygon.extrude = true; // extrude the polygon edges to the ground

        var polygonAttributes = new WorldWind.ShapeAttributes(null);
        polygonAttributes.drawInterior = true;
        polygonAttributes.drawOutline = true;
        polygonAttributes.outlineColor = WorldWind.Color.BLUE;
        polygonAttributes.interiorColor = new WorldWind.Color(0, 1, 1, 0.5);
        polygonAttributes.drawVerticals = polygon.extrude;
        polygon.attributes = polygonAttributes;
        var highlightAttributes = new WorldWind.ShapeAttributes(polygonAttributes);
        highlightAttributes.outlineColor = WorldWind.Color.RED;
        highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0.5);
        polygon.highlightAttributes = highlightAttributes;

        // Add the surface image to a layer and the layer to the World Window's layer list.
        var pathsLayer = new WorldWind.RenderableLayer();
        pathsLayer.displayName = "Polygon";
        pathsLayer.addRenderable(polygon);
        wwd.addLayer(pathsLayer);

        // Draw the World Window for the first time.
        wwd.redraw();

        // Create a layer manager for controlling layer visibility.
        var layerManger = new LayerManager('divLayerManager', wwd);

        // Now set up to handle picking.

        var highlightedItems = [];

        // The pick-handling callback function.
        var handlePick = function (o) {
            // The input argument is either an Event or a TapRecognizer. Both have the same properties for determining
            // the mouse or tap location.
            var x = o.clientX,
                y = o.clientY;

            var redrawRequired = highlightedItems.length > 0; // must redraw if we de-highlight previously picked items

            // De-highlight any previously highlighted placemarks.
            for (var h = 0; h < highlightedItems.length; h++) {
                highlightedItems[h].highlighted = false;
            }
            highlightedItems = [];

            // Perform the pick. Must first convert from window coordinates to canvas coordinates, which are
            // relative to the upper left corner of the canvas rather than the upper left corner of the page.
            var pickList = wwd.pick(wwd.canvasCoordinates(x, y));
            if (pickList.objects.length > 0) {
                redrawRequired = true;
            }

            // Highlight the items picked by simply setting their highlight flag to true.
            if (pickList.objects.length > 0) {
                for (var p = 0; p < pickList.objects.length; p++) {
                    pickList.objects[p].userObject.highlighted = true;

                    // Keep track of highlighted items in order to de-highlight them later.
                    highlightedItems.push(pickList.objects[p].userObject);
                }
            }

            // Update the window if we changed anything.
            if (redrawRequired) {
                wwd.redraw(); // redraw to make the highlighting changes take effect on the screen
            }
        };

        // Listen for mouse moves and highlight the placemarks that the cursor rolls over.
        wwd.addEventListener("mousemove", handlePick);

        // Listen for taps on mobile devices and highlight the placemarks that the user taps.
        var tapRecognizer = new WorldWind.TapRecognizer(wwd);
        tapRecognizer.addGestureListener(handlePick);
    });