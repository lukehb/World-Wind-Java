/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports PinchRecognizer
 * @version $Id$
 */
define(['../gesture/GestureRecognizer'],
    function (GestureRecognizer) {
        "use strict";

        /**
         * Constructs a pinch gesture recognizer.
         * @alias PinchRecognizer
         * @constructor
         * @augments GestureRecognizer
         * @classdesc A concrete gesture recognizer subclass that looks for two finger pinch gestures.
         * @throws {ArgumentError} If the specified target is null or undefined.
         */
        var PinchRecognizer = function (target) {
            GestureRecognizer.call(this, target);

            // Intentionally not documented.
            this._scale = 1;

            // Intentionally not documented.
            this._offsetScale = 1;

            // Intentionally not documented.
            this.referenceDistance = 0;

            // Intentionally not documented.
            this.interpretThreshold = 20;

            // Intentionally not documented.
            this.weight = 0.4;

            // Intentionally not documented.
            this.pinchTouches = [];
        };

        PinchRecognizer.prototype = Object.create(GestureRecognizer.prototype);

        Object.defineProperties(PinchRecognizer.prototype, {
            scale: {
                get: function () {
                    return this._scale * this._offsetScale;
                }
            }
        });

        // Documented in superclass.
        PinchRecognizer.prototype.reset = function () {
            GestureRecognizer.prototype.reset.call(this);

            this._scale = 1;
            this._offsetScale = 1;
            this.referenceDistance = 0;
            this.pinchTouches = [];
        };

        // Documented in superclass.
        PinchRecognizer.prototype.mouseDown = function (event) {
            if (this.state == WorldWind.POSSIBLE) {
                this.state = WorldWind.FAILED; // touch gestures fail upon receiving a mouse event
            }
        };

        // Documented in superclass.
        PinchRecognizer.prototype.touchStart = function (touch) {
            if (this.pinchTouches.length < 2) {
                if (this.pinchTouches.push(touch) == 2) {
                    this.referenceDistance = this.currentPinchDistance();
                    this._offsetScale *= this._scale;
                    this._scale = 1;
                }
            }
        };

        // Documented in superclass.
        PinchRecognizer.prototype.touchMove = function (touch) {
            if (this.pinchTouches.length == 2) {
                if (this.state == WorldWind.POSSIBLE) {
                    if (this.shouldRecognize()) {
                        this.referenceDistance = this.currentPinchDistance();
                        this._scale = 1;
                        this.state = WorldWind.BEGAN;
                    }
                } else if (this.state == WorldWind.BEGAN || this.state == WorldWind.CHANGED) {
                    var distance = this.currentPinchDistance(),
                        newScale = Math.abs(distance / this.referenceDistance),
                        w = this.weight;
                    this._scale = this._scale * (1 - w) + newScale * w;
                    this.state = WorldWind.CHANGED;
                }
            }
        };

        // Documented in superclass.
        PinchRecognizer.prototype.touchEnd = function (touch) {
            var index = this.pinchTouches.indexOf(touch);
            if (index != -1) {
                this.pinchTouches.splice(index, 1);
            }

            // Transition to the ended state if this was the last touch.
            if (this.touchCount == 0) { // last touch ended
                if (this.state == WorldWind.POSSIBLE) {
                    this.state = WorldWind.FAILED;
                } else if (this.state == WorldWind.BEGAN || this.state == WorldWind.CHANGED) {
                    this.state = WorldWind.ENDED;
                }
            }
        };

        // Documented in superclass.
        PinchRecognizer.prototype.touchCancel = function (touch) {
            var index = this.pinchTouches.indexOf(touch);
            if (index != -1) {
                this.pinchTouches.splice(index, 1);
            }

            // Transition to the cancelled state if this was the last touch.
            if (this.touchCount == 0) {
                if (this.state == WorldWind.POSSIBLE) {
                    this.state = WorldWind.FAILED;
                } else if (this.state == WorldWind.BEGAN || this.state == WorldWind.CHANGED) {
                    this.state = WorldWind.CANCELLED;
                }
            }
        };

        // Intentionally not documented.
        PinchRecognizer.prototype.shouldRecognize = function () {
            var distance = this.currentPinchDistance();

            return Math.abs(distance - this.referenceDistance) > this.interpretThreshold
        };

        // Intentionally not documented.
        PinchRecognizer.prototype.currentPinchDistance = function () {
            var touch0 = this.pinchTouches[0],
                touch1 = this.pinchTouches[1],
                dx = touch0.clientX - touch1.clientX,
                dy = touch0.clientY - touch1.clientY;

            return Math.sqrt(dx * dx + dy * dy);
        };

        return PinchRecognizer;
    });