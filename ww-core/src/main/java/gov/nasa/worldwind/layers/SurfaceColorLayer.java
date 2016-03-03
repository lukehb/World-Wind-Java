/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfaceImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Wiehann Matthysen
 */
public final class SurfaceColorLayer extends SurfaceImageLayer
{
    private SurfaceImage image;
    
    public SurfaceColorLayer(Color color)
    {
        image = new SurfaceImage(createColorImage(color), Sector.FULL_SPHERE);
        addRenderable(image);
    }
    
    public SurfaceColorLayer()
    {
        this(Color.BLACK);
    }
    
    public void setColor(Color color)
    {
        image.setImageSource(createColorImage(color), Sector.FULL_SPHERE);
    }
    
    private static BufferedImage createColorImage(Color color)
    {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); 
        Graphics2D graphics = (Graphics2D) image.getGraphics(); 
        graphics.setColor(color);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight()); 
        graphics.dispose();
        return image;
    }
}
