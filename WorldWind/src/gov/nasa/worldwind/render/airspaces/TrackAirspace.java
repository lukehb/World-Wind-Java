/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Creates a sequence of potentially disconnected rectangular airspaces specified by a collection of {@link
 * gov.nasa.worldwind.render.airspaces.Box} objects.
 *
 * @author garakl
 * @version $Id$
 */
public class TrackAirspace extends AbstractAirspace
{
    protected List<Box> legs = new ArrayList<Box>();
    protected boolean enableInnerCaps = true;
    protected boolean enableCenterLine;
    /**
     * Denotes the the threshold that defines whether the angle between two adjacent legs is small. Initially 22.5
     * degrees.
     */
    protected Angle smallAngleThreshold = Angle.fromDegrees(22.5);

    public TrackAirspace(Collection<Box> legs)
    {
        this.addLegs(legs);
    }

    public TrackAirspace(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    public TrackAirspace()
    {
    }

    public TrackAirspace(TrackAirspace source)
    {
        super(source);

        this.legs = new ArrayList<Box>(source.legs.size());
        for (Box leg : source.legs)
        {
            this.legs.add(new Box(leg));
        }

        this.enableInnerCaps = source.enableInnerCaps;
        this.enableCenterLine = source.enableInnerCaps;
        this.smallAngleThreshold = source.smallAngleThreshold;
    }

    public List<Box> getLegs()
    {
        return Collections.unmodifiableList(this.legs);
    }

    public void setLegs(Collection<Box> legs)
    {
        this.legs.clear();
        this.addLegs(legs);
    }

    protected void addLegs(Iterable<Box> newLegs)
    {
        if (newLegs != null)
        {
            for (Box b : newLegs)
            {
                if (b != null)
                    this.addLeg(b);
            }
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    public Box addLeg(LatLon start, LatLon end, double lowerAltitude, double upperAltitude,
        double leftWidth, double rightWidth)
    {
        if (start == null)
        {
            String message = "nullValue.StartIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (end == null)
        {
            String message = "nullValue.EndIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean[] terrainConformant = this.isTerrainConforming();

        Box leg = new Box();
        leg.setAltitudes(lowerAltitude, upperAltitude);
        leg.setTerrainConforming(terrainConformant[0], terrainConformant[1]);
        leg.setLocations(start, end);
        leg.setWidths(leftWidth, rightWidth);
        this.addLeg(leg);
        return leg;
    }

    protected void addLeg(Box leg)
    {
        if (leg == null)
        {
            String message = "nullValue.LegIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        leg.setAlwaysOnTop(this.isAlwaysOnTop());
        leg.setForceCullFace(true);
        leg.setEnableCenterLine(this.enableCenterLine);
        leg.setDrawSurfaceShape(this.drawSurfaceShape);
        this.legs.add(leg);
        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    public void removeAllLegs()
    {
        this.legs.clear();
    }

    public boolean isEnableInnerCaps()
    {
        return this.enableInnerCaps;
    }

    public void setEnableInnerCaps(boolean draw)
    {
        this.enableInnerCaps = draw;
        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    public boolean isEnableCenterLine()
    {
        return this.enableCenterLine;
    }

    public void setEnableCenterLine(boolean enable)
    {
        this.enableCenterLine = enable;

        for (Box leg : this.legs)
        {
            leg.setEnableCenterLine(enable);
        }
    }

    public void setEnableDepthOffset(boolean enable)
    {
        super.setEnableDepthOffset(enable);
        this.setLegsOutOfDate();
    }

    /**
     * Desnotes the threshold that defines whether the angle between two adjacent legs is small. This threshold is used
     * to determine the best method for adjusting the vertices of adjacent legs.
     *
     * @return the angle used to determine when the angle between two adjacent legs is small.
     *
     * @see #setSmallAngleThreshold(gov.nasa.worldwind.geom.Angle)
     */
    public Angle getSmallAngleThreshold()
    {
        return smallAngleThreshold;
    }

    /**
     * Specifies the threshold that defines whether the angle between two adjacent legs is small. This threshold is used
     * to determine the best method for adjusting the vertices of adjacent legs.
     * <p/>
     * When the angle between adjacent legs is small, the standard method of joining the leg's vertices forms a very
     * large peak pointing away from the leg's common point. In this case <code>TrackAirspace</code> uses a method that
     * avoids this peak and produces a seamless transition between the adjacent legs.
     *
     * @param angle the angle to use when determining when the angle between two adjacent legs is small.
     *
     * @throws IllegalArgumentException if <code>angle</code> is <code>null</code>.
     * @see #getSmallAngleThreshold()
     */
    public void setSmallAngleThreshold(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.smallAngleThreshold = angle;
    }

    public void setAltitudes(double lowerAltitude, double upperAltitude)
    {
        super.setAltitudes(lowerAltitude, upperAltitude);

        for (Box l : this.legs)
        {
            l.setAltitudes(lowerAltitude, upperAltitude);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
    {
        super.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);

        for (Box l : this.legs)
        {
            l.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        super.setAlwaysOnTop(alwaysOnTop);

        for (Box l : this.getLegs())
        {
            l.setAlwaysOnTop(alwaysOnTop);
        }
    }

    @Override
    public void setDrawSurfaceShape(boolean drawSurfaceShape)
    {
        super.setDrawSurfaceShape(drawSurfaceShape);

        for (Box l : this.getLegs())
        {
            l.setDrawSurfaceShape(drawSurfaceShape);
        }
    }

    public boolean isAirspaceVisible(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If the parent TrackAirspace is not visible, then return false immediately without testing the child legs.
        if (!super.isAirspaceVisible(dc))
            return false;

        boolean visible = false;

        // The parent TrackAirspace is visible. Since the parent TrackAirspace's extent potentially contains volumes
        // where no child geometry exists, test that at least one of the child legs are visible.
        for (Box b : this.legs)
        {
            if (b.isAirspaceVisible(dc))
            {
                visible = true;
                break;
            }
        }

        return visible;
    }

    public Position getReferencePosition()
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>(2 * this.legs.size());
        for (Box box : this.legs)
        {
            LatLon[] ll = box.getLocations();
            locations.add(ll[0]);
            locations.add(ll[1]);
        }

        return this.computeReferencePosition(locations, this.getAltitudes());
    }

    @Override
    protected Extent computeExtent(DrawContext dc)
    {
        // Update the child leg vertices if they're out of date. Since the leg vertices are input to the parent
        // TrackAirspace's extent computation, they must be current before computing the parent's extent.
        if (this.isLegsOutOfDate(dc))
        {
            this.doUpdateLegs(dc);
        }

        return super.computeExtent(dc);
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Box> trackLegs = this.getLegs();

        if (trackLegs == null || trackLegs.isEmpty())
        {
            return null;
        }
        else if (trackLegs.size() == 0)
        {
            return trackLegs.get(0).computeExtent(globe, verticalExaggeration);
        }
        else
        {
            ArrayList<gov.nasa.worldwind.geom.Box> extents = new ArrayList<gov.nasa.worldwind.geom.Box>();

            for (Box leg : trackLegs)
            {
                extents.add(leg.computeExtent(globe, verticalExaggeration));
            }

            return gov.nasa.worldwind.geom.Box.union(extents);
        }
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        return null; // Track is a geometry container, and therefore has no geometry itself.
    }

    @Override
    protected void invalidateAirspaceData()
    {
        super.invalidateAirspaceData();

        for (Box leg : this.legs)
        {
            leg.invalidateAirspaceData();
        }
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.
        for (Box box : this.legs)
        {
            box.doMoveTo(globe, oldRef, newRef);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.
        for (Box box : this.legs)
        {
            box.doMoveTo(oldRef, newRef);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate();
    }

    protected boolean isLegsOutOfDate(DrawContext dc)
    {
        for (Box leg : this.legs)
        {
            if (!leg.isVerticesValid(dc.getGlobe()))
                return true;
        }

        return false;
    }

    protected void setLegsOutOfDate()
    {
        for (Box leg : this.legs)
        {
            leg.clearVertices();
        }
    }

    protected void doUpdateLegs(DrawContext dc)
    {
        Globe globe = dc.getGlobe();
        double verticalExaggeration = dc.getVerticalExaggeration();

        // Assign the standard eight vertices to each box and enable the starting and ending caps. We start by assuming
        // that each leg is independent, then adjacent adjacent legs to give the appearance of a continuous track.
        for (Box leg : this.legs)
        {
            if (leg == null) // This should never happen, but we check anyway.
                continue;

            leg.setEnableCaps(true);
            leg.setEnableDepthOffset(this.isEnableDepthOffset());

            Vec4[] vertices = Box.computeEllipsoidalStandardVertices(globe, verticalExaggeration, leg);
            if (vertices != null && vertices.length == 8)
                leg.setEllipsoidalVertices(globe, vertices);
        }

        // If there's more than one leg, we potentially align the vertices of adjacent legs to give the appearance of
        // a continuous track. This loop never executes if the list of legs has less than two elements. Each iteration
        // works on the adjacent vertices of the current leg and the next leg. Therefore this does not modify the
        // starting vertices of the first leg, or the ending vertices of the last leg.
        for (int i = 0; i < this.legs.size() - 1; i++)
        {
            Box leg = this.legs.get(i);
            Box nextLeg = this.legs.get(i + 1);

            if (leg == null || nextLeg == null) // This should never happen, but we check anyway.
                continue;

            // If the two legs have equivalent same locations, altitude, and altitude mode where they meet, then
            // adjust each leg's vertices so the two legs appear to make a continuous shape.
            if (this.mustJoinLegs(leg, nextLeg))
                this.joinLegs(globe, verticalExaggeration, leg, nextLeg);
        }
    }

    /**
     * Specifies whether the legs must have their adjacent vertices joined. <code>leg1</code> must precede
     * <code>leg2</code>. A track's legs must be joined when two adjacent legs share a common location. In this case,
     * the geometry of the two adjacent boxes contains a gap on one side and an intersection on the other. Joining the
     * legs modifies the vertices of each leg at their common location to produce a seamless transition from the first
     * leg to the second.
     *
     * @param leg1 the first leg.
     * @param leg2 the second leg.
     *
     * @return <code>true</code> if the legs must be joined, otherwise <code>false</code>.
     */
    protected boolean mustJoinLegs(Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();
        boolean[] leg1TerrainConformance = leg1.isTerrainConforming();
        boolean[] leg2TerrainConformance = leg2.isTerrainConforming();

        if (!leg1Loc[1].equals(leg2Loc[0]))
            return false;

        if (leg1Altitudes[0] != leg2Altitudes[0] || leg1Altitudes[1] != leg2Altitudes[1])
            return false;

        //noinspection RedundantIfStatement
        if (leg1TerrainConformance[0] != leg2TerrainConformance[0]
            || leg1TerrainConformance[1] != leg2TerrainConformance[1])
            return false;

        return true;
    }

    /**
     * Modifies the vertices of the specified adjacent legs to produce a seamless transition from the first leg to the
     * second. <code>leg1</code> must precede <code>leg2</code>, and they must share a common location at the end of
     * <code>leg1</code> and the beginning of <code>leg2</code>. Without joining the adjacent vertices, the geometry of
     * two adjacent boxes contains a gap on one side and an intersection on the other.
     * <p/>
     * This does nothing if the legs cannot be joined for any reason.
     *
     * @param globe                the <code>Globe</code> the legs are related to.
     * @param verticalExaggeration the vertical exaggeration of the scene.
     * @param leg1                 the first leg.
     * @param leg2                 the second leg.
     */
    protected void joinLegs(Globe globe, double verticalExaggeration, Box leg1, Box leg2)
    {
        Vec4[] leg1Vertices = leg1.getEllipsoidalVertices(globe);
        Vec4[] leg2Vertices = leg2.getEllipsoidalVertices(globe);

        Plane bisectingPlane = this.computeEllipsoidalBisectingPlane(globe, leg1, leg2);

        // If the two legs overlap, their bisecting plane intersects either the starting cap of the first leg, or the
        // ending cap of the second leg. In this case, we cannot join the leg's vertices and exit without changing
        // anything.
        if (bisectingPlane.intersect(leg1Vertices[Box.A_LOW_LEFT], leg1Vertices[Box.A_LOW_RIGHT]) != null ||
            bisectingPlane.intersect(leg2Vertices[Box.B_LOW_LEFT], leg2Vertices[Box.B_LOW_RIGHT]) != null)
        {
            return;
        }

        // If the angle between the legs is small, then using the bisecting plane to join them causes the leg's
        // connecting vertices to form a very large peak away from the common point. Therefore we use a different
        // approach to join acute legs as follows:
        // * The first leg is extended to cover the second leg, and has its end cap enabled.
        // * The second leg is clipped when it intersects the first leg, and has its start cap enabled if and only if
        //   inner caps are enabled.
        if (this.isSmallAngle(globe, leg1, leg2))
        {
            Plane[] leg1Planes = Box.computeEllipsoidalStandardPlanes(globe, verticalExaggeration, leg1);
            Plane[] leg2Planes = Box.computeEllipsoidalStandardPlanes(globe, verticalExaggeration, leg2);

            Line low_left_line = Line.fromSegment(leg2Vertices[Box.B_LOW_LEFT], leg2Vertices[Box.A_LOW_LEFT]);
            Line low_right_line = Line.fromSegment(leg2Vertices[Box.B_LOW_RIGHT], leg2Vertices[Box.A_LOW_RIGHT]);
            Line up_left_line = Line.fromSegment(leg2Vertices[Box.B_UPR_LEFT], leg2Vertices[Box.A_UPR_LEFT]);
            Line up_right_line = Line.fromSegment(leg2Vertices[Box.B_UPR_RIGHT], leg2Vertices[Box.A_UPR_RIGHT]);

            if (this.isRightTurn(globe, leg1, leg2))
            {
                Line low = Line.fromSegment(leg1Vertices[Box.A_LOW_RIGHT], leg1Vertices[Box.B_LOW_RIGHT]);
                Line up = Line.fromSegment(leg1Vertices[Box.A_UPR_RIGHT], leg1Vertices[Box.B_UPR_RIGHT]);
                leg1Vertices[Box.B_LOW_RIGHT] = leg2Planes[Box.FACE_LEFT].intersect(low);
                leg1Vertices[Box.B_UPR_RIGHT] = leg2Planes[Box.FACE_LEFT].intersect(up);

                leg2Vertices[Box.A_LOW_LEFT] = leg1Planes[Box.FACE_RIGHT].intersect(low_left_line);
                leg2Vertices[Box.A_LOW_RIGHT] = leg1Planes[Box.FACE_RIGHT].intersect(low_right_line);
                leg2Vertices[Box.A_UPR_LEFT] = leg1Planes[Box.FACE_RIGHT].intersect(up_left_line);
                leg2Vertices[Box.A_UPR_RIGHT] = leg1Planes[Box.FACE_RIGHT].intersect(up_right_line);
            }
            else
            {
                Line low = Line.fromSegment(leg1Vertices[Box.A_LOW_LEFT], leg1Vertices[Box.B_LOW_LEFT]);
                Line up = Line.fromSegment(leg1Vertices[Box.A_UPR_LEFT], leg1Vertices[Box.B_UPR_LEFT]);
                leg1Vertices[Box.B_LOW_LEFT] = leg2Planes[Box.FACE_RIGHT].intersect(low);
                leg1Vertices[Box.B_UPR_LEFT] = leg2Planes[Box.FACE_RIGHT].intersect(up);

                leg2Vertices[Box.A_LOW_LEFT] = leg1Planes[Box.FACE_LEFT].intersect(low_left_line);
                leg2Vertices[Box.A_LOW_RIGHT] = leg1Planes[Box.FACE_LEFT].intersect(low_right_line);
                leg2Vertices[Box.A_UPR_LEFT] = leg1Planes[Box.FACE_LEFT].intersect(up_left_line);
                leg2Vertices[Box.A_UPR_RIGHT] = leg1Planes[Box.FACE_LEFT].intersect(up_right_line);
            }

            leg1.setEnableEndCap(true);
            leg2.setEnableStartCap(this.isEnableInnerCaps());
            leg1.setEllipsoidalVertices(globe, leg1Vertices);
            leg2.setEllipsoidalVertices(globe, leg2Vertices);
        }
        else
        {
            Line low_left_line = Line.fromSegment(leg1Vertices[Box.A_LOW_LEFT], leg1Vertices[Box.B_LOW_LEFT]);
            Line low_right_line = Line.fromSegment(leg1Vertices[Box.A_LOW_RIGHT], leg1Vertices[Box.B_LOW_RIGHT]);
            Line up_left_line = Line.fromSegment(leg1Vertices[Box.A_UPR_LEFT], leg1Vertices[Box.B_UPR_LEFT]);
            Line up_right_line = Line.fromSegment(leg1Vertices[Box.A_UPR_RIGHT], leg1Vertices[Box.B_UPR_RIGHT]);

            leg1Vertices[Box.B_LOW_LEFT] = bisectingPlane.intersect(low_left_line);
            leg1Vertices[Box.B_LOW_RIGHT] = bisectingPlane.intersect(low_right_line);
            leg1Vertices[Box.B_UPR_LEFT] = bisectingPlane.intersect(up_left_line);
            leg1Vertices[Box.B_UPR_RIGHT] = bisectingPlane.intersect(up_right_line);

            low_left_line = Line.fromSegment(leg2Vertices[Box.B_LOW_LEFT], leg2Vertices[Box.A_LOW_LEFT]);
            low_right_line = Line.fromSegment(leg2Vertices[Box.B_LOW_RIGHT], leg2Vertices[Box.A_LOW_RIGHT]);
            up_left_line = Line.fromSegment(leg2Vertices[Box.B_UPR_LEFT], leg2Vertices[Box.A_UPR_LEFT]);
            up_right_line = Line.fromSegment(leg2Vertices[Box.B_UPR_RIGHT], leg2Vertices[Box.A_UPR_RIGHT]);

            leg2Vertices[Box.A_LOW_LEFT] = bisectingPlane.intersect(low_left_line);
            leg2Vertices[Box.A_LOW_RIGHT] = bisectingPlane.intersect(low_right_line);
            leg2Vertices[Box.A_UPR_LEFT] = bisectingPlane.intersect(up_left_line);
            leg2Vertices[Box.A_UPR_RIGHT] = bisectingPlane.intersect(up_right_line);

            leg1.setEnableEndCap(this.isEnableInnerCaps());
            leg2.setEnableStartCap(this.isEnableInnerCaps());
            leg1.setEllipsoidalVertices(globe, leg1Vertices);
            leg2.setEllipsoidalVertices(globe, leg2Vertices);
        }
    }

    /**
     * Returns a <code>Plane</code> in ellipsoidal Cartesian coordinates that bisects the angle between the two legs at
     * the point at their common location. <code>leg1</code> must precede <code>leg2</code>, and they must share a
     * common location at the end of <code>leg1</code> and the beginning of <code>leg2</code>. This returns
     * <code>null</code> if the legs overlap and cannot be bisected.
     *
     * @param globe the <code>Globe</code> the legs are related to.
     * @param leg1  the first leg.
     * @param leg2  the second leg.
     *
     * @return a <code>Plane</code> that bisects the geometry of the two legs.
     */
    protected Plane computeEllipsoidalBisectingPlane(Globe globe, Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();

        // Compute the Cartesian point of the the first leg's starting location, the two leg's common location, and the
        // second leg's ending location. Use the lower altitude, because we're only interested in the angles between the
        // two legs.
        Vec4 a = globe.computeEllipsoidalPointFromPosition(leg1Loc[0].latitude, leg1Loc[0].longitude, leg1Altitudes[0]);
        Vec4 b = globe.computeEllipsoidalPointFromPosition(leg1Loc[1].latitude, leg1Loc[1].longitude, leg1Altitudes[0]);
        Vec4 c = globe.computeEllipsoidalPointFromPosition(leg2Loc[1].latitude, leg2Loc[1].longitude, leg2Altitudes[0]);

        // Compute a vector that lies on a plane that bisects the angle between the two legs at their common location.
        // This vector is perpendicular to the vectors connecting both leg's locations.
        Vec4 ab = a.subtract3(b).normalize3();
        Vec4 cb = c.subtract3(b).normalize3();
        Vec4 ab_plus_cb = ab.add3(cb);

        Vec4 n;

        if (ab_plus_cb.getLength3() < 0.0000001)
        {
            // If the legs are parallel or nearly parallel, computing their bisecting plane using the cross product of
            // their length vectors can produce unexpected results due to floating point rounding. In this case it's
            // safe to treat legs as parallel and use the vector connecting the first leg's locations as the bisecting
            // plane.
            n = ab.normalize3();
        }
        else
        {
            // Otherwise, we compute the bisecting plane as the plane that contains the bisecting vector and the Globe's
            // normal at the two legs' common location.
            Vec4 bNormal = globe.computeEllipsoidalNormalAtLocation(leg1Loc[1].latitude, leg1Loc[1].longitude);
            n = bNormal.cross3(ab_plus_cb).normalize3();
        }

        double d = -b.dot3(n);
        return new Plane(n.getX(), n.getY(), n.getZ(), d);
    }

    /**
     * Specifies whether the angle between the two adjacent legs is less than a threshold on the ellipsoid specified by
     * the <code>globe</code>. The threshold is configured by calling {@link #setSmallAngleThreshold(gov.nasa.worldwind.geom.Angle)}.
     * <code>leg1</code> must precede <code>leg2</code>, and they must share a common location at the end of
     * <code>leg1</code> and the beginning of <code>leg2</code>.
     *
     * @param globe the <code>Globe</code> the legs are related to.
     * @param leg1  the first leg.
     * @param leg2  the second leg.
     *
     * @return <code>true</code> if the angle between the two legs is small, otherwise <code>false</code>.
     */
    protected boolean isSmallAngle(Globe globe, Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();

        // Compute the lower center point of leg1's starting, the lower center point on the two leg's common location,
        // and the lower center point of the leg2's ending.
        Vec4 a = globe.computeEllipsoidalPointFromPosition(leg1Loc[0].latitude, leg1Loc[0].longitude, leg1Altitudes[0]);
        Vec4 b = globe.computeEllipsoidalPointFromPosition(leg1Loc[1].latitude, leg1Loc[1].longitude, leg1Altitudes[0]);
        Vec4 c = globe.computeEllipsoidalPointFromPosition(leg2Loc[1].latitude, leg2Loc[1].longitude, leg2Altitudes[0]);

        Vec4 ba = a.subtract3(b);
        Vec4 bc = c.subtract3(b);
        Angle angle = ba.angleBetween3(bc);

        return angle.compareTo(this.getSmallAngleThreshold()) <= 0;
    }

    /**
     * Specifies whether the two adjacent legs make a right turn relative to the ellipsoid specified by the
     * <code>globe</code>. <code>leg1</code> must precede <code>leg2</code>, and they must share a common location at
     * the end of <code>leg1</code> and the beginning of <code>leg2</code>.
     *
     * @param globe the <code>Globe</code> the legs are related to.
     * @param leg1  the first leg.
     * @param leg2  the second leg.
     *
     * @return @return <code>true</code> if the legs make a right turn on the specified <code>globe</code>, otherwise
     * <code>false</code>.
     */
    protected boolean isRightTurn(Globe globe, Box leg1, Box leg2)
    {
        LatLon[] leg1Loc = leg1.getLocations();
        LatLon[] leg2Loc = leg2.getLocations();
        double[] leg1Altitudes = leg1.getAltitudes();
        double[] leg2Altitudes = leg2.getAltitudes();

        // Compute the lower center point of leg1's starting, the lower center point on the two leg's common location,
        // and the lower center point of the leg2's ending.
        Vec4 a = globe.computeEllipsoidalPointFromPosition(leg1Loc[0].latitude, leg1Loc[0].longitude, leg1Altitudes[0]);
        Vec4 b = globe.computeEllipsoidalPointFromPosition(leg1Loc[1].latitude, leg1Loc[1].longitude, leg1Altitudes[0]);
        Vec4 c = globe.computeEllipsoidalPointFromPosition(leg2Loc[1].latitude, leg2Loc[1].longitude, leg2Altitudes[0]);

        Vec4 ba = a.subtract3(b);
        Vec4 bc = c.subtract3(b);
        Vec4 cross = ba.cross3(bc);

        Vec4 n = globe.computeEllipsoidalNormalAtLocation(leg1Loc[1].latitude, leg1Loc[1].longitude);
        return cross.dot3(n) >= 0;
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        this.determineActiveAttributes(dc);

        // Update the child leg vertices if they're out of date. Since the leg vertices are used to determine how each
        // leg is shaped with respect to its neighbors, the vertices must be current before rendering each leg.
        if (this.isLegsOutOfDate(dc))
        {
            this.doUpdateLegs(dc);
        }

        for (Box leg : this.legs)
        {
            // Synchronize the leg's attributes with this track's attributes, and setup this track as the leg's pick
            // delegate.
            leg.setAttributes(this.getActiveAttributes());
            leg.setDelegateOwner(this.getDelegateOwner() != null ? this.getDelegateOwner() : this);
            leg.preRender(dc);
        }
    }

    @Override
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        if (!this.isAirspaceVisible(dc))
            return;

        for (Box leg : this.legs)
        {
            leg.render(dc);
        }
    }

    @Override
    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        // Intentionally left blank.
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsBoolean(context, "enableInnerCaps", this.isEnableInnerCaps());

        RestorableSupport.StateObject so = rs.addStateObject(context, "legs");
        for (Box leg : this.legs)
        {
            RestorableSupport.StateObject lso = rs.addStateObject(so, "leg");
            leg.doGetRestorableState(rs, lso);
        }
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Boolean b = rs.getStateValueAsBoolean(context, "enableInnerCaps");
        if (b != null)
            this.setEnableInnerCaps(b);

        RestorableSupport.StateObject so = rs.getStateObject(context, "legs");
        if (so == null)
            return;

        RestorableSupport.StateObject[] lsos = rs.getAllStateObjects(so, "leg");
        if (lsos == null || lsos.length == 0)
            return;

        ArrayList<Box> legList = new ArrayList<Box>(lsos.length);

        for (RestorableSupport.StateObject lso : lsos)
        {
            if (lso != null)
            {
                Box leg = new Box();
                leg.doRestoreState(rs, lso);
                legList.add(leg);
            }
        }

        this.setLegs(legList);
    }
}
