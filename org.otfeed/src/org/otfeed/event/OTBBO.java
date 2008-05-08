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
 * The best bid or offer - this is half of a quote.
 * These get sent when only one side is changing.
 */
public final class OTBBO implements Comparable<OTBBO>, Serializable {

	private static final long serialVersionUID = -7239387622141172391L;

	private Date   timestamp;
    private double price;
    private int    size;
    private TradeSideEnum side;
    private String exchange;
    private String symbol;

    /**
     * Default constructor.
     */
    public OTBBO() { }

    /**
     * Constructor.
     *
     * @param timestamp time of the event.
     * @param price     price.
     * @param size      size.
     * @param side      side.
     * @param exchange  exchange code.
     * @param symbol    symbol code.
     */
    public OTBBO(Date timestamp, double price, int size,
                 TradeSideEnum side, String exchange, String symbol) {
        this.timestamp = timestamp;
        this.price = price;
        this.size = size;
        this.side = side;
        this.exchange = exchange;
        this.symbol = symbol;
    }

    /**
     * @return timestamp when the event occurred.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets time of the event.
     *
     * @param timestamp Time when the event occurred.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return Price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets price.
     *
     * @param price Price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return Size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets size.
     *
     * @param size Size.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return Side: B = Bid (buyer side). A = Ask (seller side).
     */
    public TradeSideEnum getSide() {
        return side;
    }

    /**
     * Sets side.
     *
     * @param side Side: B = Bid (buyer side). A = Ask (seller side).
     */
    public void setSide(TradeSideEnum side) {
        this.side = side;
    }

    /**
     * @return Exchange code.
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * Sets exchange code.
     *
     * @param exchange Exchange code.
     */
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    /**
     * Sets symbol code.
     *
     * @param symbol Symbol code.
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return Symbol code; this field is provided for option chains only, because there can be more than one symbol appropriate to the requested underlyer and expiry date.
     */
    public String getSymbol() {
        return this.symbol;
    }

    @Override
	public String toString() {
        return "OTBBO: timestamp=" + timestamp + ", size="
                + size + ", side=" + side + ", price=" + price + ", exchange="
                + exchange + ", symbol=" + symbol;

    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
    		+ 3 * safeHashCode(size)
    		+ 5 * safeHashCode(price)
    		+ 7 * safeHashCode(side)
    		+ 29 * safeHashCode(exchange)
    		+ 31 * safeHashCode(symbol);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBBO other) {
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
		
		if((rc = safeCompare(side, other.side)) != 0) {
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
