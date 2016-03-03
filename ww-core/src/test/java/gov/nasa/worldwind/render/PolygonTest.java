/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.render.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Polygon}.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class PolygonTest
{
    protected Globe globe = new Earth();
    protected double verticalExaggeration = 1.0;

    protected List<Position> positions = Arrays.asList(
        Position.fromDegrees(28, -106, 0),
        Position.fromDegrees(35, -104, 0),
        Position.fromDegrees(28, -107, 100),
        Position.fromDegrees(28, -106, 0));
    protected Sector sector = Sector.boundingSector(this.positions);

    @Test
    public void testGetExtentClampToGround()
    {
        double[] minAndMaxElevations = this.globe.getMinAndMaxElevations(this.sector);

        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector,
            minAndMaxElevations[0],
            minAndMaxElevations[1]);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtentAbsolute()
    {
        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector, 0, 100);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.ABSOLUTE);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtentRelative()
    {
        double[] minAndMaxElevations = this.globe.getMinAndMaxElevations(this.sector);

        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector,
            minAndMaxElevations[1], minAndMaxElevations[1] + 100);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }
}
