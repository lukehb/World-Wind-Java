/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.geom.LatLon;

import java.util.List;

/**
 * Interface to reverse-lookup gazetteers.
 *
 * @author tag
 * @version $Id$
 */
public interface ReverseLookupGazetteer
{
    /**
     * Find the nearest point-of-interests given a specific location.
     *
     * @param latlon a location to perform the reverse lookup with.
     *
     * @return the points-of-interests nearest to the given location.
     *
     * @throws NoItemException  if the location cannot be matched.
     * @throws ServiceException if the lookup service is not available or invocation of it fails.
     */
    public List<PointOfInterest> findPlaces(LatLon latlon) throws NoItemException, ServiceException;
}
