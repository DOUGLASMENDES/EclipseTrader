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
import static org.otfeed.event.IdentityUtil.enumSetHashCode;
import static org.otfeed.event.IdentityUtil.compareEnumSet;
import static org.otfeed.event.IdentityUtil.safeCompare;
import static org.otfeed.event.IdentityUtil.safeHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/**
 * This class provides OTDividend events and member functions to reference the data provided by the object.
 */
public final class OTDividend implements Comparable<OTDividend>, Serializable {
	
	private static final long serialVersionUID = -2243821011592708552L;

	private double price;
    private Date declarationDate;
    private Date executionDate;
    private Date recordDate;
    private Date paymentDate;
    private Set<DividendPropertyEnum> properties = new HashSet<DividendPropertyEnum>();

    /**
     * Default constructor.
     */
    public OTDividend() { }

    /**
     * Constructor.
     * @param price          Dividend (value).
     * @param declaratonDate Declaration date.
     * @param executionDate  Execution date.
     * @param recordDate     Record date.
     * @param paymentDate    Payment date.
     * @param properties     set of properties, see {@link DividendPropertyEnum}
     */
    public OTDividend(double price, 
    		Date declaratonDate, 
    		Date executionDate,
			Date recordDate, 
			Date paymentDate,
			Set<DividendPropertyEnum> properties) {

    	this.price = price;
        this.declarationDate = declaratonDate;
        this.executionDate = executionDate;
        this.recordDate = recordDate;
        this.paymentDate = paymentDate;
        this.properties = properties;
    }

    /**
     * @return Dividend (value).
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets dividend (value).
     *
     * @param price Dividend (value).
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return Date of declaration.
     */
    public Date getDeclarationDate() {
        return declarationDate;
    }

    /**
     * Sets declaration date.
     *
     * @param declarationDate Declaration date.
     */
    public void setDeclarationDate(Date declarationDate) {
        this.declarationDate = declarationDate;
    }

    /**
     * @return Date of execution.
     */
    public Date getExecutionDate() {
        return executionDate;
    }

    /**
     * Sets execution date.
     *
     * @param executionDate Execution date.
     */
    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    /**
     * @return Date of record.
     */
    public Date getRecordDate() {
        return recordDate;
    }

    /**
     * Sets record date.
     *
     * @param recordDate Record date.
     */
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    /**
     * @return Date of payment.
     */
    public Date getPaymentDate() {
        return paymentDate;
    }

    /**
     * Sets payment date.
     *
     * @param paymentDate Payment date.
     */
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * @return Set of properties, see {@link DividendPropertyEnum}.
     */
    public Set<DividendPropertyEnum> getProperties() {
    	return properties;
    }
    
    public void setProperties(Set<DividendPropertyEnum> val) {
    	properties = val;
    }
    
    /**
     * @return Is approximate.
     */
    public boolean isApproximate() {
        return properties.contains(DividendPropertyEnum.APPROXIMATE);
    }

    /**
     * @return Is annual.
     */
    public boolean isAnnual() {
        return properties.contains(DividendPropertyEnum.ANNUAL);
    }

    /**
     * @return Is canadian.
     */
    public boolean isCanadian() {
        return properties.contains(DividendPropertyEnum.CANADIAN);
    }

    /**
     * @return Is extra.
     */
    public boolean isExtra() {
        return properties.contains(DividendPropertyEnum.EXTRA);
    }

    /**
     * @return Is final.
     */
    public boolean isFinal() {
        return properties.contains(DividendPropertyEnum.FINAL);
    }

    /**
     * @return Is increase.
     */
    public boolean isIncrease() {
        return properties.contains(DividendPropertyEnum.INCREASE);
    }

    /**
     * @return IS semiannual.
     */
    public boolean isSemiannual() {
        return properties.contains(DividendPropertyEnum.SEMIANNUAL);
    }

    /**
     * @return Is stock.
     */
    public boolean isStock() {
        return properties.contains(DividendPropertyEnum.STOCK);
    }

    /**
     * @return Is special.
     */
    public boolean isSpecial() {
        return properties.contains(DividendPropertyEnum.SPECIAL);
    }

    @Override
	public String toString() {
        String out = "OTDividend: price=" + price 
        + ", decldate=" + declarationDate 
        + ", execdate=" + executionDate 
        + ", recdate=" + recordDate 
        + ", paydate=" + paymentDate;
        
        if(!properties.isEmpty()) {
        	out += ", properties=";
        	boolean first = true;
        	for(DividendPropertyEnum p : properties) {
        		if(first) { first = false; }
        		else      { out += "|"; }
        		out += p;
        	}
        }
        
        return out;
    }

    @Override
	public int hashCode() {
        return safeHashCode(price) 
        	+ 3 * safeHashCode(declarationDate) 
        	+ 5 * safeHashCode(executionDate) 
        	+ 7 * safeHashCode(recordDate) 
        	+ 11 * safeHashCode(paymentDate) 
        	+ 13 * enumSetHashCode(properties); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTDividend other) {
		int rc;

		if((rc = safeCompare(price, other.price)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(declarationDate, other.declarationDate)) != 0) {
			return rc;
		}

		if((rc = safeCompare(executionDate, other.executionDate)) != 0) {
			return rc;
		}

		if((rc = safeCompare(recordDate, other.recordDate)) != 0) {
			return rc;
		}

		if((rc = safeCompare(paymentDate, other.paymentDate)) != 0) {
			return rc;
		}

		if((rc = compareEnumSet(DividendPropertyEnum.class, properties, other.properties)) != 0) {
			return rc;
		}

		return 0;
	}
}