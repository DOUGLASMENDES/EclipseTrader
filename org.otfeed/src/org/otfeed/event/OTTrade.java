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
import static org.otfeed.event.IdentityUtil.enumSetHashCode;
import static org.otfeed.event.IdentityUtil.compareEnumSet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Provides either real-time or historical trade information.
 * This event indicates that shares were sold and purchased 
 * (excepting some tracing messages).
 * OTTrade stores price, size and other information about the deal.
 */
public final class OTTrade implements Comparable<OTTrade>, Serializable {

	private static final long serialVersionUID = -4048718961928459556L;
	
	private Date timestamp;
    private int size;
    private double price;
    private long volume;
    private int sequenceNumber;
    private char indicator;
    private char tickIndicator;
    private Set<TradePropertyEnum> properties = new HashSet<TradePropertyEnum>();
    private String exchange;
    private String symbol;

    /**
     * Default constructor.
     */
    public OTTrade() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param size Number of shares traded in this transaction.
     * @param price Trade price.
     * @param volume 	Accumulated volume for the trading day.
     * @param sequenceNumber Sequence number of trade.
     * @param indicator 	Indicator.
     * @param tickIndicator Tick indicator: D - Down, U - Up, N - Not provided.
     * @param properties Trade properties.
     * @param exchange Exchange code.
     * @param symbol Symbol code.
     */
    public OTTrade(Date timestamp, int size, double price, 
    		long volume, int sequenceNumber, 
    		char indicator, char tickIndicator, 
    		Set<TradePropertyEnum> properties, 
    		String exchange, String symbol) {
        this.timestamp = timestamp;
        this.size = size;
        this.price = price;
        this.volume = volume;
        this.sequenceNumber = sequenceNumber;
        this.indicator = indicator;
        this.tickIndicator = tickIndicator;
        this.exchange = exchange;
        this.symbol = symbol;
        this.properties = properties;
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
     * @return Number of shares traded in this transaction.
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets number of shares traded in this transaction.
     * @param size 	Number of shares traded in this transaction.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     *
     * @return Trade price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets trade price.
     * @param price Trade price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * 
     * @return Accumulated volume for the trading day.
     */
    public long getVolume() {
        return volume;
    }

    /**
     * Sets accumulated volume for the trading day.
     * @param volume Accumulated volume for the trading day.
     */
    public void setVolume(long volume) {
        this.volume = volume;
    }

    /**
     * 
     * @return     Sequence number of trade; used to correct trades in cancellations.
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets sequence number of trade.
     * @param sequenceNumber Sequence number of trade.
     */
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     *
     * @return     Indicator: See the Trade Indicators section for details.
     */
    public char getIndicator() {
        return indicator;
    }

    /**
     * Sets indicator.
     * @param indicator 	Indicator.
     */
    public void setIndicator(char indicator) {
        this.indicator = indicator;
    }

    /**
     * 
     * @return Tick indicator: D - Down, U - Up, N - Not provided.
     */
    public char getTickIndicator() {
        return tickIndicator;
    }

    /**
     * Sets tick indicator: D - Down, U - Up, N - Not provided.
     * @param tickIndicator Sets tick indicator: D - Down, U - Up, N - Not provided.
     */
    public void setTickIndicator(char tickIndicator) {
        this.tickIndicator = tickIndicator;
    }

    /**
     * 
     * @return Notes whether the trade provides the opening price for the day.
     */
    public boolean isOpen() {
        return properties.contains(TradePropertyEnum.OPEN);
    }

    /**
     * 
     * @return Notes whether the trade provides the high price for the day.
     */
    public boolean isHigh() {
        return properties.contains(TradePropertyEnum.HIGH);
    }

    /**
     * Returns set of boolean properties of this trade.
     * One can check these properties individually by
     * calling one of <code>isXXX</code> methods of this calsss.
     * 
     */
    public Set<TradePropertyEnum> getTradeProperties() {
    	return properties;
    }
    
    public void setTradeProperties(Set<TradePropertyEnum> p) {
        properties = p;
    } 

    /**
     *
     * @return Notes whether the trade provides the low price for the day.
     */
    public boolean isLow() {
        return properties.contains(TradePropertyEnum.LOW);
    }

    /**
     * 
     * @return     Notes whether the trade provides the closing price for the day.
     */
    public boolean isClose() {
        return properties.contains(TradePropertyEnum.CLOSE);
    }

    /**
     *
     * @return Notes whether the trade updates the last trade price.
     */
    public boolean isUpdateLast() {
        return properties.contains(TradePropertyEnum.UPDATE_LAST);
    }

    /**
     * 
     * @return Notes whether the volume is a replacement consolidated volume.
     */
    public boolean isUpdateVolume() {
        return properties.contains(TradePropertyEnum.UPDATE_VOLUME);
    }

    /**
     * 
     * @return Notes whether the trade is a cancel for a previous trade.
     */
    public boolean isCancel() {
        return properties.contains(TradePropertyEnum.CANCEL);
    }

    /**
     *
     * @return Notes whether the trade is from an ECN book.
     */
    public boolean isFromBook() {
        return properties.contains(TradePropertyEnum.FROM_BOOK);
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
     * Sets symbol code.
     * @param symbol Symbol code.
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
	public String toString() {
        String out = "OTTrade: timestamp=" 
        	+ timestamp + ", price=" + price 
        	+ ", size=" + size + ", volume=" + volume 
        	+ ", sequencenumber=" + sequenceNumber 
        	+ ", indicator=" + indicator 
        	+ ", tickindicator=" + tickIndicator 
        	+ ", isopen=" + isOpen() 
        	+ ", ishigh=" + isHigh() 
        	+ ", islow=" + isLow() 
        	+ ", isclose=" + isClose() 
        	+ ", isupdatelast=" + isUpdateLast() 
        	+ ", isupdatevolume=" + isUpdateVolume() 
        	+ ", iscancel=" + isCancel() 
        	+ ", isfrombook=" + isFromBook() 
        	+ ", exchange=" + exchange 
        	+ ", symbol=" + symbol;
        out += ", properties=";
        
        boolean first = true;
        for(TradePropertyEnum p : properties) {
        	if(first) { first = false; }
        	else      { out += "|"; }
        	out += p;
        }
        return out;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
    		+ 3 * safeHashCode(size)
    		+ 5 * safeHashCode(price)
    		+ 7 * safeHashCode(volume)
    		+ 11 * safeHashCode(sequenceNumber)
    		+ 17 * safeHashCode(indicator)
    		+ 19 * safeHashCode(tickIndicator)
    		+ 23 * enumSetHashCode(properties)
    		+ 29 * safeHashCode(exchange)
    		+ 31 * safeHashCode(symbol);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTTrade other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(size, other.size)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(price, other.price)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(volume, other.volume)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(sequenceNumber, other.sequenceNumber)) != 0) {
			return rc;
		}

		if((rc = safeCompare(indicator, other.indicator)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(tickIndicator, other.tickIndicator)) != 0) {
			return rc;
		}

		if((rc = compareEnumSet(TradePropertyEnum.class, properties, other.properties)) != 0) {
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