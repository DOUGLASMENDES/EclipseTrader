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

package net.sourceforge.eclipsetrader.news.providers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.INewsProvider;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RSSNewsProvider implements Runnable, INewsProvider
{
    private Thread thread;
    private boolean stopping = false;
    private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);
    private Log log = LogFactory.getLog(getClass());
    static private List oldItems = new ArrayList();

    public RSSNewsProvider()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#subscribe(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribe(Security security)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#unSubscribe(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribe(Security security)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#start()
     */
    public void start()
    {
        if (thread == null)
        {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#stop()
     */
    public void stop()
    {
        stopping = true;
        if (thread != null)
        {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            thread = null;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#snapshot()
     */
    public void snapshot()
    {
        update();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.news.INewsProvider#snapshot(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void snapshot(Security security)
    {
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        long nextRun = System.currentTimeMillis() + 2 * 1000;

        while(!stopping)
        {
            if (System.currentTimeMillis() >= nextRun)
            {
                update();
                int interval = NewsPlugin.getDefault().getPreferenceStore().getInt(NewsPlugin.PREFS_UPDATE_INTERVAL);
                nextRun = System.currentTimeMillis() + interval * 60 * 1000;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        thread = null;
    }

    private void update()
    {
        Object[] o = oldItems.toArray();
        for (int i = 0; i < o.length; i++)
        {
            ((NewsItem)o[i]).setRecent(false);
            CorePlugin.getRepository().save((NewsItem)o[i]);
        }
        oldItems.clear();
        
        Job job = new Job(Messages.RSSNewsProvider_JobName) {
            protected IStatus run(IProgressMonitor monitor)
            {
                File file = new File(Platform.getLocation().toFile(), "rss.xml"); //$NON-NLS-1$
                if (file.exists() == true)
                {
                    try
                    {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(file);
            
                        Node firstNode = document.getFirstChild();
            
                        NodeList childNodes = firstNode.getChildNodes();
                        monitor.beginTask(Messages.RSSNewsProvider_TaskName, childNodes.getLength());
                        log.info("Start fetching RSS News"); //$NON-NLS-1$

                        for (int i = 0; i < childNodes.getLength(); i++)
                        {
                            Node item = childNodes.item(i);
                            String nodeName = item.getNodeName();
                            if (nodeName.equalsIgnoreCase("source")) //$NON-NLS-1$
                                update(new URL(item.getFirstChild().getNodeValue()), item.getAttributes().getNamedItem("name").getNodeValue()); //$NON-NLS-1$

                            monitor.worked(1);
                        }
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                }

                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    private void update(URL feedUrl, String source)
    {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DATE, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_NEWS_DATE_RANGE));
        
        log.debug(feedUrl.toString());
        
        try {
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
            if (store.getBoolean(CorePlugin.PREFS_ENABLE_HTTP_PROXY))
            {
                client.getHostConfiguration().setProxy(store.getString(CorePlugin.PREFS_PROXY_HOST_ADDRESS), store.getInt(CorePlugin.PREFS_PROXY_PORT_ADDRESS));
                if (store.getBoolean(CorePlugin.PREFS_ENABLE_PROXY_AUTHENTICATION))
                    client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(store.getString(CorePlugin.PREFS_PROXY_USER), store.getString(CorePlugin.PREFS_PROXY_PASSWORD)));
            }

            SyndFeed feed = fetcher.retrieveFeed(feedUrl, client);
            for (Iterator iter = feed.getEntries().iterator(); iter.hasNext(); )
            {
                SyndEntry entry = (SyndEntry) iter.next();
                
                NewsItem news = new NewsItem();
                news.setRecent(true);
                Date entryDate = entry.getPublishedDate();
                if (entry.getUpdatedDate() != null)
                    entryDate = entry.getUpdatedDate();
                if (entryDate != null)
                {
                    Calendar date = Calendar.getInstance();
                    date.setTime(entryDate);
                    date.set(Calendar.SECOND, 0);
                    news.setDate(date.getTime());
                }
                news.setTitle(entry.getTitle());
                news.setSource(source);
                news.setUrl(entry.getLink());

                if (!news.getDate().before(limit.getTime()) && !isDuplicated(news))
                {
                    log.trace(news.getTitle() + " (" + news.getSource() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    CorePlugin.getRepository().save(news);
                    oldItems.add(news);
                }
            }
        } catch(Exception e) {
            log.error(e, e);
        }
    }
    
    boolean isDuplicated(NewsItem news)
    {
        NewsItem[] items = (NewsItem[])CorePlugin.getRepository().allNews().toArray(new NewsItem[0]);
        
        for (int i = 0; i < items.length; i++)
        {
            if (news.getTitle().equals(items[i].getTitle()) && news.getUrl().equals(items[i].getUrl()))
            {
                items[i].addSecurities(news.getSecurities());
                CorePlugin.getRepository().save(items[i]);
                return true;
            }
        }

        return false;
    }
}
