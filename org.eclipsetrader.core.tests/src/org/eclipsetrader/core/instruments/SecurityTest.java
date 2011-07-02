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

package org.eclipsetrader.core.instruments;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class SecurityTest extends TestCase {

    public void testCreateFromProperties() throws Exception {
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.NAME, "Security");
        properties.setProperty(IPropertyConstants.IDENTIFIER, new FeedIdentifier("ID", null));
        properties.setProperty(IPropertyConstants.USER_PROPERTIES, new UserProperties());
        Security security = new Security(null, properties);
        assertEquals(properties.getProperty(IPropertyConstants.NAME), security.getName());
        assertSame(properties.getProperty(IPropertyConstants.IDENTIFIER), security.getIdentifier());
        assertSame(properties.getProperty(IPropertyConstants.USER_PROPERTIES), security.getProperties());
    }

    public void testFillObjectTypeProperty() throws Exception {
        Security security = new Security();
        IStoreProperties properties = security.getStoreProperties();
        assertEquals(ISecurity.class.getName(), properties.getProperty(IPropertyConstants.OBJECT_TYPE));
    }

    public void testFillNameProperty() throws Exception {
        Security security = new Security("MSFT", null);
        IStoreProperties properties = security.getStoreProperties();
        assertEquals("MSFT", properties.getProperty(IPropertyConstants.NAME));
    }

    public void testFillIdentifierProperty() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("ID", null);
        Security security = new Security(null, identifier);
        IStoreProperties properties = security.getStoreProperties();
        assertSame(identifier, properties.getProperty(IPropertyConstants.IDENTIFIER));
    }

    public void testDontFillFactoryProperty() throws Exception {
        Security security = new Security();
        IStoreProperties properties = security.getStoreProperties();
        assertNull(properties.getProperty(IPropertyConstants.ELEMENT_FACTORY));
    }

    public void testKeepExistingPropertiesSet() throws Exception {
        StoreProperties properties = new StoreProperties();
        Security security = new Security(null, properties);
        assertSame(properties, security.getStoreProperties());
    }

    public void testReturnSamePropertiesSet() throws Exception {
        Security security = new Security();
        IStoreProperties properties = security.getStoreProperties();
        assertSame(properties, security.getStoreProperties());
    }
}
