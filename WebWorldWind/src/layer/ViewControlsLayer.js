/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports ViewControlsLayer
 * @version $Id$
 */
define([
        '../error/ArgumentError',
        '../layer/Layer',
        '../util/Logger',
        '../util/Offset',
        '../shapes/ScreenImage',
        '../geom/Vec2',
        '../util/WWUtil'
    ],
    function (ArgumentError,
              Layer,
              Logger,
              Offset,
              ScreenImage,
              Vec2,
              WWUtil) {
        "use strict";

        /**
         * Constructs a view controls layer.
         * @alias ViewControlsLayer
         * @constructor
         * @augments {WorldWindow}
         * @classdesc Displays and manages view controls.
         * @param {WorldWindow} worldWindow The World Window associated with this layer.
         * This layer may not be associated with more than one World Window.
         *
         * <p>
         *     Placement of the controls within the world window is defined by the
         *     [placement]{@link ViewControlsLayer#placement} and [alignment]{@link ViewControlsLayer#alignment}
         *     properties. The default values of these properties place the view controls at the lower left corner
         *     of the world window. The placement property specifies the overall position of the controls within the
         *     world window. The alignment property specifies the alignment of the controls collection relative to
         *     the placement position. Some common combinations are:
         *     <table>
         *         <tr>
         *             <th>Location</th>
         *             <th>Placement</th>
         *             <th>Alignment</th>
         *         </tr>
         *         <tr>
         *             <td>Bottom Left</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 0</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 0</td>
         *         </tr>
         *         <tr>
         *             <td>Top Right</td>
         *             <td>WorldWind.OFFSET_FRACTION, 1, WorldWind.OFFSET_FRACTION, 1</td>
         *             <td>WorldWind.OFFSET_FRACTION, 1, WorldWind.OFFSET_FRACTION, 1</td>
         *         </tr>
         *         <tr>
         *             <td>Top Left</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 1</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 1</td>
         *         </tr>
         *         <tr>
         *             <td>Bottom Center</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0</td>
         *             <td>WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0</td>
         *         </tr>
         *         <tr>
         *             <td>Southeast</td>
         *             <td>WorldWind.OFFSET_FRACTION, 1, WorldWind.OFFSET_FRACTION, 0.25</td>
         *             <td>WorldWind.OFFSET_FRACTION, 1, WorldWind.OFFSET_FRACTION, 0.5</td>
         *         </tr>
         *     </table>
         * </p>
         * @throws {ArgumentError} If the specified world window is null or undefined.
         */
        var ViewControlsLayer = function (worldWindow) {
            if (!worldWindow) {
                throw new ArgumentError(
                    Logger.logMessage(Logger.LEVEL_SEVERE, "ViewControlsLayer", "constructor", "missingWorldWindow"));
            }

            Layer.call(this, "View Controls");

            /**
             * The World Window associated with this layer.
             * @type {WorldWindow}
             * @readonly
             */
            this.wwd = worldWindow;

            /**
             * An {@link Offset} indicating where to place the controls on the screen.
             * @type {Offset}
             * @default The lower left corner of the window.
             */
            this.placement = new Offset(WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 0);

            /**
             * An {@link Offset} indicating the alignment of the controls collection relative to the
             * [placement position]{@link ViewControlsLayer#placement}. A value of
             * {WorldWind.FRACTION, 0, WorldWind.Fraction 0} places the bottom left corner of the control collection
             * at the placement position.
             * @type {null}
             * @default The lower left corner of the window.
             */
            this.alignment = new Offset(WorldWind.OFFSET_FRACTION, 0, WorldWind.OFFSET_FRACTION, 0);

            /**
             * The incremental vertical exaggeration to apply each cycle.
             * @type {Number}
             * @default 0.01
             */
            this.exaggerationIncrement = 0.01;

            /**
             * The incremental amount to increase or decrease the eye distance (for zoom) each cycle.
             * @type {Number}
             * @default 0.04 (4%)
             */
            this.zoomIncrement = 0.04;

            /**
             * The incremental amount to increase or decrease the heading each cycle, in degrees.
             * @type {Number}
             * @default 1.0
             */
            this.headingIncrement = 1.0;

            /**
             * The incremental amount to increase or decrease the tilt each cycle, in degrees.
             * @type {Number}
             */
            this.tiltIncrement = 1.0;

            /**
             * The incremental amount to narrow or widen the field of view each cycle, in degrees.
             * @type {Number}
             * @default 0.1
             */
            this.fieldOfViewIncrement = 0.1;

            /**
             * The number of degrees to pan at the surface each cycle.
             * @type {Number}
             * @default 0.05
             */
            this.panIncrement = 0.05;

            // These are documented in their property accessors below.
            this._inactiveOpacity = 0.5;
            this._activeOpacity = 1.0;

            // Set the screen and image offsets of each control to the lower left corner.
            var screenOffset = new Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_PIXELS, 0),
                imagePath = WWUtil.currentUrlSansFilePart() + "/../images/view/";

            // These controls are all internal and intentionally not documented.
            this.panControl = new ScreenImage(screenOffset.clone(), imagePath + "view-pan-64x64.png");
            this.zoomInControl = new ScreenImage(screenOffset.clone(), imagePath + "view-zoom-in-32x32.png");
            this.zoomOutControl = new ScreenImage(screenOffset.clone(), imagePath + "view-zoom-out-32x32.png");
            this.headingLeftControl = new ScreenImage(screenOffset.clone(), imagePath + "view-heading-left-32x32.png");
            this.headingRightControl = new ScreenImage(screenOffset.clone(), imagePath + "view-heading-right-32x32.png");
            this.tiltUpControl = new ScreenImage(screenOffset.clone(), imagePath + "view-pitch-up-32x32.png");
            this.tiltDownControl = new ScreenImage(screenOffset.clone(), imagePath + "view-pitch-down-32x32.png");
            this.exaggerationUpControl = new ScreenImage(screenOffset.clone(), imagePath + "view-elevation-up-32x32.png");
            this.exaggerationDownControl = new ScreenImage(screenOffset.clone(), imagePath + "view-elevation-down-32x32.png");
            this.fovNarrowControl = new ScreenImage(screenOffset.clone(), imagePath + "view-fov-narrow-32x32.png");
            this.fovWideControl = new ScreenImage(screenOffset.clone(), imagePath + "view-fov-wide-32x32.png");

            // Disable the FOV controls by default.
            this.fovNarrowControl.enabled = false;
            this.fovWideControl.enabled = false;

            // Put the controls in an array so we can easily apply bulk operations.
            this.controls = [
                this.panControl,
                this.zoomInControl,
                this.zoomOutControl,
                this.headingLeftControl,
                this.headingRightControl,
                this.tiltUpControl,
                this.tiltDownControl,
                this.exaggerationUpControl,
                this.exaggerationDownControl,
                this.fovNarrowControl,
                this.fovWideControl
            ];

            for (var i = 0; i < this.controls.length; i++) {
                this.controls[i].imageOffset = screenOffset.clone();
                this.controls[i].opacity = this._inactiveOpacity;
            }

            // Variable to keep track of pan control center for use during interaction.
            this.panControlCenter = new Vec2(0, 0);

            // Variable to indicate whether the device is a touch device. Set to false until a touch event
            // occurs.
            this.isTouchDevice = false;

            this.setupInteraction();
        };

        ViewControlsLayer.prototype = Object.create(Layer.prototype);

        Object.defineProperties(ViewControlsLayer.prototype, {
            showPanControl: {
                get: function () {
                    return this.panControl.enabled;
                },
                set: function (value) {
                    this.panControl.enabled = value;
                }
            },

            showZoomControl: {
                get: function () {
                    return this.zoomInControl.enabled;
                },
                set: function (value) {
                    this.zoomInControl.enabled = value;
                    this.zoomOutControl.enabled = value;
                }
            },

            showHeadingControl: {
                get: function () {
                    return this.headingLeftControl.enabled;
                },
                set: function (value) {
                    this.headingLeftControl.enabled = value;
                    this.headingRightControl.enabled = value;
                }
            },

            showTiltControl: {
                get: function () {
                    return this.tiltUpControl.enabled;
                },
                set: function (value) {
                    this.tiltUpControl.enabled = value;
                    this.tiltDownControl.enabled = value;
                }
            },

            showExaggerationControl: {
                get: function () {
                    return this.exaggerationUpControl.enabled;
                },
                set: function (value) {
                    this.exaggerationUpControl.enabled = value;
                    this.exaggerationDownControl.enabled = value;
                }
            },

            showFieldOfViewControl: {
                get: function () {
                    return this.fovNarrowControl.enabled;
                },
                set: function (value) {
                    this.fovNarrowControl.enabled = value;
                    this.fovWideControl.enabled = value;
                }
            },

            /**
             * The opacity of the controls when they are not in use. The opacity should be a value between 0 and 1,
             * with 1 indicating fully opaque.
             * @type {Number}
             * @default 0.5
             */
            inactiveOpacity: {
                get: function () {
                    return this._inactiveOpacity;
                },
                set: function (value) {
                    this._inactiveOpacity = value;
                    for (var i = 0; i < this.controls.length; i++) {
                        this.controls[i].opacity = value;
                    }
                }
            },

            /**
             * The opacity of the controls when they are in use. The opacity should be a value between 0 and 1,
             * with 1 indicating fully opaque.
             * @type {Number}
             * @default 1
             */
            activeOpacity: {
                get: function () {
                    return this._activeOpacity;
                },
                set: function (value) {
                    this._activeOpacity = value;
                    for (var i = 0; i < this.controls.length; i++) {
                        this.controls[i].opacity = value;
                    }
                }
            }
        });

        // Documented in superclass.
        ViewControlsLayer.prototype.doRender = function (dc) {
            var controlPanelWidth = 0, controlPanelHeight = 64,
                panelOffset, screenOffset,
                x, y;

            this.inCurrentFrame = false;

            // Determine the dimensions of the control panel.
            if (this.showPanControl) {
                controlPanelWidth += 64;
                this.inCurrentFrame = true;
            }
            if (this.showZoomControl) {
                controlPanelWidth += 32;
                this.inCurrentFrame = true;
            }
            if (this.showHeadingControl) {
                controlPanelWidth += 32;
                this.inCurrentFrame = true;
            }
            if (this.showTiltControl) {
                controlPanelWidth += 32;
                this.inCurrentFrame = true;
            }
            if (this.showExaggerationControl) {
                controlPanelWidth += 32;
                this.inCurrentFrame = true;
            }
            if (this.showFieldOfViewControl) {
                controlPanelWidth += 32;
                this.inCurrentFrame = true;
            }

            screenOffset = this.placement.offsetForSize(dc.navigatorState.viewport.width,
                dc.navigatorState.viewport.height);
            panelOffset = this.alignment.offsetForSize(controlPanelWidth, controlPanelHeight);
            x = screenOffset[0] - panelOffset[0];
            y = screenOffset[1] - panelOffset[1];

            if (this.showPanControl) {
                this.panControl.screenOffset.x = x;
                this.panControl.screenOffset.y = y;
                this.panControl.render(dc);
                this.panControlCenter[0] = x + 32;
                this.panControlCenter[1] = y + 32;
                x += 64;
            }

            if (this.showZoomControl) {
                this.zoomOutControl.screenOffset.x = x;
                this.zoomOutControl.screenOffset.y = y;
                this.zoomInControl.screenOffset.x = x;
                this.zoomInControl.screenOffset.y = y + 32;
                this.zoomOutControl.render(dc);
                this.zoomInControl.render(dc);
                x += 32;
            }

            if (this.showHeadingControl) {
                this.headingRightControl.screenOffset.x = x;
                this.headingRightControl.screenOffset.y = y;
                this.headingLeftControl.screenOffset.x = x;
                this.headingLeftControl.screenOffset.y = y + 32;
                this.headingRightControl.render(dc);
                this.headingLeftControl.render(dc);
                x += 32;
            }

            if (this.showTiltControl) {
                this.tiltDownControl.screenOffset.x = x;
                this.tiltDownControl.screenOffset.y = y;
                this.tiltUpControl.screenOffset.x = x;
                this.tiltUpControl.screenOffset.y = y + 32;
                this.tiltDownControl.render(dc);
                this.tiltUpControl.render(dc);
                x += 32;
            }

            if (this.showExaggerationControl) {
                this.exaggerationDownControl.screenOffset.x = x;
                this.exaggerationDownControl.screenOffset.y = y;
                this.exaggerationUpControl.screenOffset.x = x;
                this.exaggerationUpControl.screenOffset.y = y + 32;
                this.exaggerationUpControl.render(dc);
                this.exaggerationDownControl.render(dc);
                x += 32;
            }

            if (this.showFieldOfViewControl) {
                this.fovNarrowControl.screenOffset.x = x;
                this.fovNarrowControl.screenOffset.y = y;
                this.fovWideControl.screenOffset.x = x;
                this.fovWideControl.screenOffset.y = y + 32;
                this.fovNarrowControl.render(dc);
                this.fovWideControl.render(dc);
            }
        };

        ViewControlsLayer.prototype.setupInteraction = function () {
            var wwd = this.wwd,
                thisLayer = this;

            var handleMouseEvent = function (e) {
                // Prevent handling of simulated mouse events on touch devices.
                if (thisLayer.isTouchDevice) {
                    return;
                }

                var topObject, operation;

                if (e.type && (e.type === "mousemove") && thisLayer.highlightedControl) {
                    thisLayer.highlight(thisLayer.highlightedControl, false);
                } else if (e.type && (e.type === "mouseup") && thisLayer.activeControl) {
                    thisLayer.activeControl = null;
                    e.preventDefault();
                    return;
                }

                topObject = wwd.pick(wwd.canvasCoordinates(e.clientX, e.clientY)).topPickedObject();
                operation = thisLayer.determineOperation(e, topObject);
                if (operation) {
                    operation.call(thisLayer, e, topObject);
                    thisLayer.handleHighlight(e, topObject);
                }
            };

            wwd.addEventListener("mousedown", handleMouseEvent);
            wwd.addEventListener("mouseup", handleMouseEvent);
            wwd.addEventListener("mousemove", handleMouseEvent);
            window.addEventListener("mouseup", handleMouseEvent);
            window.addEventListener("mousemove", handleMouseEvent);

            var handleTouchEvent = function (e) {
                this.isTouchDevice = true;

                // Turn off any highlight. If a button is in use it will be highlighted later.
                if (thisLayer.highlightedControl) {
                    thisLayer.highlight(thisLayer.highlightedControl, false);
                    thisLayer.wwd.redraw();
                }

                if (e.type && (e.type === "touchend" || e.type === "touchcancel")) {
                    if (thisLayer.activeControl && thisLayer.isCurrentTouch(e)) {
                        thisLayer.activeControl = null;
                        e.preventDefault();
                    }
                } else {
                    var topObject,
                        touch = e.changedTouches.item(0),
                        operation;

                    topObject = wwd.pick(wwd.canvasCoordinates(touch.clientX, touch.clientY)).topPickedObject();
                    operation = thisLayer.determineOperation(e, topObject);
                    if (operation) {
                        operation.call(thisLayer, e, topObject);
                        thisLayer.handleHighlight(e, topObject);
                    }
                }
            };

            wwd.addEventListener("touchstart", handleTouchEvent);
            wwd.addEventListener("touchend", handleTouchEvent);
            wwd.addEventListener("touchcancel", handleTouchEvent);
            wwd.addEventListener("touchmove", handleTouchEvent);
        };

        ViewControlsLayer.prototype.determineOperation = function (e, topObject) {
            var operation = null;

            if (topObject && (topObject.userObject instanceof ScreenImage)) {
                if (topObject.userObject === this.panControl) {
                    operation= this.handlePan;
                } else if (topObject.userObject === this.zoomInControl
                    || topObject.userObject === this.zoomOutControl) {
                    operation = this.handleZoom;
                } else if (topObject.userObject === this.headingLeftControl
                    || topObject.userObject === this.headingRightControl) {
                    operation = this.handleHeading;
                } else if (topObject.userObject === this.tiltUpControl
                    || topObject.userObject === this.tiltDownControl) {
                    operation = this.handleTilt;
                } else if (topObject.userObject === this.exaggerationUpControl
                    || topObject.userObject === this.exaggerationDownControl) {
                    operation = this.handleExaggeration;
                } else if (topObject.userObject === this.fovNarrowControl
                    || topObject.userObject === this.fovWideControl) {
                    operation = this.handleFov;
                }
            }

            return operation;
        };

        ViewControlsLayer.prototype.isCurrentTouch = function (e) {
            for (var i = 0; i < e.changedTouches.length; i++) {
                if (e.changedTouches.item(i).identifier === this.currentTouchId) {
                    return true;
                }
            }

            return false;
        };

        ViewControlsLayer.prototype.handlePan = function (e, pickedObject) {
            if (e.type === "mousedown" || e.type === "mousemove") {
                this.currentEventPoint = this.wwd.canvasCoordinates(e.clientX, e.clientY);
            } else if (e.type === "touchstart" || e.type === "touchmove") {
                var touch = e.changedTouches.item(0);
                this.currentEventPoint = this.wwd.canvasCoordinates(touch.clientX, touch.clientY);
            }

            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setLookAtPosition = function () {
                    if (thisLayer.activeControl) {
                        var dx = thisLayer.panControlCenter[0] - thisLayer.currentEventPoint[0],
                            dy = thisLayer.panControlCenter[1]
                                - (thisLayer.wwd.viewport.height - thisLayer.currentEventPoint[1]),
                            oldLat = thisLayer.wwd.navigator.lookAtPosition.latitude,
                            oldLon = thisLayer.wwd.navigator.lookAtPosition.longitude,
                        // Scale the increment by a constant and the relative distance of the eye to the surface.
                            scale = thisLayer.panIncrement
                                * (thisLayer.wwd.navigator.range / thisLayer.wwd.globe.radiusAt(oldLat, oldLon));

                        thisLayer.wwd.navigator.lookAtPosition.latitude = oldLat - dy * scale;
                        thisLayer.wwd.navigator.lookAtPosition.longitude = oldLon - dx * scale;
                        thisLayer.wwd.redraw();
                        setTimeout(setLookAtPosition, 50);
                    }
                };
                setTimeout(setLookAtPosition, 50);
            }
        };

        ViewControlsLayer.prototype.handleZoom = function (e, pickedObject) {
            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setRange = function () {
                    if (thisLayer.activeControl) {
                        if (thisLayer.activeControl === thisLayer.zoomInControl) {
                            thisLayer.wwd.navigator.range *= (1 - thisLayer.zoomIncrement);
                        } else if (thisLayer.activeControl === thisLayer.zoomOutControl) {
                            thisLayer.wwd.navigator.range *= (1 + thisLayer.zoomIncrement);
                        }
                        thisLayer.wwd.redraw();
                        setTimeout(setRange, 50);
                    }
                };
                setTimeout(setRange, 50);
            }
        };

        ViewControlsLayer.prototype.handleHeading = function (e, pickedObject) {
            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setRange = function () {
                    if (thisLayer.activeControl) {
                        if (thisLayer.activeControl === thisLayer.headingLeftControl) {
                            thisLayer.wwd.navigator.heading += thisLayer.headingIncrement;
                        } else if (thisLayer.activeControl === thisLayer.headingRightControl) {
                            thisLayer.wwd.navigator.heading -= thisLayer.headingIncrement;
                        }
                        thisLayer.wwd.redraw();
                        setTimeout(setRange, 50);
                    }
                };
                setTimeout(setRange, 50);
            }
        };

        ViewControlsLayer.prototype.handleTilt = function (e, pickedObject) {
            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setRange = function () {
                    if (thisLayer.activeControl) {
                        if (thisLayer.activeControl === thisLayer.tiltUpControl) {
                            thisLayer.wwd.navigator.tilt =
                                Math.max(0, thisLayer.wwd.navigator.tilt - thisLayer.tiltIncrement);
                        } else if (thisLayer.activeControl === thisLayer.tiltDownControl) {
                            thisLayer.wwd.navigator.tilt =
                                Math.min(90, thisLayer.wwd.navigator.tilt + thisLayer.tiltIncrement);
                        }
                        thisLayer.wwd.redraw();
                        setTimeout(setRange, 50);
                    }
                };
                setTimeout(setRange, 50);
            }
        };

        ViewControlsLayer.prototype.handleExaggeration = function (e, pickedObject) {
            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setExaggeration = function () {
                    if (thisLayer.activeControl) {
                        if (thisLayer.activeControl === thisLayer.exaggerationUpControl) {
                            thisLayer.wwd.verticalExaggeration += thisLayer.exaggerationIncrement;
                        } else if (thisLayer.activeControl === thisLayer.exaggerationDownControl) {
                            thisLayer.wwd.verticalExaggeration =
                                Math.max(0, thisLayer.wwd.verticalExaggeration - thisLayer.exaggerationIncrement);
                        }
                        thisLayer.wwd.redraw();
                        setTimeout(setExaggeration, 50);
                    }
                };
                setTimeout(setExaggeration, 50);
            }
        };

        ViewControlsLayer.prototype.handleFov = function (e, pickedObject) {
            if ((e.type === "mousedown" && e.which === 1) || (e.type === "touchstart")) {
                this.activeControl = pickedObject.userObject;
                e.preventDefault();

                if (e.type === "touchstart") {
                    this.currentTouchId = e.changedTouches.item(0).identifier;
                }

                var thisLayer = this;
                var setRange = function () {
                    if (thisLayer.activeControl) {
                        if (thisLayer.activeControl === thisLayer.fovWideControl) {
                            thisLayer.wwd.navigator.fieldOfView =
                                Math.max(90, thisLayer.wwd.navigator.fieldOfView + thisLayer.fieldOfViewIncrement);
                        } else if (thisLayer.activeControl === thisLayer.fovNarrowControl) {
                            thisLayer.wwd.navigator.fieldOfView =
                                Math.min(0, thisLayer.wwd.navigator.fieldOfView - thisLayer.fieldOfViewIncrement);
                        }
                        thisLayer.wwd.redraw();
                        setTimeout(setRange, 50);
                    }
                };
                setTimeout(setRange, 50);
            }
        };

        ViewControlsLayer.prototype.handleHighlight = function (e, pickedObject) {
            if (e.type === "mousemove" || e.type === "touchstart" || e.type === "touchmove") {
                this.highlight(pickedObject.userObject, true);
            }
        };

        ViewControlsLayer.prototype.highlight = function (control, tf) {
            control.opacity = tf ? this._activeOpacity : this._inactiveOpacity;

            if (tf) {
                this.highlightedControl = control;
            } else {
                this.highlightedControl = null;
            }
        };

        return ViewControlsLayer;
    });