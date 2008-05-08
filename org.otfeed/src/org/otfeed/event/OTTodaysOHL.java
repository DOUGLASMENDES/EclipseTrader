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

/**
 * Provides OHL (Open/High/Low) information.
 */
public final class OTTodaysOHL implements Comparable<OTTodaysOHL>, Serializable {

	private static final long serialVersionUID = -193738134747913662L;
	
	private double openPrice;
    private double highPrice;
    private double lowPrice;

    /**
     * Default constructor.
     */
    public OTTodaysOHL() { }

    /**
     * Constructor.
     * @param openPrice Open price.
     * @param highPrice High price.
     * @param lowPrice Low price.
     */
    public OTTodaysOHL(double openPrice, 
    		double highPrice, double lowPrice) {
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }

    /**
     * 
     * @return Open price.
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * Sets Open price.
     * @param openPrice Open price.
     */
    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    /**
     * 
     * @return High price.
     */
    public double getHighPrice() {
        return highPrice;
    }

    /**
     * Sets High Price.
     * @param highPrice High price.
     */
    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    /**
     *
     * @return Low price.
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * Sets Low price.
     * @param lowPrice Low price.
     */
    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    @Override
	public String toString() {
        return "OTTodaysOHL: openprice=" + openPrice + ", highprice=" + highPrice + ", lowprice=" + lowPrice;
    }


    @Override
	public int hashCode() {
        return safeHashCode(openPrice) 
    		+ 3 * safeHashCode(highPrice)
    		+ 5 * safeHashCode(lowPrice);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTTodaysOHL other) {
		int rc;

		if((rc = safeCompare(openPrice, other.openPrice)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(highPrice, other.highPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(lowPrice, other.lowPrice)) != 0) {
			return rc;
		}

		return 0;
	}
}