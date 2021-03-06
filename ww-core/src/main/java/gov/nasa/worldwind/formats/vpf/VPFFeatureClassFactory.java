/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id$
 */
public interface VPFFeatureClassFactory
{
    public VPFFeatureClass createFromSchema(VPFCoverage coverage, VPFFeatureClassSchema schema);
}
