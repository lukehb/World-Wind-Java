package gov.nasa.worldwind.util.gdal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test to make sure our extraction of GDAL at runtime
 * is working properly.
 * @author Luke Bermingham
 */
public class GDALUtilsTest {

    @Test
    public void testGDALLoading(){
        Assert.assertTrue(GDALUtils.isGDALAvailable());
    }

}
