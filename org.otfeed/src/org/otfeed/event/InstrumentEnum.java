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

package org.otfeed.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates instrument types.
 */
public enum InstrumentEnum {
	/** Stock */
	STOCK(1),

	/** Index */
	INDEX(2),

	/** Option */
	OPTION(3),

	/** Future */
	FUTURE(4),
	
	/** Single Stock Future */
	SSFUTURE(5);

	public final int code;
	
	private InstrumentEnum(int code) {
		this.code = code;
	}
	
	public final static Map<Integer,InstrumentEnum> decoder
			= new HashMap<Integer,InstrumentEnum>();
	static {
		InstrumentEnum []v = values();
		
		for(int i = 0; i < v.length; i++) {
			if(decoder.put(v[i].code, v[i]) != null) {
				throw new AssertionError("duplicate code in: " + v[i]);
			}
		}
	}
}
