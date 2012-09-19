/*
 * Copyright (C) 2012 DreamHammer.com
 */

package gov.nasa.worldwind.kml;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * Represents the KML <i>SimpleField</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id$
 */
public class KMLSimpleField extends AbstractXMLEventParser
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLSimpleField(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getType()
    {
        return (String) this.getField("type");
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public String getDisplayName()
    {
        return (String) this.getField("displayName");
    }
}
