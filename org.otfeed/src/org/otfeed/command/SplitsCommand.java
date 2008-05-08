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

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTSplit;
import org.otfeed.protocol.ICommand;

/**
 * Request for the stock split information.
 * <p/>
 * Generates {@link OTSplit split} events.
 */
public final class SplitsCommand 
		extends ExchangeAndSymbolHolder implements ICommand {

	/**
	 * Creates new splits command, initializing all its
	 * properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 * @param dataDelegate delegate.
	 */
	public SplitsCommand(String exchangeCode,
			String symbolCode,
			Date startDate,
			Date endDate,
			IDataDelegate<OTSplit> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setStartDate(startDate);
		setEndDate(endDate);
		setDataDelegate(dataDelegate);
	}
	
	/**
	 * Default constructor. All properties must be set
	 * explicitly before using this command object.
	 *
	 */
	public SplitsCommand() {
		this(null, null, null, null, null);
	}

	private Date startDate;
	
	/**
	 * Start date/time for the split info.
	 * 
	 * @return Start date/time.
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Sets start date/time.
	 * 
	 * @param val Start date/time.
	 */
	public void setStartDate(Date val) {
		startDate = val;
	}
	
	private Date endDate;
	
	/**
	 * End date/time for the split info.
	 * 
	 * @return End date/time.
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Sets end date/time.
	 * 
	 * @param val end date/time.
	 */
	public void setEndDate(Date val) {
		endDate = val;
	}
	
	private IDataDelegate<OTSplit> dataDelegate;

	/**
	 * Delegate to receive {@link OTSplit} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTSplit> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTSplit> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
