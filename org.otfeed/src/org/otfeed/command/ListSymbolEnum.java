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

import java.util.EnumSet;
import java.util.Set;
import java.util.Collections;

/**
 * Enumerates symbol types.
 * <p/>
 * See also: {@link org.otfeed.event.OTSymbol}.
 */
public enum ListSymbolEnum {

	/** Stock */
	STOCK    (0x01),
	
	/** Index */
	INDEX    (0x02),
	
	/** Option */
	OPTION   (0x04),
	
	/** Future */
	FUTURE   (0x08),
	
	/** Single stock future */
	SSFUTURE (0x10),
	
	/** Top ten */
	TOP_TEN  (0x20);
	
	public static final int CONTAINS_FLAG = 0x20000;
	
	public final int code;

	private ListSymbolEnum(int code) {
		this.code = code;
	}

	/**
	 * Allows caller to build a set from a list of ListSymbolEnum values.
	 * <p>
	 * Example:
	 * <pre>
	 *   Set<ListSymbolEnum> indexAndOption = ListSymbolEnum.combine(
	 *                    ListSymbolEnum.INDEX, ListSymbolEnum.OPTION);
	 * </pre>
	 *
	 * @param symbol list of symbol types.
	 * @throws IllegalArgumentException if same symbol type appears more
	 *        than once in the parameter list.
	 */
	public static final Set<ListSymbolEnum> combine(ListSymbolEnum ... symbol) {
		Set<ListSymbolEnum> set = EnumSet.noneOf(ListSymbolEnum.class);

		for(int i = 0; i < symbol.length; i++) {
			if(set.add(symbol[i]) == false) {
				throw new IllegalArgumentException(
					"dumplicate symbol: " + symbol[i]);
			}
		}

		return Collections.unmodifiableSet(set);
	}

	/**
	 * Set of all possible symbol types.
	 */
	public static final Set<ListSymbolEnum> ALL 
			= combine(STOCK, INDEX, OPTION, SSFUTURE, TOP_TEN);
}
