/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports GeographicText
 * @version $Id$
 */
define([
        '../error/ArgumentError',
        '../util/Logger',
        '../shapes/Text',
        '../geom/Vec3'
    ],
    function (ArgumentError,
              Logger,
              Text,
              Vec3) {
        "use strict";

        /**
         * Constructs a geographic text shape at a specified position.
         * @alias GeographicText
         * @constructor
         * @augments Text
         * @classdesc Represents a string of text displayed at a geographic position.
         * <p>
         * See also {@link ScreenText}.
         *
         * @param {Position} position The text's geographic position.
         * @param {String} text The text to display.
         * @throws {ArgumentError} If either the specified position or text is null or undefined.
         */
        var GeographicText = function (position, text) {
            if (!position) {
                throw new ArgumentError(
                    Logger.logMessage(Logger.LEVEL_SEVERE, "Text", "constructor", "missingPosition"));
            }

            Text.call(this, text);

            /**
             * This text's geographic position.
             * @type {Position}
             */
            this.position = position;
        };

        // Internal use only. Intentionally not documented.
        GeographicText.placePoint = new Vec3(0, 0, 0); // Cartesian point corresponding to this placemark's geographic position

        GeographicText.prototype = Object.create(Text.prototype);

        GeographicText.prototype.computeScreenPointAndEyeDistance = function (dc) {
            // Compute the text's model point and corresponding distance to the eye point.
            dc.terrain.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude,
                this.altitudeMode, GeographicText.placePoint);

            this.eyeDistance = this.alwaysOnTop ? 0 : dc.navigatorState.eyePoint.distanceTo(GeographicText.placePoint);

            // Compute the text's screen point in the OpenGL coordinate system of the WorldWindow by projecting its model
            // coordinate point onto the viewport. Apply a depth offset in order to cause the text to appear above nearby
            // terrain. When text is displayed near the terrain portions of its geometry are often behind the terrain,
            // yet as a screen element the text is expected to be visible. We adjust its depth values rather than moving
            // the text itself to avoid obscuring its actual position.
            if (!dc.navigatorState.projectWithDepth(GeographicText.placePoint, this.depthOffset, Text.screenPoint)) {
                return null;
            }
        };

        return GeographicText;
    });