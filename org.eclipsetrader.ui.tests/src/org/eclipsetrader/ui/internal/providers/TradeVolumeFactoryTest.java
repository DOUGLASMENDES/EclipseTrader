/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.tests.core.AdaptableWrapper;

public class TradeVolumeFactoryTest extends TestCase {
	private IDataProvider provider = new TradeVolumeFactory().createProvider();

	public void testGetLongValue() throws Exception {
		Trade element = new Trade(null, 10.0, 2000L, 300000L);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertEquals(new Long(300000), value.getAdapter(Long.class));
    }

	public void testGetNumberValue() throws Exception {
		Trade element = new Trade(null, 10.0, 2000L, 300000L);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertEquals(new Long(300000), value.getAdapter(Number.class));
    }

	public void testGetStringValue() throws Exception {
		Trade element = new Trade(null, 10.0, 2000L, 300000L);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertNotNull(value.getAdapter(String.class));
    }

	public void testGetOtherTypeValue() throws Exception {
		Trade element = new Trade(null, 10.0, 2000L, 300000L);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertNull(value.getAdapter(Double.class));
    }

	public void testEquals() throws Exception {
		Trade element = new Trade(null, 10.0, 2000L, 300000L);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertTrue(value.equals(new AdaptableWrapper(new Long(300000))));
	    assertFalse(value.equals(new AdaptableWrapper(new Long(310000))));
	    assertFalse(value.equals(new AdaptableWrapper(new Double(300000))));
	    assertFalse(value.equals(new AdaptableWrapper(null)));
    }
}
