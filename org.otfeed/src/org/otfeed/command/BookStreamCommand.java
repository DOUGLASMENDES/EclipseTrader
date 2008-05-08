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
import org.otfeed.event.OTBookCancel;
import org.otfeed.event.OTBookChange;
import org.otfeed.event.OTBookDelete;
import org.otfeed.event.OTBookExecute;
import org.otfeed.event.OTBookOrder;
import org.otfeed.event.OTBookPriceLevel;
import org.otfeed.event.OTBookPurge;
import org.otfeed.event.OTBookReplace;
import org.otfeed.protocol.ICommand;

/**
 * Requests real-time (live) book events.
 * <p/>
 * Generates {@link OTBookOrder order}, {@link OTBookChange},
 * {@link OTBookCancel}, {@link OTBookReplace replace}, {@link OTBookDelete delete},
 * {@link OTBookExecute execute}, and {@link OTBookPriceLevel price level}
 * events.
 */
public final class BookStreamCommand 
		extends ExchangeAndSymbolHolder implements ICommand {

	/**
	 * Creates new book stream command, initializing all
	 * its properties, except delegates.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 */
	public BookStreamCommand(String exchangeCode,
			String symbolCode) { 
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
	}

	/**
	 * Default constructor. 
	 * Properties must be set explicitly before
	 * using this command object.
	 */
	public BookStreamCommand() { 
		this(null, null);
	}

	private IDataDelegate<OTBookOrder>   orderDelegate;
	public IDataDelegate<OTBookOrder> getOrderDelegate() {
		return orderDelegate;
	}
	public void setOrderDelegate(IDataDelegate<OTBookOrder> val) {
		orderDelegate = val;
	}
	
	private IDataDelegate<OTBookChange>  changeDelegate;
	public IDataDelegate<OTBookChange> getChangeDelegate() {
		return changeDelegate;
	}
	public void setChangeDelegate(IDataDelegate<OTBookChange> val) {
		changeDelegate = val;
	}

	private IDataDelegate<OTBookReplace> replaceDelegate;
	public IDataDelegate<OTBookReplace> getReplaceDelegate() {
		return replaceDelegate;
	}
	public void setReplaceDelegate(IDataDelegate<OTBookReplace> val) {
		replaceDelegate = val;
	}
	
	private IDataDelegate<OTBookCancel>  cancelDelegate;
	public IDataDelegate<OTBookCancel> getCancelDelegate() {
		return cancelDelegate;
	}
	public void setCancelDelegate(IDataDelegate<OTBookCancel> val) {
		cancelDelegate = val;
	}

	private IDataDelegate<OTBookPurge>   purgeDelegate;
	public IDataDelegate<OTBookPurge> getPurgeDelegate() {
		return purgeDelegate;
	}
	public void setPurgeDelegate(IDataDelegate<OTBookPurge> val) {
		purgeDelegate = val;
	}
	
	private IDataDelegate<OTBookExecute> executeDelegate;
	public IDataDelegate<OTBookExecute> getExecuteDelegate() {
		return executeDelegate;
	}
	public void setExecuteDelegate(IDataDelegate<OTBookExecute> val) {
		executeDelegate = val;
	}

	private IDataDelegate<OTBookDelete>  deleteDelegate;
	public IDataDelegate<OTBookDelete> getDeleteDelegate() {
		return deleteDelegate;
	}
	public void setDeleteDelegate(IDataDelegate<OTBookDelete> val) {
		deleteDelegate = val;
	}

	private IDataDelegate<OTBookPriceLevel> priceLevelDelegate;
	public IDataDelegate<OTBookPriceLevel> getPriceLevelDelegate() {
		return priceLevelDelegate;
	}
	public void setPriceLevelDelegate(IDataDelegate<OTBookPriceLevel> val) {
		priceLevelDelegate = val;
	}
}
