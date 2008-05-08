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
import org.otfeed.event.OTOptionInit;
import org.otfeed.protocol.ICommand;

/**
 * Request for option detailed information.
 * <p/>
 * Generates {@link OTOptionInit} event.
 */
public final class OptionInitCommand 
		extends ExchangeAndSymbolHolder implements ICommand {

	/**
	 * Creates new option init command, initializing 
	 * all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param expiration expiration.
	 * @param strike strike.
	 * @param dataDelegate delegate.
	 */
	public OptionInitCommand(String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			PriceRange strike,
			IDataDelegate<OTOptionInit> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setExpiration(expiration);
		setStrike(strike);
		setDataDelegate(dataDelegate);
	}
	
	/**
	 * Default constructor. Initializes
	 * {@link #getExpiration expiration} property to its 
	 * default value of <code>null</code> (meaning "any").
	 * Initializes {@link #getStrike strike} property to
	 * its default value of <code>null</code> (meaning "any").
	 * All other properties must be explicitly set before 
	 * using this command object.
	 */
	public OptionInitCommand() {
		this(null, null, null, null, null);
	}
	
	/**
	 * Creates new option init command with any expiration,
	 * initializing all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param strike strike.
	 * @param dataDelegate delegate.
	 */
	public OptionInitCommand(String exchangeCode,
			String symbolCode,
			PriceRange strike,
			IDataDelegate<OTOptionInit> dataDelegate) {
		this(exchangeCode, symbolCode, null, strike, dataDelegate);
	}

	/**
	 * Creates new option init command with any strike,
	 * initializing all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param expiration expiration.
	 * @param dataDelegate delegate.
	 */
	public OptionInitCommand(String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			IDataDelegate<OTOptionInit> dataDelegate) {
		this(exchangeCode, symbolCode, expiration, null, dataDelegate);
	}

	/**
	 * Creates new option init command with any strike and
	 * any expiration date,
	 * initializing all its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param dataDelegate delegate.
	 */
	public OptionInitCommand(String exchangeCode,
			String symbolCode,
			IDataDelegate<OTOptionInit> dataDelegate) {
		this(exchangeCode, symbolCode, null, null, dataDelegate);
	}

	private MonthAndYear expiration;
	
	/**
	 * Expiration month and year for the option.
	 * This is optional property. Defaults to null, which
	 * is interpreted as any expiration date.
	 * 
	 * @return expiration month and year.
	 */
	public MonthAndYear getExpiration() {
		return expiration;
	}
	
	/**
	 * Sets expiration month and year.
	 * 
	 * @param val expiration month and year.
	 */
	public void setExpiration(MonthAndYear val) {
		expiration = val;
	}
	
	private PriceRange strike;
	
	/**
	 * Price range for the option strike.
	 * This property is optional. Defaults to null, which 
	 * means "any strike".
	 * 
	 * @return strike price range.
	 */
	public PriceRange getStrike() {
		return strike;
	}
	
	/**
	 * Sets strike price range.
	 * 
	 * @param val end date/time.
	 */
	public void setStrike(PriceRange val) {
		strike = val;
	}
	
	private IDataDelegate<OTOptionInit> dataDelegate;

	/**
	 * Delegate to receive {@link OTOptionInit} events.
	 * This parameter is mandatory.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTOptionInit> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTOptionInit> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
