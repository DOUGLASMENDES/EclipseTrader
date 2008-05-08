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

package org.otfeed.protocol.request;

/**
 * Helper class to validate parameters.
 */
public final class Check {
	
	private Check() { }
	
	/**
	 * Throws NullPointerException with the appropriate message
	 * if <code>obj</code> parameter is null.
	 * 
	 * @param obj object to check.
	 * @param name name of the object to display as part of the
	 * 		exception message.
	 */
	public static void notNull(Object obj, String name) {
		if(obj == null) {
			throw new NullPointerException(name + " can not be null");
		}
	}
	
	public static void isTrue(boolean predicate, String message) {
		if(!predicate) {
			throw new IllegalArgumentException(message);
		}
	}
}
