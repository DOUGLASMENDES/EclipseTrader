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
import org.otfeed.event.OTTodaysOHL;
import org.otfeed.protocol.ICommand;

/**
 * Request for the current (today's) OHL (open, high, low) info.
 * <p/>
 * Generates {@link OTTodaysOHL today's ohl} events.
 */
public final class TodaysOHLCommand 
		extends ExchangeAndSymbolHolder implements ICommand {

	/**
	 * Creates new todays OHL command, initializing all
	 * its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param dataDelegate delegate.
	 */
	public TodaysOHLCommand(String exchangeCode,
			String symbolCode,
			IDataDelegate<OTTodaysOHL> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setDataDelegate(dataDelegate);
	}
	
	/**
	 * Default constructor. All properties must be
	 * set explicitly before using this command object.
	 */
	public TodaysOHLCommand() {
		this(null ,null, null);
	}

	private IDataDelegate<OTTodaysOHL> dataDelegate;

	/**
	 * Delegate to receive {@link OTTodaysOHL} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTTodaysOHL> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTTodaysOHL> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
