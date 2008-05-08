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

import java.util.HashMap;
import java.util.Map;

import org.otfeed.event.OTBookDelete;

/**
 * Enumerates possible types for the {@link OTBookDelete}
 * event.
 * 
 * @see OTBookDelete
 */
public enum BookDeleteTypeEnum {

	/** Order delete */
	ORDER		('1'), 

	/** Previous delete */
	PREVIOUS	('2'), 
	
	/** Delete of all */
	ALL			('3'), 
	
	/** After delete */
	AFTER		('A');
	
	public final int code;
	
	private BookDeleteTypeEnum(int code) {
		this.code = code;
	}
	
	public final static Map<Integer,BookDeleteTypeEnum> decoder
			= new HashMap<Integer,BookDeleteTypeEnum>();
	static {
		BookDeleteTypeEnum []v = values();
		for(int i = 0; i < v.length; i++) {
			if(decoder.put(v[i].code, v[i]) != null) {
				throw new AssertionError("duplicate book delete type code for: " + v[i]);
			}
		}
	}
}
