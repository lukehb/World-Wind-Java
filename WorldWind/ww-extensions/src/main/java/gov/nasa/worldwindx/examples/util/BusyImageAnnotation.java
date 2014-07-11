/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL2;

/**
 * @author dcollins
 * @version $Id$
 */
public class BusyImageAnnotation extends ImageAnnotation
{
    protected boolean autoUpdate;
    protected boolean shouldUpdate;
    protected Angle angle;
    protected Angle increment;
    protected long lastFrameTime;
    
    protected long count;

    public BusyImageAnnotation(Object imageSource)
    {
        super(imageSource);
        this.setUseMipmaps(false);
        this.autoUpdate = true;
        this.shouldUpdate = false;
        this.angle = Angle.ZERO;
        this.increment = Angle.fromDegrees(300);
        this.count = 0;
    }

    public Angle getAngle()
    {
        return this.angle;
    }

    public void setAngle(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double a = angle.degrees % 360;
        a = (a > 180) ? (a - 360) : (a < -180 ? 360 + a : a);
        this.angle = Angle.fromDegrees(a);
    }
    
    public void setAutoUpdateState(boolean autoUpdate)
    {
        this.autoUpdate = autoUpdate;
    }
    
    public boolean isAutoUpdateState()
    {
        return this.autoUpdate;
    }

    public Angle getIncrement()
    {
        return this.increment;
    }

    public void setIncrement(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.increment = angle;
    }

    public void drawContent(DrawContext dc, int width, int height, double opacity, Position pickPosition)
    {
        super.drawContent(dc, width, height, opacity, pickPosition);
        if (this.autoUpdate)
        {
            this.updateState(dc);
        }
        else
        {
            if (this.shouldUpdate)
            {
                this.updateState(dc);
                this.shouldUpdate = false;
            }
        }
    }

    protected void transformBackgroundImageCoordsToAnnotationCoords(DrawContext dc, int width, int height, WWTexture texture)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Rotate around an axis originating from the center of the image and coming out of the screen.
        double hw = (double) texture.getWidth(dc) / 2d;
        double hh = (double) texture.getHeight(dc) / 2d;
        gl.glTranslated(hw, hh, 0);
        gl.glRotated(-this.getAngle().degrees, 0, 0, 1);
        gl.glTranslated(-hw, -hh, 0);

        super.transformBackgroundImageCoordsToAnnotationCoords(dc, width, height, texture);
    }

    protected void updateState(DrawContext dc)
    {
        // Increment the angle by a fixed increment each frame.
        Angle increment = this.getIncrement();
        increment = this.adjustAngleIncrement(dc, increment);
        this.setAngle(this.getAngle().add(increment));

        // Fire a property change to force a repaint.
        if (this.autoUpdate)
        {
            dc.getView().firePropertyChange(AVKey.VIEW, null, dc.getView());
        }

        // Update the frame time stamp.
        this.lastFrameTime = dc.getFrameTimeStamp();
    }
    
    public void doUpdateState()
    {
        this.shouldUpdate = true;
    }

    protected Angle adjustAngleIncrement(DrawContext dc, Angle unitsPerSecond)
    {
        long millis = dc.getFrameTimeStamp() - this.lastFrameTime;
        double seconds = millis / 1000.0;
        double degrees = seconds * unitsPerSecond.degrees;

        return Angle.fromDegrees(degrees);
    }
}