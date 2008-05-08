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
import org.otfeed.event.OTDividend;
import org.otfeed.protocol.ICommand;

/**
 * Requests dividend info.
 * <p/>
 * Generates {@link OTDividend dividend} events.
 */
public final class DividendsCommand 
		extends ExchangeSymbolAndDatesHolder implements ICommand {

	/**
	 * Creates new dividends command, initializing 
	 * all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param startDate start date.
	 * @param endDate end date.
	 * @param dataDelegate data delegate.
	 */
	public DividendsCommand(String exchangeCode,
			String symbolCode,
			Date startDate,
			Date endDate,
			IDataDelegate<OTDividend> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setStartDate(startDate);
		setEndDate(endDate);
		setDataDelegate(dataDelegate);
	}

	/**
	 * Default constructor. All properties must 
	 * be initialized
	 * explicitly before using this command object.
	 */
	public DividendsCommand() { 
		this(null, null, null, null, null);
	}
	
	private IDataDelegate<OTDividend> dataDelegate;

	/**
	 * Delegate to receive {@link OTDividend} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTDividend> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTDividend> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
