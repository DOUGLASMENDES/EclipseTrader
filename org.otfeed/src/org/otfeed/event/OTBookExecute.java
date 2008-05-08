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
 * This is a change to an existing order, and implies that 
 * a trade occurred. Look up the existing order using the 
 * order reference, and subtract the size from the order.
 * If the order now has no shares, remove the order. 
 */
public final class OTBookExecute implements Comparable<OTBookExecute>, Serializable {
	
	private static final long serialVersionUID = -1247678289972113218L;

	private Date timestamp;
    private String reference;
    private int size;
    private int matchNumber;

    /**
     * Default constructor.
     */
    public OTBookExecute() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param reference Order reference.
     * @param size Number of shares.
     * @param matchNumber ISLD specific unique match identifier.
     */
    public OTBookExecute(Date timestamp, 
    		String reference, 
    		int size, 
    		int matchNumber) {
        this.timestamp = timestamp;
        this.reference = reference;
        this.size = size;
        this.matchNumber = matchNumber;
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
        return reference;
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
     * @return ISLD specific unique match identifier.
     */
    public int getMatchNumber() {
        return matchNumber;
    }

    /**
     * Sets time of the event.
     * @param matchNumber Time when the event occurred.
     */
    public void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }

    @Override
	public String toString() {
        return "OTBookExecute: timestamp=" + timestamp + ", orderref=" + reference + ", size=" + size + ", matchnumber=" + matchNumber;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(reference) 
    		+ 5 * safeHashCode(size) 
			+ 7 * safeHashCode(matchNumber); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookExecute other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(reference, other.reference)) != 0) {
			return rc;
		}

		if((rc = safeCompare(size, other.size)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(matchNumber, other.matchNumber)) != 0) {
			return rc;
		}

		return 0;
	}
}