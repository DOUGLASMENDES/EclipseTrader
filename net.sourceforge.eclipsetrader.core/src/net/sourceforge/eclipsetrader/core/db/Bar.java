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

package net.sourceforge.eclipsetrader.core.db;

import java.util.Date;

/**
 * Price bar data.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class Bar extends PersistentObject
{
    Date date;
    double open;
    double high = -99999999;
    double low = 99999999;
    double close;
    long volume;

    /**
     * Constructs an empty bar object.
     */
    public Bar()
    {
    }
    
    /**
     * Constructs an empty bar object with the specified id.
     * 
     * @param id the unique id
     */
    public Bar(Integer id)
    {
        super(id);
    }

    /**
     * Constructs a bar object with the data from the specified object.
     * 
     * @param bar the object to copy the data from.
     */
    public Bar(Bar bar)
    {
        this.date = bar.date;
        this.open = bar.open;
        this.high = bar.high;
        this.low = bar.low;
        this.close = bar.close;
        this.volume = bar.volume;
    }
    
    /**
     * Constructs a bar object with the specified data.
     * 
     * @param open the price at open.
     * @param high the highest price.
     * @param low the lowest price.
     * @param close the price at close.
     */
    public Bar(double open, double high, double low, double close)
    {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
    
    /**
     * Constructs a bar object with the specified data.
     * 
     * @param open the price at open.
     * @param high the highest price.
     * @param low the lowest price.
     * @param close the price at close.
     * @param volume the exchange volume.
     */
    public Bar(double open, double high, double low, double close, long volume)
    {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    
    /**
     * Constructs a bar object with the specified data.
     *
     * @param date the date that this object is referring to.
     * @param open the price at open.
     * @param high the highest price.
     * @param low the lowest price.
     * @param close the price at close.
     * @param volume the exchange volume.
     */
    public Bar(Date date, double open, double high, double low, double close, long volume)
    {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /**
     * Returns the price at bar's close.
     * 
     * @return the close price.
     */
    public double getClose()
    {
        return close;
    }

    public void setClose(double close)
    {
        this.close = close;
    }

    /**
     * Returns the bar's date and time.
     * 
     * @return the date and time.
     */
    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * Returns the highest price.
     * 
     * @return the highest price.
     */
    public double getHigh()
    {
        return high;
    }

    public void setHigh(double high)
    {
        this.high = high;
    }

    /**
     * Returns the lowest price.
     * 
     * @return the lowest price.
     */
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
    
    /**
     * Updates this object with the specified data.
     *
     * @param open the price at open.
     * @param high the highest price.
     * @param low the lowest price.
     * @param close the price at close.
     * @param volume the exchange volume.
     */
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
    
    /**
     * Updates this object with the data from the specified object.
     * 
     * @param bar the object to update the data from.
     */
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
