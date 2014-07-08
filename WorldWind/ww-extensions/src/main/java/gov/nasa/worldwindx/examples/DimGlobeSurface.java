/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.layers.SurfaceColorLayer;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Shows how to add a layer over the globe's surface imagery to simulate dimming
 * the surface. The technique is very simple: just create a {@link SurfaceColorLayer},
 * and use its opacity to control the amount of dimming. This example uses a
 * black surface image, but any color could be used.
 *
 * Note that this does not provide a filtering effect -- enhancing or blocking
 * specific colors. For that
 * <code>SurfaceImage</code> would need blending controls, but it doesn't have
 * them.
 *
 * @author tag
 * @version $Id$
 */
public class DimGlobeSurface extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected SurfaceColorLayer layer;
        protected JSlider opacitySlider;

        public AppFrame()
        {
            super(true, true, false);

            // Create a surface color layer covering the full globe and set its initial opacity.
            this.layer = new SurfaceColorLayer(Color.BLACK);
            this.layer.setOpacity(0.10);
            this.layer.setPickEnabled(false);
            this.layer.setName("Surface Dimmer");

            ApplicationTemplate.insertBeforePlacenames(this.getWwd(), layer);

            // Create an opacity control panel.

            JPanel opacityPanel = new JPanel(new BorderLayout(5, 5));
            opacityPanel.setBorder(new EmptyBorder(5, 10, 10, 5));
            opacityPanel.add(new JLabel("Opacity"), BorderLayout.WEST);
            this.makeOpacitySlider();
            opacityPanel.add(this.opacitySlider, BorderLayout.CENTER);
            this.getControlPanel().add(opacityPanel, BorderLayout.SOUTH);
        }

        protected final void makeOpacitySlider()
        {
            this.opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (this.layer.getOpacity() * 100));
            this.opacitySlider.setToolTipText("Filter opacity");
            this.opacitySlider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent event)
                {
                    double value = opacitySlider.getValue();
                    layer.setOpacity(value / 100);
                    getWwd().redraw();
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Surface Dimming", AppFrame.class);
    }
}
