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

package net.sourceforge.eclipsetrader.trading.alerts;

import java.text.NumberFormat;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;

public class PriceAlert extends AlertPlugin
{
    public static final int DAILY = BarData.INTERVAL_DAILY;
    public static final int WEEKLY = BarData.INTERVAL_WEEKLY;
    public static final int MONTHLY = BarData.INTERVAL_MONTHLY;
    public static final int HIGH = 0;
    public static final int LOW = 1;
    private int period = 2;
    private int interval = WEEKLY;
    private int type = HIGH;
    private NumberFormat priceFormatter = NumberFormat.getInstance();

    public PriceAlert()
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
        String value = (String)params.get("period");
        if (value != null)
            period = Integer.parseInt(value);
        value = (String)params.get("interval");
        if (value != null)
            interval = Integer.parseInt(value);
        value = (String)params.get("type");
        if (value != null)
            type = Integer.parseInt(value);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#getDescription()
     */
    public String getDescription()
    {
        String s = "Price reaches a new " + String.valueOf(period);
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
        s += type == HIGH ? " high" : " low";
        return s;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPlugin#apply()
     */
    public boolean apply()
    {
        BarData barData = new BarData(getSecurity().getHistory().getList());
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
        
        if (barData.size() < period || barData.size() == 0)
            return false;

        boolean result = false;
        double value = 0;
        
        if (type == HIGH)
        {
            if (getSecurity().getHigh() == null)
                return false;
            double max = 0;
            for (int i = barData.size() - period; i < barData.size(); i++)
                max = Math.max(max, barData.getHigh(i));
            value = getSecurity().getHigh().doubleValue();
            result = value > max;
        }
        else if (type == LOW)
        {
            if (getSecurity().getLow() == null)
                return false;
            double min = 999999;
            for (int i = barData.size() - period; i < barData.size(); i++)
                min = Math.min(min, barData.getLow(i));
            value = getSecurity().getLow().doubleValue();
            result = value < min;
        }
        
        if (result)
        {
            String s = "Price reached a new " + String.valueOf(period);
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
            s += type == HIGH ? " high" : " low";
            s += " at " + priceFormatter.format(value);
            fireEvent(s);
        }
        
        return result;
    }

}
