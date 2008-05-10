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

package org.eclipsetrader.news.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.internal.repository.HeadLine;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsConnector extends Job {
	static private List<HeadLine> oldItems = new ArrayList<HeadLine>();
	private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
	private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);
	private Set<HeadLine> headLines = new HashSet<HeadLine>();

	public RSSNewsConnector() {
		super("RSS News Connector");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Fetch RSS News", IProgressMonitor.UNKNOWN);
		try {

		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	private void updateHeadLines(URL feedUrl, String source) {
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

				Date entryDate = entry.getPublishedDate();
				if (entry.getUpdatedDate() != null)
					entryDate = entry.getUpdatedDate();
				if (entryDate != null) {
					Calendar date = Calendar.getInstance();
					date.setTime(entryDate);
					date.set(Calendar.SECOND, 0);
					entryDate = date.getTime();
				}

				String url = entry.getLink();
				if (url.indexOf('*') != -1)
					url = url.substring(url.indexOf('*') + 1);

				HeadLine headLine = new HeadLine(entryDate, source, entry.getTitle(), new ISecurity[0], url);
				if (!oldItems.contains(headLine)) {
					oldItems.add(headLine);
					headLines.add(headLine);
				}
			}
		} catch (Exception e) {
	        e.printStackTrace();
		}
	}

	protected HeadLine[] getHeadLines() {
    	return headLines.toArray(new HeadLine[headLines.size()]);
    }

	public static void main(String[] args) {
		try {
	        RSSNewsConnector c = new RSSNewsConnector();
	        c.updateHeadLines(new URL("http://it.finance.yahoo.com/rss/headline?s=TIT.MI"), "Yahoo! Finance");
	        for (HeadLine h : c.getHeadLines())
	        	System.out.println(NLS.bind("{0} - {1} - {2}", new Object[] { h.getDate(), h.getText(), h.getLink() }));
        } catch (MalformedURLException e) {
	        e.printStackTrace();
        }
    }
}
