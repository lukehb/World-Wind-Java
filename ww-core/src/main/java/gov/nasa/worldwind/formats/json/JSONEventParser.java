/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id$
 */
public interface JSONEventParser
{
    Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException;
}
