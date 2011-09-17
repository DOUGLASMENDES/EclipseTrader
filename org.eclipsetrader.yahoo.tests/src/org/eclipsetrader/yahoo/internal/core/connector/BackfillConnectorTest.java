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

package org.eclipsetrader.yahoo.internal.core.connector;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class BackfillConnectorTest extends TestCase {

    public void testParseResponseLine() throws Exception {
        BackfillConnector connector = new BackfillConnector();

        Calendar date = Calendar.getInstance();
        date.set(2011, Calendar.JANUARY, 3, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);

        OHLC ohlc = connector.parseResponseLine("20110103,1.5840,1.6190,1.5660,1.5750,204432704");

        assertEquals(date.getTime(), ohlc.getDate());
        assertEquals(1.5750, ohlc.getOpen());
        assertEquals(1.6190, ohlc.getHigh());
        assertEquals(1.5660, ohlc.getLow());
        assertEquals(1.5840, ohlc.getClose());
        assertEquals(new Long(204432704), ohlc.getVolume());
    }

    public void testParseAlternateDateFormat() throws Exception {
        BackfillConnector connector = new BackfillConnector();

        Calendar date = Calendar.getInstance();
        date.set(2011, Calendar.JANUARY, 3, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);

        OHLC ohlc = connector.parseResponseLine("03-Jan-2011,1.5840,1.6190,1.5660,1.5750,204432704");

        assertEquals(date.getTime(), ohlc.getDate());
        assertEquals(1.5750, ohlc.getOpen());
        assertEquals(1.6190, ohlc.getHigh());
        assertEquals(1.5660, ohlc.getLow());
        assertEquals(1.5840, ohlc.getClose());
        assertEquals(new Long(204432704), ohlc.getVolume());
    }

    public void testParse1DayResponseLine() throws Exception {
        BackfillConnector connector = new BackfillConnector();

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        date.set(2009, Calendar.JUNE, 19, 9, 30, 0);
        date.set(Calendar.MILLISECOND, 0);

        OHLC ohlc = connector.parse1DayResponseLine("1245418200,29.0600,29.0700,28.9400,28.9900,1598500");

        assertEquals(date.getTime(), ohlc.getDate());
        assertEquals(28.99, ohlc.getOpen());
        assertEquals(29.07, ohlc.getHigh());
        assertEquals(28.94, ohlc.getLow());
        assertEquals(29.06, ohlc.getClose());
        assertEquals(new Long(1598500), ohlc.getVolume());
    }

    public void testReadBackfillStream() throws Exception {
        StringBuilder text = new StringBuilder();
        text.append("values:Date,close,high,low,open,volume\r\n");
        text.append("20110103,1.5840,1.6190,1.5660,1.5750,204432704\r\n");
        text.append("20110104,1.5900,1.6060,1.5690,1.5830,254641200\r\n");
        StringReader sr = new StringReader(text.toString());

        List<OHLC> list = new ArrayList<OHLC>();

        BackfillConnector connector = new BackfillConnector();
        connector.readBackfillStream(list, new BufferedReader(sr));

        assertEquals(2, list.size());
    }

    public void testReadEmptyBackfillStream() throws Exception {
        StringBuilder text = new StringBuilder();
        text.append("values:Date,close,high,low,open,volume\r\n");
        StringReader sr = new StringReader(text.toString());

        List<OHLC> list = new ArrayList<OHLC>();

        BackfillConnector connector = new BackfillConnector();
        connector.readBackfillStream(list, new BufferedReader(sr));

        assertEquals(0, list.size());
    }

    public void testRead1DayBackfillStream() throws Exception {
        StringBuilder text = new StringBuilder();
        text.append("uri:/instrument/1.0/adbe/chartdata;type=quote;range=1d/csv/\r\n");
        text.append("ticker:adbe\r\n");
        text.append("unit:MIN\r\n");
        text.append("timezone:EDT\r\n");
        text.append("gmtoffset:-14400\r\n");
        text.append("previous_close:28.7200\r\n");
        text.append("Timestamp:1245418200,1245441600\r\n");
        text.append("labels:1245420000,1245423600,1245427200,1245430800,1245434400,1245438000,1245441600\r\n");
        text.append("values:Timestamp,close,high,low,open,volume\r\n");
        text.append("close:29.0300,29.4200\r\n");
        text.append("high:29.0700,29.4400\r\n");
        text.append("low:28.9400,29.4000\r\n");
        text.append("open:28.9900,29.4220\r\n");
        text.append("volume:400,1598500\r\n");
        text.append("1245418200,29.0600,29.0700,28.9400,28.9900,1598500\r\n");
        text.append("1245418260,29.0700,29.0890,29.0300,29.0700,18100\r\n");
        text.append("1245418320,29.2200,29.2300,29.0700,29.0800,24100\r\n");
        StringReader sr = new StringReader(text.toString());

        List<OHLC> list = new ArrayList<OHLC>();

        BackfillConnector connector = new BackfillConnector();
        connector.read1DayBackfillStream(list, new BufferedReader(sr));

        assertEquals(3, list.size());
    }

    public void testCanBackfillDailyHistory() throws Exception {
        BackfillConnector connector = new BackfillConnector();
        assertTrue(connector.canBackfill(new FeedIdentifier("MSFT", new FeedProperties()), TimeSpan.days(1)));
    }

    public void testCanBackfill1MinuteHistory() throws Exception {
        BackfillConnector connector = new BackfillConnector();
        assertTrue(connector.canBackfill(new FeedIdentifier("MSFT", new FeedProperties()), TimeSpan.minutes(1)));
    }

    public void testCanBackfill5MinuteHistory() throws Exception {
        BackfillConnector connector = new BackfillConnector();
        assertTrue(connector.canBackfill(new FeedIdentifier("MSFT", new FeedProperties()), TimeSpan.minutes(5)));
    }

    public void testCanBackfillOtherTimeSpans() throws Exception {
        BackfillConnector connector = new BackfillConnector();
        assertFalse(connector.canBackfill(new FeedIdentifier("MSFT", new FeedProperties()), TimeSpan.minutes(2)));
    }

    public void testCantBackfillMinuteIndexes() throws Exception {
        BackfillConnector connector = new BackfillConnector();
        assertFalse(connector.canBackfill(new FeedIdentifier("^IXIC", new FeedProperties()), TimeSpan.minutes(1)));
        assertFalse(connector.canBackfill(new FeedIdentifier("^IXIC", new FeedProperties()), TimeSpan.minutes(5)));
    }
}
