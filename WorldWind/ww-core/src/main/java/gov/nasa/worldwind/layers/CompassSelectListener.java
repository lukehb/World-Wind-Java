/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

/**
 * Controller for on-screen compass displayed by {@link CompassLayer}. This
 * controller provides for highlighting and selection logic of the compass.
 * When a selection is detected on the compass, this controller will cause
 * the view's heading, pitch or roll angle to snap to the configured heading,
 * pitch or roll angles respectively.
 *
 * @author Wiehann Matthysen
 * @version $Id$
 * @see ViewControlsLayer
 */
public class CompassSelectListener implements SelectListener
{
    protected WorldWindow wwd;
    protected CompassLayer compassLayer;
    
    protected double prevOpacity = -1.0;
    protected double highlightOpacity = 1.0;
    
    protected boolean snapToHeading = true;
    protected boolean snapToPitch = false;
    protected boolean snapToRoll = false;
    
    protected Angle snapToHeadingAngle = Angle.ZERO;
    protected Angle snapToPitchAngle = Angle.ZERO;
    protected Angle snapToRollAngle = Angle.ZERO;
    
    /**
     * Construct a controller for specified <code>WorldWindow</code> and <code>CompassLayer</code>.
     *
     * @param wwd the <code>WorldWindow</code> the specified layer is associated with.
     * @param layer the layer to control.
     */
    public CompassSelectListener(WorldWindow wwd, CompassLayer layer)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        this.wwd = wwd;
        this.compassLayer = layer;
    }

    @Override
    public void selected(SelectEvent event)
    {
        if (this.wwd == null)
            return;
        
        if (this.prevOpacity == -1.0)
        {
            this.prevOpacity = this.compassLayer.getOpacity();
        }
        if (this.compassLayer.getOpacity() != this.prevOpacity)
        {
            this.compassLayer.setOpacity(this.prevOpacity);
        }
        
        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
            return;
        
        if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this.getParentLayer()
                || !(event.getTopObject() instanceof AVList))
            return;
        
        if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            // Highlight on rollover
            this.prevOpacity = this.compassLayer.getOpacity();
            this.compassLayer.setOpacity(this.highlightOpacity);
            this.wwd.redraw();
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            // just consume drag events
            event.consume();
        }
        else if (event.getEventAction().equals(SelectEvent.HOVER))
        {
            // Highlight on hover
            this.prevOpacity = this.compassLayer.getOpacity();
            this.compassLayer.setOpacity(this.highlightOpacity);
            this.wwd.redraw();
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            // Handle left click on compass
            View view = this.wwd.getView();
            if (view instanceof BasicOrbitView)
            {
                // Animate snap-to angles when view is BasicOrbitView.
                view.stopAnimations();
                BasicOrbitView orbitView = (BasicOrbitView)view;
                if (this.snapToHeading)
                {
                    Angle currentHeading = orbitView.getHeading();
                    orbitView.addHeadingAnimator(currentHeading, this.snapToHeadingAngle);
                }
                if (this.snapToPitch)
                {
                    Angle currentPitch = orbitView.getPitch();
                    orbitView.addPitchAnimator(currentPitch, this.snapToPitchAngle);
                }
                if (this.snapToRoll)
                {
                    view.setRoll(this.snapToRollAngle);
                }
            }
            else
            {
                // Else, immediately set snap-to angles.
                view.stopAnimations();
                if (this.snapToHeading)
                {
                    view.setHeading(this.snapToHeadingAngle);
                }
                if (this.snapToPitch)
                {
                    view.setPitch(this.snapToPitchAngle);
                }
                if (this.snapToRoll)
                {
                    view.setRoll(this.snapToRollAngle);
                }
            }
        }
    }
    
    /**
     * Returns this CompassSelectListener's parent layer. The parent layer is associated with picked objects,
     * and is used to determine which SelectEvents this CompassSelectListener responds to.
     *
     * @return this CompassSelectListener's parent layer.
     */
    protected Layer getParentLayer()
    {
        return this.compassLayer;
    }
    
    /**
     * Set the opacity that is used when the compass is highlighted (when a rollover or hover occurs).
     *
     * @param opacity the opacity as a fractional value between 0.0 and 1.0.
     *
     * @throws IllegalArgumentException if an invalid value is specified for the opacity.
     */
    public void setHighlightOpacity(double opacity)
    {
        if (opacity < 0.0 || opacity > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.highlightOpacity = opacity;
    }
    
    /**
     * Get the opacity that is used when the compass is highlighted.
     *
     * @return the opacity as a fractional value between 0.0 and 1.0.
     */
    public double getHighlightOpacity()
    {
        return this.highlightOpacity;
    }
    
    /**
     * Sets whether or not a click on the compass will cause the view's heading
     * to snap to the <code>snapToHeading</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToHeadingAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @param state If <code>true</code>, a click on the compass will cause the
     * view's heading to snap to the <code>snapToHeading</code> angle.
     */
    public void setSnapToHeading(boolean state)
    {
        this.snapToHeading = state;
    }
    
    /**
     * Returns whether or not a click on the compass will cause the view's heading
     * to snap to the <code>snapToHeading</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToHeadingAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @return <code>true</code> if a click on the compass will cause the view's
     * heading to snap to the <code>snapToHeading</code> angle; <code>false</code> otherwise.
     */
    public boolean isSnapToHeading()
    {
        return this.snapToHeading;
    }
    
    /**
     * Sets whether or not a click on the compass will cause the view's pitch
     * to snap to the <code>snapToPitch</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToPitchAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @param state If <code>true</code>, a click on the compass will cause the
     * view's pitch to snap to the <code>snapToPitch</code> angle.
     */
    public void setSnapToPitch(boolean state)
    {
        this.snapToPitch = state;
    }
    
    /**
     * Returns whether or not a click on the compass will cause the view's pitch
     * to snap to the <code>snapToPitch</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToPitchAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @return <code>true</code> if a click on the compass will cause the view's
     * pitch to snap to the <code>snapToPitch</code> angle; <code>false</code> otherwise.
     */
    public boolean isSnapToPitch()
    {
        return this.snapToPitch;
    }
    
    /**
     * Sets whether or not a click on the compass will cause the view's roll
     * to snap to the <code>snapToRoll</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToRollAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @param state If <code>true</code>, a click on the compass will cause the
     * view's roll to snap to the <code>snapToRoll</code> angle.
     */
    public void setSnapToRoll(boolean state)
    {
        this.snapToRoll = state;
    }
    
    /**
     * Returns whether or not a click on the compass will cause the view's roll
     * to snap to the <code>snapToRoll</code> angle.
     *
     * @see {@link CompassSelectListener#setSnapToRollAngle(gov.nasa.worldwind.geom.Angle)}
     * 
     * @return <code>true</code> if a click on the compass will cause the view's
     * roll to snap to the <code>snapToRoll</code> angle; <code>false</code> otherwise.
     */
    public boolean isSnapToRoll()
    {
        return this.snapToRoll;
    }
    
    /**
     * Set the angle that will be used as heading for the view when the compass
     * is clicked. The angle will only be set if the <code>snapToHeading</code>
     * flag has been activated.
     * 
     * @see {@link CompassSelectListener#setSnapToHeading(boolean)}.
     * 
     * @throws NullPointerException if <code>angle</code> is null.
     * 
     * @param angle the angle that will be used as heading for the view when
     * the compass is clicked.
     */
    public void setSnapToHeadingAngle(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.snapToHeadingAngle = angle;
    }
    
    /**
     * Get the angle that will be used as heading for the view when the compass
     * is clicked.
     * 
     * @return the angle that will be used as heading for the view.
     */
    public Angle getSnapToHeadingAngle()
    {
        return this.snapToHeadingAngle;
    }
    
    /**
     * Set the angle that will be used as pitch for the view when the compass
     * is clicked. The angle will only be set if the <code>snapToPitch</code>
     * flag has been activated.
     * 
     * @see {@link CompassSelectListener#setSnapToPitch(boolean)}.
     * 
     * @throws NullPointerException if <code>angle</code> is null.
     * 
     * @param angle the angle that will be used as pitch for the view when the
     * compass is clicked.
     */
    public void setSnapToPitchAngle(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.snapToPitchAngle = angle;
    }
    
    /**
     * Get the angle that will be used as pitch for the view when the compass
     * is clicked.
     * 
     * @return the angle that will be used as pitch for the view.
     */
    public Angle getSnapToPitchAngle()
    {
        return this.snapToPitchAngle;
    }
    
    /**
     * Set the angle that will be used as roll for the view when the compass
     * is clicked. The angle will only be set if the <code>snapToRoll</code>
     * flag has been activated.
     * 
     * @see {@link CompassSelectListener#setSnapToRoll(boolean)}.
     * 
     * @throws NullPointerException if <code>angle</code> is null.
     * 
     * @param angle the angle that will be used as roll for the view when the
     * compass is clicked.
     */
    public void setSnapToRollAngle(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.snapToRollAngle = angle;
    }
    
    /**
     * Get the angle that will be used as roll for the view when the compass
     * is clicked.
     * 
     * @return the angle that will be used as roll for the view.
     */
    public Angle getSnapToRollAngle()
    {
        return this.snapToRollAngle;
    }
}
