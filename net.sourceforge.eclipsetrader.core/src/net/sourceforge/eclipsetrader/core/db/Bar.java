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

import java.util.Date;


/**
 */
public class Bar extends PersistentObject
{
    private Date date;
    private double open;
    private double high = -99999999;
    private double low = 99999999;
    private double close;
    private long volume;
    
    public Bar()
    {
    }
    
    public Bar(Integer id)
    {
        super(id);
    }
    
    public Bar(Bar bar)
    {
        this.date = bar.date;
        this.open = bar.open;
        this.high = bar.high;
        this.low = bar.low;
        this.close = bar.close;
        this.volume = bar.volume;
    }
    
    public Bar(double open, double high, double low, double close)
    {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
    
    public Bar(double open, double high, double low, double close, long volume)
    {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    
    public Bar(Date date, double open, double high, double low, double close, long volume)
    {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public double getClose()
    {
        return close;
    }

    public void setClose(double close)
    {
        this.close = close;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public double getHigh()
    {
        return high;
    }

    public void setHigh(double high)
    {
        this.high = high;
    }

    public double getLow()
    {
        return low;
    }

    public void setLow(double low)
    {
        this.low = low;
    }

    public double getOpen()
    {
        return open;
    }

    public void setOpen(double open)
    {
        this.open = open;
    }

    public long getVolume()
    {
        return volume;
    }

    public void setVolume(long volume)
    {
        this.volume = volume;
    }
    
    public void update(double open, double high, double low, double close, long volume)
    {
        this.open = open;
        if (high > this.high)
            this.high = high;
        if (low < this.low)
            this.low = low;
        this.close = close;
        this.volume += volume;
    }
    
    public void update(Bar bar)
    {
        if (bar.high > this.high)
            this.high = bar.high;
        if (bar.low < this.low)
            this.low = bar.low;
        this.close = bar.close;
        this.volume += bar.volume;
    }
}
