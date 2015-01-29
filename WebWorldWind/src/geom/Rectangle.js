/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports Rectangle
 * @version $Id$
 */
define([
        '../util/Logger'
    ],
    function (Logger) {
        "use strict";

        /**
         * Constructs a rectangle with a specified origin and size.
         * @alias Rectangle
         * @constructor
         * @classdesc Represents a rectangle in 2D Cartesian coordinates.
         * @param {Number} x The X coordinate of the rectangle's origin.
         * @param {Number} y The Y coordinate of the rectangle's origin.
         * @param {Number} width The rectangle's width.
         * @param {Number} height The rectangle's height.
         */
        var Rectangle = function (x, y, width, height) {

            /**
             * The X coordinate of this rectangle's origin.
             * @type {Number}
             */
            this.x = x;

            /**
             * The Y coordinate of this rectangle's origin.
             * @type {Number}
             */
            this.y = y;

            /**
             * This rectangle's width.
             * @type {Number}
             */
            this.width = width;

            /**
             * This rectangle's height.
             * @type {Number}
             */
            this.height = height;
        };

        /**
         * Fill in a pre-existing rectangle.
         * @param {Number} x The X coordinate of the rectangle's origin.
         * @param {Number} y The Y coordinate of the rectangle's origin.
         * @param {Number} width The rectangle's width.
         * @param {Number} height The rectangle's height.
         */
        Rectangle.prototype.set = function(x, y, width, height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        };

        /**
         * Returns the minimum X value of this rectangle.
         * @returns {Number} The rectangle's minimum X value.
         */
        Rectangle.prototype.getMinX = function () {
            return this.x;
        };

        /**
         * Returns the minimum Y value of this rectangle.
         * @returns {Number} The rectangle's minimum Y value.
         */
        Rectangle.prototype.getMinY = function () {
            return this.y;
        };

        /**
         * Returns the maximum X value of this rectangle.
         * @returns {Number} The rectangle's maximum X value.
         */
        Rectangle.prototype.getMaxX = function () {
            return this.x + this.width;
        };

        /**
         * Returns the maximum Y value of this rectangle.
         * @returns {Number} The rectangle's maximum Y value.
         */
        Rectangle.prototype.getMaxY = function () {
            return this.y + this.height;
        };

        /**
         * Indicates whether this rectangle contains a specified point.
         * @param {Vec2} point The point to test.
         * @returns {boolean} <code>true</code> if this rectangle contains the specified point, otherwise
         * <code>false</code>.
         */
        Rectangle.prototype.containsPoint = function (point) {
            return point[0] >= this.x && point[0] <= (this.x + this.width)
                && point[1] >= this.y && point[1] <= (this.y + this.height);
        };
        /**
         *
         * Indicates whether this rectangle intersects a specified one.
         * @param {Rectangle} that The rectangle to test.
         * @returns {boolean} <code>true</code> if this triangle and the specified one intersect,
         * otherwise <code>false</code>.
         */
        Rectangle.prototype.intersects = function (that) {
            if ((that.x + that.width) < this.x)
                return false;

            if (that.x > (this.x + this.width))
                return false;

            if ((that.y + that.height) < this.y)
                return false;

            //noinspection RedundantIfStatementJS
            if (that.y > (this.y + this.height))
                return false;

            return true;
        };

        return Rectangle;
    });