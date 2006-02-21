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

package net.sourceforge.eclipsetrader.core.db.feed;

import java.util.Date;

public class Quote
{
    private Date date;
    private double last = 0;
    private double bid = 0;
    private int bidSize = 0;
    private double ask = 0;
    private int askSize = 0;
    private int volume = 0;
    
    public Quote()
    {
    }

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

    public Quote(double last, double bid, double ask)
    {
        this.last = last;
        this.bid = bid;
        this.ask = ask;
    }

    public Date getDate()
    {
        return date;
    }

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

    public int getVolume()
    {
        return volume;
    }

    public void setVolume(int volume)
    {
        this.volume = volume;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
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
