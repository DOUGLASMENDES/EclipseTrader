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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.Sentiment;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.trading.views.IPatternSearchPage;
import net.sourceforge.eclipsetrader.trading.views.PatternSearchItem;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class PatternsSearchPage implements IPatternSearchPage
{
    protected List securities = new ArrayList();
    protected int period;
    protected boolean bullishOnly;
    protected Date begin;
    protected Date end;
    protected boolean allOccurrences = false;
    protected Map patterns = new HashMap();
    protected List results = new ArrayList();

    public PatternsSearchPage(int period, Date begin, Date end, boolean bullishOnly)
    {
        this.period = period;
        this.bullishOnly = bullishOnly;
        this.begin = begin;
        this.end = end;
        
        for (Iterator iter = CorePlugin.getAllPatternPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            IPattern plugin = CorePlugin.createPatternPlugin(element.getAttribute("id"));
            patterns.put(element.getAttribute("name"), plugin);
        }
    }

    public PatternsSearchPage(IPattern plugin, String name, int period, Date begin, Date end, boolean bullishOnly)
    {
        this.period = period;
        this.bullishOnly = bullishOnly;
        this.begin = begin;
        this.end = end;
        
        patterns.put(name, plugin);
    }
    
    public abstract String getDescription();
    
    public abstract String getShortDescription();

    public void run(IProgressMonitor monitor)
    {
        getResults().clear();
        
        monitor.beginTask("Searching", securities.size());

        for (Iterator iter = securities.iterator(); iter.hasNext(); )
        {
            Security security = (Security) iter.next();

            BarData barData = new BarData(security.getHistory().getList(), begin, end);
            if (period != BarData.INTERVAL_DAILY)
                barData = barData.getCompressed(period);

            Map lastComplete = new HashMap();
            
            for (Iterator p = patterns.keySet().iterator(); p.hasNext(); )
            {
                String name = (String) p.next();
                IPattern pattern = (IPattern) patterns.get(name);
                
                pattern.init(security);

                for (int i = 0; i < barData.size(); i++)
                {
                    pattern.add(barData.get(i));
                    if (pattern.getSentiment().equals(Sentiment.BULLISH) || pattern.getSentiment().equals(Sentiment.BEARISH))
                    {
                        Bar complete = barData.get(i);
                        
                        Date last = (Date)lastComplete.get(pattern);
                        if (last == null || !last.equals(complete.getDate()))
                        {
                            PatternSearchItem item = new PatternSearchItem();
                            item.setCode(security.getCode());
                            item.setDescription(security.getDescription());
                            item.setDate(complete.getDate());
                            item.setPrice(complete.getClose());
                            item.setPattern(name);
                            item.setOpportunity(pattern.getSentiment().equals(Sentiment.BULLISH) ? "Bullish" : "Bearish");

                            if (!allOccurrences)
                            {
                                PatternSearchItem[] items = (PatternSearchItem[])getResults().toArray(new PatternSearchItem[0]);
                                for (int x = 0; x < items.length; x++)
                                {
                                    if (items[x].getCode().equals(item.getCode()) && items[x].getDescription().equals(item.getDescription()) && items[x].getPattern().equals(item.getPattern()))
                                        getResults().remove(items[x]);
                                }
                            }
                            getResults().add(item);
                            
                            lastComplete.put(pattern, complete.getDate());
                        }
                    }
                }
            }

            monitor.worked(1);
        }

        monitor.done();
    }

    public List getResults()
    {
        return results;
    }

    public void setAllOccurrences(boolean allOccurrences)
    {
        this.allOccurrences = allOccurrences;
    }
}
