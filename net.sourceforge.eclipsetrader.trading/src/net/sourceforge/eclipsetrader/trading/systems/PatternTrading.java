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

package net.sourceforge.eclipsetrader.trading.systems;

import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.Sentiment;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.trading.TradingSystemPlugin;

public class PatternTrading extends TradingSystemPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.trading.pattern";
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    private String patternId;
    private int period = BarData.INTERVAL_WEEKLY;
//    private int bars = 0;

    public PatternTrading()
    {
    }
    
    public void run()
    {
        setSignal(TradingSystem.SIGNAL_NONE);

        BarData barData = new BarData(getSecurity().getHistory().getList());
        if (period != BarData.INTERVAL_DAILY)
            barData = barData.getCompressed(period);

        IPattern pattern = CorePlugin.createPatternPlugin(patternId);
        pattern.init(getSecurity());
        
        for (int i = 0; i < barData.size(); i++)
            pattern.add(barData.get(i));

        Sentiment sentiment = pattern.getSentiment();
        if (sentiment.equals(Sentiment.BULLISH))
            fireOpenLongSignal();
        else if (sentiment.equals(Sentiment.BEARISH))
            fireCloseLongSignal();
        
        setChanged();
        notifyObservers();
    }

    public void setParameters(Map parameters)
    {
        if (parameters.get("pattern") != null)
            patternId = (String)parameters.get("pattern");
        if (parameters.get("period") != null)
        {
            switch(Integer.parseInt((String)parameters.get("period")))
            {
                case DAILY:
                    period = BarData.INTERVAL_DAILY;
                    break;
                case WEEKLY:
                    period = BarData.INTERVAL_WEEKLY;
                    break;
                case MONTHLY:
                    period = BarData.INTERVAL_MONTHLY;
                    break;
            }
        }
//        if (parameters.get("bars") != null)
//            bars = Integer.parseInt((String)parameters.get("bars"));
    }
}
