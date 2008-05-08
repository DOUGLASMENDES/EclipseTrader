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
 * Clear all orders for the ECN.
 */
public final class OTBookPurge implements Comparable<OTBookPurge>, Serializable {

	private static final long serialVersionUID = 5257014452539624594L;

	private Date timestamp;
    private String nameRoot;

    /**
     * Default constructor.
     */
    public OTBookPurge() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param nameRoot ECN name root.
     */
    public OTBookPurge(Date timestamp, String nameRoot) {
        this.timestamp = timestamp;
        this.nameRoot = nameRoot;
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
     * @return ECN name root.
     */
    public String getExchangeNameRoot() {
        return nameRoot;
    }

    /**
     * Sets ECN name root.
     * @param nameRoot ECN name root.
     */
    public void setExchangeNameRoot(String nameRoot) {
        this.nameRoot = nameRoot;
    }

    @Override
	public String toString() {
        return "OTBookPurge: timestamp=" + timestamp + ", exchangenamebase=" + nameRoot;

    }

    @Override
	public int hashCode() {
         return safeHashCode(timestamp) 
        	+ 3 * safeHashCode(nameRoot); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTBookPurge other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(nameRoot, other.nameRoot)) != 0) {
			return rc;
		}

		return 0;
	}
}