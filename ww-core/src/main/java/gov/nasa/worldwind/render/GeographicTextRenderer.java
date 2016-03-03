/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

/**
 *
 * @author Wiehann Matthysen
 */
public interface GeographicTextRenderer
{
    String getEffect();
    
    void setEffect(String effect);
    
    void render(DrawContext dc, Iterable<? extends GeographicText> textIterable);
}
