/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;

import java.awt.*;
import java.util.*;

/**
 * @author tag
 * @version $Id$
 */
public class SurfaceShapeEditor implements SelectListener
{
    protected static final int NONE = 0;
    protected static final int MOVING = 1;
    protected static final int SIZING = 2;

    protected final WorldWindow wwd;
    protected SurfaceShape shape;
    protected MarkerLayer controlPointLayer;
    protected RenderableLayer accessoryLayer;
    protected RenderableLayer annotationLayer;
    protected RenderableLayer shadowLayer;
    protected EditorAnnotation annotation;
    protected UnitsFormat unitsFormat;

    protected boolean armed;

    protected boolean active;
    protected int activeOperation = NONE;
    protected Position previousPosition = null;
    protected ControlPointMarker currentSizingMarker;
    protected ShapeAttributes originalAttributes;
    protected ShapeAttributes originalHighlightAttributes;

    protected static class ControlPointMarker extends BasicMarker
    {
        protected int index;
        protected Double size;
        protected Angle rotation;

        public ControlPointMarker(Position position, MarkerAttributes attrs, int index)
        {
            super(position, attrs);
            this.index = index;
        }

        public int getIndex()
        {
            return this.index;
        }
    }

    public SurfaceShapeEditor(WorldWindow wwd, SurfaceShape originalShape)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (originalShape == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.shape = originalShape;

        this.controlPointLayer = new MarkerLayer();
        this.controlPointLayer.setOverrideMarkerElevation(true);
        this.controlPointLayer.setElevation(0);
        this.controlPointLayer.setKeepSeparated(false);
        this.controlPointLayer.setValue(AVKey.IGNORE, true);

        this.accessoryLayer = new RenderableLayer();
        this.accessoryLayer.setPickEnabled(false);
        this.accessoryLayer.setValue(AVKey.IGNORE, true);

        ShapeAttributes lineAttrs = new BasicShapeAttributes();
        lineAttrs.setOutlineMaterial(Material.GREEN);
        lineAttrs.setOutlineWidth(1);
        java.util.List<LatLon> lineLocations = new ArrayList<LatLon>(2);
        lineLocations.add(LatLon.ZERO);
        lineLocations.add(LatLon.ZERO);
        SurfacePolyline rotationLine = new SurfacePolyline(lineAttrs, lineLocations);
        this.accessoryLayer.addRenderable(rotationLine);

        this.annotationLayer = new RenderableLayer();
        this.annotationLayer.setPickEnabled(false);
        this.annotationLayer.setValue(AVKey.IGNORE, true);

        this.annotation = new EditorAnnotation("");
        this.annotationLayer.addRenderable(this.annotation);

        this.shadowLayer = new RenderableLayer();
        this.shadowLayer.setPickEnabled(false);
        this.shadowLayer.setValue(AVKey.IGNORE, true);

        this.unitsFormat = new UnitsFormat();
        this.unitsFormat.setFormat(UnitsFormat.FORMAT_LENGTH, " %,12.3f %s");
    }

    public UnitsFormat getUnitsFormat()
    {
        return unitsFormat;
    }

    public void setUnitsFormat(UnitsFormat unitsFormat)
    {
        this.unitsFormat = unitsFormat != null ? unitsFormat : new UnitsFormat();
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public SurfaceShape getSurfaceShape()
    {
        return this.shape;
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        if (!this.armed && armed)
        {
            this.enable();
        }
        else if (this.armed && !armed)
        {
            this.disable();
        }

        this.armed = armed;
    }

    protected void enable()
    {
        LayerList layers = this.wwd.getModel().getLayers();

        if (!layers.contains(this.controlPointLayer))
            layers.add(this.controlPointLayer);

        if (!this.controlPointLayer.isEnabled())
            this.controlPointLayer.setEnabled(true);

        if (!layers.contains(this.accessoryLayer))
            layers.add(this.accessoryLayer);

        if (!this.accessoryLayer.isEnabled())
            this.accessoryLayer.setEnabled(true);

        if (!layers.contains(this.annotationLayer))
            layers.add(this.annotationLayer);

        if (!layers.contains(this.shadowLayer))
            layers.add(0, this.shadowLayer);
        this.shadowLayer.setEnabled(true);

        this.updateControlPoints();

        this.wwd.addSelectListener(this);
    }

    protected void disable()
    {
        LayerList layers = this.wwd.getModel().getLayers();

        layers.remove(this.controlPointLayer);
        layers.remove(this.accessoryLayer);
        layers.remove(this.annotationLayer);
        layers.remove(this.shadowLayer);

        wwd.removeSelectListener(this);

        ((Component) this.wwd).setCursor(null);
    }

    public void selected(SelectEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.active = false;
            this.activeOperation = NONE;
            this.previousPosition = null;
            ((Component) this.wwd).setCursor(null);
            this.removeShadow();
            this.updateAnnotation(null);
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            if (!(this.wwd instanceof Component))
                return;

            Cursor cursor = null;
            if (this.activeOperation == MOVING)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            else if (this.activeOperation == SIZING)
                cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            else if (event.getTopObject() != null && event.getTopObject() == this.shape)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            else if (event.getTopObject() != null && event.getTopObject() instanceof Marker)
                cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

            ((Component) this.wwd).setCursor(cursor);

            if (this.activeOperation == MOVING && event.getTopObject() == this.shape)
                this.updateShapeAnnotation();
            else if (this.activeOperation == SIZING)
                this.updateAnnotation(this.currentSizingMarker);
            else if (event.getTopObject() != null && event.getTopObject() == this.shape)
                this.updateShapeAnnotation();
            else if (event.getTopObject() != null && event.getTopObject() instanceof ControlPointMarker)
                this.updateAnnotation((ControlPointMarker) event.getTopObject());
            else
                this.updateAnnotation(null);

            // Take over highlighting control for this shape.
            if (event.getTopObject() != null && event.getTopObject() instanceof ControlPointMarker)
                this.shape.setHighlighted(true);
            else if (this.activeOperation == MOVING || this.activeOperation == SIZING)
                this.shape.setHighlighted(true);
            else if (event.getTopObject() != null && event.getTopObject() == this.shape)
                this.shape.setHighlighted(true);
            else
                this.shape.setHighlighted(false);
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_PRESS))
        {
            this.active = true;
            PickedObject terrainObject = this.wwd.getObjectsAtCurrentPosition().getTerrainObject();
            if (terrainObject != null)
                this.previousPosition = terrainObject.getPosition();
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            if (!this.active)
                return;

            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (this.activeOperation == NONE)
                this.makeShadow();

            if (topObject == this.shape || this.activeOperation == MOVING)
            {
                this.activeOperation = MOVING;
                this.dragWholeShape(dragEvent, topObject);
                this.updateControlPoints();
                this.updateShapeAnnotation();
                event.consume();
            }
            else if (dragEvent.getTopPickedObject().getParentLayer() == this.controlPointLayer
                || this.activeOperation == SIZING)
            {
                this.activeOperation = SIZING;
                this.resizeShape(topObject);
                this.updateControlPoints();
                this.updateAnnotation(this.currentSizingMarker);
                event.consume();
            }

            this.wwd.redraw();
        }
    }

    protected void makeShadow()
    {
        SurfaceShape shadowShape = this.makeShadowShape();
        if (shadowShape != null)
        {
            // Set up to keep the shape highlighted during editing but with a reduced opacity.
            this.originalAttributes = this.shape.getAttributes();
            this.originalHighlightAttributes = this.shape.getHighlightAttributes();

            ShapeAttributes editingHighlightAttributes = new BasicShapeAttributes(this.originalHighlightAttributes);
            if (this.originalHighlightAttributes.getInteriorOpacity() == 1)
                editingHighlightAttributes.setInteriorOpacity(0.7);
            this.shape.setAttributes(editingHighlightAttributes);
            this.shape.setHighlightAttributes(editingHighlightAttributes);

            this.shadowLayer.addRenderable(shadowShape);
        }
    }

    protected void removeShadow()
    {
        this.shadowLayer.removeAllRenderables();
        if (this.originalAttributes != null)
        {
            this.shape.setAttributes(this.originalAttributes);
            this.shape.setHighlightAttributes(this.originalHighlightAttributes);
        }
        this.originalAttributes = null;
        this.wwd.redraw();
    }

    protected SurfaceShape makeShadowShape()
    {
        if (this.shape instanceof SurfaceSquare)
            return new SurfaceSquare((SurfaceSquare) this.shape);
        else if (this.shape instanceof SurfaceQuad)
            return new SurfaceQuad((SurfaceQuad) this.shape);
        else if (this.shape instanceof SurfaceCircle)
            return new SurfaceCircle((SurfaceCircle) this.shape);
        else if (this.shape instanceof SurfaceEllipse)
            return new SurfaceEllipse((SurfaceEllipse) this.shape);
        else if (this.shape instanceof SurfacePolyline)
            return new SurfacePolyline((SurfacePolyline) this.shape);
        else if (this.shape instanceof SurfacePolygon)
            return new SurfacePolygon((SurfacePolygon) this.shape);

        return null;
    }

    protected void dragWholeShape(DragSelectEvent dragEvent, Object topObject)
    {
        if (!(topObject instanceof Movable))
            return;

        Movable dragObject = (Movable) topObject;

        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();

        // Compute ref-point position in screen coordinates. Since the shape implicitly follows the surface
        // geometry, we will override the reference elevation with the current surface elevation. This will improve
        // cursor tracking in areas where the elevations are far from zero.
        Position refPos = dragObject.getReferencePosition();
        if (refPos == null)
            return;

        double refElevation = this.computeSurfaceElevation(wwd, refPos);
        refPos = new Position(refPos, refElevation);
        Vec4 refPoint = globe.computePointFromPosition(refPos);
        Vec4 screenRefPoint = view.project(refPoint);

        // Compute screen-coord delta since last event.
        int dx = dragEvent.getPickPoint().x - dragEvent.getPreviousPickPoint().x;
        int dy = dragEvent.getPickPoint().y - dragEvent.getPreviousPickPoint().y;

        // Find intersection of screen coord ref-point with globe.
        double x = screenRefPoint.x + dx;
        double y = dragEvent.getMouseEvent().getComponent().getSize().height - screenRefPoint.y + dy - 1;
        Line ray = view.computeRayFromScreenPoint(x, y);
        Intersection inters[] = globe.intersect(ray, refPos.getElevation());

        if (inters != null)
        {
            // Intersection with globe. Move reference point to the intersection point.
            Position p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            dragObject.moveTo(p);
        }
    }

    protected void resizeShape(Object topObject)
    {
        if (!(topObject instanceof ControlPointMarker))
            return;

        this.currentSizingMarker = (ControlPointMarker) topObject;

        // If the terrain beneath the control point is null, then the user is attempting to drag the handle off the
        // globe. This is not a valid state for SurfaceImage, so we will ignore this action but keep the drag operation
        // in effect.
        PickedObject terrainObject = this.wwd.getObjectsAtCurrentPosition().getTerrainObject();
        if (terrainObject == null)
            return;

        if (this.shape instanceof SurfacePolygon)
            this.reshapeSurfacePolygon(terrainObject.getPosition(), (ControlPointMarker) topObject);
        else if (this.shape instanceof SurfacePolyline)
            this.reshapeSurfacePolyline(terrainObject.getPosition(), (ControlPointMarker) topObject);
        else if (this.shape instanceof SurfaceCircle)
            this.reshapeSurfaceCircle(terrainObject.getPosition(), (ControlPointMarker) topObject);
        else if (this.shape instanceof SurfaceSquare)
            this.reshapeSurfaceSquare(terrainObject.getPosition(), (ControlPointMarker) topObject);
        else if (this.shape instanceof SurfaceQuad)
            this.reshapeSurfaceQuad(terrainObject.getPosition(), (ControlPointMarker) topObject);
        else if (this.shape instanceof SurfaceEllipse)
            this.reshapeSurfaceEllipse(terrainObject.getPosition(), (ControlPointMarker) topObject);

        this.previousPosition = terrainObject.getPosition();
    }

    protected void updateControlPoints()
    {
        if (this.shape instanceof SurfacePolygon || this.shape instanceof SurfacePolyline)
            this.updateSurfacePolygonControlPoints();
        else if (this.shape instanceof SurfaceCircle)
            this.updateSurfaceCircleControlPoints();
        else if (this.shape instanceof SurfaceSquare)
            this.updateSurfaceSquareControlPoints();
        else if (this.shape instanceof SurfaceQuad)
            this.updateSurfaceQuadControlPoints();
        else if (this.shape instanceof SurfaceEllipse)
            this.updateSurfaceEllipseControlPoints();
    }

    protected double computeSurfaceElevation(WorldWindow wwd, LatLon latLon)
    {
        SectorGeometryList sgl = wwd.getSceneController().getTerrain();
        if (sgl != null)
        {
            Vec4 point = sgl.getSurfacePoint(latLon.getLatitude(), latLon.getLongitude(), 0.0);
            if (point != null)
            {
                Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
                return pos.getElevation();
            }
        }

        return wwd.getModel().getGlobe().getElevation(latLon.getLatitude(), latLon.getLongitude());
    }

    protected void reshapeSurfacePolygon(Position terrainPosition, ControlPointMarker controlPoint)
    {
        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon ll : ((SurfacePolygon) this.shape).getLocations())
        {
            locations.add(ll);
        }

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Position markerPosition = wwd.getModel().getGlobe().computePositionFromEllipsoidalPoint(
            markerPoint.add3(delta));

        locations.set(controlPoint.getIndex(), markerPosition);
        ((SurfacePolygon) this.shape).setLocations(locations);
    }

    protected void reshapeSurfacePolyline(Position terrainPosition, ControlPointMarker controlPoint)
    {
        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon ll : ((SurfacePolyline) this.shape).getLocations())
        {
            locations.add(ll);
        }

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Position markerPosition = wwd.getModel().getGlobe().computePositionFromEllipsoidalPoint(
            markerPoint.add3(delta));

        locations.set(controlPoint.getIndex(), markerPosition);
        ((SurfacePolyline) this.shape).setLocations(locations);
    }

    protected void reshapeSurfaceCircle(Position terrainPosition, ControlPointMarker controlPoint)
    {
        SurfaceCircle circle = (SurfaceCircle) this.shape;

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(circle.getCenter());
        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        double radius = circle.getRadius() + delta.dot3(vMarker);
        if (radius > 0)
            circle.setRadius(radius);
    }

    protected void reshapeSurfaceSquare(Position terrainPosition, ControlPointMarker controlPoint)
    {
        SurfaceSquare square = (SurfaceSquare) this.shape;

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(square.getCenter());
        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint);

        if (controlPoint.getIndex() == 0)
        {
            double size = square.getSize() + delta.dot3(vMarker.normalize3());
            if (size > 0)
                square.setSize(size);
        }
        else
        {
            double deltaAngle = this.computeHeadingDelta(centerPoint, previousPoint, terrainPoint, delta);
            square.setHeading(this.normalizedHeading(square.getHeading(), deltaAngle));
        }
    }

    protected void reshapeSurfaceQuad(Position terrainPosition, ControlPointMarker controlPoint)
    {
        SurfaceQuad quad = (SurfaceQuad) this.shape;

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(quad.getCenter());
        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        if (controlPoint.getIndex() != 2)
        {
            double width = quad.getWidth() + (controlPoint.getIndex() == 0 ? delta.dot3(vMarker) : 0);
            double height = quad.getHeight() + (controlPoint.getIndex() == 1 ? delta.dot3(vMarker) : 0);
            if (width > 0 && height > 0)
                quad.setSize(width, height);
        }
        else
        {
            double deltaAngle = this.computeHeadingDelta(centerPoint, previousPoint, terrainPoint, delta);
            quad.setHeading(this.normalizedHeading(quad.getHeading(), deltaAngle));
        }
    }

    protected void reshapeSurfaceEllipse(Position terrainPosition, ControlPointMarker controlPoint)
    {
        SurfaceEllipse ellipse = (SurfaceEllipse) this.shape;

        Vec4 terrainPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(this.previousPosition);
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(ellipse.getCenter());
        Vec4 markerPoint = wwd.getModel().getGlobe().computeEllipsoidalPointFromLocation(controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        if (controlPoint.getIndex() != 2)
        {
            double majorRadius = ellipse.getMajorRadius() + (controlPoint.getIndex() == 0 ? delta.dot3(vMarker) : 0);
            double minorRadius = ellipse.getMinorRadius() + (controlPoint.getIndex() == 1 ? delta.dot3(vMarker) : 0);
            if (majorRadius > 0 && minorRadius > 0)
                ellipse.setRadii(majorRadius, minorRadius);
        }
        else
        {
            double deltaAngle = this.computeHeadingDelta(centerPoint, previousPoint, terrainPoint, delta);
            ellipse.setHeading(this.normalizedHeading(ellipse.getHeading(), deltaAngle));
        }

        this.updateAnnotation(controlPoint);
    }

    protected Angle normalizedHeading(Angle originalHeading, double deltaHeading)
    {
        final double twoPI = 2 * Math.PI;

        double newHeading = originalHeading.getRadians() + deltaHeading;

        if (Math.abs(newHeading) > twoPI)
            newHeading = newHeading % twoPI;

        return Angle.fromRadians(newHeading >= 0 ? newHeading : newHeading + twoPI);
    }

    protected void updateSurfacePolygonControlPoints()
    {
        Iterable<? extends LatLon> corners = null;

        if (this.shape instanceof SurfacePolygon)
            corners = ((SurfacePolygon) this.shape).getLocations();
        else if (this.shape instanceof SurfacePolyline)
            corners = ((SurfacePolyline) this.shape).getLocations();

        if (corners == null)
            return;

        Iterable<Marker> markers = this.controlPointLayer.getMarkers();
        if (markers == null)
        {
            double d = LatLon.getAverageDistance(corners).radians * wwd.getModel().getGlobe().getRadius();
            MarkerAttributes markerAttrs =
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1, 0.1 * d);

            ArrayList<Marker> controlPoints = new ArrayList<Marker>();
            int i = 0;
            for (LatLon corner : corners)
            {
                controlPoints.add(new ControlPointMarker(new Position(corner, 0), markerAttrs, i++));
            }

            this.controlPointLayer.setMarkers(controlPoints);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            for (LatLon cpPosition : corners)
            {
                markerIterator.next().setPosition(new Position(cpPosition, 0));
            }
        }
    }

    protected void updateSurfaceCircleControlPoints()
    {
        SurfaceCircle circle = (SurfaceCircle) this.shape;

        LatLon cpPosition = LatLon.greatCircleEndPosition(circle.getCenter(), Angle.fromDegrees(90),
            Angle.fromRadians(circle.getRadius() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.controlPointLayer.getMarkers();
        if (markers == null)
        {
            MarkerAttributes markerAttrs =
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1);

            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            markerList.add(new ControlPointMarker(new Position(cpPosition, 0), markerAttrs, 0));
            this.controlPointLayer.setMarkers(markerList);
        }
        else
        {
            markers.iterator().next().setPosition(new Position(cpPosition, 0));
        }

        Iterator<Marker> markerIterator = this.controlPointLayer.getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = circle.getRadius();
    }

    protected void updateSurfaceSquareControlPoints()
    {
        SurfaceSquare square = (SurfaceSquare) this.shape;

        LatLon cpPosition = LatLon.greatCircleEndPosition(square.getCenter(),
            Angle.fromDegrees(90 + square.getHeading().degrees),
            Angle.fromRadians(0.5 * square.getSize() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        LatLon cpPositionR = LatLon.greatCircleEndPosition(square.getCenter(),
            Angle.fromDegrees(square.getHeading().degrees),
            Angle.fromRadians(0.7 * square.getSize() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.controlPointLayer.getMarkers();
        if (markers == null)
        {
            MarkerAttributes markerAttrs =
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1);

            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            markerList.add(new ControlPointMarker(new Position(cpPosition, 0), markerAttrs, 0));

            markerAttrs =
                new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.SPHERE, 1, 10, 0.1);
            markerList.add(new ControlPointMarker(new Position(cpPositionR, 0), markerAttrs, 1));

            this.controlPointLayer.setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(cpPosition, 0));
            markerIterator.next().setPosition(new Position(cpPositionR, 0));
        }

        Iterator<Marker> markerIterator = this.controlPointLayer.getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = square.getSize();
        ((ControlPointMarker) markerIterator.next()).rotation = square.getHeading();

        this.updateOrientationLine(square.getCenter(), cpPositionR);
    }

    protected void updateSurfaceQuadControlPoints()
    {
        SurfaceQuad quad = (SurfaceQuad) this.shape;

        LatLon cpPositionW = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(90 + quad.getHeading().degrees),
            Angle.fromRadians(0.5 * quad.getWidth() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        LatLon cpPositionH = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(quad.getHeading().degrees),
            Angle.fromRadians(0.5 * quad.getHeight() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        LatLon cpPositionR = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(quad.getHeading().degrees),
            Angle.fromRadians(0.7 * quad.getHeight() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.controlPointLayer.getMarkers();
        if (markers == null)
        {
            MarkerAttributes markerAttrs =
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1);

            java.util.List<Marker> markerList = new ArrayList<Marker>(2);
            markerList.add(new ControlPointMarker(new Position(cpPositionW, 0), markerAttrs, 0));
            this.controlPointLayer.setMarkers(markerList);
            markerList.add(new ControlPointMarker(new Position(cpPositionH, 0), markerAttrs, 1));

            markerAttrs =
                new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.SPHERE, 1, 10, 0.1);
            markerList.add(new ControlPointMarker(new Position(cpPositionR, 0), markerAttrs, 2));

            this.controlPointLayer.setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(cpPositionW, 0));
            markerIterator.next().setPosition(new Position(cpPositionH, 0));
            markerIterator.next().setPosition(new Position(cpPositionR, 0));
        }

        Iterator<Marker> markerIterator = this.controlPointLayer.getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = quad.getWidth();
        ((ControlPointMarker) markerIterator.next()).size = quad.getHeight();
        ((ControlPointMarker) markerIterator.next()).rotation = quad.getHeading();

        this.updateOrientationLine(quad.getCenter(), cpPositionR);
    }

    protected void updateSurfaceEllipseControlPoints()
    {
        SurfaceEllipse ellipse = (SurfaceEllipse) this.shape;

        LatLon cpPositionW = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(90 + ellipse.getHeading().degrees),
            Angle.fromRadians(ellipse.getMajorRadius() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        LatLon cpPositionH = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(ellipse.getHeading().degrees),
            Angle.fromRadians(ellipse.getMinorRadius() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        LatLon cpPositionR = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(ellipse.getHeading().degrees),
            Angle.fromRadians(1.15 * ellipse.getMinorRadius() / this.wwd.getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.controlPointLayer.getMarkers();
        if (markers == null)
        {
            MarkerAttributes markerAttrs =
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1);

            java.util.List<Marker> markerList = new ArrayList<Marker>(2);
            markerList.add(new ControlPointMarker(new Position(cpPositionW, 0), markerAttrs, 0));
            this.controlPointLayer.setMarkers(markerList);
            markerList.add(new ControlPointMarker(new Position(cpPositionH, 0), markerAttrs, 1));

            markerAttrs =
                new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.SPHERE, 1, 10, 0.1);
            markerList.add(new ControlPointMarker(new Position(cpPositionR, 0), markerAttrs, 2));

            this.controlPointLayer.setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(cpPositionW, 0));
            markerIterator.next().setPosition(new Position(cpPositionH, 0));
            markerIterator.next().setPosition(new Position(cpPositionR, 0));
        }

        Iterator<Marker> markerIterator = this.controlPointLayer.getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = ellipse.getMajorRadius();
        ((ControlPointMarker) markerIterator.next()).size = ellipse.getMinorRadius();
        ((ControlPointMarker) markerIterator.next()).rotation = ellipse.getHeading();

        this.updateOrientationLine(ellipse.getCenter(), cpPositionR);
    }

    protected void updateOrientationLine(LatLon centerPosition, LatLon cpPositionR)
    {
        java.util.List<LatLon> lineLocations = new ArrayList<LatLon>(2);
        lineLocations.add(centerPosition);
        lineLocations.add(cpPositionR);
        SurfacePolyline rotationLine = (SurfacePolyline) this.accessoryLayer.getRenderables().iterator().next();
        rotationLine.setLocations(lineLocations);
    }

    protected void updateShapeAnnotation()
    {
        LatLon center = null;
        if (this.shape instanceof SurfaceEllipse)
            center = ((SurfaceEllipse) this.shape).getCenter();
        else if (shape instanceof SurfaceQuad)
            center = ((SurfaceQuad) this.shape).getCenter();

        if (center != null)
        {
            ControlPointMarker dummyMarker = new ControlPointMarker(new Position(center, 0),
                new BasicMarkerAttributes(), 0);
            this.updateAnnotation(dummyMarker);
        }
        else
        {
            this.updateAnnotation(null);
        }
    }

    protected void updateAnnotation(ControlPointMarker marker)
    {
        if (marker == null)
        {
            this.annotationLayer.setEnabled(false);
            return;
        }

        this.annotationLayer.setEnabled(true);
        this.annotation.setPosition(marker.getPosition());

        String annotationText;
        if (marker.size != null)
            annotationText = this.unitsFormat.length(null, marker.size);
        else if (marker.rotation != null)
            annotationText = this.unitsFormat.angle(null, marker.rotation);
        else
            annotationText = this.unitsFormat.latLon2(marker.getPosition());

        this.annotation.setText(annotationText);
    }

    protected double computeHeadingDelta(Vec4 centerPoint, Vec4 previousPoint, Vec4 terrainPoint, Vec4 delta)
    {
        Vec4 vP = previousPoint.subtract3(centerPoint);
        Vec4 vT = terrainPoint.subtract3(centerPoint);
        Vec4 cross = vT.cross3(vP);
        double sign = cross.z >= 0 ? -1 : 1;

        return sign * Math.atan2(delta.getLength3(), vP.getLength3());
    }
}
