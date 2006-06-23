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

import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;

public class RSICrossover extends AlertPlugin
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
    private int period = 7;
    private int interval = WEEKLY;
    private int direction = UPWARD;
    private int level = 80;
    private RGB hilightColor = new RGB(255, 0, 0);
    private NumberFormat numberFormatter = NumberFormat.getInstance();
    private NumberFormat priceFormatter = NumberFormat.getInstance();

    public RSICrossover()
    {
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);

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
        value = (String)params.get("level");
        if (value != null)
            level = Integer.parseInt(value);
        value = (String)params.get("period");
        if (value != null)
            period = Integer.parseInt(value);
        value = (String)params.get("interval");
        if (value != null)
            interval = Integer.parseInt(value);
        value = (String)params.get("direction");
        if (value != null)
            direction = Integer.parseInt(value);
        value = (String)params.get("hilightBackground");
        if (value != null)
        {
            String[] ar = value.split(",");
            hilightColor = new RGB(Integer.parseInt(ar[0]), Integer.parseInt(ar[1]), Integer.parseInt(ar[2]));
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#getDescription()
     */
    public String getDescription()
    {
        String s = "When the " + String.valueOf(period);
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
        s += " RSI goes";
        if (direction == UPWARD)
            s += " over";
        else if (direction == DOWNWARD)
            s += " under";
        s += " " + String.valueOf(level);
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

        double value = getRSI(barData, field, period, getSecurity().getQuote().getLast());

        boolean result = false;
        if (direction == UPWARD)
            result = value > level;
        else if (direction == DOWNWARD)
            result = value < level;
        
        if (result)
        {
            String s = "The " + String.valueOf(period);
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
            s += " RSI has gone";
            if (direction == UPWARD)
                s += " over";
            else if (direction == DOWNWARD)
                s += " under";
            s += " " + String.valueOf(level) + "%";
            s += " at " + numberFormatter.format(value) + "% with price at " + priceFormatter.format(getSecurity().getQuote().getLast()); 
            fireEvent(s);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#getHilightBackground()
     */
    public RGB getHilightBackground()
    {
        return hilightColor;
    }

    private double getRSI(BarData in, int field, int period, double lastPrice)
    {
        double rsi = 0;

        if (period < 1 || period >= in.size())
            return rsi;

        double data[] = new double[period + 1];

        int index = in.size() - period;
        for (int i = 0; i < data.length - 1; i++, index++)
        {
            switch(field)
            {
                case BarData.OPEN:
                    data[i] = in.getOpen(index);
                    break;
                case BarData.HIGH:
                    data[i] = in.getHigh(index);
                    break;
                case BarData.LOW:
                    data[i] = in.getLow(index);
                    break;
                case BarData.CLOSE:
                    data[i] = in.getClose(index);
                    break;
            }
        }
        data[data.length - 1] = lastPrice;

        int loop;
        for (loop = period; loop < data.length; loop++)
        {
            double loss = 0;
            double gain = 0;
            int loop2;
            for (loop2 = 0; loop2 < period; loop2++)
            {
                double t = data[loop - loop2] - data[loop - loop2 - 1];
                if (t > 0)
                    gain = gain + t;
                if (t < 0)
                    loss = loss + Math.abs(t);
            }

            double again = gain / period;
            double aloss = loss / period;
            double rs = again / aloss;
            double t = 100 - (100 / (1 + rs));
            if (t > 100)
                t = 100;
            if (t < 0)
                t = 0;

            rsi = t;
        }
        
        return rsi;
    }
}
