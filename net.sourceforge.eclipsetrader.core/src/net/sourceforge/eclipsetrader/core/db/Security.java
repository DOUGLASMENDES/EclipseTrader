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

import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.feed.FeedSource;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.core.db.feed.TradeSource;
import net.sourceforge.eclipsetrader.core.internal.CObservable;


/**
 */
public class Security extends PersistentObject
{
    public static final int SUN = 0x0001;
    public static final int MON = 0x0002;
    public static final int TUE = 0x0004;
    public static final int WED = 0x0008;
    public static final int THU = 0x0010;
    public static final int FRI = 0x0020;
    public static final int SAT = 0x0040;
    String code = "";
    String description = "";
    Currency currency;
    SecurityGroup group;
    FeedSource quoteFeed;
    FeedSource level2Feed;
    FeedSource historyFeed;
    TradeSource tradeSource;
    Quote quote;
    Level2Bid level2Bid;
    Level2Ask level2Ask;
    Double close;
    Double open;
    Double low;
    Double high;
    CObservable quoteMonitor = new CObservable();
    CObservable level2Monitor = new CObservable();
    boolean enableDataCollector = false;
    int beginTime = 0;
    int endTime = 0;
    int weekDays = MON|TUE|WED|THU|FRI;
    int keepDays = 1;
    List splits = new ArrayList();
    List dividends = new ArrayList();
    String comment = "";

    public Security()
    {
    }

    public Security(Integer id)
    {
        super(id);
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        if (!this.code.equals(code))
            setChanged();
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        if (!this.description.equals(description))
            setChanged();
        this.description = description;
    }

    public Currency getCurrency()
    {
        if (currency == null && group != null)
            return group.getCurrency();
        return currency;
    }

    public void setCurrency(Currency currency)
    {
        if (currency != null && !currency.equals(this.currency))
            setChanged();
        else if (currency == null && this.currency != null)
            setChanged();
        this.currency = currency;
    }

    public SecurityGroup getGroup()
    {
        return group;
    }

    public void setGroup(SecurityGroup group)
    {
        if (group != null && !group.equals(this.group))
            setChanged();
        else if (group == null && this.group != null)
            setChanged();
        this.group = group;
    }

    public FeedSource getHistoryFeed()
    {
        return historyFeed;
    }

    public void setHistoryFeed(FeedSource historyFeed)
    {
        if (this.historyFeed != null && !this.historyFeed.equals(historyFeed))
            setChanged();
        if (this.historyFeed == null && historyFeed != null)
            setChanged();
        this.historyFeed = historyFeed;
    }

    public FeedSource getQuoteFeed()
    {
        return quoteFeed;
    }

    public void setQuoteFeed(FeedSource quoteFeed)
    {
        if (this.quoteFeed != null && !this.quoteFeed.equals(quoteFeed))
            setChanged();
        if (this.quoteFeed == null && quoteFeed != null)
            setChanged();
        this.quoteFeed = quoteFeed;
    }

    public FeedSource getLevel2Feed()
    {
        return level2Feed;
    }

    public void setLevel2Feed(FeedSource level2Feed)
    {
        if (this.level2Feed != null && !this.level2Feed.equals(level2Feed))
            setChanged();
        if (this.level2Feed == null && level2Feed != null)
            setChanged();
        this.level2Feed = level2Feed;
    }

    public TradeSource getTradeSource()
    {
        return tradeSource;
    }

    public void setTradeSource(TradeSource tradeSource)
    {
        if (this.tradeSource != null && !this.tradeSource.equals(tradeSource))
            setChanged();
        if (this.tradeSource == null && tradeSource != null)
            setChanged();
        this.tradeSource = tradeSource;
    }

    public History getHistory()
    {
        return (History)getRepository().load(History.class, getId());
    }
    
    public History getAdjustedHistory()
    {
        History list = new History();

        for (Iterator iter = getHistory().iterator(); iter.hasNext(); )
        {
            Bar bar = new Bar((Bar)iter.next());
            double factor = 1.0;
            for (Iterator iter2 = getSplits().iterator(); iter2.hasNext(); )
            {
                Split split = (Split)iter2.next();
                if (bar.getDate().before(split.getDate()))
                    factor *= (double)split.getToQuantity() / (double)split.getFromQuantity();
            }
            double dividends = 0.0;
            for (Iterator iter2 = getDividends().iterator(); iter2.hasNext(); )
            {
                Dividend dividend = (Dividend)iter2.next();
                if (bar.getDate().before(dividend.getDate()))
                    dividends += dividend.getValue();
            }

            bar.setOpen((bar.getOpen() * factor) - dividends);
            bar.setHigh((bar.getHigh() * factor) - dividends);
            bar.setLow((bar.getLow() * factor) - dividends);
            bar.setClose((bar.getClose() * factor) - dividends);
            bar.setVolume((long)(bar.getVolume() / factor));
            list.add(bar);
        }
        
        list.clearChanged();
        
        return list;
    }

    public IntradayHistory getIntradayHistory()
    {
        return (IntradayHistory)getRepository().load(IntradayHistory.class, getId());
    }

    public Quote getQuote()
    {
        return quote;
    }

    public void setQuote(Quote quote)
    {
        if (this.quote != null && !this.quote.equals(quote))
            quoteMonitor.setChanged();
        else if (this.quote == null && quote != null)
            quoteMonitor.setChanged();
        this.quote = quote;
        quoteMonitor.notifyObservers(this);
    }

    public void setQuote(Quote quote, Double open, Double high, Double low, Double close)
    {
        if (this.quote != null && !this.quote.equals(quote))
            quoteMonitor.setChanged();
        else if (this.quote == null && quote != null)
            quoteMonitor.setChanged();
        this.quote = quote;

        if (this.open != null && !this.open.equals(open))
            quoteMonitor.setChanged();
        else if (this.open == null && open != null)
            quoteMonitor.setChanged();
        this.open = open;
        
        if (this.high != null && !this.high.equals(high))
            quoteMonitor.setChanged();
        else if (this.high == null && high != null)
            quoteMonitor.setChanged();
        this.high = high;
        
        if (this.low != null && !this.low.equals(low))
            quoteMonitor.setChanged();
        else if (this.low == null && low != null)
            quoteMonitor.setChanged();
        this.low = low;
        
        if (this.close != null && !this.close.equals(close))
            quoteMonitor.setChanged();
        else if (this.close == null && close != null)
            quoteMonitor.setChanged();
        this.close = close;

        quoteMonitor.notifyObservers(this);
    }

    public void setQuote(Quote quote, Double high, Double low)
    {
        if (this.quote != null && !this.quote.equals(quote))
            quoteMonitor.setChanged();
        else if (this.quote == null && quote != null)
            quoteMonitor.setChanged();
        this.quote = quote;

        if (this.high != null && !this.high.equals(high))
            quoteMonitor.setChanged();
        else if (this.high == null && high != null)
            quoteMonitor.setChanged();
        this.high = high;
        
        if (this.low != null && !this.low.equals(low))
            quoteMonitor.setChanged();
        else if (this.low == null && low != null)
            quoteMonitor.setChanged();
        this.low = low;

        quoteMonitor.notifyObservers(this);
    }

    public void setQuote(Quote quote, Double open)
    {
        if (this.quote != null && !this.quote.equals(quote))
            quoteMonitor.setChanged();
        else if (this.quote == null && quote != null)
            quoteMonitor.setChanged();
        this.quote = quote;

        if (this.open != null && !this.open.equals(open))
            quoteMonitor.setChanged();
        else if (this.open == null && open != null)
            quoteMonitor.setChanged();
        this.open = open;

        quoteMonitor.notifyObservers(this);
    }

    public Double getHigh()
    {
        return high;
    }

    public void setHigh(Double high)
    {
        if (this.high != null && !this.high.equals(high))
            quoteMonitor.setChanged();
        else if (this.high == null && high != null)
            quoteMonitor.setChanged();
        this.high = high;
        quoteMonitor.notifyObservers(this);
    }

    public Double getLow()
    {
        return low;
    }

    public void setLow(Double low)
    {
        if (this.low != null && !this.low.equals(low))
            quoteMonitor.setChanged();
        else if (this.low == null && low != null)
            quoteMonitor.setChanged();
        this.low = low;
        quoteMonitor.notifyObservers(this);
    }

    public Double getOpen()
    {
        return open;
    }

    public void setOpen(Double open)
    {
        if (this.open != null && !this.open.equals(open))
            quoteMonitor.setChanged();
        else if (this.open == null && open != null)
            quoteMonitor.setChanged();
        this.open = open;
        quoteMonitor.notifyObservers(this);
    }

    public Double getClose()
    {
        return close;
    }

    public void setClose(Double close)
    {
        if (this.close != null && !this.close.equals(close))
            quoteMonitor.setChanged();
        else if (this.close == null && close != null)
            quoteMonitor.setChanged();
        this.close = close;
        quoteMonitor.notifyObservers(this);
    }

    public Level2 getLevel2Bid()
    {
        return level2Bid;
    }

    public void setLevel2(Level2Bid level2Bid)
    {
        this.level2Bid = level2Bid;
        level2Monitor.setChanged();
        level2Monitor.notifyObservers(this.level2Bid);
    }

    public Level2 getLevel2Ask()
    {
        return level2Ask;
    }

    public void setLevel2(Level2Ask level2Ask)
    {
        this.level2Ask = level2Ask;
        level2Monitor.setChanged();
        level2Monitor.notifyObservers(this.level2Ask);
    }

    public void setLevel2(Level2Bid level2Bid, Level2Ask level2Ask)
    {
        this.level2Bid = level2Bid;
        this.level2Ask = level2Ask;
        level2Monitor.setChanged();
        level2Monitor.notifyObservers();
    }

    public CObservable getLevel2Monitor()
    {
        return level2Monitor;
    }

    public CObservable getQuoteMonitor()
    {
        return quoteMonitor;
    }

    public int getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(int beginTime)
    {
        this.beginTime = beginTime;
    }

    public boolean isEnableDataCollector()
    {
        return enableDataCollector;
    }

    public void setEnableDataCollector(boolean enableDataCollector)
    {
        this.enableDataCollector = enableDataCollector;
    }

    public int getEndTime()
    {
        return endTime;
    }

    public void setEndTime(int endTime)
    {
        this.endTime = endTime;
    }

    public int getWeekDays()
    {
        return weekDays;
    }

    public void setWeekDays(int weekDays)
    {
        this.weekDays = weekDays;
    }

    public int getKeepDays()
    {
        return keepDays;
    }

    public void setKeepDays(int keepDays)
    {
        this.keepDays = keepDays;
    }

    public List getDividends()
    {
        return dividends;
    }

    public void setDividends(List dividends)
    {
        this.dividends = dividends;
    }

    public List getSplits()
    {
        return splits;
    }

    public void setSplits(List splits)
    {
        this.splits = splits;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getCode() + " - " + getDescription();
    }
}
