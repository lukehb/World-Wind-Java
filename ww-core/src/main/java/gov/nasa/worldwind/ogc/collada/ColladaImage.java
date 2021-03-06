/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>image</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaImage extends ColladaAbstractObject
{
    public ColladaImage(String ns)
    {
        super(ns);
    }

    public String getInitFrom()
    {
        return (String) this.getField("init_from");
    }
}
