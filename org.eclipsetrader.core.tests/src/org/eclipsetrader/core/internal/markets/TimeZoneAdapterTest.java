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

package org.eclipsetrader.core.internal.markets;

import java.util.TimeZone;

import junit.framework.TestCase;

public class TimeZoneAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        assertEquals("America/New_York", new TimeZoneAdapter().marshal(TimeZone.getTimeZone("America/New_York")));
    }

    public void testMarshalNull() throws Exception {
        assertNull(new TimeZoneAdapter().marshal(null));
    }

    public void testUnmarshal() throws Exception {
        assertEquals(TimeZone.getTimeZone("America/New_York"), new TimeZoneAdapter().unmarshal("America/New_York"));
    }

    public void testUnmarshalNull() throws Exception {
        assertNull(new TimeZoneAdapter().unmarshal(null));
    }
}
