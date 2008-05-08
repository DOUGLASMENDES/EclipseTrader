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
import org.otfeed.event.OTExchange;
import org.otfeed.protocol.ICommand;

/**
 * Retrieves list of supported exchanges.
 * <p/>
 * This command allows to retrieve a complete list of 
 * exchanges supported by the server.
 * <p/>
 * Generates {@link OTExchange exchange} events.
 * </p>
 * Unless property description says otherwise, all properties 
 * are mandatory.
 */
public final class ListExchangesCommand extends CompletionDelegateHolder implements ICommand {
	
	/**
	 * Creates new list exchanges command, initializing all its
	 * properties.
	 * 
	 * @param dataDelegate delegate.
	 */
	public ListExchangesCommand(IDataDelegate<OTExchange> dataDelegate) {
		setDataDelegate(dataDelegate);
	}
	
	/**
	 * Default constructor. {@link #getDataDelegate DataDelegate} property
	 * must be initiaized before this command object is used.
	 */
	public ListExchangesCommand() { }

	private IDataDelegate<OTExchange> dataDelegate;

	/**
	 * Delegate to receive {@link OTExchange} events.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTExchange> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTExchange> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
