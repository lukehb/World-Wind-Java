/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports Compass
 * @version $Id$
 */
define([
        '../error/ArgumentError',
        '../util/Logger',
        '../util/Offset',
        '../shapes/ScreenImage',
        '../geom/Vec3',
        '../util/WWUtil'
    ],
    function (ArgumentError,
              Logger,
              Offset,
              ScreenImage,
              Vec3,
              WWUtil) {
        "use strict";

        /**
         * Constructs a compass.
         * @alias Compass
         * @constructor
         * @augments ScreenImage
         * @classdesc Displays a compass image at a specified location in the World Window. The compass image rotates
         * and tilts to reflect the current navigator's heading and tilt.
         * @param {Offset} screenOffset The offset indicating the image's placement on the screen. If null or undefined
         * the compass is placed at the upper-right corner of the World Window.
         * Use [the image offset property]{@link ScreenImage#imageOffset} to position the image relative to the
         * screen point.
         * @param {String} imagePath The URL of the image to display. If null or undefined, a default compass image is used.
         */
        var Compass = function (screenOffset, imagePath) {

            var sOffset = screenOffset ? screenOffset
                : new Offset(WorldWind.OFFSET_FRACTION, 1, WorldWind.OFFSET_FRACTION, 1), // upper-right placement
                iPath = imagePath ? imagePath : WWUtil.currentUrlSansFilePart() + "/../images/notched-compass.png";

            ScreenImage.call(this, sOffset, iPath);

            // Must set the default image offset and scale after calling the constructor above.

            if (!screenOffset) {
                // Align the upper right corner of the image with the screen point, and give the image some padding.
                this.imageOffset = new Offset(WorldWind.OFFSET_FRACTION, 1.1, WorldWind.OFFSET_FRACTION, 1.1);
            }

            if (!imagePath) {
                // Scale the default image to half size.
                this.imageScale = 0.5;
            }
        };

        Compass.prototype = Object.create(ScreenImage.prototype);

        Compass.prototype.render = function (dc) {
            // Capture the navigator's heading and tilt and apply it to the compass' screen image.
            this.imageRotation = dc.navigatorState.heading;
            this.imageTilt = dc.navigatorState.tilt;

            ScreenImage.prototype.render.call(this, dc);
        };

        return Compass;
    })
;