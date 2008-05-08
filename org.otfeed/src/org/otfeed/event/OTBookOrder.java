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
 * A new order.
 */
public final class OTBookOrder implements Comparable<OTBookOrder>, Serializable {

	private static final long serialVersionUID = -6395276753169887976L;
	
	private Date timestamp;
    private String reference;
    private double price;
    private int size;
    private TradeSideEnum side;
    private boolean display;

    /**
     * Default constructor.
     */
    public OTBookOrder() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param reference Order reference.
     * @param price Price.
     * @param size Order size in number of shares.
     * @param side Side: B = Buy, S = Sell.
     * @param display Display.
     */
    public OTBookOrder(Date timestamp, String reference, 
    		double price, int size, 
    		TradeSideEnum side, 
    		boolean display) {
        this.timestamp = timestamp;
        this.reference = reference;
        this.price = price;
        this.size = size;
        this.side = side;
        this.display = display;
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
        return this.reference;
    }

    /**
     * Sets order reference.
     * @param reference Order reference.
     */
    public void setOrderRef(String reference) {
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
     * @return Size of the order.
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets order size in number of shares.
     * @param size Order size in number of shares.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 
     * @return Trade side (BUYER/SELLER).
     */
    public TradeSideEnum getSide() {
        return side;
    }

    /**
     * Sets Trade side (BUYER/SELLER).
     * @param side side.
     */
    public void setSide(TradeSideEnum side) {
        this.side = side;
    }

    /**
     * 
     * @return Display.
     */
    public boolean getDisplay() {
        return display;
    }

    /**
     * Sets display.
     * @param display Display.
     */
    public void setDisplay(boolean display) {
        this.display = display;
    }


    @Override
	public String toString() {
        return "OTBookOrder: timestamp=" + timestamp + ", orderref=" + reference + ", size=" + size + ", sideindicator=" + side + ", display=" + display + ", price=" + price;

    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(reference) 
    		+ 5 * safeHashCode(price) 
			+ 7 * safeHashCode(size) 
        	+ 11 * safeHashCode(side) 
        	+ 13 * safeHashCode(display); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookOrder other) {
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

		if((rc = safeCompare(display, other.display)) != 0) {
			return rc;
		}

		return 0;
	}
}