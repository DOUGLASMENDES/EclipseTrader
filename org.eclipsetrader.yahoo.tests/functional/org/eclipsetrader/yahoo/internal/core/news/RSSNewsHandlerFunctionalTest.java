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

package org.eclipsetrader.yahoo.internal.core.news;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsHandlerFunctionalTest extends TestCase {

    @SuppressWarnings("rawtypes")
    public void testGetUSNews() throws Exception {
        HttpClient client = new HttpClient();
        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

        SyndFeed feed = fetcher.retrieveFeed(new URL("http://feeds.finance.yahoo.com/rss/2.0/category-stocks?region=US&lang=en-US"), client);
        List entries = feed.getEntries();
        assertFalse(entries.size() == 0);

        SyndEntry entry = (SyndEntry) entries.get(0);
        assertNotNull(entry.getTitle());
        assertNotNull(entry.getLink());
        assertNotNull(entry.getPublishedDate());
    }

    @SuppressWarnings("rawtypes")
    public void testGetItalianNews() throws Exception {
        HttpClient client = new HttpClient();
        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

        SyndFeed feed = fetcher.retrieveFeed(new URL("http://eu.feeds.finance.yahoo.com/rss/2.0/category-affari-legali?region=IT&lang=it-IT"), client);
        List entries = feed.getEntries();
        assertFalse(entries.size() == 0);

        SyndEntry entry = (SyndEntry) entries.get(0);
        assertNotNull(entry.getTitle());
        assertNotNull(entry.getLink());
        assertNotNull(entry.getPublishedDate());
    }

    @SuppressWarnings("rawtypes")
    public void testGetUSSecurityNews() throws Exception {
        HttpClient client = new HttpClient();
        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

        SyndFeed feed = fetcher.retrieveFeed(new URL("http://finance.yahoo.com/rss/headline?s=MSFT"), client);
        List entries = feed.getEntries();
        assertFalse(entries.size() == 0);

        SyndEntry entry = (SyndEntry) entries.get(0);
        assertNotNull(entry.getTitle());
        assertNotNull(entry.getLink());
        assertNotNull(entry.getPublishedDate());
    }

    @SuppressWarnings("rawtypes")
    public void testGetItalianSecurityNews() throws Exception {
        HttpClient client = new HttpClient();
        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

        SyndFeed feed = fetcher.retrieveFeed(new URL("http://finance.yahoo.com/rss/headline?s=TIT.MI"), client);
        List entries = feed.getEntries();
        assertFalse(entries.size() == 0);

        SyndEntry entry = (SyndEntry) entries.get(0);
        assertNotNull(entry.getTitle());
        assertNotNull(entry.getLink());
        assertNotNull(entry.getPublishedDate());
    }
}
