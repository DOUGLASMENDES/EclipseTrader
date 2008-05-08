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

package org.otfeed.support;

/**
 * Interface specifying contract for
 * objects that write quote data out.
 */
public interface IDataWriter {

	/**
	 * Writes data out, including optional id string.
	 * 
	 * @param id string, identifying the quote source.
	 * @param data quote data.
	 */
	public void writeData(String id, Object data);
	
	/**
	 * Closes stream.
	 * 
	 * If implementation uses internal buffering, its content 
	 * is written out. 
	 *
	 */
	public void close();
}
