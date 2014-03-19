/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.formats.shapefile.ShapefileLoader;

/**
 * Loads and renders country-borders from a local shape-file.
 * 
 * @author Wiehann Matthysen
 */
public class BordersLayer extends ShapefileLayer
{
    protected static final String DEFAULT_BORDERS_FILE = "shapes/Borders";
    
    public BordersLayer()
    {
        super(DEFAULT_BORDERS_FILE);
    }
    
    public BordersLayer(ShapefileLoader shapefileLoader)
    {
        super(DEFAULT_BORDERS_FILE, shapefileLoader);
    }
}
