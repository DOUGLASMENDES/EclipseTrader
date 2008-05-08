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

import org.otfeed.command.OptionTypeEnum;

/**
 * This class provides an Option Initialize event and 
 * member functions to reference the data returned.
 */
public final class OTOptionInit implements Comparable<OTOptionInit>, Serializable {

	private static final long serialVersionUID = -4489013332981157210L;
	
	private String underlyerSymbol;
    private String symbol;
    private double strikePrice;
    private int contractSize;
    private int expYear;
    private int expMonth;
    private int expDay;
    private OptionTypeEnum exerciseStyle;
    private String underlyerCusip;
    private String currency;
    private char optionMarker;

    /**
     * Default constructor.
     */
    public OTOptionInit() { }

    /**
     * Constructor.
     * @param underlyerSymbol Underlying symbol name.
     * @param symbol Symbol.
     * @param strikePrice  Strike price.
     * @param contractSize Contract size.
     * @param expYear Expiration year.
     * @param expMonth Expiration month.
     * @param expDay Expiration daty.
     * @param exerciseStyle Exercise style.
     * @param underlyerCusip Underlyer CUSIP.
     * @param currency Currency type used.
     * @param optionMarker Option marker.
     */
    public OTOptionInit(String underlyerSymbol, 
    		String symbol, 
    		double strikePrice, 
    		int contractSize, 
    		int expYear,
    		int expMonth, 
    		int expDay, 
    		OptionTypeEnum exerciseStyle,    
    		String underlyerCusip, 
    		String currency, 
    		char optionMarker) {
        this.underlyerSymbol=underlyerSymbol;
        this.symbol=symbol;
        this.strikePrice=strikePrice;
        this.contractSize=contractSize;
        this.expYear=expYear;
        this.expMonth=expMonth;
        this.expDay=expDay;
        this.exerciseStyle=exerciseStyle;
        this.underlyerCusip=underlyerCusip;
        this.currency=currency;
        this.optionMarker=optionMarker;
    }

    /**
     * 
     * @return     Contract size.
     */
    public int getContractSize() {
        return contractSize;
    }
    
    public void setConstrctSize(int val) {
    	contractSize = val;
    }

    /**
     * 
     * @return Currency type used.
     */
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String val) {
    	currency = val;
    }

    /**
     * 
     * @return Option excersize style (AMERICAN/EUROPEAN/CAPPED).
     */
    public OptionTypeEnum getExerciseStyle() {
        return exerciseStyle;
    }
    
    public void setExcerciseStyle(OptionTypeEnum val) {
    	exerciseStyle = val;
    }

    /**
     * 
     * @return Expiration day.
     */
    public int getExpDay() {
        return expDay;
    }
    
    public void setExpDay(int val) {
    	expDay = val;
    }

    /**
     * 
     * @return Expiration month.
     */
    public int getExpMonth() {
        return expMonth;
    }
    
    public void setExpMonth(int val) {
    	expMonth = val;
    }

    /**
     * 
     * @return Expiration year.
     */
    public int getExpYear() {
        return expYear;
    }
    
    public void setExpYear(int val) {
    	expYear = val;
    }

    /**
     * (Montreal only).
     * @return Option marker.
     */
    public char getOptionMarker() {
        return optionMarker;
    }
    
    public void setOptionMarker(char val) {
    	optionMarker = val;
    }

    /**
     * 
     * @return Strike price for the option.
     */
    public double getStrikePrice() {
        return strikePrice;
    }
    
    public void setStrikePrice(double val) {
    	strikePrice = val;
    }

    /**
     *
     * @return Symbol name for the option.
     */
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String val) {
    	symbol = val;
    }

    /**
     *
     * @return CUSIP ID for the underlying symbol.
     */
    public String getUnderlyerCusip() {
        return underlyerCusip;
    }
    
    public void setUnderlyerCusip(String val) {
    	underlyerCusip = val;
    }

    /**
     * 
     * @return Underlying symbol name.
     */
    public String getUnderlyerSymbol() {
        return underlyerSymbol;
    }
    
    public void setUnderlyerSymbol(String val) {
    	underlyerSymbol = val;
    }
    
    @Override
	public String toString() {
        return "OTOptionInit: underlyerSymbol=" + underlyerSymbol
			+ ", symbol=" + symbol
			+ ", strikeprice=" + strikePrice 
			+ ", contractsize=" + contractSize
			+ ", expyear=" + expYear
			+ ", expmonth=" + expMonth
			+ ", expday=" + expDay 
			+ ", exercisestyle=" + exerciseStyle
			+ ", underlyercusip=" + underlyerCusip
			+ ", currency=" + currency
			+ ", optionmarker=" + optionMarker;
    }

    @Override
	public int hashCode() {
        return safeHashCode(underlyerSymbol) 
    		+ 3 * safeHashCode(symbol)
    		+ 5 * safeHashCode(strikePrice)
    		+ 7 * safeHashCode(contractSize)
    		+ 11 * safeHashCode(expYear)
    		+ 13 * safeHashCode(expMonth)
    		+ 17 * safeHashCode(expDay)
			+ 23 * safeHashCode(exerciseStyle)
			+ 29 * safeHashCode(underlyerCusip)
			+ 31 * safeHashCode(currency)
			+ 41 * safeHashCode(optionMarker);
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTOptionInit other) {
		int rc;

		if((rc = safeCompare(underlyerSymbol, other.underlyerSymbol)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(symbol, other.symbol)) != 0) {
			return rc;
		}

		if((rc = safeCompare(strikePrice, other.strikePrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(contractSize, other.contractSize)) != 0) {
			return rc;
		}

		if((rc = safeCompare(expYear, other.expYear)) != 0) {
			return rc;
		}

		if((rc = safeCompare(expMonth, other.expMonth)) != 0) {
			return rc;
		}

		if((rc = safeCompare(expDay, other.expDay)) != 0) {
			return rc;
		}

		if((rc = safeCompare(exerciseStyle, other.exerciseStyle)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(underlyerCusip, other.underlyerCusip)) != 0) {
			return rc;
		}

		if((rc = safeCompare(currency, other.currency)) != 0) {
			return rc;
		}

		if((rc = safeCompare(optionMarker, other.optionMarker)) != 0) {
			return rc;
		}

		return 0;
	}
}
