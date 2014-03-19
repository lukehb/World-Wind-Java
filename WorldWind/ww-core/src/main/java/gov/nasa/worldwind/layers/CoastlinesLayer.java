/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.formats.shapefile.ShapefileLoader;

/**
 * Loads and renders coast-lines from a local shape-file.
 * 
 * @author Wiehann Matthysen
 */
public class CoastlinesLayer extends ShapefileLayer
{
    protected static final String DEFAULT_COASTLINES_FILE = "shapes/Coastlines";
    
    public CoastlinesLayer()
    {
        super(DEFAULT_COASTLINES_FILE);
    }
    
    public CoastlinesLayer(ShapefileLoader shapefileLoader)
    {
        super(DEFAULT_COASTLINES_FILE, shapefileLoader);
    }
}