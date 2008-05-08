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
 * Enumerates server response status values.
 */
public enum StatusEnum {

	OK(1),
	
	ERROR(2);
	
	public final int code;
	
	private StatusEnum(int code) {
		this.code = code;
	}
	
	public static StatusEnum decode(int code) {
		if(code == OK.code) return OK;
		else if(code == ERROR.code) return ERROR;
		return null;
	}
}
