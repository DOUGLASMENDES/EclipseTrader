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

package net.sourceforge.eclipsetrader.trading.alerts;

import java.text.NumberFormat;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Event;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;

public class MovingAverageCrossover extends AlertPlugin
{
    public static final int OPEN = BarData.OPEN;
    public static final int HIGH = BarData.HIGH;
    public static final int LOW = BarData.LOW;
    public static final int CLOSE = BarData.CLOSE;
    public static final int DAILY = BarData.INTERVAL_DAILY;
    public static final int WEEKLY = BarData.INTERVAL_WEEKLY;
    public static final int MONTHLY = BarData.INTERVAL_MONTHLY;
    public static final int UPWARD = 0;
    public static final int DOWNWARD = 1;
    private int field = CLOSE;
    private int maType = MA.EMA;
    private int period = 7;
    private int interval = CLOSE;
    private int direction = UPWARD;
    private NumberFormat priceFormatter = NumberFormat.getInstance();

    public MovingAverageCrossover()
    {
        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(4);
        priceFormatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#init(java.util.Map)
     */
    public void init(Map params)
    {
        String value = (String)params.get("field");
        if (value != null)
            field = Integer.parseInt(value);
        value = (String)params.get("maType");
        if (value != null)
            maType = Integer.parseInt(value);
        value = (String)params.get("period");
        if (value != null)
            period = Integer.parseInt(value);
        value = (String)params.get("interval");
        if (value != null)
            interval = Integer.parseInt(value);
        value = (String)params.get("direction");
        if (value != null)
            direction = Integer.parseInt(value);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#getDescription()
     */
    public String getDescription()
    {
        String s = "Price crosses";
        if (direction == UPWARD)
            s += " upward";
        else if (direction == DOWNWARD)
            s += " downward";
        s += " over its " + String.valueOf(period);
        switch(interval)
        {
            case DAILY:
                s += " days";
                break; 
            case WEEKLY:
                s += " weeks";
                break; 
            case MONTHLY:
                s += " months";
                break; 
        }
        switch(maType)
        {
            case MA.SMA:
                s += "";
                break; 
            case MA.EMA:
                s += " exponential";
                break; 
            case MA.WMA:
                s += " weighted";
                break; 
            case MA.Wilder:
                s += " Wilder's";
                break; 
        }
        s += " moving average";
        return s; 
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#apply()
     */
    public boolean apply()
    {
        BarData barData = new BarData(getSecurity().getHistory());
        switch(interval)
        {
            case DAILY:
                break; 
            case WEEKLY:
                barData = barData.getCompressed(BarData.INTERVAL_WEEKLY);
                break; 
            case MONTHLY:
                barData = barData.getCompressed(BarData.INTERVAL_MONTHLY);
                break; 
        }
        
        if (barData.size() <= period || barData.size() == 0 || getSecurity().getQuote() == null)
            return false;

        double value = MA.getMA(barData, field, maType, period);
        boolean result = false;
        if (direction == UPWARD)
            result = (getSecurity().getQuote().getLast() > value);
        else if (direction == DOWNWARD)
            result = getSecurity().getQuote().getLast() < value;
        
        if (result)
        {
            String s = "Price crossed";
            if (direction == UPWARD)
                s += " upward";
            else if (direction == DOWNWARD)
                s += " downward";
            s += " over its " + String.valueOf(period);
            switch(interval)
            {
                case DAILY:
                    s += " days";
                    break; 
                case WEEKLY:
                    s += " weeks";
                    break; 
                case MONTHLY:
                    s += " months";
                    break; 
            }
            switch(maType)
            {
                case MA.SMA:
                    s += "";
                    break; 
                case MA.EMA:
                    s += " exponential";
                    break; 
                case MA.WMA:
                    s += " weighted";
                    break; 
                case MA.Wilder:
                    s += " Wilder's";
                    break; 
            }
            s += " moving average at " + priceFormatter.format(getSecurity().getQuote().getLast());
            Event event = new Event();
            event.setSecurity(getSecurity());
            event.setMessage(s);
            CorePlugin.getRepository().save(event);
        }
        
        return result;
    }
}
