/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import gov.nasa.worldwindx.examples.util.SelectionHighlightController;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Demonstrates how to use the {@link gov.nasa.worldwindx.examples.util.ScreenSelector} utility to perform
 * multiple-object selection in screen space.
 *
 * @author dcollins
 * @version $Id$
 */
public class ScreenSelection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ScreenSelector screenSelector;
        protected SelectionHighlightController selectionHighlightController;

        public AppFrame()
        {
            // Create a screen selector to display a screen selection rectangle and track the objects intersecting
            // that rectangle.
            this.screenSelector = new ScreenSelector(this.getWwd());

            // Set up a custom highlight controller that highlights objects both under the cursor and inside the
            // selection rectangle. Disable the superclass' default highlight controller to prevent it from interfering
            // with our highlight controller.
            this.selectionHighlightController = new SelectionHighlightController(this.getWwd(), this.screenSelector);
            this.getWwjPanel().highlightController.dispose();

            // Create a button to enable and disable screen selection.
            JButton btn = new JButton(new EnableSelectorAction());
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.add(btn, BorderLayout.CENTER);
            this.getLayerPanel().add(panel, BorderLayout.SOUTH);

            // Create layer of highlightable shapes to select.
            this.addShapes();
        }

        protected void addShapes()
        {
            RenderableLayer layer = new RenderableLayer();

            ShapeAttributes highlightAttrs = new BasicShapeAttributes();
            highlightAttrs.setInteriorMaterial(Material.RED);
            highlightAttrs.setOutlineMaterial(Material.WHITE);

            for (int lon = -180; lon < 180; lon += 10)
            {
                for (int lat = -90; lat < 90; lat += 10)
                {
                    ExtrudedPolygon poly = new ExtrudedPolygon(Arrays.asList(
                        LatLon.fromDegrees(lat - 1, lon - 1),
                        LatLon.fromDegrees(lat - 1, lon + 1),
                        LatLon.fromDegrees(lat + 1, lon + 1),
                        LatLon.fromDegrees(lat + 1, lon - 1)),
                        100000d);
                    poly.setHighlightAttributes(highlightAttrs);
                    poly.setSideHighlightAttributes(highlightAttrs);
                    layer.addRenderable(poly);
                }
            }

            this.getWwd().getModel().getLayers().add(layer);
        }

        protected class EnableSelectorAction extends AbstractAction
        {
            public EnableSelectorAction()
            {
                super("Start");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new DisableSelectorAction());
                screenSelector.enable();
            }
        }

        protected class DisableSelectorAction extends AbstractAction
        {
            public DisableSelectorAction()
            {
                super("Stop");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new EnableSelectorAction());
                screenSelector.disable();
            }
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Screen Selection", AppFrame.class);
    }
}
