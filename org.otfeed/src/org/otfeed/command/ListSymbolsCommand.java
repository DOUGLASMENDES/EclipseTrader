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

import java.util.Set;

import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTSymbol;
import org.otfeed.protocol.ICommand;

/**
 * Retrieves list of symbols traded on an exchange.
 * </p>
 * Allows to get a full list, or search symbols by
 * prefix or substring. 
 * <p/>
 * Generates {@link OTSymbol symbol} events.
 */
public final class ListSymbolsCommand extends CompletionDelegateHolder implements ICommand {
	
	/**
	 * Type of string matching: prefix or contains.
	 */
	public enum MatchStyleEnum {
		/** Prefix */
		PREFIX, 
		
		/** Contains */
		CONTAINS;
	}

	/**
	 * Creates new list symbols command, initializing all
	 * its properties.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern symbol pattern.
	 * @param types set of types.
	 * @param matchStyle matching style (e.g. PREFIX or CONTAINS).
	 * @param dataDelegate delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			Set<ListSymbolEnum> types,
			MatchStyleEnum matchStyle,
			IDataDelegate<OTSymbol> dataDelegate) {
		setExchangeCode(exchangeCode);
		setSymbolCodePattern(symbolCodePattern);
		setTypes(types);
		setMatchStyle(matchStyle);
		setDataDelegate(dataDelegate);
	}

	/**
	 * Default constructor. Initializes 
	 * {@link #getTypes types} property to the
	 * default value of {@link ListSymbolEnum#ALL ALL}.
	 * Initializes {@link #getSymbolCodePattern symbolCodePattern} property
	 * to the default value of blank (which means 
	 * "any symbol code"). 
	 * You must set other properties explicitly before using 
	 * this command object.
	 */
	public ListSymbolsCommand() {
		this(null, "", ListSymbolEnum.ALL, MatchStyleEnum.PREFIX, null);
	}

	/**
	 * Creates new list symbols command for a single
	 * symbol type.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern symbol pattern.
	 * @param type instrument type.
	 * @param matchStyle matching style (e.g. PREFIX or CONTAINS).
	 * @param dataDelegate data delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			ListSymbolEnum type, 
			MatchStyleEnum matchStyle,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				symbolCodePattern, 
				ListSymbolEnum.combine(type),
				matchStyle,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for a single
	 * symbol type, with wildcard value for the
	 * {@link #getSymbolCodePattern symbolCodePattern} property.
	 * 
	 * @param exchangeCode exchange code.
	 * @param type instrument type.
	 * @param matchStyle matching style (e.g. PREFIX, or CONTAINS).
	 * @param dataDelegate delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			ListSymbolEnum type,
			MatchStyleEnum matchStyle,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				"", 
				ListSymbolEnum.combine(type),
				matchStyle,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for all
	 * symbol types.
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern symbol pattern.
	 * @param matchStyle matching style (e.g. PREFIX or CONTAINS).
	 * @param dataDelegate delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			MatchStyleEnum matchStyle,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				symbolCodePattern, 
				ListSymbolEnum.ALL,
				matchStyle,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for a given set of symbol
	 * types, exchangeCode, and symbolCodePattern.
	 * Match type is "prefix".
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern symbol pattern.
	 * @param types set of types.
	 * @param dataDelegate delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			Set<ListSymbolEnum> types,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, symbolCodePattern, types, 
				MatchStyleEnum.PREFIX, dataDelegate);
	}

	/**
	 * Creates new list symbols command for a single
	 * symbol type.
	 * Match type is "prefix".
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern symbol pattern.
	 * @param type instrument type.
	 * @param dataDelegate delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			ListSymbolEnum type, 
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				symbolCodePattern, 
				ListSymbolEnum.combine(type),
				MatchStyleEnum.PREFIX,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for a single
	 * symbol type, with wildcard value for the
	 * {@link #getSymbolCodePattern symbolCodePattern} property.
	 * 
	 * @param exchangeCode exchange code.
	 * @param type instrument type.
	 * @param dataDelegate data delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			ListSymbolEnum type,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				"", 
				ListSymbolEnum.combine(type),
				MatchStyleEnum.PREFIX,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for all
	 * symbol types. 
	 * Match type is "prefix".
	 * 
	 * @param exchangeCode exchange code.
	 * @param symbolCodePattern cymbol code.
	 * @param dataDelegate data delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			String symbolCodePattern,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				symbolCodePattern, 
				ListSymbolEnum.ALL,
				MatchStyleEnum.PREFIX,
				dataDelegate);
	}

	/**
	 * Creates new list symbols command for all
	 * symbol types, with wildcard value for the
	 * {@link #getSymbolCodePattern symbolCodePattern} property.
	 * Match type is "prefix".
	 * 
	 * @param exchangeCode exchange code.
	 * @param dataDelegate data delegate.
	 */
	public ListSymbolsCommand(
			String exchangeCode,
			IDataDelegate<OTSymbol> dataDelegate) {
		this(exchangeCode, 
				"", 
				ListSymbolEnum.ALL,
				MatchStyleEnum.PREFIX,
				dataDelegate);
	}

	private String exchangeCode;

	/**
	 * Exchange code.
	 * 
	 * @return exchange code.
	 */
	public String getExchangeCode() {
		return exchangeCode;
	}

	/**
	 * Sets the exchange code.
	 * 
	 * @param exchangeCode exchange code.
	 */
	public void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	private String symbolCodePattern;

	/**
	 * Symbol code. Optional parameter. If not specified, defaults to
	 * an empty string, which will act as a wildcard (all symbols
	 * will be returned). It acts as a match prefix when searching 
	 * for symbols. For example, symbolCodePattern value of "GO" may
	 * return "GOOG", "GOK", and "GOAT".
	 * 
	 * @return symbol code.
	 */
	public String getSymbolCodePattern() { 
		return symbolCodePattern; 
	}

	/**
	 * Sets symbol code.
	 * 
	 * @param val symbol code.
	 */
	public void setSymbolCodePattern(String val) {
		symbolCodePattern = val;
	}

	private Set<ListSymbolEnum> types = ListSymbolEnum.ALL;

	/**
	 * Types of symbols to request. Optional property, 
	 * defaults to {@link ListSymbolEnum#ALL}
	 * 
	 * @return set of types.
	 */
	public Set<ListSymbolEnum> getTypes() {
		return types;
	}

	/**
	 * Sets types of symbols.
	 * 
	 * @param val set of symbol types.
	 */
	public void setTypes(Set<ListSymbolEnum> val) {
		types = val;
	}
	
	private MatchStyleEnum matchStyle;
	
	/**
	 * Determines how {@link #getSymbolCodePattern() symbolCodePattern}
	 * is treated. 
	 * 
	 * By default, <code>matchStyle</code> is <code>PREFIX</code>
	 * that means that <code>symbolCodePattern</code> is matched as a 
	 * prefix: all symbols that start with this string
	 * will be returned. If <code>matchStyle</code> is set to
	 * <code>CONTAINS</code>,
	 * all symbols that contain this string will be returned.
	 * 
	 * @return true, if containsMatch is set, false otherwise.
	 */
	public MatchStyleEnum getMatchStyle() {
		return matchStyle;
	}
	
	/**
	 * Sets match style.
	 * 
	 * @param val matchStyle value.
	 */
	public void setMatchStyle(MatchStyleEnum val) {
		matchStyle = val;
	}

	private IDataDelegate<OTSymbol> dataDelegate;

	/**
	 * Delegate to receive {@link OTSymbol} events.
	 * 
	 * @return delegate.
	 */
	public IDataDelegate<OTSymbol> getDataDelegate() {
		return dataDelegate;
	}

	/**
	 * Sets delegate.
	 * 
	 * @param dataDelegate delegate.
	 */
	public void setDataDelegate(IDataDelegate<OTSymbol> dataDelegate) {
		this.dataDelegate = dataDelegate;
	}
}
