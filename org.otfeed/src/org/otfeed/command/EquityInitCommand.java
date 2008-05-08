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

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTEquityInit;
import org.otfeed.protocol.ICommand;

/**
 * Request for the detailed equity information.
 * <p/>
 * Generates {@link OTEquityInit equity init} event.
 */
public final class EquityInitCommand 
		extends ExchangeAndSymbolHolder implements ICommand {
	
	/**
	 * Creates new equity init command, initializing all
	 * its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param dataDelegate delegate.
	 */
	public EquityInitCommand(String exchangeCode,
			String symbolCode,
			IDataDelegate<OTEquityInit> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setDataDelegate(dataDelegate);
	}

	/**
	 * Default constructor. All properties must be initialized
	 * explicitly before using this command object.
	 */
	public EquityInitCommand() {
		this(null, null, null);
	}

	private IDataDelegate<OTEquityInit> dataDelegate;

	/**
	 * Delegate to receive {@link OTEquityInit} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTEquityInit> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTEquityInit> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
