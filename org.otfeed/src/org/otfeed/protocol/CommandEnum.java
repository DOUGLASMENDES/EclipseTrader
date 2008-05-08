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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates command types (request and response types).
 */
public enum CommandEnum {

	LOGIN                   	(1),
	LOGOUT                  	(2),

	REQUEST_TICK_STREAM     (3),
	REQUEST_TICK_STREAM_EX  (15),
	CANCEL_TICK_STREAM      (4),

	REQUEST_HIST_DATA       (5),
	REQUEST_HIST_TICKS      (17),
	CANCEL_HIST_DATA        (6),

	REQUEST_LIST_EXCHANGES  (7),
	REQUEST_LIST_SYMBOLS    (8),

	HEARTBEAT               (9),

	REQUEST_EQUITY_INIT     (10),
	REQUEST_OPTION_CHAIN    (11),
	REQUEST_OPTION_CHAIN_EX (16),
	CANCEL_OPTION_CHAIN     (12),

	REQUEST_BOOK_STREAM     (13),
	REQUEST_BOOK_STREAM_EX  (21),
	CANCEL_BOOK_STREAM      (14),

	REQUEST_SPLITS          (18),
	REQUEST_DIVIDENDS       (19),

	REQUEST_HIST_BOOKS      (20),

	REQUEST_OPTION_CHAIN_U  (22),
	REQUEST_OPTION_INIT     (23),
	REQUEST_LIST_SYMBOLS_EX (24),

	REQUEST_TICK_SNAPSHOT   (25),
	REQUEST_OPTION_CHAIN_SNAPSHOT (26);

	public final int code;
	private CommandEnum(int code) {
		this.code = code;
	}
	
	public static final Map<Integer,CommandEnum> decoder
				= new HashMap<Integer,CommandEnum>();
	static {
		CommandEnum []v = values();
		for(int i = 0; i < v.length; i++) {
			if(decoder.put(v[i].code, v[i]) != null) {
				throw new AssertionError("duplicate code for " + v[i]);
			}
		}
	}
}
