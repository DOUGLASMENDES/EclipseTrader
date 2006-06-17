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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.core.db.NewsItem;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;


/**
 */
public class Repository
{
    private ObservableList securityGroups;
    private ObservableList securities;
    private ObservableList watchlists;
    private ObservableList news;
    private ObservableList accounts;
    private ObservableList accountGroups;
    private ObservableList events;
    private ObservableList tradingSystems;
    private ObservableList tradingSystemGroups;
    private Map newsMap = new HashMap();

    public Repository()
    {
    }
    
    public void dispose()
    {
    }
    
    public ObservableList allSecurityGroups()
    {
        if (securityGroups == null)
            securityGroups = new ObservableList();
        return securityGroups;
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
    
    public ObservableList getTradingSystems()
    {
        if (tradingSystems == null)
            tradingSystems = new ObservableList();
        return tradingSystems;
    }
    
    public ObservableList getTradingSystemGroups()
    {
        if (tradingSystemGroups == null)
            tradingSystemGroups = new ObservableList();
        return tradingSystemGroups;
    }
    
    public TradingSystem getTradingSystem(String pluginId)
    {
        for (Iterator iter = getTradingSystems().iterator(); iter.hasNext(); )
        {
            TradingSystem system = (TradingSystem) iter.next();
            if (system.getPluginId().equals(pluginId))
                return system;
        }
        
        return null;
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
    
    public ObservableList allAccounts()
    {
        if (accounts == null)
            accounts = new ObservableList();
        return accounts;
    }
    
    public ObservableList allAccountGroups()
    {
        if (accountGroups == null)
            accountGroups = new ObservableList();
        return accountGroups;
    }
    
    public Security getSecurity(String code)
    {
        try {
            PersistentObject obj = load(Security.class, new Integer(code));
            if (obj != null && obj instanceof Security)
                return (Security) obj;
        } catch(Exception e) {}
        
        for (Iterator iter = allSecurities().iterator(); iter.hasNext(); )
        {
            Security security = (Security) iter.next();
            if (security.getCode().equals(code))
                return security;
        }
        
        return null;
    }
    
    public ObservableList allEvents()
    {
        if (events == null)
            events = new ObservableList();
        return events;
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
        obj.setRepository(this);
        if (obj instanceof Observable)
            ((Observable)obj).notifyObservers();

        if (obj instanceof Event)
        {
            if (!allEvents().contains(obj))
                allEvents().add(obj);
        }

        if (obj instanceof SecurityGroup)
        {
            if (!allSecurityGroups().contains(obj))
                allSecurityGroups().add(obj);
        }
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

            Calendar limit = Calendar.getInstance();
            limit.add(Calendar.DATE, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_NEWS_DATE_RANGE));
            if (news.getDate().before(limit.getTime()))
                return;
            
            if (!allNews().contains(news))
                allNews().add(news);
            if (news.getSecurity() != null)
            {
                ObservableList list = (ObservableList) newsMap.get(news.getSecurity());
                if (list != null && !list.contains(news))
                    list.add(news);
            }
        }

        if (obj instanceof AccountGroup)
        {
            AccountGroup group = (AccountGroup)obj;
            if (!allAccountGroups().contains(group))
                allAccountGroups().add(group);
            if (group.getParent() != null)
            {
                AccountGroup parent = group.getParent();
                if (!parent.getGroups().contains(group))
                    parent.getGroups().add(group);
            }
        }
        if (obj instanceof Account)
        {
            Account account = (Account)obj;
            if (!allAccounts().contains(account))
                allAccounts().add(account);
            if (account.getGroup() != null)
            {
                AccountGroup group = account.getGroup();
                if (!group.getAccounts().contains(account))
                    group.getAccounts().add(account);
            }
        }
    }
    
    public void delete(PersistentObject obj)
    {
        if (obj instanceof Event)
            allEvents().remove(obj);

        if (obj instanceof SecurityGroup)
        {
            for (Iterator iter = ((SecurityGroup)obj).getSecurities().iterator(); iter.hasNext(); )
                ((Security)iter.next()).setGroup(null);
            allSecurityGroups().remove(obj);
        }
        
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
            
            TradingSystem[] systems = (TradingSystem[])getTradingSystems().toArray(new TradingSystem[0]);
            for (int i = 0; i < systems.length; i++)
            {
                if (systems[i].getSecurity().equals(obj))
                    delete(systems[i]);
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

        if (obj instanceof Account)
        {
            allAccounts().remove(obj);
            if (((Account)obj).getGroup() != null)
                ((Account)obj).getGroup().getAccounts().remove(obj);
            
            TradingSystem[] systems = (TradingSystem[])getTradingSystems().toArray(new TradingSystem[0]);
            for (int i = 0; i < systems.length; i++)
            {
                if (systems[i].getAccount().equals(obj))
                    delete(systems[i]);
            }
        }

        if (obj instanceof AccountGroup)
        {
            AccountGroup group = (AccountGroup)obj;
            
            Object[] accounts = group.getAccounts().toArray();
            for (int i = 0; i < accounts.length; i++)
                delete((Account)accounts[i]);
            
            Object[] groups = group.getAccounts().toArray();
            for (int i = 0; i < groups.length; i++)
                delete((AccountGroup)groups[i]);
            
            if (group.getParent() != null)
                group.getParent().getGroups().remove(group);

            allAccountGroups().remove(group);
        }
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
