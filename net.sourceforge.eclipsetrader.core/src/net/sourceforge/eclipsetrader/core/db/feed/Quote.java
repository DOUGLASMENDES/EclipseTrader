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

package net.sourceforge.eclipsetrader.core.db.feed;

import java.util.Date;

/**
 * Price quote snapshot.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class Quote
{
    private Date date;
    private double last = 0;
    private double bid = 0;
    private int bidSize = 0;
    private double ask = 0;
    private int askSize = 0;
    private long volume = 0;
    
    public Quote()
    {
    }

    /**
     * Build a new instance that is the exact copy of the given instance.
     * 
     * @param quote the instance to copy
     */
    public Quote(Quote quote)
    {
        if (quote != null)
        {
            this.date = quote.date;
            this.last = quote.last;
            this.bid = quote.bid;
            this.bidSize = quote.bidSize;
            this.ask = quote.ask;
            this.askSize = quote.askSize;
            this.volume = quote.volume;
        }
    }

    /**
     * Build a new instance giving the last, bid and ask prices.
     * 
     * @param last the last price
     * @param bid the bid price
     * @param ask the ask price
     */
    public Quote(double last, double bid, double ask)
    {
        this.last = last;
        this.bid = bid;
        this.ask = ask;
    }

    /**
     * Build a new instance giving the last update date, last, bid and ask prices.
     * 
     * @param date the last update date
     * @param last the last price
     * @param bid the bid price
     * @param ask the ask price
     */
    public Quote(Date date, double last, double bid, double ask)
    {
        this.date = date;
        this.last = last;
        this.bid = bid;
        this.ask = ask;
    }

    /**
     * Returns the date when the quote was last updated.
     * 
     * @return the last update date/time
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Sets the date when the quote was last updated.
     * 
     * @param date the last update date/time
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    public double getAsk()
    {
        return ask;
    }

    public void setAsk(double ask)
    {
        this.ask = ask;
    }

    public double getBid()
    {
        return bid;
    }

    public void setBid(double bid)
    {
        this.bid = bid;
    }

    public double getLast()
    {
        return last;
    }

    public void setLast(double last)
    {
        this.last = last;
    }

    public int getAskSize()
    {
        return askSize;
    }

    public void setAskSize(int askSize)
    {
        this.askSize = askSize;
    }

    public int getBidSize()
    {
        return bidSize;
    }

    public void setBidSize(int bidSize)
    {
        this.bidSize = bidSize;
    }

    /**
     * Returns the cumulative volume exchange.
     * 
     * @return the volume value
     */
    public long getVolume()
    {
        return volume;
    }

    /**
     * Sets the cumulative volume exchange.
     * 
     * @param volume the volume value
     */
    public void setVolume(long volume)
    {
        this.volume = volume;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>Quote objects are equal if all fields, excluding the date field, are equals.</p>
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Quote))
            return false;
        Quote that = (Quote)obj;
        return (this.getLast() == that.getLast() && 
                this.getBid() == that.getBid() &&
                this.getAsk() == that.getAsk() &&
                this.getBidSize() == that.getBidSize() &&
                this.getAskSize() == that.getAskSize() &&
                this.getVolume() == that.getVolume());
    }
}
