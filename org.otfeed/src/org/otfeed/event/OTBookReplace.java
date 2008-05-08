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
 * Look up the order, and change its size.
 * If the order is not there, create it, setting the symbol price and size.
 */
public final class OTBookReplace implements Comparable<OTBookReplace>, Serializable {

	private static final long serialVersionUID = 6500191194223899665L;

	private Date timestamp;
    private String reference;
    private double price;
    private int size;
    private TradeSideEnum side;

    /**
     * Default constructor.
     */
    public OTBookReplace() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param reference Order reference.
     * @param price Price.
     * @param size Number of shares.
     * @param side Side: B = Buy, S = Sell.
     */
    public OTBookReplace(Date timestamp, String reference, 
    		double price, int size, TradeSideEnum side) {
        this.timestamp = timestamp;
        this.reference = reference;
        this.price = price;
        this.size = size;
        this.side = side;
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
     * @return Order reference; identifies the order for subsequent events.
     */
    public String getOrderRef() {
        return reference;
    }

    /**
     * Sets Order reference.
     * @param reference Order reference.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * 
     * @return Price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets price.
     * @param price Price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * 
     * @return Number of shares.
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets number of shares.
     * @param size Number of shares.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 
     * @return Side: B = Buy, S = Sell.
     */
    public TradeSideEnum getSide() {
        return side;
    }

    /**
     * Sets side: B = Buy, S = Sell.
     * @param side Side: B = Buy, S = Sell.
     */
    public void setSide(TradeSideEnum side) {
        this.side = side;
    }

    @Override
	public String toString() {
        return "OTBookReplace: timestamp=" + timestamp + ", orderRef=" + reference + ", size=" + size + ", sideindicator=" + side + ", price=" + price;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(reference) 
    		+ 5 * safeHashCode(price) 
    		+ 7 * safeHashCode(size) 
        	+ 11 * safeHashCode(side); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookReplace other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(reference, other.reference)) != 0) {
			return rc;
		}

		if((rc = safeCompare(price, other.price)) != 0) {
			return rc;
		}

		if((rc = safeCompare(size, other.size)) != 0) {
			return rc;
		}

		if((rc = safeCompare(side, other.side)) != 0) {
			return rc;
		}

		return 0;
	}
}