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
 * Represents a Split event.
 */
public final class OTSplit implements Comparable<OTSplit>, Serializable {

	private static final long serialVersionUID = 1917216759776849575L;
	
	private int  toFactor;
    private int  forFactor;
    private Date declarationDate;
    private Date executionDate;
    private Date recordDate;
    private Date paymentDate;

    /**
     * Default constructor.
     */
    public OTSplit() { }

    /**
     * Constructor.
     * @param toFactor To factor.
     * @param forFactor For factor.
     * @param declaratonDate Declaration date.
     * @param executionDate Execution date.
     * @param recordDate 	Record date.
     * @param paymentDate Payment date.
     */
    public OTSplit(int toFactor, int forFactor, Date declaratonDate,
    		Date executionDate, Date recordDate, 
    		Date paymentDate) {
        this.toFactor=toFactor;
        this.forFactor=forFactor;
        this.declarationDate=declaratonDate;
        this.executionDate=executionDate;
        this.recordDate=recordDate;
        this.paymentDate=paymentDate;
    }

    /**
     *
     * @return To factor.
     */
    public int getToFactor() {
        return toFactor;
    }

    /**
     * Sets to factor.
     * @param toFactor 	To factor.
     */
    public void setToFactor(int toFactor) {
        this.toFactor = toFactor;
    }

    /**
     * 
     * @return For factor.
     */
    public int getForFactor() {
        return forFactor;
    }

    /**
     * Sets for factor.
     * @param forFactor For factor.
     */
    public void setForFactor(int forFactor) {
        this.forFactor = forFactor;
    }

    /**
     * 
     * @return Declaration date.
     */
    public Date getDeclarationDate() {
        return declarationDate;
    }

    /**
     * Sets declaration date.
     * @param declarationDate Declaration date.
     */
    public void setDeclarationDate(Date declarationDate) {
        this.declarationDate = declarationDate;
    }

    /**
     *
     * @return Execution date.
     */
    public Date getExecutionDate() {
        return executionDate;
    }

    /**
     * Sets execution date.
     * @param executionDate Execution date.
     */
    public void setExecutionDate(Date executionDate) {
    	this.executionDate = executionDate;
    }

   /**
     *
     * @return Record date.
     */
    public Date getRecordDate() {
        return recordDate;
    }

    /**
     * Sets record date.
     * @param recordDate Record date.
     */
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    /**
     *
     * @return Payment date.
     */
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    /**
     * Sets payment date.
     * @param paymentDate Payment date.
     */
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
	public String toString() {
        return "OTSplit: toFactor=" + toFactor
        		+ ", forFactor=" + forFactor
        		+ ", decldate=" + declarationDate
        		+ ", execdate=" + executionDate
        		+ ", recdate=" + recordDate
        		+ ", paydate=" + paymentDate;
    }

    @Override
	public int hashCode() {
        return safeHashCode(toFactor) 
    		+ 3 * safeHashCode(forFactor)
    		+ 5 * safeHashCode(declarationDate)
    		+ 7 * safeHashCode(executionDate)
    		+ 11 * safeHashCode(recordDate)
    		+ 13 * safeHashCode(paymentDate);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTSplit other) {
		int rc;

		if((rc = safeCompare(toFactor, other.toFactor)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(forFactor, other.forFactor)) != 0) {
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

		return 0;
	}
}
