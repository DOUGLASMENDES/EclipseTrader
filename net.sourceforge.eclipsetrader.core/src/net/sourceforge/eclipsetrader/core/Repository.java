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

package net.sourceforge.eclipsetrader.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;


/**
 */
public class Repository
{
    private ObservableList securities;
    private ObservableList watchlists;
    private ObservableList news;
    private Map newsMap = new HashMap();

    public Repository()
    {
    }
    
    public void dispose()
    {
    }
    
    public ObservableList allSecurities()
    {
        if (securities == null)
            securities = new ObservableList();
        return securities;
    }
    
    public ObservableList allWatchlists()
    {
        if (watchlists == null)
            watchlists = new ObservableList();
        return watchlists;
    }
    
    public ObservableList allNews()
    {
        if (news == null)
            news = new ObservableList();
        return news;
    }
    
    public ObservableList allNews(Security security)
    {
        ObservableList list = (ObservableList) newsMap.get(security);
        if (list == null)
        {
            list = new ObservableList();
            for (Iterator iter = allNews().iterator(); iter.hasNext(); )
            {
                NewsItem news = (NewsItem)iter.next();
                if (news.getSecurity() != null && news.getSecurity().equals(security))
                    list.add(news);
            }
            newsMap.put(security, list);
        }
        return list;
    }
    
    public Security getSecurity(String code)
    {
        PersistentObject obj = load(Security.class, new Integer(code));
        if (obj != null && obj instanceof Security)
            return (Security) obj;
        
        for (Iterator iter = allSecurities().iterator(); iter.hasNext(); )
        {
            Security security = (Security) iter.next();
            if (security.getCode().equals(code))
                return security;
        }
        
        return null;
    }

    public PersistentObject load(Class clazz, Integer id)
    {
        if (clazz.equals(Security.class))
        {
            for (Iterator iter = allSecurities().iterator(); iter.hasNext(); )
            {
                PersistentObject obj = (PersistentObject)iter.next();
                if (id.equals(obj.getId()))
                    return obj;
            }
        }

        if (clazz.equals(Watchlist.class))
        {
            for (Iterator iter = allWatchlists().iterator(); iter.hasNext(); )
            {
                PersistentObject obj = (PersistentObject)iter.next();
                if (id.equals(obj.getId()))
                    return obj;
            }
        }

        return null;
    }
    
    public void save(PersistentObject obj)
    {
        getSaveableObject(obj);
    }
    
    public void delete(PersistentObject obj)
    {
        if (obj instanceof Security)
        {
            for (Iterator iter = allWatchlists().iterator(); iter.hasNext(); )
            {
                Watchlist watchlist = (Watchlist)iter.next();
                Object[] items = watchlist.getItems().toArray();
                for (int i = 0; i < items.length; i++)
                {
                    WatchlistItem item = (WatchlistItem)items[i];
                    if (item.getSecurity().equals(obj))
                        watchlist.getItems().remove(item);
                }
            }
            allSecurities().remove(obj);
        }

        if (obj instanceof Watchlist)
            allWatchlists().remove(obj);

        if (obj instanceof NewsItem)
        {
            NewsItem news = (NewsItem) obj;
            allNews().remove(news);
            if (news.getSecurity() != null)
            {
                ObservableList list = (ObservableList) newsMap.get(news.getSecurity());
                if (list != null)
                    list.remove(news);
            }
        }
    }
    
    protected PersistentObject getSaveableObject(PersistentObject obj)
    {
        obj.setRepository(this);
        if (obj instanceof Observable)
            ((Observable)obj).notifyObservers();

        if (obj instanceof Security)
        {
            if (!allSecurities().contains(obj))
                allSecurities().add(obj);
        }

        if (obj instanceof Watchlist)
        {
            if (!allWatchlists().contains(obj))
                allWatchlists().add(obj);
        }

        if (obj instanceof NewsItem)
        {
            NewsItem news = (NewsItem) obj;
            if (!allNews().contains(news))
                allNews().add(news);
            if (news.getSecurity() != null)
            {
                ObservableList list = (ObservableList) newsMap.get(news.getSecurity());
                if (list != null && !list.contains(news))
                    list.add(news);
            }
        }

        return obj;
    }

    public List loadHistory(Integer id)
    {
        return new ArrayList();
    }

    public void saveHistory(Integer id, List list)
    {
    }

    public List loadIntradayHistory(Integer id)
    {
        return new ArrayList();
    }

    public void saveIntradayHistory(Integer id, List list)
    {
    }
}
