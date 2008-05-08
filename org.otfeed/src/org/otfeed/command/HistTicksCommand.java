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
 */

package org.otfeed.command;

import java.util.Date;
import org.otfeed.protocol.ICommand;

/**
 * Request for the historical data with tick resolution.
 * <p/>
 * Generates {@link org.otfeed.event.OTQuote quote}, 
 * {@link org.otfeed.event.OTTrade trade},
 * {@link org.otfeed.event.OTMMQuote marker-maker quote}, and 
 * {@link org.otfeed.event.OTBBO bbo} events.
 * <p/>
 * For the request to receive aggregated quote data, 
 * see {@link org.otfeed.command.HistDataCommand HistDataCommand}.
 */
public final class HistTicksCommand 
		extends ExchangeSymbolAndQuoteDelegateHolder implements ICommand {

	/**
	 * Creates new Historical ticks command, initializing
	 * all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 * @param volumeStyle style of volume reporting (INDIVIDUAL or COMPOUND).
	 */
	public HistTicksCommand(String exchangeCode, 
			String symbolCode,
			Date startDate,
			Date endDate,
			VolumeStyleEnum volumeStyle) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setStartDate(startDate);
		setEndDate(endDate);
		setVolumeStyle(volumeStyle);
	}
	
	/**
	 * Default constructor. Sets {@link #getVolumeStyle volumeStyle}
	 * property to its default value of 
	 * {@link VolumeStyleEnum#COMPOUND COMPOUND}.
	 * All other properties must be set explicitly before
	 * using this command object.
	 */
	public HistTicksCommand() {
		this(null, null, null, null, VolumeStyleEnum.COMPOUND);
	}
	
	/**
	 * Creates new Historical ticks command, initializing all properties except
	 * <code>volumeStyle</code> one, which defaults to <code>COMPOUND</code>.
	 * 
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 */
	public HistTicksCommand(String exchangeCode, 
			String symbolCode,
			Date startDate,
			Date endDate) {
		this(exchangeCode, symbolCode, startDate, endDate,
				VolumeStyleEnum.COMPOUND);
	}

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
