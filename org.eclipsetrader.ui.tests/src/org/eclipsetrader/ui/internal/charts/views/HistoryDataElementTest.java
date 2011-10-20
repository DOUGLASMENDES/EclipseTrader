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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.OHLC;

public class HistoryDataElementTest extends TestCase {

    public void testIsEmpty() throws Exception {
        HistoryDataElement element = new HistoryDataElement();

        assertTrue(element.isEmpty());

        element.setClose(1.0);

        assertFalse(element.isEmpty());
    }

    public void testIsValid() throws Exception {
        HistoryDataElement element = new HistoryDataElement();

        assertFalse(element.isValid());

        element.setDate(new Date());
        assertFalse(element.isValid());

        element.setOpen(1.0);
        assertFalse(element.isValid());

        element.setHigh(1.0);
        assertFalse(element.isValid());

        element.setLow(1.0);
        assertFalse(element.isValid());

        element.setClose(1.0);
        assertFalse(element.isValid());

        element.setVolume(1L);
        assertTrue(element.isValid());
    }

    public void testEquals() throws Exception {
        HistoryDataElement element = new HistoryDataElement(new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L));

        HistoryDataElement otherElement = new HistoryDataElement();
        assertFalse(element.equals(otherElement));

        otherElement.setDate(getTime(2007, Calendar.NOVEMBER, 13));
        assertFalse(element.equals(otherElement));

        otherElement.setOpen(200.0);
        assertFalse(element.equals(otherElement));

        otherElement.setHigh(210.0);
        assertFalse(element.equals(otherElement));

        otherElement.setLow(190.0);
        assertFalse(element.equals(otherElement));

        otherElement.setClose(195.0);
        assertFalse(element.equals(otherElement));

        otherElement.setVolume(100000L);
        assertTrue(element.equals(otherElement));
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
