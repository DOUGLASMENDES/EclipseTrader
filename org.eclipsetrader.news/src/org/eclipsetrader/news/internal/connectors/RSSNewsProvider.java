/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal.connectors;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Platform;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsFetcher;
import org.eclipsetrader.news.internal.Activator;
import org.eclipsetrader.news.internal.repository.HeadLine;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsProvider implements INewsFetcher {
	private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
	private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

	static private List<HeadLine> oldItems = new ArrayList<HeadLine>();

	public RSSNewsProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines()
	 */
	public IHeadLine[] fetchHeadLines() {
		File file = new File(Platform.getLocation().toFile(), "rss.xml"); //$NON-NLS-1$
		if (file.exists() == true) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(file);

				Node firstNode = document.getFirstChild();

				NodeList childNodes = firstNode.getChildNodes();

				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					String nodeName = item.getNodeName();
					if (nodeName.equalsIgnoreCase("source")) //$NON-NLS-1$
						update(new URL(item.getFirstChild().getNodeValue()), item.getAttributes().getNamedItem("name").getNodeValue()); //$NON-NLS-1$
				}
			} catch (Exception e) {
				// TODO Log
				e.printStackTrace();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines(org.eclipsetrader.core.instruments.ISecurity[])
	 */
	public IHeadLine[] fetchHeadLines(ISecurity[] securities) {
		return null;
	}

	private IHeadLine[] update(URL feedUrl, String source) {
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
					IProxyData data = proxy.getProxyDataForHost(feedUrl.getHost(), IProxyData.HTTP_PROXY_TYPE);
					if (data != null) {
						if (data.getHost() != null)
							client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
						if (data.isRequiresAuthentication())
							client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
					}
					context.ungetService(reference);
				}
			}

			SyndFeed feed = fetcher.retrieveFeed(feedUrl, client);
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
			}

			for (SyndEntry entry : entries.keySet()) {
				Set<ISecurity> set = entries.get(entry);

				String url = entry.getLink();
				if (url.indexOf('*') != -1)
					url = url.substring(url.indexOf('*') + 1);

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
			// TODO Log
			e.printStackTrace();
		}
		return headLines.toArray(new IHeadLine[headLines.size()]);
	}

	boolean isDuplicated(IHeadLine news) {
		IHeadLine[] items = oldItems.toArray(new IHeadLine[0]);

		for (int i = 0; i < items.length; i++) {
			if (news.getText().equals(items[i].getText()) && news.getLink().equals(items[i].getLink()))
				return true;
		}

		return false;
	}
}
