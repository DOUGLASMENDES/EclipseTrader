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

import static org.otfeed.event.IdentityUtil.safeCompare;
import static org.otfeed.event.IdentityUtil.safeHashCode;
import static org.otfeed.event.IdentityUtil.equalsTo;

import java.io.Serializable;

/**
 * Provides exchange information.
 */
public final class OTExchange implements Comparable<OTExchange>, Serializable {
	
 	private static final long serialVersionUID = 7151477567631158035L;
 	
	private String code;
    private String title;
    private String description;
    private boolean available;
    private String subscriptionURL;

    /**
     * Default constructor.
     */
    public OTExchange() { }

    /**
     * Constructor.
     * @param code        Exchange code.
     * @param title       Exchange title.
     * @param description Description of the exchange.
     * @param available   Available field.
     * @param subscriptionURL URL. 
     */
    public OTExchange(String code, 
    		String title, 
    		String description, 
    		boolean available, 
    		String subscriptionURL) {
        this.title = title;
        this.code = code;
        this.description = description;
        this.available = available;
        this.subscriptionURL = subscriptionURL;
    }

    /**
     * 
     * @return Exchange code.
     */
    public String getCode() { 
    	return this.code; 
    }

    /**
     * Sets exchange code.
     *
     * @param code Exchange code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return The exchange title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets exchange title.
     *
     * @param title Exchange title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return A description of the exchange.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets description of the exchange.
     *
     * @param description Description of the exchange.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Is available.
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Sets available field.
     *
     * @param available Available field.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * @return If the user is not subscribed to this exchange, 
     * he can go to this URL to do so.
     */
    public String getSubscriptionURL() {
        return this.subscriptionURL;
    }

    /**
     * Sets SubscriptionURL field.
     *
     * @param url URL.
     */
    public void setSubscriptionURL(String url) {
        this.subscriptionURL = url;
    }

    @Override
	public String toString() {
        return "OTExchange: code=" + code + ", title=" + title 
        	+ ", description=" + description 
        	+ ", available=" + available
        	+ ", subscriptionURL=" + subscriptionURL;
    }
    
    @Override
	public int hashCode() {
    	return safeHashCode(code) 
    		+ 3  * safeHashCode(title) 
    		+ 5  * safeHashCode(description) 
    		+ 7  * safeHashCode(subscriptionURL)
    		+ 11 * safeHashCode(available);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTExchange other) {
		int rc;
		
		if((rc = safeCompare(code, other.code)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(title, other.title)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(description, other.description)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(available, other.available)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(subscriptionURL, other.subscriptionURL)) != 0) {
			return rc;
		}
		
		return 0;
	}
}