/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.ogc.collada;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Timer;

/**
 * Test loading a COLLADA model and moving the model along a path. World Wind does not support animations defined in
 * COLLADA files, but models may be animated by application logic.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaMovingModel extends ColladaViewer
{
    public static class AppFrame extends ColladaViewer.AppFrame
    {
        @Override
        protected void addColladaLayer(final ColladaRoot colladaRoot)
        {
            super.addColladaLayer(colladaRoot);

            // Rotate the duck to an upright position
            colladaRoot.setPitch(Angle.POS90);

            int delay = 1000; //milliseconds
            ActionListener taskPerformer = new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    double deltaDegrees = 0.001;
                    Position position = colladaRoot.getPosition();
                    Position newPosition = position.add(Position.fromDegrees(deltaDegrees, deltaDegrees));

                    // Move the model
                    colladaRoot.setPosition(newPosition);

                    // Move the view to follow the model
                    WorldWindow wwd = getWwd();
                    wwd.getView().goTo(newPosition, 2000);
                    wwd.redraw();
                }
            };
            new Timer(delay, taskPerformer).start();
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.028);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -105.27284091410579);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 4000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 50);

        final AppFrame af = (AppFrame) ApplicationTemplate.start("World Wind COLLADA Viewer", AppFrame.class);

        new WorkerThread(new File("testData/collada/duck_triangulate.dae"),
            Position.fromDegrees(40.00779229910037, -105.27494931422459, 100), af).start();
    }
}