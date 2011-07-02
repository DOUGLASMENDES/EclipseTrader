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

package org.eclipsetrader.core.feed;

import junit.framework.TestCase;

public class FeedPropertiesTest extends TestCase {

    public void testGetPropertiesIDs() throws Exception {
        FeedProperties properties = new FeedProperties();
        assertEquals(0, properties.getPropertyIDs().length);
        properties.setProperty("p1", "value1");
        assertEquals(1, properties.getPropertyIDs().length);
        assertEquals("p1", properties.getPropertyIDs()[0]);
    }

    public void testSetAndGetProperty() throws Exception {
        FeedProperties properties = new FeedProperties();
        assertNull(properties.getProperty("p1"));
        properties.setProperty("p1", "value1");
        assertEquals("value1", properties.getProperty("p1"));
    }

    public void testSetNullValueRemovesProperty() throws Exception {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("p1", "value1");
        assertEquals(1, properties.getPropertyIDs().length);
        properties.setProperty("p1", null);
        assertEquals(0, properties.getPropertyIDs().length);
    }
}
