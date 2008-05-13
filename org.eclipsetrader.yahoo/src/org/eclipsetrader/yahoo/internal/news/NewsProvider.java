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

package org.eclipsetrader.yahoo.internal.news;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceRunnable;
import org.eclipsetrader.news.internal.repository.HeadLine;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class NewsProvider implements INewsProvider {
	public static final String HOST = "finance.yahoo.com";

	private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
	private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

	private INewsService newsService;
	private IRepositoryService repositoryService;
	private boolean started;

	static private List<HeadLine> oldItems = new ArrayList<HeadLine>();

	private JobChangeAdapter jobChangeListener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
    		IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();
        	int interval = store.getInt(YahooActivator.PREFS_NEWS_UPDATE_INTERVAL);
        	job.schedule(interval * 60 * 1000);
        }
	};

	private Job job = new Job("Yahoo! News") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
	        return jobRunner(monitor);
        }
	};

	public NewsProvider() {
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#start()
     */
    public void start() {
    	if (!started) {
    		job.schedule(5 * 1000);
    		job.addJobChangeListener(jobChangeListener);
    		started = true;
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#stop()
     */
    public void stop() {
    	if (started) {
        	job.removeJobChangeListener(jobChangeListener);
        	job.cancel();
        	started = false;
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#refresh()
     */
    public void refresh() {
    	job.removeJobChangeListener(jobChangeListener);
    	job.cancel();

    	if (started)
    		job.addJobChangeListener(jobChangeListener);

    	job.schedule(0);
    }

    protected IStatus jobRunner(IProgressMonitor monitor) {
		IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();

		URL[] urls = getPages();
		ISecurity[] securities = getRepositoryService().getSecurities();

		int total = urls.length;
		if (store.getBoolean(YahooActivator.PREFS_UPDATE_SECURITIES_NEWS))
			total += securities.length;

		monitor.beginTask("Fetching Yahoo! News", total);
		try {
			resetRecentFlag();
			if (monitor.isCanceled())
				return Status.OK_STATUS;

			fetchHeadLines(urls, monitor);
			if (monitor.isCanceled())
				return Status.OK_STATUS;

			if (store.getBoolean(YahooActivator.PREFS_UPDATE_SECURITIES_NEWS)) {
				fetchHeadLines(securities, monitor);
				if (monitor.isCanceled())
					return Status.OK_STATUS;
			}
		} catch(Exception e) {
			// TODO Log
		} finally {
			monitor.done();
		}

		return Status.OK_STATUS;
    }

    protected void resetRecentFlag() {
    	final List<HeadLine> updated = new ArrayList<HeadLine>();

    	for (HeadLine headLine : oldItems) {
			if (headLine.isRecent()) {
				headLine.setRecent(false);
				updated.add(headLine);
			}
		}

		final INewsService service = getNewsService();
		service.runInService(new INewsServiceRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
            	service.updateHeadLines(updated.toArray(new IHeadLine[updated.size()]));
                return Status.OK_STATUS;
            }
		}, null);
    }

    protected void fetchHeadLines(URL[] urls, IProgressMonitor monitor) {
    	final List<HeadLine> added = new ArrayList<HeadLine>();

		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			for (int i = 0; i < urls.length && !monitor.isCanceled(); i++) {
				added.clear();

				monitor.subTask(urls[i].toString());

				if (YahooActivator.getDefault() != null) {
					BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
					ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
					if (reference != null) {
						IProxyService proxy = (IProxyService) context.getService(reference);
						IProxyData data = proxy.getProxyDataForHost(urls[i].getHost(), IProxyData.HTTP_PROXY_TYPE);
						if (data != null) {
							if (data.getHost() != null)
								client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
							if (data.isRequiresAuthentication())
								client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
						}
						context.ungetService(reference);
					}
				}

				try {
					SyndFeed feed = fetcher.retrieveFeed(urls[i], client);
					for (Iterator<?> iter = feed.getEntries().iterator(); iter.hasNext();) {
						SyndEntry entry = (SyndEntry) iter.next();

						String url = entry.getLink();
						if (url.indexOf('*') != -1)
							url = url.substring(url.indexOf('*') + 1);

						String source = null;

						String title = entry.getTitle();
						if (title.startsWith("[$$]"))
							title = title.substring(4, title.length());

						if (title.endsWith(")")) {
							int s = title.lastIndexOf('(');
							if (s != -1) {
								source = title.substring(s + 1, title.length() - 1);
								if (source.startsWith("at "))
									source = source.substring(3);
								title = title.substring(0, s - 1).trim();
							}
						}

						HeadLine headLine = new HeadLine(entry.getPublishedDate(), source, title, null, url);
						if (!oldItems.contains(headLine)) {
							headLine.setRecent(true);
							oldItems.add(headLine);
							added.add(headLine);
						}
					}
				} catch (IllegalArgumentException e) {
					// Do nothing, could be an invalid URL
				} catch (Exception e) {
			        e.printStackTrace();
				}

				final INewsService service = getNewsService();
				service.runInService(new INewsServiceRunnable() {
                    public IStatus run(IProgressMonitor monitor) throws Exception {
                    	service.addHeadLines(added.toArray(new IHeadLine[added.size()]));
                        return Status.OK_STATUS;
                    }
				}, null);

				monitor.worked(1);
			}
		} catch (Exception e) {
	        e.printStackTrace();
		}
    }

    protected void fetchHeadLines(ISecurity[] securities, IProgressMonitor monitor) {
    	final List<HeadLine> added = new ArrayList<HeadLine>();
    	final List<HeadLine> updated = new ArrayList<HeadLine>();

		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			if (YahooActivator.getDefault() != null) {
				BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
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

			for (int i = 0; i < securities.length && !monitor.isCanceled(); i++) {
				IFeedIdentifier identifier = (IFeedIdentifier) securities[i].getAdapter(IFeedIdentifier.class);
				if (identifier == null)
					continue;

				String symbol = identifier.getSymbol();

				IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
				if (properties != null) {
					if (properties.getProperty("org.eclipsetrader.yahoo.symbol") != null)
						symbol = properties.getProperty("org.eclipsetrader.yahoo.symbol");
				}

				String feedUrl = "http://" + HOST + "/rss/headline?s=" + symbol;
				monitor.subTask(feedUrl);

				added.clear();
				updated.clear();

				try {
					SyndFeed feed = fetcher.retrieveFeed(new URL(feedUrl), client);
					for (Iterator<?> iter = feed.getEntries().iterator(); iter.hasNext();) {
						SyndEntry entry = (SyndEntry) iter.next();

						String url = entry.getLink();
						if (url.indexOf('*') != -1)
							url = url.substring(url.indexOf('*') + 1);

						String source = null;

						String title = entry.getTitle();
						if (title.startsWith("[$$]"))
							title = title.substring(4, title.length());

						if (title.endsWith(")")) {
							int s = title.lastIndexOf('(');
							if (s != -1) {
								source = title.substring(s + 1, title.length() - 1);
								if (source.startsWith("at "))
									source = source.substring(3);
								title = title.substring(0, s - 1).trim();
							}
						}

						HeadLine headLine = new HeadLine(entry.getPublishedDate(), source, title, new ISecurity[] { securities[i] }, url);
						int index = oldItems.indexOf(headLine);
						if (index != -1) {
							headLine = oldItems.get(index);
							if (!headLine.contains(securities[i])) {
								headLine.addMember(securities[i]);
								updated.add(headLine);
							}
						}
						else {
							headLine.setRecent(true);
							oldItems.add(headLine);
							added.add(headLine);
						}
					}
				} catch (IllegalArgumentException e) {
					// Do nothing, could be an invalid URL
				} catch (Exception e) {
			        e.printStackTrace();
				}

				final INewsService service = getNewsService();
				service.runInService(new INewsServiceRunnable() {
                    public IStatus run(IProgressMonitor monitor) throws Exception {
                    	service.addHeadLines(added.toArray(new IHeadLine[added.size()]));
                    	service.updateHeadLines(updated.toArray(new IHeadLine[updated.size()]));
                        return Status.OK_STATUS;
                    }
				}, null);

				monitor.worked(1);
			}
		} catch (Exception e) {
	        e.printStackTrace();
		}
    }

	protected URL[] getPages() {
		List<URL> list = new ArrayList<URL>();
		IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Category[].class);
	        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

	        InputStream inputStream = FileLocator.openStream(YahooActivator.getDefault().getBundle(), new Path("data/news_feeds.xml"), false);
	        JAXBElement<Category[]> element = unmarshaller.unmarshal(new StreamSource(inputStream), Category[].class);

	        for (Category category : element.getValue()) {
	        	for (Page page : category.getPages()) {
	        		if (store.getBoolean(YahooActivator.PREFS_SUBSCRIBE_PREFIX + page.getId()))
	        			list.add(new URL(page.getUrl()));
	        	}
	        }

		} catch(Exception e) {
    		Status status = new Status(Status.WARNING, YahooActivator.PLUGIN_ID, 0, "Error reading feed sources", e); //$NON-NLS-1$
    		YahooActivator.getDefault().getLog().log(status);
		}

		return list.toArray(new URL[list.size()]);
	}

	protected INewsService getNewsService() {
		if (newsService == null) {
			BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
			if (serviceReference != null) {
				newsService = (INewsService) context.getService(serviceReference);
				context.ungetService(serviceReference);
			}
		}
		return newsService;
	}

	protected IRepositoryService getRepositoryService() {
		if (repositoryService == null) {
			BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				repositoryService = (IRepositoryService) context.getService(serviceReference);
				context.ungetService(serviceReference);
			}
		}
		return repositoryService;
	}
}
