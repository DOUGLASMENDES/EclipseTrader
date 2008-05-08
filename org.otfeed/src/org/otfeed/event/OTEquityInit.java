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
 * Provides Equity Initialize information.
 * This is information about a company and its stock. 
 */
public final class OTEquityInit implements Comparable<OTEquityInit>, Serializable {
 
	private static final long serialVersionUID = -2370048578021455014L;
	
	private String currency;
    private InstrumentEnum instrumentType;
    private String company;
    private double prevClosePrice;
    private String prevCloseDate;   // FIXME: textual date. this is bad!
    private double annualHighPrice;
    private String annualHighDate;  // FIXME: textual date. this is bad!
    private double annualLowPrice;
    private String annualLowDate;   // FIXME: textual date. this is bad!
    private double earningsPrice;
    private String earningsDate;    // FIXME: textual date. this is bad!
    private long totalShares;
    private long averageVolume;
    private String CUSIP;
    private String ISIN;
    private boolean isUPC11830;
    private boolean isSmallCap;
    private boolean isTestIssue;

    /**
     * Default constructor.
     */
    public OTEquityInit() { }

    /**
     * Constructor.
     * @param currency Currency.
     * @param type 	Instrument type.
     * @param company Company name.
     * @param prevClosePrice Previous close price.
     * @param prevCloseDate	Previous close date.
     * @param annualHighPrice Annual high price. 
     * @param annualHighDate  Annual high date.
     * @param annualLowPrice Annual low price.
     * @param annualLowDate Annual low date.
     * @param earningsPrice Earnings price.
     * @param earningsDate Earnings date.
     * @param totalShares Total shares.
     * @param averageVolume Average volume.
     * @param CUSIP CUSIP number.
     * @param ISIN 	International Securities Identification Number, or ISIN.
     * @param isUPC11830 Notes whether Nasdaq flagged as UPC11830.
     * @param isSmallCap Notes whether it is a Nasdaq Small Cap.
     * @param isTestIssue Notes whether it is a known test symbol.
     */
    public OTEquityInit(
                        String currency,
                        InstrumentEnum type,
                        String company,
                        double prevClosePrice,
                        String prevCloseDate,
                        double annualHighPrice,
                        String annualHighDate,
                        double annualLowPrice,
                        String annualLowDate,
                        double earningsPrice,
                        String earningsDate,
                        long totalShares,
                        long averageVolume,
                        String CUSIP,
                        String ISIN,
                        boolean isUPC11830,
                        boolean isSmallCap,
                        boolean isTestIssue) {
        this.currency = currency;
        this.instrumentType = type;
        this.company = company;
        this.prevClosePrice = prevClosePrice;
        this.prevCloseDate = prevCloseDate;
        this.annualHighPrice = annualHighPrice;
        this.annualHighDate = annualHighDate;
        this.annualLowPrice = annualLowPrice;
        this.annualLowDate = annualLowDate;
        this.earningsPrice = earningsPrice;
        this.earningsDate = earningsDate;
        this.totalShares = totalShares;
        this.averageVolume = averageVolume;
        this.CUSIP = CUSIP;
        this.ISIN = ISIN;
        this.isUPC11830 = isUPC11830;
        this.isSmallCap = isSmallCap;
        this.isTestIssue = isTestIssue;
    }

    /**
     *
     * @return Currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets currency.
     * @param currency Currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 
     * @return     Instrument type.
     */
    public InstrumentEnum getInstrumentType() {
        return instrumentType;
    }


    /**
     * Sets instrument type.
     * @param type 	Instrument type.
     */
    public void setInstrumentType(InstrumentEnum type) {
        this.instrumentType = type;
    }

    /**
     * 
     * @return Company name.
     */
    public String getCompanyName() {
        return company;
    }

    /**
     * Sets company name. 
     * @param company Company name.
     */
    public void setCompanyName(String company) {
        this.company = company;
    }

    /**
     * 
     * @return     Previous close price.
     */
    public double getPrevClosePrice() {
        return prevClosePrice;
    }

    /**
     * Sets previous close price.
     * @param prevClosePrice 	Previous close price.
     */
    public void setPrevClosePrice(double prevClosePrice) {
        this.prevClosePrice = prevClosePrice;
    }

    /**
     *
     * @return Previous close date: YYYYMMDD or 8 spaces if not provided.
     */
    public String getPrevCloseDate() {
        return prevCloseDate;
    }

    /**
     * Sets previous close date.
     * @param prevCloseDate Previous close date.
     */
    public void setPrevCloseDate(String prevCloseDate) {
        this.prevCloseDate = prevCloseDate;
    }

    /**
     * 
     * @return Annual high price.
     */
    public double getAnnualHighPrice() {
        return annualHighPrice;
    }

    /**
     * Sets annual high price.
     * @param annualHighPrice Annual high price.
     */
    public void setAnnualHighPrice(double annualHighPrice) {
        this.annualHighPrice = annualHighPrice;
    }

    /**
     * 
     * @return Annual high date: YYYYMMDD or 8 spaces if not provided.
     */
    public String getAnnualHighDate() {
        return annualHighDate;
    }

    /**
     * Sets annual high date.
     * @param annualHighDate Annual high date.
     */
    public void setAnnualHighDate(String annualHighDate) {
        this.annualHighDate = annualHighDate;
    }

    /**
     * 
     * @return Annual low price.
     */
    public double getAnnualLowPrice() {
        return annualLowPrice;
    }

    /**
     * Sets annual low price.
     * @param annualLowPrice Annual low price.
     */
    public void setAnnualLowPrice(double annualLowPrice) {
        this.annualLowPrice = annualLowPrice;
    }

    /**
     *
     * @return     Annual low date: YYYYMMDD or 8 spaces if not provided.
     */
    public String getAnnualLowDate() {
        return annualLowDate;
    }

    /**
     * Sets annual low date.
     * @param annualLowDate Annual low date.
     */
    public void setAnnualLowDate(String annualLowDate) {
        this.annualLowDate = annualLowDate;
    }

    /**
     *
     * @return Earnings price (earnings per share).
     */
    public double getEarningsPrice() {
        return earningsPrice;
    }

    /**
     * Sets earnings price.
     * @param earningsPrice Earnings price.
     */
    public void setEarningsPrice(double earningsPrice) {
        this.earningsPrice = earningsPrice;
    }

    /**
     * 
     * @return Earnings date: YYYYMMDD or 8 spaces if not provided.
     */
    public String getEarningsDate() {
        return earningsDate;
    }

    /**
     * Sets earnings date.
     * @param earningsDate Earnings date.
     */
    public void setEarningsDate(String earningsDate) {
        this.earningsDate = earningsDate;
    }

    /**
     * 
     * @return Total shares.
     */
    public long getTotalShares() {
        return totalShares;
    }

    /**
     * Sets total shares.
     * @param totalShares Total shares.
     */
    public void setTotalShares(long totalShares) {
        this.totalShares = totalShares;
    }

    /**
     * 
     * @return Average volume.
     */
    public long getAverageVolume() {
        return averageVolume;
    }

    /**
     *  Sets average volume.
     * @param averageVolume Average volume.
     */
    public void setAverageVolume(long averageVolume) {
        this.averageVolume = averageVolume;
    }

    /**
     *
     * @return CUSIP number.
     */
    public String getCUSIP() {
        return CUSIP;
    }

    /**
     * Sets CUSIP number.
     * @param CUSIP CUSIP number.
     */
    public void setCUSIP(String CUSIP) {
        this.CUSIP = CUSIP;
    }

    /**
     *
     * @return International Securities Identification Number, or ISIN.
     */
    public String getISIN() {
        return ISIN;
    }

    /**
     * Sets International Securities Identification Number, or ISIN.
     * @param ISIN International Securities Identification Number, or ISIN.
     */
    public void setISIN(String ISIN) {
        this.ISIN = ISIN;
    }

    /**
     *
     * @return Notes whether Nasdaq flagged as UPC11830. 
     */
    public boolean isUPC11830() {
        return isUPC11830;
    }

    /**
     * Adds UPC118300 bit.
     * @param isUPC11830 sUPC11830 	Notes whether Nasdaq flagged as UPC11830.
     */
    public void setUPC11830(boolean isUPC11830) {
        this.isUPC11830 = isUPC11830;
    }

    /**
     * 
     * @return Notes whether it is a Nasdaq Small Cap.
     */
    public boolean isSmallCap() {
        return isSmallCap;
    }

    /**
     * Adds Small Cap bit.
     * @param isSmallCap Notes whether it is a Nasdaq Small Cap.
     */
    public void setSmallCap(boolean isSmallCap) {
        this.isSmallCap = isSmallCap;
    }

    /**
     *
     * @return Notes whether it is a known test symbol.
     */
    public boolean isTestIssue() {
        return isTestIssue;
    }

    /**
     * Adds test symbol bit.
     * @param isTestIssue Notes whether it is a known test symbol.
     */
    public void setTestIssue(boolean isTestIssue) {
        this.isTestIssue = isTestIssue;
    }

    @Override
	public String toString() {
        return "OTEquityInit: currency=" + currency 
        + ", instrumentType=" + instrumentType 
		+ ", company=" + company 
		+ ", prevClosePrice=" + prevClosePrice 
		+ ", prevCloseDate=" + prevCloseDate 
		+ ", annualHighPrice=" + annualHighPrice 
		+ ", annualHighDate=" + annualHighDate 
		+ ", annualLowPrice=" + annualLowPrice 
		+ ", annualLowDate=" + annualLowDate 
		+ ", earningsPrice=" + earningsPrice 
		+ ", earningsDate=" + earningsDate 
		+ ", totalShares=" + totalShares 
		+ ", averageVolume=" + averageVolume 
		+ ", CUSIP=" + CUSIP 
		+ ", ISIN=" + ISIN 
		+ ", isUPC11830=" + isUPC11830 
		+ ", isSmallCap=" + isSmallCap 
		+ ", isTestIssue=" + isTestIssue;
    }

    @Override
	public int hashCode() {
        return safeHashCode(currency) 
        	+ 3 * safeHashCode(instrumentType) 
        	+ 5 * safeHashCode(company) 
        	+ 7 * safeHashCode(prevClosePrice) 
        	+ 11 * safeHashCode(prevCloseDate) 
        	+ 13 * safeHashCode(annualHighPrice) 
        	+ 17 * safeHashCode(annualHighDate) 
        	+ 19 * safeHashCode(annualLowPrice) 
        	+ 23 * safeHashCode(annualLowDate) 
        	+ 29 * safeHashCode(earningsPrice) 
        	+ 31 * safeHashCode(earningsDate) 
        	+ 41 * safeHashCode(totalShares) 
        	+ 47 * safeHashCode(averageVolume) 
        	+ 53 * safeHashCode(CUSIP) 
        	+ 61 * safeHashCode(ISIN) 
        	+ 83 * safeHashCode(isUPC11830) 
        	+ 113 * safeHashCode(isSmallCap) 
        	+ 119 * safeHashCode(isTestIssue); 
    }
        
    @Override
	public boolean equals(Object o) {
    	return equalsTo(this, o);
    }
    
	public int compareTo(OTEquityInit other) {
		int rc;

		if((rc = safeCompare(currency, other.currency)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(instrumentType, other.instrumentType)) != 0) {
			return rc;
		}

		if((rc = safeCompare(company, other.company)) != 0) {
			return rc;
		}

		if((rc = safeCompare(prevClosePrice, other.prevClosePrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(annualHighPrice, other.annualHighPrice)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(annualHighDate, other.annualHighDate)) != 0) {
			return rc;
		}

		if((rc = safeCompare(annualLowPrice, other.annualLowPrice)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(annualLowDate, other.annualLowDate)) != 0) {
			return rc;
		}

		if((rc = safeCompare(earningsPrice, other.earningsPrice)) != 0) {
			return rc;
		}

		if((rc = safeCompare(earningsDate, other.earningsDate)) != 0) {
			return rc;
		}
		
		if((rc = safeCompare(totalShares, other.totalShares)) != 0) {
			return rc;
		}

		if((rc = safeCompare(averageVolume, other.averageVolume)) != 0) {
			return rc;
		}

		if((rc = safeCompare(CUSIP, other.CUSIP)) != 0) {
			return rc;
		}

		if((rc = safeCompare(ISIN, other.ISIN)) != 0) {
			return rc;
		}

		if((rc = safeCompare(isUPC11830, other.isUPC11830)) != 0) {
			return rc;
		}

		if((rc = safeCompare(isSmallCap, other.isSmallCap)) != 0) {
			return rc;
		}

		if((rc = safeCompare(isTestIssue, other.isTestIssue)) != 0) {
			return rc;
		}

		return 0;
	}
}