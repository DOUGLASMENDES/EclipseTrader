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

package net.sourceforge.eclipsetrader.yahoo;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class ItalianNewsProvider implements Runnable, INewsProvider
{
    private Thread thread;
    private boolean stopping = false;
    static private List oldItems = new ArrayList();
    private FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher(feedInfoCache);
    private Log log = LogFactory.getLog(getClass());

    public ItalianNewsProvider()
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
                log.error(e);
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
        try {
            update(new URL("http://it.finance.yahoo.com/rss/headline?s=" + security.getCode().toUpperCase()), security);
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        long nextRun = System.currentTimeMillis() + 2 * 1000;

        while (!stopping)
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
                log.error(e);
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
        
        Job job = new Job("Yahoo! News (Italy)") {
            protected IStatus run(IProgressMonitor monitor)
            {
                IPreferenceStore store = YahooPlugin.getDefault().getPreferenceStore();

                List urls = new ArrayList();
                try
                {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(FileLocator.openStream(YahooPlugin.getDefault().getBundle(), new Path("categories.it.xml"), false)); //$NON-NLS-1$

                    org.w3c.dom.NodeList childNodes = document.getFirstChild().getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++)
                    {
                        org.w3c.dom.Node node = childNodes.item(i);
                        String nodeName = node.getNodeName();
                        if (nodeName.equalsIgnoreCase("category")) //$NON-NLS-1$
                        {
                            String id = ((org.w3c.dom.Node)node).getAttributes().getNamedItem("id").getNodeValue(); //$NON-NLS-1$
                         
                            org.w3c.dom.NodeList list = node.getChildNodes();
                            for (int x = 0; x < list.getLength(); x++)
                            {
                                org.w3c.dom.Node item = list.item(x);
                                nodeName = item.getNodeName();
                                org.w3c.dom.Node value = item.getFirstChild();
                                if (value != null)
                                {
                                    if (nodeName.equalsIgnoreCase("url")) //$NON-NLS-1$
                                    {
                                        if (store.getBoolean(id))
                                            urls.add(value.getNodeValue());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e, e);
                }

                List securities = CorePlugin.getRepository().allSecurities();
                monitor.beginTask("Fetching Yahoo! News (Italy)", urls.size() + securities.size());
                log.info("Start fetching Yahoo! News (Italy)");

                for (Iterator iter = securities.iterator(); iter.hasNext(); )
                {
                    Security security = (Security) iter.next();
                    try {
                        String url = "http://it.finance.yahoo.com/rss/headline?s=" + security.getCode().toUpperCase(); //$NON-NLS-1$
                        monitor.subTask(url);
                        update(new URL(url), security);
                        monitor.worked(1);
                    } catch(Exception e) {
                        log.error(e, e);
                    }
                }

                for (Iterator iter = urls.iterator(); iter.hasNext(); )
                {
                    String url = (String) iter.next();
                    monitor.subTask(url);
                    parseNewsPage(url);
                    monitor.worked(1);
                }

                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    private void parseNewsPage(String url)
    {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DATE, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_NEWS_DATE_RANGE));

        int dtCount = 0;
        int liCount = 0;
        
        try {
            log.debug(url);
            Parser parser = new Parser(url);

            NodeList list = parser.extractAllNodesThatMatch(new OrFilter(new TagNameFilter("dt"), new TagNameFilter("li")));
            for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();)
            {
                Node root = iter.nextNode();
                list = root.getChildren();

                if (((TagNode) root).getTagName().equalsIgnoreCase("dt") && list.size() == 12)
                {
                    LinkTag link = (LinkTag)list.elementAt(3);

                    NewsItem news = new NewsItem();
                    news.setRecent(true);
                    news.setTitle(decode(link.getLinkText().trim()));
                    news.setUrl(link.getLink());

                    String source = list.elementAt(9).getText();
                    source = source.replaceAll("[\r\n]", " ");
                    source = source.replaceAll("[()]", "");
                    source = source.replaceAll("[ ]{2,}", " ").trim();
                    news.setSource(source);

                    news.setDate(parseDateString(root.getNextSibling().getNextSibling().getNextSibling().getChildren().elementAt(1).getText()));

                    if (!news.getDate().before(limit.getTime()))
                    {
                        log.trace(news.getTitle() + " (" + news.getSource() + ")");
                        CorePlugin.getRepository().save(news);
                        oldItems.add(news);
                    }
                    dtCount++;
                }
                else if (((TagNode) root).getTagName().equalsIgnoreCase("li") && list.size() == 14)
                {
                    LinkTag link = (LinkTag)list.elementAt(1);

                    NewsItem news = new NewsItem();
                    news.setRecent(true);
                    news.setTitle(decode(link.getLinkText().trim()));
                    news.setUrl(link.getLink());

                    String source = list.elementAt(6).getText();
                    source = source.replaceAll("[\r\n]", " ");
                    source = source.replaceAll("[()]", "");
                    source = source.replaceAll("[ ]{2,}", " ").trim();
                    news.setSource(source);

                    news.setDate(parseDateString(list.elementAt(10).getText()));

                    if (!news.getDate().before(limit.getTime()))
                    {
                        log.trace(news.getTitle() + " (" + news.getSource() + ")");
                        CorePlugin.getRepository().save(news);
                        oldItems.add(news);
                    }
                    liCount++;
                }
            }
            
            if (dtCount == 0 && liCount == 0)
                log.warn("No news found on that page (" + url + ")");
            else if (dtCount == 0 || liCount == 0)
                log.warn("Page not completely parsed (" + url + ")");
        }
        catch (Exception e) {
            log.error(e, e);
        }
    }

    private void update(URL feedUrl, Security security)
    {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DATE, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_NEWS_DATE_RANGE));

        boolean subscribersOnly = YahooPlugin.getDefault().getPreferenceStore().getBoolean(YahooPlugin.PREFS_SHOW_SUBSCRIBERS_ONLY);
        log.debug(feedUrl);
        
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
                
                if (!subscribersOnly && entry.getTitle().indexOf("[$$]") != -1)
                    continue;
                
                NewsItem news = new NewsItem();
                news.setRecent(true);

                String title = entry.getTitle();
                if (title.endsWith(")"))
                {
                    int s = title.lastIndexOf('(');
                    if (s != -1)
                    {
                        news.setSource(title.substring(s + 1, title.length() - 1));
                        title = title.substring(0, s - 1).trim();
                    }
                }
                news.setTitle(decode(title));

                news.setUrl(entry.getLink());

                Date entryDate = entry.getPublishedDate();
                if (entry.getUpdatedDate() != null)
                    entryDate = entry.getUpdatedDate();
                if (entryDate != null)
                {
                    Calendar date = Calendar.getInstance();
                    date.setTime(entryDate);
                    date.set(Calendar.SECOND, 0);
                    date.set(Calendar.MILLISECOND, 0);
                    news.setDate(date.getTime());
                }
                
                if (security != null)
                    news.addSecurity(security);

                if (!news.getDate().before(limit.getTime()) && !isDuplicated(news))
                {
                    log.trace(news.getTitle() + " (" + news.getSource() + ")");
                    CorePlugin.getRepository().save(news);
                    oldItems.add(news);
                }
            }
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
    }

    private Date parseDateString(String date)
    {
        Calendar gc = Calendar.getInstance(Locale.ITALY);
        StringTokenizer st = new StringTokenizer(date, " ,:");
        st.nextToken();
        Integer vint = new Integer(st.nextToken());
        gc.set(Calendar.DAY_OF_MONTH, vint.intValue());
        gc.set(Calendar.MONTH, getMonth(st.nextToken()) - 1);
        vint = new Integer(st.nextToken());
        gc.set(Calendar.YEAR, vint.intValue());
        vint = new Integer(st.nextToken());
        gc.set(Calendar.HOUR_OF_DAY, vint.intValue());
        vint = new Integer(st.nextToken());
        gc.set(Calendar.MINUTE, vint.intValue());
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    private int getMonth(String t)
    {
        if (t.equalsIgnoreCase("Gennaio") == true)
            return 1;
        if (t.equalsIgnoreCase("Febbraio") == true)
            return 2;
        if (t.equalsIgnoreCase("Marzo") == true)
            return 3;
        if (t.equalsIgnoreCase("Aprile") == true)
            return 4;
        if (t.equalsIgnoreCase("Maggio") == true)
            return 5;
        if (t.equalsIgnoreCase("Giugno") == true)
            return 6;
        if (t.equalsIgnoreCase("Luglio") == true)
            return 7;
        if (t.equalsIgnoreCase("Agosto") == true)
            return 8;
        if (t.equalsIgnoreCase("Settembre") == true)
            return 9;
        if (t.equalsIgnoreCase("Ottobre") == true)
            return 10;
        if (t.equalsIgnoreCase("Novembre") == true)
            return 11;
        if (t.equalsIgnoreCase("Dicembre") == true)
            return 12;

        return 0;
    }
    
    private String decode(String s)
    {
        if (s.indexOf("&#") == -1)
            return s;
        
        int i = 0;
        StringBuffer sb = new StringBuffer();
        byte[] bytes = new byte[0];
        try {
            bytes = s.getBytes();
        } catch(Exception e) {
            log.error(e, e);
            bytes = s.getBytes();
        }
        
        while(i < bytes.length)
        {
            byte c = bytes[i++];
            if (c == '&' && i < bytes.length)
            {
                c = bytes[i++];
                if (c == '#')
                {
                    int data = 0;
                    while(i < bytes.length)
                    {
                        c = bytes[i++];
                        if (c < '0' || c > '9')
                            break;
                        data = (data * 10) + (c - '0');
                    }
                    if (data >= ' ')
                    {
                        try {
                            sb.append(new String(new byte[] { (byte)data }));
                        } catch(Exception e) {
                            log.error(e, e);
                        }
                    }
                }
                else
                {
                    try {
                        sb.append('&');
                        sb.append(new String(new byte[] { c }));
                    } catch(Exception e) {
                        log.error(e, e);
                    }
                }
            }
            else if (c >= ' ')
            {
                try {
                    sb.append(new String(new byte[] { c }));
                } catch(Exception e) {
                    log.error(e, e);
                }
            }
        }
        
        return sb.toString();
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
