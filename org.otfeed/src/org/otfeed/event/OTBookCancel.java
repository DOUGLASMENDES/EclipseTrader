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
 * Class for book cancelation data representation.
 */
public final class OTBookCancel implements Comparable<OTBookCancel>, Serializable {
	
 	private static final long serialVersionUID = -1770059413413742405L;

 	private Date timestamp;
    private String reference;
    private int size;

    /**
     * Default constructor.
     */
    public OTBookCancel() { }

    /**
     * Constructor.
     *
     * @param timestamp Time of the event.
     * @param reference Order reference.
     * @param size      Number of shares.
     */
    public OTBookCancel(Date timestamp, 
    		String reference, int size) {
        this.timestamp = timestamp;
        this.reference = reference;
        this.size = size;
    }

    /**
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
     * Sets order reference.
     * @param reference Order reference.
     */
    public void setOrderRef(String reference) {
        this.reference = reference;
    }

    /**
     * 
     * @return Order reference.
     */
    public String getOrderRef() {
        return this.reference;
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

    @Override
	public String toString() {
        return "OTBookCancel: timestamp=" + timestamp + ", orderRef=" + reference + ", size=" + size;
    }

    @Override
	public int hashCode() {
        return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(reference) 
			+ 5 * safeHashCode(size); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookCancel other) {
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
		
		return 0;
	}
}