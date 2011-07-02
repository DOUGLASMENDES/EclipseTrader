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

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.yahoo.internal.core.Util;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsHandler implements INewsHandler {

    private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

    public RSSNewsHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.yahoo.internal.news.INewsHandler#parseNewsPages(java.net.URL[], org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public HeadLine[] parseNewsPages(URL[] url, IProgressMonitor monitor) {
        List<HeadLine> list = new ArrayList<HeadLine>();

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        for (int i = 0; i < url.length && !monitor.isCanceled(); i++) {
            monitor.subTask(url[i].toString());

            try {
                Util.setupProxy(client, url[i].getHost());

                SyndFeed feed = fetcher.retrieveFeed(url[i], client);
                for (Iterator<?> iter = feed.getEntries().iterator(); iter.hasNext();) {
                    SyndEntry entry = (SyndEntry) iter.next();

                    String link = entry.getLink();
                    if (link.lastIndexOf('*') != -1) {
                        link = link.substring(link.lastIndexOf('*') + 1);
                    }
                    link = URLDecoder.decode(link, "UTF-8");

                    String source = null;

                    String title = entry.getTitle();
                    if (title.startsWith("[$$]")) {
                        title = title.substring(4, title.length());
                    }

                    if (title.endsWith(")")) {
                        int s = title.lastIndexOf('(');
                        if (s != -1) {
                            source = title.substring(s + 1, title.length() - 1);
                            if (source.startsWith("at ")) {
                                source = source.substring(3);
                            }
                            title = title.substring(0, s - 1).trim();
                        }
                    }

                    list.add(new HeadLine(entry.getPublishedDate(), source, title, null, link));
                }
            } catch (IllegalArgumentException e) {
                // Do nothing, could be an invalid URL
            } catch (Exception e) {
                e.printStackTrace();
            }

            monitor.worked(1);
        }

        return list.toArray(new HeadLine[list.size()]);
    }
}
