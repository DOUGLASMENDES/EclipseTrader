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

package org.eclipsetrader.core.internal.repositories;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.StoreProperties;

public class DefaultElementFactoryTest extends TestCase {

    public void testDontCreateUnknownObjectType() throws Exception {
        DefaultElementFactory factory = new DefaultElementFactory();
        StoreProperties properties = new StoreProperties();
        assertNull(factory.createElement(null, properties));
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, StoreProperties.class.getName());
        assertNull(factory.createElement(null, properties));
    }

    public void testCreateSecurity() throws Exception {
        DefaultElementFactory factory = new DefaultElementFactory();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());
        Object object = factory.createElement(null, properties);
        assertNotNull(object);
        assertTrue(object instanceof Security);
    }

    public void testCreateCommonStock() throws Exception {
        DefaultElementFactory factory = new DefaultElementFactory();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IStock.class.getName());
        Object object = factory.createElement(null, properties);
        assertNotNull(object);
        assertTrue(object instanceof Stock);
    }

    public void testCreateCurrencyExchange() throws Exception {
        DefaultElementFactory factory = new DefaultElementFactory();
        StoreProperties properties = new StoreProperties();
        properties.setProperty(IPropertyConstants.OBJECT_TYPE, ICurrencyExchange.class.getName());
        Object object = factory.createElement(null, properties);
        assertNotNull(object);
        assertTrue(object instanceof CurrencyExchange);
    }
}
