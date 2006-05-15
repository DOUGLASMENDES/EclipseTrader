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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.trading.views.IPatternSearchPage;
import net.sourceforge.eclipsetrader.trading.views.PatternSearchItem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;

public class SecurityPatternsSearchPage implements IPatternSearchPage
{
    private Security security;
    private int period;
    private int bars;
    private boolean bullishOnly;
    private Map patterns = new HashMap();
    private List results = new ArrayList();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private NumberFormat nf = NumberFormat.getInstance();

    public SecurityPatternsSearchPage(Security security, int period, int bars, boolean bullishOnly)
    {
        this.security = security;
        this.period = period;
        this.bars = bars + 1;
        this.bullishOnly = bullishOnly;
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        
        for (Iterator iter = CorePlugin.getAllPatternPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            IPattern plugin = CorePlugin.createPatternPlugin(element.getAttribute("id"));
            patterns.put(element.getAttribute("name"), plugin);
        }
    }

    public SecurityPatternsSearchPage(Security security, IPattern plugin, String name, int period, int bars, boolean bullishOnly)
    {
        this.security = security;
        this.period = period;
        this.bars = bars + 1;
        this.bullishOnly = bullishOnly;
        
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        
        patterns.put(name, plugin);
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
        s += " - " + String.valueOf(bars - 1) + " period(s) - ";
        if (bullishOnly)
            s += "bullish - ";
        s += String.valueOf(results.size()) + " result(s) in ";
        s += security.getDescription();
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
        s += " - " + String.valueOf(bars - 1) + " period(s) - ";
        if (bullishOnly)
            s += " bullish - ";
        s += security.getDescription();
        return s;
    }

    public void run(IProgressMonitor monitor)
    {
        monitor.beginTask("Searching", 1);
        getResults().clear();

        BarData barData = new BarData(security.getHistory());
        if (period != BarData.INTERVAL_DAILY)
            barData = barData.getCompressed(period);

        boolean loop = true;
        for (int i = 1; i < barData.size() && loop; i++)
        {
            Bar[] recs = barData.toArray(i);

            for (Iterator p = patterns.keySet().iterator(); p.hasNext(); )
            {
                String name = (String) p.next();
                IPattern pattern = (IPattern) patterns.get(name);
                if (pattern.applies(recs))
                {
                    if (pattern.getComplete() != 0 && (pattern.getComplete() >= barData.size() - i - bars || i + bars > barData.size()) && ((pattern.isBullish() && bullishOnly) || !bullishOnly))
                    {
                        PatternSearchItem item = new PatternSearchItem();
                        item.setCode(security.getCode());
                        item.setDescription(security.getDescription());
                        item.setDate(df.format(recs[pattern.getComplete()].getDate()));
                        item.setPrice(nf.format(recs[pattern.getComplete()].getClose()));
                        item.setPattern(name);
                        item.setOpportunity(pattern.isBullish() ? "Bullish" : "Bearish");
                        getResults().add(item);
                        loop = false;
                    }
                }
            }
        }

        monitor.worked(1);

        monitor.done();
    }

    public List getResults()
    {
        return results;
    }
}
