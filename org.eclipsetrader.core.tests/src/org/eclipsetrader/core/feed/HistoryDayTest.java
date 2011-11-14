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

package org.eclipsetrader.core.feed;

import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.tests.Helper;

public class HistoryDayTest extends TestCase {

    public void testCreateStoreObjects() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 3), 26.55, 26.6, 26.51, 26.52, 35083L),
        };

        HistoryDay history = new HistoryDay(null, TimeSpan.minutes(1));
        IStoreObject[] storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);
        assertEquals(0, storeObjects.length);
        history.setOHLC(bars);
        storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);
        assertEquals(2, storeObjects.length);
        assertTrue(storeObjects[0].getStoreProperties().getProperty(TimeSpan.minutes(1).toString()) != null);
        assertTrue(storeObjects[1].getStoreProperties().getProperty(TimeSpan.minutes(1).toString()) != null);
    }

    public void testFillStoreObjects() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 3), 26.55, 26.6, 26.51, 26.52, 35083L),
        };

        Security security = new Security("Test", null);
        HistoryDay history = new HistoryDay(security, TimeSpan.minutes(1));
        history.setOHLC(bars);

        IStoreObject[] storeObjects = (IStoreObject[]) history.getAdapter(IStoreObject[].class);

        assertSame(security, storeObjects[0].getStoreProperties().getProperty(IPropertyConstants.SECURITY));
        assertEquals(Helper.getTime(2008, Calendar.MAY, 22, 0, 0), storeObjects[0].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE));
        IOHLC[] propBars1 = (IOHLC[]) storeObjects[0].getStoreProperties().getProperty(TimeSpan.minutes(1).toString());
        assertEquals(1, propBars1.length);
        assertSame(bars[0], propBars1[0]);

        assertSame(security, storeObjects[1].getStoreProperties().getProperty(IPropertyConstants.SECURITY));
        assertEquals(Helper.getTime(2008, Calendar.MAY, 23, 0, 0), storeObjects[1].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE));
        IOHLC[] propBars2 = (IOHLC[]) storeObjects[1].getStoreProperties().getProperty(TimeSpan.minutes(1).toString());
        assertEquals(1, propBars2.length);
        assertSame(bars[1], propBars2[0]);
    }

    public void testGetLowestTimespanBars() throws Exception {
        IOHLC[] bars1min = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 4), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
        };
        IOHLC[] bars5min = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 8), 26.55, 26.6, 26.51, 26.52, 35083L),
        };

        StoreProperties properties = new StoreProperties();
        properties.setProperty(TimeSpan.minutes(1).toString(), bars1min);
        properties.setProperty(TimeSpan.minutes(5).toString(), bars5min);

        Security security = new Security("Test", null);
        HistoryDay history = new HistoryDay(security, TimeSpan.minutes(30));

        IOHLC[] bars = history.getLowestTimespanBars(properties);
        assertSame(bars, bars1min);
    }

    public void testNoLowestTimespanBars() throws Exception {
        IOHLC[] bars5min = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 8), 26.55, 26.6, 26.51, 26.52, 35083L),
        };

        StoreProperties properties = new StoreProperties();
        properties.setProperty(TimeSpan.minutes(5).toString(), bars5min);

        Security security = new Security("Test", null);
        HistoryDay history = new HistoryDay(security, TimeSpan.minutes(1));

        IOHLC[] bars = history.getLowestTimespanBars(properties);
        assertNull(bars);
    }
}
