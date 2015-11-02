/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Unit tests for the {@link EntityMap} class.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class EntityMapTest
{
    /** Test basic entity replacement. */
    @Test
    public void testReplace()
    {
        String expected = "text < > & more text";
        String actual = EntityMap.replaceAll("text &lt; &gt; &amp; more text");

        TestCase.assertEquals(expected, actual);
    }

    /** Test replacement of each entity in EntityMap. */
    @Test
    public void testAllReplacements() throws IllegalAccessException
    {
        for (int i = 0; i < EntityMap.entityKeys.length; i++)
        {
            String expected = EntityMap.entityReplacements[i];
            String actual = EntityMap.replaceAll(EntityMap.entityKeys[i]);

            TestCase.assertEquals("Failed entity replacement: " + EntityMap.entityKeys[i], expected, actual);
        }
    }

    /** Test with a missing entity. (Missing entity should NOT be replaced.) */
    @Test
    public void testMissingEntity()
    {
        String expected = "text &thisIsNotAnEntity; more text";
        String actual = EntityMap.replaceAll(expected);

        TestCase.assertEquals(expected, actual);
    }
}