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
 * This class provides historical OHLC values (open, high, low, and close prices) for the requested time period.
 * Also it provides the volume value (the accumulated number of shares) in the requested time period. In cases
 * when there were not any transactions within certain time period, all the price values, as well as the volume
 * value, are set to zero.
 */
public final class OTOHLC implements Comparable<OTOHLC>, Serializable {
	
	private static final long serialVersionUID = 4758194026639533702L;
	
	private Date timestamp;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private long volume;

    /**
     * Default constructor.
     */
    public OTOHLC() { }

    /**
     * Constructor.
     * @param timestamp Time when the event occurred.
     * @param openPrice Open price.
     * @param highPrice High price.
     * @param lowPrice Low price.
     * @param closePrice Close price.
     * @param volume Accumulated volume for the requested time period.
     */
    public OTOHLC(Date timestamp, 
    		double openPrice, 
    		double highPrice, 
    		double lowPrice, 
    		double closePrice, 
    		long volume) {
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    /**
     *
     * @return Starting time of the time period.
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
     * @return High price.
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * Sets open price.
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
     * Sets high price.
     * @param highPrice High price.
     */
    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    /**
     *
     * @return Low Price.
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * Sets low price.
     * @param lowPrice 	Low price.
     */
    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    /**
     *
     * @return Close price.
     */
    public double getClosePrice() {
        return closePrice;
    }

    /**
     * Sets close price.
     * @param closePrice Close price.
     */
    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    /**
     * 
     * @return Accumulated volume for the requested time period.
     */
    public long getVolume() {
        return volume;
    }

    /**
     * Sets accumulated volume for the requested time period.
     * @param volume Accumulated volume for the requested time period.
     */
    public void setVolume(long volume) {
        this.volume = volume;
    }

    @Override
	public String toString() {
        return "OTOHLC: timestamp=" + timestamp + ", o=" + openPrice + ", h=" + highPrice + ", l=" + lowPrice + ", c=" + closePrice + ", volume=" + volume;
    }

    @Override
	public int hashCode() {
       return safeHashCode(timestamp) 
    		+ 3 * safeHashCode(openPrice)
    		+ 5 * safeHashCode(highPrice)
    		+ 7 * safeHashCode(lowPrice)
    		+ 11 * safeHashCode(closePrice)
    		+ 13 * safeHashCode(volume);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTOHLC other) {
		int rc;

		if((rc = safeCompare(timestamp, other.timestamp)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(openPrice, other.openPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(highPrice, other.highPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(lowPrice, other.lowPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(closePrice, other.closePrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(volume, other.volume)) != 0) {
			return rc;
		}
		
		return 0;
	}
}