package org.otfeed.command;

import java.util.Date;

/**
 * Common superclass for all commands that bear 
 * <code>startDate</code> and <code>endDate</code>
 * properties.
 *
 */
abstract class ExchangeSymbolAndDatesHolder extends ExchangeAndSymbolHolder {

	private Date startDate;
	
	/**
	 * Start date/time for the historical data.
	 * 
	 * @return Start date/time.
	 */
	public final Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Sets start date/time.
	 * 
	 * @param val Start date/time.
	 */
	public final void setStartDate(Date val) {
		startDate = val;
	}
	
	private Date endDate;
	
	/**
	 * End date/time for the historical data.
	 * 
	 * @return End date/time.
	 */
	public final Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Sets end date/time.
	 * 
	 * @param val end date/time.
	 */
	public final void setEndDate(Date val) {
		endDate = val;
	}
}
