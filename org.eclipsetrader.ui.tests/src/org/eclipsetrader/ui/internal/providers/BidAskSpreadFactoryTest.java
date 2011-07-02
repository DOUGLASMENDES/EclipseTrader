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

package org.eclipsetrader.ui.internal.providers;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.tests.core.AdaptableWrapper;

public class BidAskSpreadFactoryTest extends TestCase {

    public void testGetValueAsDouble() throws Exception {
        IDataProvider provider = new BidAskSpreadFactory().createProvider();
        IAdaptable value = new AdaptableWrapper(new Quote(10.0, 10.15));
        assertEquals(new Double(1.5), (Double) provider.getValue(value).getAdapter(Double.class), 0.01);
    }

    public void testGetValueAsString() throws Exception {
        IDataProvider provider = new BidAskSpreadFactory().createProvider();
        IAdaptable value = new AdaptableWrapper(new Quote(10.0, 10.15));
        assertEquals("1.5%", provider.getValue(value).getAdapter(String.class));
    }

    public void testZeroBidValue() throws Exception {
        IDataProvider provider = new BidAskSpreadFactory().createProvider();
        IAdaptable value = new AdaptableWrapper(new Quote(0.0, 10.15));
        assertNull(provider.getValue(value));
    }

    public void testZeroAskValue() throws Exception {
        IDataProvider provider = new BidAskSpreadFactory().createProvider();
        IAdaptable value = new AdaptableWrapper(new Quote(10.0, 0.0));
        assertNull(provider.getValue(value));
    }

    public void testZeroValues() throws Exception {
        IDataProvider provider = new BidAskSpreadFactory().createProvider();
        IAdaptable value = new AdaptableWrapper(new Quote(0.0, 0.0));
        assertNull(provider.getValue(value));
    }

    @SuppressWarnings("unchecked")
    public void testGetClassTypes() throws Exception {
        Class[] types = new BidAskSpreadFactory().getType();
        assertSame(Double.class, types[0]);
        assertSame(String.class, types[1]);
    }
}
