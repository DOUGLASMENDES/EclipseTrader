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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
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
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceRunnable;
import org.eclipsetrader.news.internal.Activator;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.core.Util;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class NewsProvider implements INewsProvider {

    public static final String HEADLINES_FILE = "headlines.xml"; //$NON-NLS-1$

    private String id;
    private String name;

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

    public NewsProvider(String id, String name) {
        this.id = id;
        this.name = name;
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

    public void startUp() throws JAXBException {
        File file = YahooActivator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
        if (file.exists()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(HeadLine[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, YahooActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    YahooActivator.getDefault().getLog().log(status);
                    return true;
                }
            });
            JAXBElement<HeadLine[]> element = unmarshaller.unmarshal(new StreamSource(file), HeadLine[].class);
            oldItems.addAll(Arrays.asList(element.getValue()));

            Date limitDate = getLimitDate();
            for (Iterator<HeadLine> iter = oldItems.iterator(); iter.hasNext();) {
                if (iter.next().getDate().before(limitDate)) {
                    iter.remove();
                }
            }

            int hoursAsRecent = YahooActivator.getDefault().getPreferenceStore().getInt(YahooActivator.PREFS_HOURS_AS_RECENT);

            Calendar today = Calendar.getInstance();
            today.add(Calendar.HOUR_OF_DAY, -hoursAsRecent);
            Date recentLimitDate = today.getTime();

            for (HeadLine headLine : oldItems) {
                if (!headLine.getDate().before(recentLimitDate)) {
                    headLine.setRecent(true);
                }
            }
        }
    }

    protected void save() throws JAXBException, IOException {
        File file = YahooActivator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(HeadLine[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, YahooActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                YahooActivator.getDefault().getLog().log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        JAXBElement<HeadLine[]> element = new JAXBElement<HeadLine[]>(new QName("list"), HeadLine[].class, oldItems.toArray(new HeadLine[oldItems.size()]));
        marshaller.marshal(element, new FileWriter(file));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsProvider#getHeadLines()
     */
    @Override
    public IHeadLine[] getHeadLines() {
        return oldItems.toArray(new IHeadLine[oldItems.size()]);
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
        IPreferenceStore store = YahooActivator.getDefault().getPreferenceStore();

        Date limitDate = getLimitDate();

        Calendar today = Calendar.getInstance();
        int hoursAsRecent = YahooActivator.getDefault().getPreferenceStore().getInt(YahooActivator.PREFS_HOURS_AS_RECENT);
        today.add(Calendar.HOUR_OF_DAY, -hoursAsRecent);
        Date recentLimitDate = today.getTime();

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Category[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            InputStream inputStream = FileLocator.openStream(YahooActivator.getDefault().getBundle(), new Path("data/news_feeds.xml"), false);
            JAXBElement<Category[]> element = unmarshaller.unmarshal(new StreamSource(inputStream), Category[].class);

            int total = 0;
            for (Category category : element.getValue()) {
                for (Page page : category.getPages()) {
                    if (store.getBoolean(YahooActivator.PREFS_SUBSCRIBE_PREFIX + page.getId())) {
                        total++;
                    }
                }
            }

            ISecurity[] securities = getRepositoryService().getSecurities();
            total += securities.length;

            monitor.beginTask("Fetching Yahoo! News", total);

            resetRecentFlag();

            final Set<HeadLine> added = new HashSet<HeadLine>();
            final Set<HeadLine> updated = new HashSet<HeadLine>();

            Category[] category = element.getValue();
            for (int i = 0; i < category.length && !monitor.isCanceled(); i++) {
                INewsHandler handler = new RSSNewsHandler();
                if (category[i].getHandler() != null) {
                    try {
                        handler = (INewsHandler) Class.forName(category[i].getHandler()).newInstance();
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Invalid news handler class", e);
                        YahooActivator.log(status);
                    }
                }

                List<URL> l = new ArrayList<URL>();

                Page[] page = category[i].getPages();
                for (int ii = 0; ii < page.length && !monitor.isCanceled(); ii++) {
                    if (store.getBoolean(YahooActivator.PREFS_SUBSCRIBE_PREFIX + page[ii].getId())) {
                        l.add(new URL(page[ii].getUrl()));
                    }
                }

                HeadLine[] list = handler.parseNewsPages(l.toArray(new URL[l.size()]), monitor);
                for (HeadLine headLine : list) {
                    if (headLine.getDate() == null || headLine.getDate().before(limitDate)) {
                        continue;
                    }
                    if (!oldItems.contains(headLine)) {
                        if (!headLine.getDate().before(recentLimitDate)) {
                            headLine.setRecent(true);
                        }
                        oldItems.add(headLine);
                        added.add(headLine);
                    }
                }
            }

            if (store.getBoolean(YahooActivator.PREFS_UPDATE_SECURITIES_NEWS)) {
                for (int i = 0; i < securities.length && !monitor.isCanceled(); i++) {
                    URL feedUrl = Util.getRSSNewsFeedForSecurity(securities[i]);
                    if (feedUrl == null) {
                        continue;
                    }

                    monitor.subTask(feedUrl.toString());

                    try {
                        if (YahooActivator.getDefault() != null) {
                            BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
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

                            String link = entry.getLink();
                            if (link == null && entry.getLinks().size() != 0) {
                                link = (String) entry.getLinks().get(0);
                            }
                            if (link != null) {
                                while (link.indexOf('*') != -1) {
                                    link = link.substring(link.indexOf('*') + 1);
                                }
                                link = URLDecoder.decode(link, "UTF-8");
                            }
                            if (link == null) {
                                continue;
                            }

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

                            HeadLine headLine = new HeadLine(entry.getPublishedDate(), source, title, new ISecurity[] {
                                securities[i]
                            }, link);
                            int index = oldItems.indexOf(headLine);
                            if (index != -1) {
                                if (headLine.getDate().before(limitDate)) {
                                    continue;
                                }
                                headLine = oldItems.get(index);
                                if (!headLine.contains(securities[i])) {
                                    headLine.addMember(securities[i]);
                                    updated.add(headLine);
                                }
                            }
                            else {
                                if (!headLine.getDate().before(recentLimitDate)) {
                                    headLine.setRecent(true);
                                }
                                oldItems.add(headLine);
                                added.add(headLine);
                            }
                        }
                    } catch (Exception e) {
                        String msg = "Error fetching news from " + feedUrl.toString();
                        Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, msg, e);
                        YahooActivator.log(status);
                    }

                    monitor.worked(1);
                }
            }

            if (added.size() != 0 || updated.size() != 0) {
                final INewsService service = getNewsService();
                service.runInService(new INewsServiceRunnable() {

                    @Override
                    public IStatus run(IProgressMonitor monitor) throws Exception {
                        service.addHeadLines(added.toArray(new IHeadLine[added.size()]));
                        service.updateHeadLines(updated.toArray(new IHeadLine[updated.size()]));
                        return Status.OK_STATUS;
                    }
                }, null);
            }

            save();
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error fetching news", e);
            YahooActivator.log(status);
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

    protected void resetRecentFlag() {
        int hoursAsRecent = YahooActivator.getDefault().getPreferenceStore().getInt(YahooActivator.PREFS_HOURS_AS_RECENT);

        Calendar today = Calendar.getInstance();
        today.add(Calendar.HOUR_OF_DAY, -hoursAsRecent);
        Date limit = today.getTime();

        final List<HeadLine> updated = new ArrayList<HeadLine>();

        for (HeadLine headLine : oldItems) {
            if (headLine.getDate().before(limit) && headLine.isRecent()) {
                headLine.setRecent(false);
                updated.add(headLine);
            }
        }

        if (updated.size() != 0) {
            final INewsService service = getNewsService();
            service.runInService(new INewsServiceRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    service.updateHeadLines(updated.toArray(new IHeadLine[updated.size()]));
                    return Status.OK_STATUS;
                }
            }, null);
        }
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

    protected Date getLimitDate() {
        Calendar limit = Calendar.getInstance();
        limit.set(Calendar.HOUR_OF_DAY, 0);
        limit.set(Calendar.MINUTE, 0);
        limit.set(Calendar.SECOND, 0);
        limit.set(Calendar.MILLISECOND, 0);
        limit.add(Calendar.DATE, -Activator.getDefault().getPreferenceStore().getInt(Activator.PREFS_DATE_RANGE));
        return limit.getTime();
    }
}
