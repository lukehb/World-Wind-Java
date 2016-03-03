/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.formats.nitfs.*;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */
public class RPFUserDefinedHeaderSegment extends NITFSUserDefinedHeaderSegment
{
    private RPFFileComponents components;

    public RPFUserDefinedHeaderSegment(java.nio.ByteBuffer buffer)
    {
        super(buffer);

        if( RPFHeaderSection.DATA_TAG.equals(this.dataTag) )
        {
            this.components = new RPFFileComponents(buffer);
        }
        else
            throw new NITFSRuntimeException("NITFSReader.RPFHeaderNotFoundInUserDefinedSegment", this.dataTag);
        this.restoreBufferPosition();
    }

    public RPFFileComponents getRPFFileComponents()
    {
        return this.components;
    }
}
