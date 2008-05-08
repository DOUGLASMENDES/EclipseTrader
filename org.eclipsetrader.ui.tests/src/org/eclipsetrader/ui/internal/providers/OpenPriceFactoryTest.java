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
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.tests.core.AdaptableWrapper;

public class OpenPriceFactoryTest extends TestCase {
	private IDataProvider provider = new OpenPriceFactory().createProvider();

	public void testGetDoubleValue() throws Exception {
		TodayOHL element = new TodayOHL(20.0, 30.0, 10.0);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertEquals(new Double(20.0), value.getAdapter(Double.class));
	    assertEquals(new Double(20.0), value.getAdapter(Number.class));
    }

	public void testGetNumberValue() throws Exception {
		TodayOHL element = new TodayOHL(20.0, 30.0, 10.0);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertEquals(new Double(20.0), value.getAdapter(Number.class));
    }

	public void testGetStringValue() throws Exception {
		TodayOHL element = new TodayOHL(20.0, 30.0, 10.0);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertNotNull(value.getAdapter(String.class));
    }

	public void testGetOtherTypeValue() throws Exception {
		TodayOHL element = new TodayOHL(20.0, 30.0, 10.0);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertNull(value.getAdapter(Long.class));
    }

	public void testEquals() throws Exception {
		TodayOHL element = new TodayOHL(20.0, 30.0, 10.0);
	    IAdaptable value = provider.getValue(new AdaptableWrapper(element));
	    assertTrue(value.equals(new AdaptableWrapper(new Double(20.0))));
	    assertFalse(value.equals(new AdaptableWrapper(new Double(20.5))));
	    assertFalse(value.equals(new AdaptableWrapper(new Long(20))));
	    assertFalse(value.equals(new AdaptableWrapper(null)));
    }
}
