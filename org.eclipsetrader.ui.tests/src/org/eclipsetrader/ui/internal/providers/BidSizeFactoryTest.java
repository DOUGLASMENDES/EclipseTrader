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

public class BidSizeFactoryTest extends TestCase {

    private IDataProvider provider = new BidSizeFactory().createProvider();

    public void testGetLongValue() throws Exception {
        Quote quote = new Quote(10.0, 20.0, 1000L, 2000L);
        IAdaptable value = provider.getValue(new AdaptableWrapper(quote));
        assertEquals(new Long(1000), value.getAdapter(Long.class));
    }

    public void testGetNumberValue() throws Exception {
        Quote quote = new Quote(10.0, 20.0, 1000L, 2000L);
        IAdaptable value = provider.getValue(new AdaptableWrapper(quote));
        assertEquals(new Long(1000), value.getAdapter(Number.class));
    }

    public void testGetStringValue() throws Exception {
        Quote quote = new Quote(10.0, 20.0, 1000L, 2000L);
        IAdaptable value = provider.getValue(new AdaptableWrapper(quote));
        assertNotNull(value.getAdapter(String.class));
    }

    public void testGetOtherTypeValue() throws Exception {
        Quote quote = new Quote(10.0, 20.0, 1000L, 2000L);
        IAdaptable value = provider.getValue(new AdaptableWrapper(quote));
        assertNull(value.getAdapter(Double.class));
    }

    public void testEquals() throws Exception {
        Quote quote = new Quote(10.0, 20.0, 1000L, 2000L);
        IAdaptable value = provider.getValue(new AdaptableWrapper(quote));
        assertTrue(value.equals(new NumberValue(new Long(1000), "1000")));
        assertFalse(value.equals(new NumberValue(new Long(1100), "1100")));
        assertFalse(value.equals(new NumberValue(new Double(1000), "1000.0")));
        assertFalse(value.equals(new AdaptableWrapper(null)));
    }
}
