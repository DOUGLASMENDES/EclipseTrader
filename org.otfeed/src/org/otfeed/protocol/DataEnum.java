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

package org.otfeed.protocol;

/**
 * Enumerates types of data objects received from the server.
 */
public enum DataEnum {

	EOD              (0),
	QUOTE            (1),
	MMQUOTE          (2),
	TRADE            (3),
	BBO              (4),
	BOOK_CANCEL      (5),
	BOOK_CHANGE      (6),
	BOOK_DELETE      (7),
	BOOK_EXECUTE     (8),
	BOOK_ORDER       (9),
	BOOK_PRICE_LEVEL (10),
	BOOK_PURGE       (11),
	BOOK_REPLACE     (12),
	HALT             (13),
	SPLIT            (14),
	DIVIDEND         (15),
	SYMBOL_CHANGE    (16),
	EQUITY_INIT      (17),
	OPTION_INIT      (18),
	OHLC             (50),
	OHL_TODAY        (51);

	public final int code;
	private DataEnum(int code) {
		this.code = code;
	}
}
