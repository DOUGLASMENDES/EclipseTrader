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

package org.eclipsetrader.repository.local.internal.types;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;

public class TimeSpanAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        TimeSpanAdapter adapter = new TimeSpanAdapter();
        assertEquals("5min", adapter.marshal(TimeSpan.minutes(5)));
    }

    public void testUnmarshal() throws Exception {
        TimeSpanAdapter adapter = new TimeSpanAdapter();
        TimeSpan aggregation = adapter.unmarshal("5min");
        assertEquals(Units.Minutes, aggregation.getUnits());
        assertEquals(5, aggregation.getLength());
    }

    public void testUnmarshalSimple() throws Exception {
        TimeSpanAdapter adapter = new TimeSpanAdapter();
        TimeSpan aggregation = adapter.unmarshal("5");
        assertEquals(Units.Minutes, aggregation.getUnits());
        assertEquals(5, aggregation.getLength());
    }

    public void testMarshalNull() throws Exception {
        TimeSpanAdapter adapter = new TimeSpanAdapter();
        assertNull(adapter.marshal(null));
    }

    public void testUnmarshalNull() throws Exception {
        TimeSpanAdapter adapter = new TimeSpanAdapter();
        assertNull(adapter.unmarshal(null));
    }
}
