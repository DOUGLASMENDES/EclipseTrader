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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class MarketDayTest extends TestCase {

    public void testGetOpenTime() throws Exception {
        MarketDay day = new MarketDay(getTime(9, 30), null, null);
        assertEquals(getTime(9, 30), day.getOpenTime());
    }

    public void testGetCloseTime() throws Exception {
        MarketDay day = new MarketDay(null, getTime(16, 0), null);
        assertEquals(getTime(16, 0), day.getCloseTime());
    }

    public void testGetMessage() throws Exception {
        MarketDay day = new MarketDay(null, null, "Message");
        assertEquals("Message", day.getMessage());
    }

    public void testIsOpenAtOpenTime() throws Exception {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        assertTrue(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 9, 30)));
    }

    public void testIsOpenAtClosTime() throws Exception {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 16, 0)));
    }

    public void testIsOpenEarly() throws Exception {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 6, 0)));
    }

    public void testIsOpenLate() throws Exception {
        MarketDay day = new MarketDay(getTime(2007, Calendar.NOVEMBER, 11, 9, 30), getTime(2007, Calendar.NOVEMBER, 11, 16, 0), null);
        assertFalse(day.isOpen(getTime(2007, Calendar.NOVEMBER, 11, 17, 0)));
    }

    public void testIsOpenWithNullTime() throws Exception {
        MarketDay day = new MarketDay(null, null, null);
        assertFalse(day.isOpen(getTime(12, 30)));
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
