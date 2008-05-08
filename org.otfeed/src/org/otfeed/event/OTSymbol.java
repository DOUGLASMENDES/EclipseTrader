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
 * Provides information about the symbol.
 */
public final class OTSymbol implements Comparable<OTSymbol>, Serializable {
	
	private static final long serialVersionUID = 6692254804572091563L;

	private String code;
    private String company;
    private String currency;
    private InstrumentEnum type;

    /**
     * Default constructor.
     */
    public OTSymbol() { }

    /**
     * Constructor.
     * @param code String that identifies the equity, option, future, or index.
     * @param company Company name.
     * @param currency Currency.
     * @param type 	Instrument type.
     */
    public OTSymbol(String code, String company, 
    		String currency, InstrumentEnum type) {
        this.code = code;
        this.company = company;
        this.currency = currency;
        this.type = type;
    }


    /**
     *
     * @return String that identifies the equity, option, future, or index.
     */
    public String getCode() {
        return this.code;
    }


    /**
     * Sets code of the symbol.
     * @param code String that identifies the equity, option, future, or index.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return Company name.
     */
    public String getCompany() {
        return this.company;
    }

    /**
     * Sets company name.
     * @param company Company name.
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     *
     * @return The currency for any prices provided in the events for this symbol; in the current system this value is "USD" for United States Dollars.
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * Sets currency.
     * @param currency 	Currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 
     * @return     Instrument type.
     */
    public InstrumentEnum getType() {
        return this.type;
    }

    /**
     * Sets instrument type.
     * @param type 	Instrument type.
     */
    public void setType(InstrumentEnum type) {
        this.type = type;
    }

    @Override
	public String toString() {
        return "OTSymbol: code=" + code + ", company=" + company + ", currency=" + currency + ", type=" + type;
    }

    @Override
	public int hashCode() {
    	return safeHashCode(code) 
    		+ 3 * safeHashCode(company) 
    		+ 5 * safeHashCode(currency) 
    		+ 7 * safeHashCode(type);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTSymbol other) {
		int rc;
		
		if((rc = safeCompare(code, other.code)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(company, other.company)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(currency, other.currency)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(type, other.type)) != 0) {
			return rc;
		}
		
		return 0;
	}
}
