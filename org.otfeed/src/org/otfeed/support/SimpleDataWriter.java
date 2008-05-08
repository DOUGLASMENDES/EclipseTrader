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

import java.io.PrintWriter;


/**
 * Class that writes events to a PrintWriter (defaults to 
 * terminal), using Java's <code>toStirng()</code> formatting.
 */
public class SimpleDataWriter implements IDataWriter {
	
	// set autoflush property to true
	private PrintWriter out = new PrintWriter(System.out, true);

	/**
	 * Determines the output destination.
	 * Defaults to System.out.
	 * 
	 * @return destination.
	 */
	public PrintWriter getPrintWriter() {
		return out;
	}
	
	/**
	 * Sets the output destination.
	 * 
	 * @param val destination.
	 */
	public void setPrintWriter(PrintWriter val) {
		out = val;
	}

	public void writeData(String id, Object data) {
		if(id != null) out.print("[" + id + "] ");
		out.println(data);
	}
	
	public void close() {
		out.flush(); // FIXME: maybe we should close() ot?
	}
}
