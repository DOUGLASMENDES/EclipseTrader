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
 * A book price level is the processed output of a bookServer.
 * The bookServer maintains the depth of book for ECN data, 
 * keeping track of each order.
 * In the bookServer orders of the same price are consolidated 
 * into price levels, and a Book Price Level message is 
 * issued whenever the size of a price level changes. Even
 * though we maintain the book on all orders only the top 
 * book levels are sent as Book
 * Price Level events. 
 * Typically the top six levels are sent for both the 
 * buy and sell sides.
 */
public final class OTBookPriceLevel implements Comparable<OTBookPriceLevel>, Serializable {
	
	private static final long serialVersionUID = 5859804510929854117L;

	private Date timestamp;
    private double price;
    private int size;
    private TradeSideEnum side;
    private String levelID;

    /**
     * Default constructor.
     */
    public OTBookPriceLevel() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param price Price.
     * @param size Number of shares.
     * @param side 	Side: B = Bid, S = Sell.
     * @param levelID Unique level identifier.
     */
    public OTBookPriceLevel(Date timestamp, double price, 
    		int size, TradeSideEnum side, String levelID) {
        this.timestamp = timestamp;
        this.price = price;
        this.size = size;
        this.side = side;
        this.levelID = levelID;
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
     * @return Side (BUYER/SELLER).
     */
    public TradeSideEnum getSide() {
        return side;
    }

    /**
     * Sets side: BUYER(Bid) or SELLER(Ask). 
     * param side side.
     */
    public void setSide(TradeSideEnum side) {
        this.side = side;
    }

    /**
     * 
     * @return Unique level identifier.
     */
    public String getLevelId() {
        return levelID;
    }

    /**
     * Sets unique level identifier. 
     * @param levelID Unique level identifier.
     */
    public void setLevelId(String levelID) {
        this.levelID = levelID;
    }

    @Override
	public String toString() {
        return "OTBookPriceLevel: timestamp=" + timestamp + ", size=" + size + ", sideindicator=" + side + ", levelid=" + levelID + ", price=" + price;
    }

    @Override
	public int hashCode() {
         return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(price) 
    		+ 5 * safeHashCode(size) 
			+ 7 * safeHashCode(side) 
        	+ 11 * safeHashCode(levelID);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookPriceLevel other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
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

		if((rc = safeCompare(levelID, other.levelID)) != 0) {
			return rc;
		}

		return 0;
	}
}