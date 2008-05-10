/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal.connectors;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsFetcher;
import org.eclipsetrader.news.internal.Activator;
import org.eclipsetrader.news.internal.repository.HeadLine;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class YahooNewsFetcher implements INewsFetcher {
	public static final String HOST = "finance.yahoo.com";
	static private List<HeadLine> oldItems = new ArrayList<HeadLine>();
	private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
	private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

	public YahooNewsFetcher() {
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines()
     */
    public IHeadLine[] fetchHeadLines() {
	    return new IHeadLine[0];
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines(org.eclipsetrader.core.instruments.ISecurity[])
     */
    public IHeadLine[] fetchHeadLines(ISecurity[] securities) {
		Map<String, SyndEntry> titles = new HashMap<String, SyndEntry>();
		Map<SyndEntry, Set<ISecurity>> entries = new HashMap<SyndEntry, Set<ISecurity>>();
		Set<HeadLine> headLines = new HashSet<HeadLine>();

		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			if (Activator.getDefault() != null) {
				BundleContext context = Activator.getDefault().getBundle().getBundleContext();
				ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
				if (reference != null) {
					IProxyService proxy = (IProxyService) context.getService(reference);
					IProxyData data = proxy.getProxyDataForHost(HOST, IProxyData.HTTP_PROXY_TYPE);
					if (data != null) {
						if (data.getHost() != null)
							client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
						if (data.isRequiresAuthentication())
							client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
					}
					context.ungetService(reference);
				}
			}

			for (ISecurity security : securities) {
				IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
				String feedUrl = "http://" + HOST + "/rss/headline?s=" + identifier.getSymbol();

				SyndFeed feed = fetcher.retrieveFeed(new URL(feedUrl), client);
				for (Iterator<?> iter = feed.getEntries().iterator(); iter.hasNext();) {
					SyndEntry entry = (SyndEntry) iter.next();

					if (titles.containsKey(entry.getTitle()))
						entry = titles.get(entry.getTitle());
					else
						titles.put(entry.getTitle(), entry);

					Set<ISecurity> set = entries.get(entry);
					if (set == null) {
						set = new HashSet<ISecurity>();
						entries.put(entry, set);
					}
					set.add(security);
				}
			}

			for (SyndEntry entry : entries.keySet()) {
				Set<ISecurity> set = entries.get(entry);

				String url = entry.getLink();
				if (url.indexOf('*') != -1)
					url = url.substring(url.indexOf('*') + 1);

				String source = null;
				String title = entry.getTitle();
				if (title.endsWith(")")) {
					int s = title.lastIndexOf('(');
					if (s != -1) {
						source = title.substring(s + 1, title.length() - 1);
						if (source.startsWith("at "))
							source = source.substring(3);
						title = title.substring(0, s - 1).trim();
					}
				}

				HeadLine headLine = new HeadLine(entry.getPublishedDate(), source, title, set.toArray(new ISecurity[set.size()]), url);
				if (!oldItems.contains(headLine)) {
					oldItems.add(headLine);
					headLines.add(headLine);
				}
			}
		} catch (Exception e) {
	        e.printStackTrace();
		}
	    return headLines.toArray(new IHeadLine[headLines.size()]);
    }

	public static void main(String[] args) {
        YahooNewsFetcher c = new YahooNewsFetcher();
        IHeadLine[] headLines = c.fetchHeadLines(new ISecurity[] {
        		new Security("Microsoft", new FeedIdentifier("MSFT", new FeedProperties())),
        		new Security("Google", new FeedIdentifier("GOOG", new FeedProperties())),
        		new Security("Google", new FeedIdentifier("AAPL", new FeedProperties())),
        	});
        for (IHeadLine h : headLines) {
    		System.out.println(NLS.bind("{0} - {3} / {1} - {2}", new Object[] { h.getDate(), h.getText(), h.getLink(), h.getSource() }));
        	if (h.getMembers() != null && h.getMembers().length != 0) {
        		ISecurity[] members = h.getMembers();
        		for (int i = 0; i < members.length; i++)
        			System.out.print((i != 0 ? " / " : "   ") + members[i].getName());
        		System.out.println();
        	}
        }
    }
}
