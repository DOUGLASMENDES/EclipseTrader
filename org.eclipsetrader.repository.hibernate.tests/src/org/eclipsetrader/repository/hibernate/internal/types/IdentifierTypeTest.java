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

package org.eclipsetrader.repository.hibernate.internal.types;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;

public class IdentifierTypeTest extends TestCase {

    public void testConstructor() throws Exception {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("id1", "value1");
        FeedIdentifier identifier = new FeedIdentifier("TEST", properties);
        IdentifierType type = new IdentifierType(identifier);
        assertEquals("TEST", type.getSymbol());
        assertEquals(1, type.properties.size());
        assertEquals("id1", type.properties.get(0).getName());
        assertEquals("value1", type.properties.get(0).getValue());
        assertSame(identifier, type.getIdentifier());
    }

    public void testUpdateProperties() throws Exception {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("id1", "value1");
        IdentifierType type = new IdentifierType(new FeedIdentifier("TEST", properties));
        assertEquals(1, type.properties.size());
        IdentifierPropertyType propertyType = type.properties.get(0);

        type.updateProperties(properties);
        assertEquals(1, type.properties.size());
        assertSame(propertyType, type.properties.get(0));
    }

    public void testUpdateExistingProperties() throws Exception {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("id1", "value1");
        IdentifierType type = new IdentifierType(new FeedIdentifier("TEST", properties));
        assertEquals(1, type.properties.size());
        IdentifierPropertyType propertyType = type.properties.get(0);

        properties.setProperty("id1", "value1a");
        type.updateProperties(properties);
        assertEquals(1, type.properties.size());
        assertSame(propertyType, type.properties.get(0));
        assertEquals("value1a", type.properties.get(0).getValue());
    }
}
