/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.ogc.collada.impl.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Represents the COLLADA <i>instance_visual_scene</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaInstanceVisualScene extends ColladaAbstractInstance<ColladaVisualScene> implements ColladaRenderable
{
    public ColladaInstanceVisualScene(String ns)
    {
        super(ns);
    }

    @Override
    public Box getLocalExtent(ColladaTraversalContext tc)
    {
        ColladaVisualScene instance = this.get();

        return instance != null ? instance.getLocalExtent(tc) : null;
    }

    /** {@inheritDoc} Renders the target of the instance pointer, if the target can be resolved. */
    public void preRender(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaVisualScene instance = this.get();
        if (instance != null)
            instance.preRender(tc, dc);
    }

    /** {@inheritDoc} Renders the target of the instance pointer, if the target can be resolved. */
    public void render(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaVisualScene instance = this.get();
        if (instance != null)
            instance.render(tc, dc);
    }
}
