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

package org.eclipsetrader.yahoo.internal.news;

import java.util.Date;

import junit.framework.TestCase;

public class DateTimeAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        DateTimeAdapter adapter = new DateTimeAdapter();
        Date date = new Date();
        assertEquals(DateTimeAdapter.dateFormat.format(date), adapter.marshal(date));
    }

    public void testMarshalNull() throws Exception {
        DateTimeAdapter adapter = new DateTimeAdapter();
        assertNull(adapter.marshal(null));
    }

    public void testUnmarshal() throws Exception {
        DateTimeAdapter adapter = new DateTimeAdapter();
        Date date = new Date(System.currentTimeMillis() / 1000 * 1000);
        assertEquals(date, adapter.unmarshal(DateTimeAdapter.dateFormat.format(date)));
    }

    public void testUnmarshalNull() throws Exception {
        DateTimeAdapter adapter = new DateTimeAdapter();
        assertNull(adapter.unmarshal(null));
    }
}
