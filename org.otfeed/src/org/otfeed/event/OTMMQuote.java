/**
 * Copyright 2007 Mike Kroutikov.
 *
 * This program is free software; you can redistribute it and/or modify
 *   it under the terms of the Lesser GNU General Public License as 
 *   published by the Free Software Foundation; either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Lesser GNU General Public License for more details.
 *
 *   You should have received a copy of the Lesser GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Derived from code developed by Opentick Corporation, http://www.opentick.com.
 */

package org.otfeed.event;

import static org.otfeed.event.IdentityUtil.equalsTo;
import static org.otfeed.event.IdentityUtil.safeCompare;
import static org.otfeed.event.IdentityUtil.safeHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * This class provides the level II quote provided by a market maker for NASDAQ equities, or regional quotes for listed stocks.
 * The market maker ID (MMID) identifies which specific market participant has changed his quote.
 * In the case of NASDAQ, the MMID is a string identifying the market maker. From SIAC and OPRA,
 * the quotes are from regional exchanges, and the MMID identifies the exchange that the regional quote comes from.
 */
public final class OTMMQuote implements Comparable<OTMMQuote>, Serializable {
 
	private static final long serialVersionUID = -4591121602837458929L;
	
	private Date timestamp;
    private int bidSize;
    private int askSize;
    private double bidPrice;
    private double askPrice;
    private String MMID;
    private char indicator;
    private String exchange;
    private String symbol;

    /**
     * Default constructor.
     */
    public OTMMQuote() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param bidSize Number of round lots in the bid.
     * @param bidPrice Bid price.
     * @param askSize Number of round lots in the ask.
     * @param askPrice Ask price.
     * @param MMID Market Maker ID.
     * @param indicator Indicator.
     * @param exchange
     * @param symbol Symbol code.
     */
    public OTMMQuote(Date timestamp, 
    		int bidSize, 
    		double bidPrice, 
    		int askSize, 
    		double askPrice, 
    		String MMID, 
    		char indicator, 
    		String exchange, 
    		String symbol) {
        this.timestamp = timestamp;
        this.bidSize = bidSize;
        this.bidPrice = bidPrice;
        this.askSize = askSize;
        this.askPrice = askPrice;
        this.MMID = MMID;
        this.indicator = indicator;
        this.exchange = exchange;
        this.symbol = symbol;
    }

     /**
     * 
     * @return Time when the event occurred.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets time of the event.
     * @param timestamp Time when the event occurred.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * @return Number of round lots in the bid.
     */
    public int getBidSize() {
        return bidSize;
    }

    /**
     * Sets ask price.
     * @param bidSize Ask price.
     */
    public void setBidSize(int bidSize) {
        this.bidSize = bidSize;
    }

    /**
     *
     * @return Bid price.
     */
    public double getBidPrice() {
        return bidPrice;
    }

    /**
     * Sets bid price.
     * @param bidPrice Bid price.
     */
    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    /**
     *
     * @return Number of round lots in the ask.
     */
    public int getAskSize() {
        return askSize;
    }

    /**
     * Sets number of round lots in the ask.
     * @param askSize Number of round lots in the ask.
     */
    public void setAskSize(int askSize) {
        this.askSize = askSize;
    }

    /**
     *
     * @return Ask price.
     */
    public double getAskPrice() {
        return askPrice;
    }

    /**
     * Sets ask price.
     * @param askPrice Ask price.
     */
    public void setAskPrice(double askPrice) {
        this.askPrice = askPrice;
    }

    /**
     *
     * @return Market Maker ID.
     */
    public String getMMID() {
        return MMID;
    }

    /**
     * Sets market maker id.
     * @param MMID Market Maker ID.
     */
    public void setMMID(String MMID) {
        this.MMID = MMID;
    }

    /**
     * 
     * @return Indicator.
     */
    public char getIndicator() {
        return indicator;
    }

    /**
     * Sets indicator.
     * @param indicator Indicator.
     */
    public void setIndicator(char indicator) {
        this.indicator = indicator;
    }

    /**
     * 
     * @return Exchange code.
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * Sets exchange code.
     * @param exchange Exchange code.
     */
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * 
     * @return Symbol code; this field is provided for option chains only, because there can be more than one symbol appropriate to the requested underlyer and expiry date.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Symbol code.
     * @param symbol Symbol code.
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
	public String toString() {
        return "OTMMQuote: timestamp=" + timestamp + ", askprice=" + askPrice + ", bidprice=" + bidPrice + ", asksize=" + askSize + ", bidsize=" + bidSize + ", mmid=" + MMID + ", indicator=" + indicator + ", exchange=" + exchange + ", symbol=" + symbol;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
    		+ 3 * safeHashCode(bidSize) 
    		+ 5 * safeHashCode(askSize) 
    		+ 7 * safeHashCode(bidPrice)
    		+ 11 * safeHashCode(askPrice)
    		+ 13 * safeHashCode(MMID)
    		+ 17 * safeHashCode(indicator)
    		+ 29 * safeHashCode(exchange)
    		+ 31 * safeHashCode(symbol);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTMMQuote other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(bidSize, other.bidSize)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(askSize, other.askSize)) != 0) {
			return rc;
		}

		if((rc = safeCompare(bidPrice, other.bidPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(askPrice, other.askPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(MMID, other.MMID)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(indicator, other.indicator)) != 0) {
			return rc;
		}

		if((rc = safeCompare(exchange, other.exchange)) != 0) {
			return rc;
		}

		if((rc = safeCompare(symbol, other.symbol)) != 0) {
			return rc;
		}

		return 0;
	}
}