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
 * Enumerates message types.
 */
public enum MessageEnum {

	REQUEST         (1),
	RESPONSE        (2),

	END_OF_DATA     (10),
	END_OF_REQUEST  (20),
	END_OF_SNAPSHOT (30);

	public final int code;
	private MessageEnum(int code) {
		this.code = code;
	}
	
	public final static Map<Integer,MessageEnum> decoder
				= new HashMap<Integer,MessageEnum>();
	static {
		MessageEnum[] v = MessageEnum.values();
		
		for(int i = 0; i < v.length; i++) {
			if(decoder.put(v[i].code, v[i]) != null) {
				throw new AssertionError("duplicate code in: " + v[i]);
			}
		}
	}
}
