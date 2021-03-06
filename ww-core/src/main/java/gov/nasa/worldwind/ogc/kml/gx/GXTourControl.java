/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml.gx;

/**
 * @author tag
 * @version $Id$
 */
public class GXTourControl extends GXAbstractTourPrimitive
{
    public GXTourControl(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getPlayMode()
    {
        return (String) this.getField("playMode");
    }
}
