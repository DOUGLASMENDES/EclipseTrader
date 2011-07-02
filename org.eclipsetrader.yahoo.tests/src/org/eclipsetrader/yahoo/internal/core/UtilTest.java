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

package org.eclipsetrader.yahoo.internal.core;

import java.net.URL;
import java.util.Currency;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.instruments.Stock;

public class UtilTest extends TestCase {

    public void testGetYahooSymbol() throws Exception {
        FeedProperties properties = new FeedProperties();
        properties.setProperty("org.eclipsetrader.yahoo.symbol", "F.MI");
        FeedIdentifier identifier = new FeedIdentifier("F", properties);
        assertEquals("F.MI", Util.getSymbol(identifier));
    }

    public void testGetDefaultSymbolIfYahooIsMissing() throws Exception {
        FeedProperties properties = new FeedProperties();
        FeedIdentifier identifier = new FeedIdentifier("F.MI", properties);
        assertEquals("F.MI", Util.getSymbol(identifier));
    }

    public void testGet1YearBackfillMethod() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("MSFT", new FeedProperties());
        String expected = "http://chartapi.finance.yahoo.com/instrument/1.0/msft/chartdata;type=quote;ys=2011;yz=1/csv/";

        HttpMethod method = Util.get1YearHistoryFeedMethod(identifier, 2011);
        assertEquals(expected, method.getURI().toString());
    }

    public void testGet1DaysBackfillMethod() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("MSFT", new FeedProperties());
        String expected = "http://chartapi.finance.yahoo.com/instrument/1.0/msft/chartdata;type=quote;range=1d/csv/";

        HttpMethod method = Util.get1DayHistoryFeedMethod(identifier);
        assertEquals(expected, method.getURI().toString());
    }

    public void testGet5DaysBackfillMethod() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("MSFT", new FeedProperties());
        String expected = "http://chartapi.finance.yahoo.com/instrument/1.0/msft/chartdata;type=quote;range=5d/csv/";

        HttpMethod method = Util.get5DayHistoryFeedMethod(identifier);
        assertEquals(expected, method.getURI().toString());
    }

    public void testGetBackfillMethodForIndex() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("^IXIC", new FeedProperties());
        String expected = "http://chartapi.finance.yahoo.com/instrument/1.0/%5Eixic/chartdata;type=quote;ys=2011;yz=1/csv/";

        HttpMethod method = Util.get1YearHistoryFeedMethod(identifier, 2011);
        assertEquals(expected, method.getURI().toString());
    }

    public void testGetRSSNewsFeed() throws Exception {
        Stock stock = new Stock("Stock", new FeedIdentifier("MSFT", new FeedProperties()), Currency.getInstance("USD"));
        String expected = "http://finance.yahoo.com/rss/headline?s=MSFT";

        URL url = Util.getRSSNewsFeedForSecurity(stock);
        assertEquals(expected, url.toString());
    }

    public void testGetRSSNewsFeedForIndex() throws Exception {
        Stock stock = new Stock("Stock", new FeedIdentifier("^IXIC", new FeedProperties()), Currency.getInstance("USD"));
        String expected = "http://finance.yahoo.com/rss/headline?s=%5EIXIC";

        URL url = Util.getRSSNewsFeedForSecurity(stock);
        assertEquals(expected, url.toString());
    }
}
