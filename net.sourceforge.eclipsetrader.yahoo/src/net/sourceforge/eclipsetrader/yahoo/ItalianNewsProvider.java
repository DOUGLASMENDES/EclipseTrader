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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.INewsProvider;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

public class ItalianNewsProvider implements Runnable, INewsProvider
{
    private Thread thread;
    private boolean stopping = false;
    private String url[] = {
            "http://it.biz.yahoo.com/finance_top_business.html",
            "http://it.biz.yahoo.com/attualita/mib30/index.html",
            "http://it.biz.yahoo.com/attualita/nasdaq.html",
            "http://it.biz.yahoo.com/francoforte.html",
            "http://it.biz.yahoo.com/londra.html",
            "http://it.biz.yahoo.com/parigi.html",
            "http://it.biz.yahoo.com/attualita/giappone.html",
            "http://it.biz.yahoo.com/attualita/avviso.html",
            "http://it.biz.yahoo.com/attualita/occhio.html",
            "http://it.biz.yahoo.com/attualita/companyres.html",
            "http://it.biz.yahoo.com/researchalerts.html",
            "http://it.biz.yahoo.com/attualita/giornali.html",
            "http://it.biz.yahoo.com/listasettori.html",
            "http://it.biz.yahoo.com/attualita/comunicati.html",
    };

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
                e.printStackTrace();
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
                e.printStackTrace();
                break;
            }
        }
        
        thread = null;
    }

    private void update()
    {
        Job job = new Job("Yahoo! News") {
            protected IStatus run(IProgressMonitor monitor)
            {
                monitor.beginTask("Fetching Yahoo! News (Italy)", url.length);

                for (int i = 0; i < url.length; i++)
                {
                    monitor.subTask(url[i]);
                    parseNewsPage(url[i]);
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
        try {
          Parser parser = new Parser(url);
          NodeList list = parser.extractAllNodesThatMatch(new OrFilter(new TagNameFilter("dt"), new TagNameFilter("li")));
          for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes(); )
          {
              Node root = iter.nextNode();
              if (root instanceof TagNode)
              {
                  if (((TagNode) root).getTagName().equalsIgnoreCase("dt"))
                  {
                      NodeList childs = root.getChildren();
                      NewsItem news = new NewsItem();

                      Node node = childs.elementAt(3);
                      news.setTitle(node.getFirstChild().getText().trim());
                      news.setUrl(((LinkTag) node).getLink());

                      node = childs.elementAt(9);
                      String source = node.getText();
                      source = source.replaceAll("[\r\n]", " ");
                      source = source.replaceAll("[()]", "");
                      source = source.replaceAll("[ ]{2,}", " ").trim();
                      news.setSource(source);

                      childs = root.getNextSibling().getNextSibling().getNextSibling().getChildren();

                      node = childs.elementAt(1);
                      news.setDate(parseDateString(node.getText()));

                      CorePlugin.getRepository().save(news);
                  }
                  if (((TagNode) root).getTagName().equalsIgnoreCase("li"))
                  {
                      NodeList childs = root.getChildren();
                      NewsItem news = new NewsItem();

                      Node node = childs.elementAt(1);
                      news.setTitle(node.getFirstChild().getText().trim());
                      news.setUrl(((LinkTag) node).getLink());

                      node = childs.elementAt(6);
                      String source = node.getText();
                      source = source.replaceAll("[\r\n]", " ");
                      source = source.replaceAll("[()]", "");
                      source = source.replaceAll("[ ]{2,}", " ").trim();
                      news.setSource(source);

                      node = childs.elementAt(10);
                      news.setDate(parseDateString(node.getText()));

                      CorePlugin.getRepository().save(news);
                  }
              }
          }
      } catch (Exception e) {
          e.printStackTrace();
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
}
