/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports ProjectionUPS
 * @version $Id$
 */
define([
        '../geom/Angle',
        '../error/ArgumentError',
        '../projections/GeographicProjection',
        '../util/Logger',
        '../geom/Sector',
        '../geom/Vec3',
        '../util/WWMath'
    ],
    function (Angle,
              ArgumentError,
              GeographicProjection,
              Logger,
              Sector,
              Vec3,
              WWMath) {
        "use strict";

        /**
         * Constructs a Uniform Polar Stereographic geographic projection.
         * @alias ProjectionUPS
         * @constructor
         * @augments GeographicProjection
         * @classdesc Represents a Uniform Polar Stereographic geographic projection.
         * @param {String} pole Indicates the north or south aspect. Specify "North" for the north aspect or "South"
         * for the south aspect.
         */
        var ProjectionUPS = function (pole) {

            // Internal. Intentionally not documented.
            this.north = !(pole === "South");

            var limits = this.north ? new Sector(0, 90, -180, 180) : new Sector(-90, 0, -180, 180);

            GeographicProjection.call(this, "Uniform Polar Stereographic", false, limits);

            // Internal. Intentionally not documented. See "pole" property accessor below for public interface.
            this._pole = pole;

            // Documented in superclass.
            this.displayName = this.north ? "North UPS" : "South UPS";

            // Internal. Intentionally not documented. See "stateKey" property accessor below for public interface.
            this._stateKey = "projection ups " + this._pole + " ";
        };

        ProjectionUPS.prototype = Object.create(GeographicProjection.prototype);

        Object.defineProperties(ProjectionUPS.prototype, {

            /**
             * Indicates the north or south aspect. Specify "North" or "South".
             * @memberof ProjectionPolarEquidistant.prototype
             * @type {String}
             */
            pole: {
                get: function () {
                    return this._pole;
                },
                set: function (pole) {
                    this._pole = pole;
                    this.north = !(this._pole === "South");
                    this.projectionLimits = this.north ? new Sector(0, 90, -180, 180) : new Sector(-90, 0, -180, 180);
                    this._stateKey = "projection ups " + this._pole + " ";
                }
            },

            /**
             * A string identifying this projection's current state. Used to compare states during rendering to
             * determine whether globe-state dependent cached values must be updated. Applications typically do not
             * interact with this property.
             * @memberof ProjectionPolarEquidistant.prototype
             * @readonly
             * @type {String}
             */
            stateKey: {
                get: function () {
                    return this._stateKey;
                }
            }
        });

        // Documented in base class.
        ProjectionUPS.prototype.geographicToCartesian = function (globe, latitude, longitude, elevation,
                                                                  offset, result) {
            if (!globe) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesian", "missingGlobe"));
            }

            if (!result) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesian", "missingResult"));
            }

            // Formulas taken from "Map Projections -- A Working Manual", Snyder, USGS paper 1395, pg. 161.

            if ((this.north && latitude === 90) || (!this.north && latitude === -90)) {
                result[0] = 0;
                result[1] = 0;
                result[2] = elevation;
            } else {
                var poleFactor = this.north ? 1 : -1,
                    lat = latitude * Angle.DEGREES_TO_RADIANS,
                    lon = longitude * Angle.DEGREES_TO_RADIANS,
                    k0 = 0.994, // standard UPS scale factor -- see above reference pg.157, pp 2.
                    ecc = Math.sqrt(globe.eccentricitySquared),
                    s = Math.sqrt(Math.pow(1 + ecc, 1 + ecc) * Math.pow(1 - ecc, 1 - ecc)),
                    sp, t, r;

                if ((this.north && lat < 0) || (!this.north && lat > 0)) {
                    lat = 0;
                }

                sp = Math.sin(lat * poleFactor);
                t = Math.sqrt(((1 - sp) / (1 + sp)) * Math.pow((1 + ecc * sp) / (1 - ecc * sp), ecc));
                r = 2 * globe.equatorialRadius * k0 * t / s;

                result[0] = r * Math.sin(lon);
                result[1] = -r * Math.cos(lon) * poleFactor;
                result[2] = elevation;
            }

            return result;
        };

        // Documented in base class.
        ProjectionUPS.prototype.geographicToCartesianGrid = function (globe, sector, numLat, numLon,
                                                                      elevations, referenceCenter,
                                                                      offset, result) {
            if (!globe) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesianGrid", "missingGlobe"));
            }

            if (!sector) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesianGrid", "missingSector"));
            }

            if (!elevations || elevations.length < numLat * numLon) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesianGrid",
                    "The specified elevations array is null, undefined or insufficient length"));
            }

            if (!result) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "geographicToCartesianGrid", "missingResult"));
            }

            // Formulas taken from "Map Projections -- A Working Manual", Snyder, USGS paper 1395, pg. 161.

            var eqr = globe.equatorialRadius,
                minLat = sector.minLatitude * Angle.DEGREES_TO_RADIANS,
                maxLat = sector.maxLatitude * Angle.DEGREES_TO_RADIANS,
                minLon = sector.minLongitude * Angle.DEGREES_TO_RADIANS,
                maxLon = sector.maxLongitude * Angle.DEGREES_TO_RADIANS,
                deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat : 1),
                deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon : 1),
                minLatLimit = this.projectionLimits.minLatitude * Angle.DEGREES_TO_RADIANS,
                maxLatLimit = this.projectionLimits.maxLatitude * Angle.DEGREES_TO_RADIANS,
                k0 = 0.994, // standard UPS scale factor -- see above reference pg.157, pp 2.
                ecc = Math.sqrt(globe.eccentricitySquared),
                s = Math.sqrt(Math.pow(1 + ecc, 1 + ecc) * Math.pow(1 - ecc, 1 - ecc)),
                poleFactor = this.north ? 1 : -1,
                refCenter = referenceCenter ? referenceCenter : new Vec3(0, 0, 0),
                pos = 0, k = 0,
                lat, lon, clampedLat, sp, t, r;

            // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
            // corresponding to each latitude and longitude.
            lat = minLat;
            for (var j = 0; j < numLat + 1; j++, lat += deltaLat) {
                if (j === numLat) // explicitly set the last lat to the max latitude to ensure alignment
                    lat = maxLat;

                clampedLat = WWMath.clamp(lat, minLatLimit, maxLatLimit);

                sp = Math.sin(clampedLat * poleFactor);
                t = Math.sqrt(((1 - sp) / (1 + sp)) * Math.pow((1 + ecc * sp) / (1 - ecc * sp), ecc));
                r = 2 * eqr * k0 * t / s;

                lon = minLon;
                for (var i = 0; i < numLon + 1; i++, lon += deltaLon) {
                    if (i === numLon)
                        lon = maxLon;

                    result[k++] = r * Math.sin(lon) - refCenter[0];
                    result[k++] = -r * Math.cos(lon) * poleFactor - refCenter[1];
                    result[k++] = elevations[pos++] - refCenter[2];
                }
            }

            return result;
        };

        // Documented in base class.
        ProjectionUPS.prototype.cartesianToGeographic = function (globe, x, y, z, offset, result) {
            if (!globe) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "cartesianToGeographic", "missingGlobe"));
            }

            if (!result) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "cartesianToGeographic", "missingResult"));
            }

            var lon = Math.atan2(x, y * (this.north ? -1 : 1)),
                k0 = 0.994,
                ecc = Math.sqrt(globe.eccentricitySquared),
                r = Math.sqrt(x * x + y * y),
                s = Math.sqrt(Math.pow(1 + ecc, 1 + ecc) * Math.pow(1 - ecc, 1 - ecc)),
                t = r * s / (2 * globe.equatorialRadius * k0),
                ecc2 = globe.eccentricitySquared,
                ecc4 = ecc2 * ecc2,
                ecc6 = ecc4 * ecc2,
                ecc8 = ecc6 * ecc2,
                A = Math.PI / 2 - 2 * Math.atan(t),
                B = ecc2 / 2 + 5 * ecc4 / 24 + ecc6 / 12 + 13 * ecc8 / 360,
                C = 7 * ecc4 / 48 + 29 * ecc6 / 240 + 811 * ecc8 / 11520,
                D = 7 * ecc6 / 120 + 81 * ecc8 / 1120,
                E = 4279 * ecc8 / 161280,
                Ap = A - C + E,
                Bp = B - 3 * D,
                Cp = 2 * C - 8 * E,
                Dp = 4 * D,
                Ep = 8 * E,
                s2p = Math.sin(2 * A),
                lat = Ap + s2p * (Bp + s2p * (Cp + s2p * (Dp + Ep * s2p)));

            lat *= this.north ? 1 : -1;

            result.latitude = lat * Angle.RADIANS_TO_DEGREES;
            result.longitude = lon * Angle.RADIANS_TO_DEGREES;
            result.altitude = z;

            return result;
        };

        // Documented in base class.
        ProjectionUPS.prototype.northTangentAtLocation = function (globe, latitude, longitude, result) {
            if (!result) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionUPS",
                    "northTangentAtLocation", "missingResult"));
            }

            // The north pointing tangent depends on the pole. With the south pole, the north pointing tangent points in
            // the same direction as the vector returned by cartesianToGeographic. With the north pole, the north
            // pointing tangent has the opposite direction.

            result[0] = Math.sin(longitude * Angle.DEGREES_TO_RADIANS) * (this.north ? -1 : 1);
            result[1] = Math.cos(longitude * Angle.DEGREES_TO_RADIANS);
            result[2] = 0;

            return result;
        };

        // Documented in base class.
        ProjectionUPS.prototype.northTangentAtPoint = function (globe, x, y, z, offset, result) {
            if (!result) {
                throw new ArgumentError(Logger.logMessage(Logger.LEVEL_SEVERE, "ProjectionPolarEquidistant",
                    "northTangentAtLocation", "missingResult"));
            }

            var r = Math.sqrt(x * x + y * y);

            if (r < 1.0e-4) {
                result[0] = 0;
                result[1] = 1;
                result[2] = 0;
            } else {
                result[0] = x / r * (this.north ? -1 : 1);
                result[1] = y / r * (this.north ? -1 : 1);
                result[2] = 0;
            }

            return result;
        };

        return ProjectionUPS;
    });