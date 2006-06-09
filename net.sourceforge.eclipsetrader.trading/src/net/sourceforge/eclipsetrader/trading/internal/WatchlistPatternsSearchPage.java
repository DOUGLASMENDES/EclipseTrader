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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

public class WatchlistPatternsSearchPage extends PatternsSearchPage
{
    private Watchlist watchlist;

    public WatchlistPatternsSearchPage(Watchlist watchlist, int period, Date begin, Date end, boolean bullishOnly)
    {
        super(period, begin, end, bullishOnly);
        this.watchlist = watchlist;

        securities = new ArrayList();
        for (Iterator iter = watchlist.getItems().iterator(); iter.hasNext(); )
            securities.add(((WatchlistItem) iter.next()).getSecurity());
    }

    public WatchlistPatternsSearchPage(Watchlist watchlist, IPattern plugin, String name, int period, Date begin, Date end, boolean bullishOnly)
    {
        super(plugin, name, period, begin, end, bullishOnly);
        this.watchlist = watchlist;

        securities = new ArrayList();
        for (Iterator iter = watchlist.getItems().iterator(); iter.hasNext(); )
            securities.add(((WatchlistItem) iter.next()).getSecurity());
    }
    
    public String getDescription()
    {
        String s = "";
        if (patterns.size() == 1)
            s += (String)patterns.keySet().toArray()[0] + " - ";
        else
            s += "All Patterns - ";
        switch(period)
        {
            case BarData.INTERVAL_DAILY:
                s += "Daily";
                break;
            case BarData.INTERVAL_WEEKLY:
                s += "Weekly";
                break;
            case BarData.INTERVAL_MONTHLY:
                s += "Monthly";
                break;
        }
        if (bullishOnly)
            s += " - bullish";
        s += " - " + String.valueOf(results.size()) + " result(s) in " + watchlist.getDescription();
        return s;
    }
    
    public String getShortDescription()
    {
        String s = "";
        if (patterns.size() == 1)
            s += (String)patterns.keySet().toArray()[0] + " - ";
        else
            s += "All Patterns - ";
        switch(period)
        {
            case BarData.INTERVAL_DAILY:
                s += "Daily";
                break;
            case BarData.INTERVAL_WEEKLY:
                s += "Weekly";
                break;
            case BarData.INTERVAL_MONTHLY:
                s += "Monthly";
                break;
        }
        if (bullishOnly)
            s += " - bullish";
        s += " - " + watchlist.getDescription();
        return s;
    }
}
