/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class TerrainProfileAdapter implements PropertyChangeListener
{
    private final WorldWindow ww;
    private final TerrainProfileLayer profileLayer;
    
    public TerrainProfileAdapter(WorldWindow ww, TerrainProfileLayer profileLayer)
    {
        this.ww = ww;
        this.profileLayer = profileLayer;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        MeasureTool measureTool = (MeasureTool)event.getSource();
        // Measure shape position list changed - update terrain profile
        if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
        {
            ArrayList<? extends LatLon> positions = measureTool.getPositions();
            if (positions != null && positions.size() > 1)
            {
                this.profileLayer.setPathPositions(positions);
                this.profileLayer.setEnabled(true);
            } else
            {
                this.profileLayer.setEnabled(false);
            }
            this.ww.redraw();
        }
    }
}
