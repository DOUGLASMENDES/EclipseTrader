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

import java.util.Currency;
import java.util.List;

import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.core.internal.CObservable;


/**
 */
public class Security extends PersistentObject
{
    private String code = "";
    private String description = "";
    private Currency currency;
    private Feed quoteFeed;
    private Feed level2Feed;
    private Feed historyFeed;
    private ObservableList history;
    private ObservableList intradayHistory;
    private Quote quote;
    private Level2Bid level2Bid;
    private Level2Ask level2Ask;
    private Double close;
    private Double open;
    private Double low;
    private Double high;
    private CObservable quoteMonitor = new CObservable();
    private CObservable level2Monitor = new CObservable();

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

    public Feed getHistoryFeed()
    {
        return historyFeed;
    }

    public void setHistoryFeed(Feed historyFeed)
    {
        if (this.historyFeed != null && !this.historyFeed.equals(historyFeed))
            setChanged();
        if (this.historyFeed == null && historyFeed != null)
            setChanged();
        this.historyFeed = historyFeed;
    }

    public Feed getQuoteFeed()
    {
        return quoteFeed;
    }

    public void setQuoteFeed(Feed quoteFeed)
    {
        if (this.quoteFeed != null && !this.quoteFeed.equals(quoteFeed))
            setChanged();
        if (this.quoteFeed == null && quoteFeed != null)
            setChanged();
        this.quoteFeed = quoteFeed;
    }

    public Feed getLevel2Feed()
    {
        return level2Feed;
    }

    public void setLevel2Feed(Feed level2Feed)
    {
        if (this.level2Feed != null && !this.level2Feed.equals(level2Feed))
            setChanged();
        if (this.level2Feed == null && level2Feed != null)
            setChanged();
        this.level2Feed = level2Feed;
    }

    public List getHistory()
    {
        if (history == null)
        {
            history = new ObservableList(getRepository().loadHistory(getId()));
            history.addCollectionObserver(new ICollectionObserver() {
                public void itemAdded(Object o)
                {
                    setChanged();
                }

                public void itemRemoved(Object o)
                {
                    setChanged();
                }
            });
        }
        return history;
    }

    public List getIntradayHistory()
    {
        if (intradayHistory == null)
        {
            intradayHistory = new ObservableList(getRepository().loadIntradayHistory(getId()));
            intradayHistory.addCollectionObserver(new ICollectionObserver() {
                public void itemAdded(Object o)
                {
                    setChanged();
                }

                public void itemRemoved(Object o)
                {
                    setChanged();
                }
            });
        }
        return intradayHistory;
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
        quoteMonitor.notifyObservers();
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

        quoteMonitor.notifyObservers();
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

        quoteMonitor.notifyObservers();
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

        quoteMonitor.notifyObservers();
    }

    public Double getHigh()
    {
        return high;
    }

    public void setHigh(Double high)
    {
        if (this.high != null && !this.high.equals(open))
            quoteMonitor.setChanged();
        else if (this.high == null && this.high != null)
            quoteMonitor.setChanged();
        this.high = high;
        quoteMonitor.notifyObservers();
    }

    public Double getLow()
    {
        return low;
    }

    public void setLow(Double low)
    {
        if (this.low != null && !this.low.equals(open))
            quoteMonitor.setChanged();
        else if (this.low == null && this.low != null)
            quoteMonitor.setChanged();
        this.low = low;
        quoteMonitor.notifyObservers();
    }

    public Double getOpen()
    {
        return open;
    }

    public void setOpen(Double open)
    {
        if (this.open != null && !this.open.equals(open))
            quoteMonitor.setChanged();
        else if (this.open == null && this.open != null)
            quoteMonitor.setChanged();
        this.open = open;
        quoteMonitor.notifyObservers();
    }

    public Double getClose()
    {
        return close;
    }

    public void setClose(Double close)
    {
        if (this.close != null && !this.close.equals(open))
            quoteMonitor.setChanged();
        else if (this.close == null && this.close != null)
            quoteMonitor.setChanged();
        this.close = close;
        quoteMonitor.notifyObservers();
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
    
    public class Feed
    {
        private String id = "";
        private String symbol = "";
        
        public Feed()
        {
        }
        
        public String getId()
        {
            return id;
        }
        
        public void setId(String id)
        {
            this.id = id;
            setChanged();
        }
        
        public String getSymbol()
        {
            return symbol;
        }
        
        public void setSymbol(String symbol)
        {
            this.symbol = symbol;
            setChanged();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            if (obj == null || !(obj instanceof Feed))
                return false;
            Feed that = (Feed)obj;
            return this.id.equals(that.id) && this.symbol.equals(that.symbol);
        }
    }
}
