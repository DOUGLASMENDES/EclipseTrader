/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.directa.internal.core.repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;

@XmlRootElement(name = "identifier")
@XmlType(name = "org.eclipsetrader.directaworld.Identifier")
public class IdentifierType {

    @XmlAttribute(name = "symbol")
    private String symbol;

    @XmlElementRef
    private PriceDataType priceData;

    private IFeedIdentifier identifier;
    private ITrade trade;
    private IQuote quote;
    private ITodayOHL todayOHL;
    private ILastClose lastClose;

    protected IdentifierType() {
    }

    public IdentifierType(String symbol) {
        this.symbol = symbol;
    }

    @XmlTransient
    public String getSymbol() {
        return symbol;
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @XmlTransient
    public PriceDataType getPriceData() {
        if (priceData == null) {
            priceData = new PriceDataType();
        }
        return priceData;
    }

    public void setPriceData(PriceDataType data) {
        this.priceData = data;
    }

    @XmlTransient
    public IFeedIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(IFeedIdentifier identifier) {
        this.identifier = identifier;
    }

    @XmlTransient
    public ITrade getTrade() {
        if (trade == null) {
            getPriceData();
            trade = new Trade(priceData.getTime(), priceData.getLast(), null, priceData.getVolume());
        }
        return trade;
    }

    public void setTrade(ITrade price) {
        this.trade = price;
    }

    @XmlTransient
    public IQuote getQuote() {
        if (quote == null) {
            getPriceData();
            quote = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
        }
        return quote;
    }

    public void setQuote(IQuote quote) {
        this.quote = quote;
    }

    @XmlTransient
    public ITodayOHL getTodayOHL() {
        if (todayOHL == null) {
            getPriceData();
            if (priceData.getOpen() != null && priceData.getOpen() != 0.0 && priceData.getHigh() != null && priceData.getHigh() != 0.0 && priceData.getLow() != null && priceData.getLow() != 0.0) {
                todayOHL = new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow());
            }
        }
        return todayOHL;
    }

    public void setTodayOHL(ITodayOHL todayOHL) {
        this.todayOHL = todayOHL;
    }

    @XmlTransient
    public ILastClose getLastClose() {
        if (lastClose == null) {
            getPriceData();
            lastClose = new LastClose(priceData.getLastClose(), null);
        }
        return lastClose;
    }

    public void setLastClose(ILastClose lastClose) {
        this.lastClose = lastClose;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return symbol.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentifierType)) {
            return false;
        }
        return getSymbol().equals(((IdentifierType) obj).getSymbol());
    }
}
