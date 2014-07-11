/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Wiehann Matthysen
 * @version $Id$
 */
public class AngleTest
{
    @Test
    public void testCreateAngleFromDMS()
    {
        // Test angle with zero-degrees, and non-zero minutes and seconds.
        Angle angle01 = Angle.fromDMS(0, 30, 18);
        // Degrees should be 30 / 60 + 18 / 3600 = 0.505
        assertThat(angle01.getDegrees(), is(0.505));
    }
    
    @Test
    public void testCreateAngleFromDM() {
        // Test angle with zero-degrees, and non-zero minutes.
        Angle angle02 = Angle.fromDMdS(0, 30);
        // Degrees should be 30 / 60 + 0 / 3600 = 0.5
        assertThat(angle02.getDegrees(), is(0.5));
    }
}
