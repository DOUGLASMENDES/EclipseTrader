/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.IntradayHistory;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataCollector implements Observer, ICollectionObserver
{
    private int minutes = 1;
    private int changes = 15;
    private Map map = new HashMap();
    private Calendar barTime = Calendar.getInstance();
    private Log log = LogFactory.getLog(getClass());

    public DataCollector()
    {
        Security[] items = (Security[])CorePlugin.getRepository().allSecurities().toArray(new Security[0]);
        for (int i = 0; i < items.length; i++)
            itemAdded(items[i]);
        CorePlugin.getRepository().allSecurities().addCollectionObserver(this);
    }
    
    public void dispose()
    {
        CorePlugin.getRepository().allSecurities().removeCollectionObserver(this);
        Security[] items = (Security[])CorePlugin.getRepository().allSecurities().toArray(new Security[0]);
        for (int i = 0; i < items.length; i++)
            itemRemoved(items[i]);
    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        Security security = (Security) arg;
        Quote quote = security.getQuote();

        if (!security.isEnableDataCollector() || quote == null || quote.getDate() == null)
            return;
        
        barTime.setTime(quote.getDate());
        barTime.set(Calendar.SECOND, 0);
        barTime.set(Calendar.MILLISECOND, 0);

        int quoteTime = barTime.get(Calendar.HOUR_OF_DAY) * 60 + barTime.get(Calendar.MINUTE);
        if (quoteTime < security.getBeginTime() || quoteTime > security.getEndTime())
            return;
        
        MapData data = (MapData)map.get(security);
        if (data.bar != null && data.bar.getDate() != null)
        {
            if (barTime.after(data.nextBarTime) || barTime.equals(data.nextBarTime))
            {
                data.history.add(data.bar);
                
                if (security.getKeepDays() != 0)
                {
                    Calendar keepLimit = Calendar.getInstance();
                    keepLimit.setTime(data.bar.getDate());
                    keepLimit.set(Calendar.HOUR, 0);
                    keepLimit.set(Calendar.MINUTE, 0);
                    keepLimit.set(Calendar.SECOND, 0);
                    keepLimit.set(Calendar.MILLISECOND, 0);
                    keepLimit.add(Calendar.DATE, - security.getKeepDays());

                    Date limit = keepLimit.getTime();
                    while(data.history.size() > 0 && ((Bar) data.history.get(0)).getDate().before(limit))
                        data.history.remove(0);
                }
                
                try {
                    log.trace("Notifying intraday data updated for " + security.getCode() + " - " + security.getDescription());
                    data.history.notifyObservers();
                    data.changes++;
                    if (data.changes >= changes)
                    {
                        CorePlugin.getRepository().save(data.history);
                        data.changes = 0;
                    }
                } catch(Exception e) {
                    log.error(e, e);
                }
                data.bar = null;
            }
        }
        else if (data.bar == null)
        {
            data.bar = new Bar();
            data.bar.setOpen(quote.getLast());
            data.bar.setHigh(quote.getLast());
            data.bar.setLow(quote.getLast());
            data.bar.setClose(quote.getLast());
            data.volume = quote.getVolume();
            barTime.add(Calendar.MINUTE, - (barTime.get(Calendar.MINUTE) % minutes));
            data.bar.setDate(barTime.getTime());
            data.nextBarTime.setTime(data.bar.getDate());
            data.nextBarTime.add(Calendar.MINUTE, minutes);
        }

        if (data.bar != null)
        {
            if (quote.getLast() > data.bar.getHigh())
                data.bar.setHigh(quote.getLast());
            if (quote.getLast() < data.bar.getLow())
                data.bar.setLow(quote.getLast());
            data.bar.setClose(quote.getLast());
            data.bar.setVolume(quote.getVolume() - data.volume);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        if (o instanceof Security)
        {
            Security security = (Security)o;
            if (map.get(security) == null)
            {
                map.put(security, new MapData(security.getIntradayHistory()));
                security.getQuoteMonitor().addObserver(this);
                log.info("Added " + security.getDescription());
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        if (o instanceof Security)
        {
            Security security = (Security)o;
            MapData data = (MapData)map.get(security);
            if (data != null)
            {
                security.getQuoteMonitor().deleteObserver(this);
                map.remove(security);

                if (data.history.size() != 0)
                    CorePlugin.getRepository().save(data.history);
                
                log.info("Removed " + security.getDescription());
            }
        }
    }
    
    private class MapData
    {
        Bar bar;
        IntradayHistory history;
        long volume;
        Calendar nextBarTime;
        int changes;

        MapData(IntradayHistory data)
        {
            this.history = data;
            nextBarTime = Calendar.getInstance();
            nextBarTime.set(Calendar.SECOND, 0);
            nextBarTime.set(Calendar.MILLISECOND, 0);
        }
    }
}
