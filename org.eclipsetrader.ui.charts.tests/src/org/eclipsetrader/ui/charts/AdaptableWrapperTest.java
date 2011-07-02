/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import junit.framework.TestCase;

public class AdaptableWrapperTest extends TestCase {

    public void testGetObjectAdapter() throws Exception {
        AdaptableWrapper wrapper = new AdaptableWrapper("Test");
        String s = (String) wrapper.getAdapter(String.class);
        assertEquals("Test", s);
    }

    public void testGetUnknownObjectAdapter() throws Exception {
        AdaptableWrapper wrapper = new AdaptableWrapper("Test");
        Double s = (Double) wrapper.getAdapter(Double.class);
        assertNull(s);
    }

    public void testGetGenericObjectAdapter() throws Exception {
        AdaptableWrapper wrapper = new AdaptableWrapper("Test");
        Object s = wrapper.getAdapter(Object.class);
        assertEquals("Test", s);
    }

    public void testEquals() throws Exception {
        AdaptableWrapper wrapper = new AdaptableWrapper("Test");
        assertTrue(wrapper.equals("Test"));
        assertEquals(new AdaptableWrapper("Test"), wrapper);
    }
}
