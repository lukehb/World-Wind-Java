/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * @author tag
 * @version $Id$
 */
public interface SelectListener extends EventListener
{
    public void selected(SelectEvent event);
}
