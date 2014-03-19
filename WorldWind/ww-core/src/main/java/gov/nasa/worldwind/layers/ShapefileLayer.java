/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileLoader;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;

import java.io.InputStream;

/**
 *
 * @author Wiehann Matthysen
 */
public class ShapefileLayer extends RenderableLayer
{
    protected String shapeFileName = "shapes/Empty";
    protected ShapefileLoader shapefileLoader = new ShapefileLoader();
    protected boolean rebuild;
    
    public ShapefileLayer(String fileName, ShapefileLoader shapefileLoader)
    {
        initialize(fileName, shapefileLoader);
    }
    
    public ShapefileLayer(String fileName)
    {
        initialize(fileName, null);
    }
    
    protected void initialize(String shapeFileName, ShapefileLoader shapefileLoader)
    {
        if (shapeFileName != null)
            this.shapeFileName = shapeFileName;
        
        if (shapefileLoader != null)
            this.shapefileLoader = shapefileLoader;
        
        InputStream shpStream = WWIO.openFileOrResourceStream(WWIO.replaceSuffix(this.shapeFileName, ".shp"), this.getClass());
        InputStream shxStream = WWIO.openFileOrResourceStream(WWIO.replaceSuffix(this.shapeFileName, ".shx"), this.getClass());
        InputStream dbfStream = WWIO.openFileOrResourceStream(WWIO.replaceSuffix(this.shapeFileName, ".dbf"), this.getClass());
        InputStream prjStream = WWIO.openFileOrResourceStream(WWIO.replaceSuffix(this.shapeFileName, ".prj"), this.getClass());
        Shapefile shapefile = new Shapefile(shpStream, shxStream, dbfStream, prjStream);
        this.shapefileLoader.populateLayerFromShapefile(shapefile, this);
    }
    
    /**
     * Indicates the path and filename of the shape-file.
     *
     * @return name of shape-file.
     */
    public String getShapeFileName()
    {
        return this.shapeFileName;
    }
    
    /**
     * Specifies the path and filename of the shape-file.
     *
     * @param shapeFileName the path and filename.
     *
     * @throws IllegalArgumentException if the file name is null or empty.
     */
    public void setShapeFileName(String shapeFileName)
    {
        if (WWUtil.isEmpty(shapeFileName))
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.shapeFileName = shapeFileName;
        this.rebuild = true;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (this.rebuild)
        {
            clearRenderables();
            initialize(this.shapeFileName, this.shapefileLoader);
            this.rebuild = false;
        }
        super.doRender(dc);
    }
}
