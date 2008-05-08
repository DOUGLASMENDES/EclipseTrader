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

import org.otfeed.command.BookDeleteTypeEnum;

/**
 * Look up the order using the order reference, and delete orders 
 * depending on the Delete Type.
 */
public final class OTBookDelete implements Comparable<OTBookDelete>, Serializable {

	private static final long serialVersionUID = -3291794686907632810L;

	private Date timestamp;
    private String reference;
    private BookDeleteTypeEnum deleteType;
    private TradeSideEnum side;

    /**
     * Default constructor.
     */
    public OTBookDelete() { }

    /**
     * Constructor
     * @param timestamp Time when the event occurred.
     * @param reference Order reference.
     * @param deleteType Order = 1, Previous = 2, All = 3, After = A
     * @param side Side: B = Buy, S = Sell.
     */
    public OTBookDelete(Date timestamp, 
    		String reference, 
    		BookDeleteTypeEnum deleteType, 
    		TradeSideEnum side) {
        this.timestamp = timestamp;
        this.reference = reference;
        this.deleteType = deleteType;
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
     * @return Order reference.
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
     * @return type of the delete (ORDER, PREVIOUS, ALL, AFTER).
     */
    public BookDeleteTypeEnum getDeleteType() {
        return deleteType;
    }

    /**
     * Sets delete type (ORDER, PREVIOUS, ALL, AFTER).
     * @param deleteType type of the delete.
     */
    public void setDeleteType(BookDeleteTypeEnum deleteType) {
        this.deleteType = deleteType;
    }

    /**
     *
     * @return Trade side (BUYER/SELLER).
     */
    public TradeSideEnum getSide() {
        return side;
    }

    /**
     * Sets trade side (BUYER/SELLER).
     * @param side side.
     */
    public void setSide(TradeSideEnum side) {
        this.side = side;
    }

    @Override
	public String toString() {
        return "OTBookDelete: timestamp=" + timestamp + ", orderref=" + reference + ", sideindicator=" + side + ", deletetype = " + deleteType;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(reference) 
    		+ 5 * safeHashCode(deleteType) 
			+ 7 * safeHashCode(side); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookDelete other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(reference, other.reference)) != 0) {
			return rc;
		}

		if((rc = safeCompare(deleteType, other.deleteType)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(side, other.side)) != 0) {
			return rc;
		}

		return 0;
	}
}