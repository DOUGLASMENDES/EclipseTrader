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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
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

public class RSSNewsProvider implements INewsFetcher {
	public static final String HEADLINES_FILE = "rss.xml"; //$NON-NLS-1$

	private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
	private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

	static private List<HeadLine> oldItems = new ArrayList<HeadLine>();

	public RSSNewsProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines()
	 */
	public IHeadLine[] fetchHeadLines() {
		List<IHeadLine> list = new ArrayList<IHeadLine>();
		FeedSource[] sources = new FeedSource[0];

		try {
			File file = Activator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
			if (file.exists()) {
				JAXBContext jaxbContext = JAXBContext.newInstance(FeedSource[].class);
		        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	            unmarshaller.setEventHandler(new ValidationEventHandler() {
	            	public boolean handleEvent(ValidationEvent event) {
	            		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
	            		Activator.getDefault().getLog().log(status);
	            		return true;
	            	}
	            });
		        JAXBElement<FeedSource[]> element = unmarshaller.unmarshal(new StreamSource(file), FeedSource[].class);
		        sources = element.getValue();
			}
		} catch(Exception e) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error reading RSS subscriptions", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
		}

		if (sources != null) {
			for (int i = 0; i < sources.length; i++) {
				try {
					HeadLine[] headLines = update(new URL(sources[i].getUrl()), sources[i].getName());
					for (HeadLine h : headLines) {
						int index = oldItems.indexOf(h);
						if (index != -1) {
							if (!list.contains(h))
								oldItems.get(index).setRecent(false);
						}
						else {
							oldItems.add(h);
							list.add(h);
						}
					}
				} catch(Exception e) {
		    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error updating headlines from " + sources[i].getUrl(), null); //$NON-NLS-1$
		    		Activator.getDefault().getLog().log(status);
				}
			}
		}

		return list.toArray(new IHeadLine[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.news.core.INewsFetcher#fetchHeadLines(org.eclipsetrader.core.instruments.ISecurity[])
	 */
	public IHeadLine[] fetchHeadLines(ISecurity[] securities) {
		return null;
	}

	private HeadLine[] update(URL feedUrl, String source) {
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
		return headLines.toArray(new HeadLine[headLines.size()]);
	}
}
