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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class WeekdaysAdapterTest extends TestCase {

    public void testMarshalWorkdays() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        assertEquals("-MTWTF-", adapter.marshal(new HashSet<Integer>(Arrays.asList(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        }))));
    }

    public void testUnmarshalWorkdays() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Set<Integer> set = adapter.unmarshal("-MTWTF-");
        assertFalse(set.contains(Calendar.SUNDAY));
        assertTrue(set.contains(Calendar.MONDAY));
        assertTrue(set.contains(Calendar.TUESDAY));
        assertTrue(set.contains(Calendar.WEDNESDAY));
        assertTrue(set.contains(Calendar.THURSDAY));
        assertTrue(set.contains(Calendar.FRIDAY));
        assertFalse(set.contains(Calendar.SATURDAY));
    }

    public void testMarshalFull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        assertEquals("SMTWTFS", adapter.marshal(new HashSet<Integer>(Arrays.asList(new Integer[] {
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
        }))));
    }

    public void testUnmarshalFull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        Set<Integer> set = adapter.unmarshal("SMTWTFS");
        assertTrue(set.contains(Calendar.SUNDAY));
        assertTrue(set.contains(Calendar.MONDAY));
        assertTrue(set.contains(Calendar.TUESDAY));
        assertTrue(set.contains(Calendar.WEDNESDAY));
        assertTrue(set.contains(Calendar.THURSDAY));
        assertTrue(set.contains(Calendar.FRIDAY));
        assertTrue(set.contains(Calendar.SATURDAY));
    }

    public void testMarshalNull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        assertNull(adapter.marshal(null));
    }

    public void testUnmarshalNull() throws Exception {
        WeekdaysAdapter adapter = new WeekdaysAdapter();
        assertNull(adapter.unmarshal(null));
    }
}
