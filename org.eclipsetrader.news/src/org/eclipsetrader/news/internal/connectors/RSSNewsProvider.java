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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceRunnable;
import org.eclipsetrader.news.internal.Activator;
import org.eclipsetrader.news.internal.repository.HeadLine;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsProvider implements INewsProvider, IExecutableExtension {

    public static final String HEADLINES_FILE = "rss.xml"; //$NON-NLS-1$

    private String id;
    private String name;

    private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);

    private INewsService newsService;
    private boolean started;

    static private List<HeadLine> oldItems = new ArrayList<HeadLine>();

    private JobChangeAdapter jobChangeListener = new JobChangeAdapter() {

        @Override
        public void done(IJobChangeEvent event) {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            int interval = store.getInt(Activator.PREFS_UPDATE_INTERVAL);
            job.schedule(interval * 60 * 1000);
        }
    };

    private Job job = new Job("RSS Subscriptions") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            return jobRunner(monitor);
        }
    };

    public RSSNewsProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#getHeadLines()
     */
    @Override
    public IHeadLine[] getHeadLines() {
        return new IHeadLine[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#start()
     */
    @Override
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
    @Override
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
    @Override
    public void refresh() {
        job.removeJobChangeListener(jobChangeListener);
        job.cancel();

        if (started) {
            job.addJobChangeListener(jobChangeListener);
        }

        job.schedule(0);
    }

    protected IStatus jobRunner(IProgressMonitor monitor) {
        final List<IHeadLine> list = new ArrayList<IHeadLine>();

        FeedSource[] sources = getActiveSubscriptions();
        monitor.beginTask("Updating RSS Subscriptions", sources.length);

        try {
            for (int i = 0; i < sources.length; i++) {
                try {
                    HeadLine[] headLines = update(new URL(sources[i].getUrl()), sources[i].getName());
                    for (HeadLine headLine : headLines) {
                        if (!oldItems.contains(headLine)) {
                            headLine.setRecent(true);
                            oldItems.add(headLine);
                            list.add(headLine);
                        }
                    }
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error updating headlines from " + sources[i].getUrl(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                } finally {
                    monitor.worked(1);
                }
            }

            final INewsService service = getNewsService();
            service.runInService(new INewsServiceRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    service.addHeadLines(list.toArray(new IHeadLine[list.size()]));
                    return Status.OK_STATUS;
                }
            }, null);
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

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.updateHeadLines(updated.toArray(new IHeadLine[updated.size()]));
                return Status.OK_STATUS;
            }
        }, null);
    }

    protected FeedSource[] getActiveSubscriptions() {
        List<FeedSource> list = new ArrayList<FeedSource>();

        try {
            File file = Activator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(FeedSource[].class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        Activator.getDefault().getLog().log(status);
                        return true;
                    }
                });
                JAXBElement<FeedSource[]> element = unmarshaller.unmarshal(new StreamSource(file), FeedSource[].class);
                for (FeedSource source : element.getValue()) {
                    if (source.isEnabled()) {
                        list.add(source);
                    }
                }
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error reading RSS subscriptions", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }

        return list.toArray(new FeedSource[list.size()]);
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
                        if (data.getHost() != null) {
                            client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
                        }
                        if (data.isRequiresAuthentication()) {
                            client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
                        }
                    }
                    context.ungetService(reference);
                }
            }

            SyndFeed feed = fetcher.retrieveFeed(feedUrl, client);
            for (Iterator<?> iter = feed.getEntries().iterator(); iter.hasNext();) {
                SyndEntry entry = (SyndEntry) iter.next();

                if (titles.containsKey(entry.getTitle())) {
                    entry = titles.get(entry.getTitle());
                }
                else {
                    titles.put(entry.getTitle(), entry);
                }

                Set<ISecurity> set = entries.get(entry);
                if (set == null) {
                    set = new HashSet<ISecurity>();
                    entries.put(entry, set);
                }
            }

            for (SyndEntry entry : entries.keySet()) {
                Set<ISecurity> set = entries.get(entry);

                String url = entry.getLink();
                if (url.indexOf('*') != -1) {
                    url = url.substring(url.indexOf('*') + 1);
                }

                String title = entry.getTitle();
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

                HeadLine headLine = new HeadLine(entry.getPublishedDate(), source, title, set.toArray(new ISecurity[set.size()]), url);
                headLines.add(headLine);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error reading RSS subscription " + feedUrl.toString(), e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
        return headLines.toArray(new HeadLine[headLines.size()]);
    }

    protected INewsService getNewsService() {
        if (newsService == null) {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
            if (serviceReference != null) {
                newsService = (INewsService) context.getService(serviceReference);
                context.ungetService(serviceReference);
            }
        }
        return newsService;
    }
}
