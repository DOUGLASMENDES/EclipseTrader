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

import org.otfeed.protocol.ICommand;

/**
 * Request for option chain events snapshot. Snapshot is the 
 * set of the latest events of the given type.
 * <p/>
 * Generates {@link org.otfeed.event.OTQuote quote}, 
 * {@link org.otfeed.event.OTTrade trade},
 * {@link org.otfeed.event.OTMMQuote marker-maker quote}, and 
 * {@link org.otfeed.event.OTBBO bbo} events.
 */
public final class OptionChainSnapshotCommand 
		extends ExchangeSymbolAndQuoteDelegateHolder implements ICommand {

	/**
	 * Creates new option chain snapshot command, initializing 
	 * all its properties, except delegates.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param expiration expiration.
	 * @param strike strike.
	 * @param volumeStyle volume reporting style.
	 */
	public OptionChainSnapshotCommand(String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			PriceRange strike,
			VolumeStyleEnum volumeStyle) {
		setExchangeCode(exchangeCode);
		setSymbolCode(symbolCode);
		setExpiration(expiration);
		setStrike(strike);
		setVolumeStyle(volumeStyle);
	}

	/**
	 * Default constructor. Initializes
	 * {@link #getExpiration expiration} property to
	 * its default value of <code>null</code> (meaning "any").
	 * Initializes {@link #getStrike strike} property to
	 * its default value of <code>null</code> (meaning "any").
	 * Initializes {@link #getVolumeStyle volumeStyle}
	 * property to <code>COMPOUND</code>.
	 * All other properties must be set explicitly before using 
	 * this command object.
	 */
	public OptionChainSnapshotCommand() {
		this(null, null, null, null, VolumeStyleEnum.COMPOUND);
	}

	/**
	 * Creates new option chain snapshot command, initializing 
	 * all its properties, except <code>volumeStyle<code>
	 * property, which defaults to <code>COMPOUND</code>.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCode symbol code.
	 * @param expiration expiration.
	 * @param strike strike.
	 */
	public OptionChainSnapshotCommand(String exchangeCode,
			String symbolCode,
			MonthAndYear expiration,
			PriceRange strike) {
		this(exchangeCode, symbolCode, 
				expiration, strike, VolumeStyleEnum.COMPOUND);
	}

	private MonthAndYear expiration;
	
	/**
	 * Option expiration date (month and year). This property is
	 * optional. Defaults to null, which is interpreted as 
	 * "any expiration date".
	 * 
	 * @return option expiration date.
	 */
	public MonthAndYear getExpiration() {
		return expiration;
	}
	
	/**
	 * Sets option expiration date.
	 * 
	 * @param val option expiration date.
	 */
	public void setExpiration(MonthAndYear val) {
		expiration = val;
	}

	private PriceRange strike;
	
	/**
	 * Price range for the option strike. This property
	 * is optional. Defaults to null, which means 
	 * "any strike price".
	 * 
	 * @return option strike price range.
	 */
	public PriceRange getStrike() {
		return strike;
	}
	
	/**
	 * Sets option strike price range.
	 * 
	 * @param val strike price range.
	 */
	public void setStrike(PriceRange val) {
		strike = val;
	}
}
