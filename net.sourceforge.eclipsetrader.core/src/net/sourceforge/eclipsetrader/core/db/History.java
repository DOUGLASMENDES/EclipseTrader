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

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 */
public class History extends Observable
{
    static Map map = new HashMap();
    static Map adjustedMap = new HashMap();
    Security security;
    History unadjustedHistory;
    BarData barData = new BarData();
    int instanceCount = 0;
    Observer historyObserver = new Observer() {
        public void update(Observable o, Object arg)
        {
            buildAdjustedBarData();
            setChanged();
            notifyObservers();
        }
    };
    
    public static History getInstance(Security security, boolean adjusted)
    {
        History instance = null;
        
        if (adjusted)
        {
            instance = (History)adjustedMap.get(security);
            if (instance == null)
            {
                instance = new History(security, adjusted);
                adjustedMap.put(security, instance);
            }
        }
        else
        {
            instance = (History)map.get(security);
            if (instance == null)
            {
                instance = new History(security, adjusted);
                map.put(security, instance);
            }
        }
        
        instance.instanceCount++;
        
        return instance;
    }
    
    History(Security security, boolean adjusted)
    {
        if (security.getRepository() != null)
        {
            if (adjusted)
            {
                unadjustedHistory = History.getInstance(security, false);
                buildAdjustedBarData();
                unadjustedHistory.addObserver(historyObserver);
            }
            else
                barData = new BarData(security.getRepository().loadHistory(security.getId()));
        }

        this.security = security;
    }
    
    public void dispose()
    {
        if (instanceCount > 0)
        {
            instanceCount--;
            if (instanceCount == 0)
            {
                if (unadjustedHistory != null)
                {
                    unadjustedHistory.deleteObserver(historyObserver);
                    unadjustedHistory.dispose();
                    unadjustedHistory = null;
                    adjustedMap.remove(security);
                }
                else
                    map.remove(security);
            }
        }
        if (instanceCount <= 0)
            deleteObservers();
    }
    
    void buildAdjustedBarData()
    {
        List list = new ArrayList();
        for (Iterator iter = unadjustedHistory.iterator(); iter.hasNext(); )
        {
            Bar bar = new Bar((Bar)iter.next());
            double factor = 1.0;
            for (Iterator iter2 = security.getSplits().iterator(); iter2.hasNext(); )
            {
                Split split = (Split)iter2.next();
                if (bar.getDate().before(split.getDate()))
                    factor *= (double)split.getToQuantity() / (double)split.getFromQuantity();
            }
            double dividends = 0.0;
            for (Iterator iter2 = security.getDividends().iterator(); iter2.hasNext(); )
            {
                Dividend dividend = (Dividend)iter2.next();
                if (bar.getDate().before(dividend.getDate()))
                    dividends += dividend.getValue();
            }

            bar.setOpen((bar.getOpen() * factor) - dividends);
            bar.setHigh((bar.getHigh() * factor) - dividends);
            bar.setLow((bar.getLow() * factor) - dividends);
            bar.setClose((bar.getClose() * factor) - dividends);
            bar.setVolume((long)(bar.getVolume() / factor));
            list.add(bar);
        }
        
        barData = new BarData(list);
    }
    
    public BarData getBarData()
    {
        return barData;
    }
    
    public void clear()
    {
        barData.clear();
        setChanged();
    }
    
    public void save()
    {
        security.getRepository().saveHistory(security.getId(), barData.getBars());
        notifyObservers();
    }
    
    public boolean append(Bar obj)
    {
        boolean result = barData.append(obj);
        if (result)
            setChanged();
        return result;
    }

    public boolean remove(Bar obj)
    {
        boolean result = barData.remove(obj);
        if (result)
            setChanged();
        return result;
    }
    
    public Iterator iterator()
    {
        return barData.iterator();
    }
}
