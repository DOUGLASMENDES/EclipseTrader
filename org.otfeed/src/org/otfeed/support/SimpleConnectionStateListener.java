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

import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.event.OTHost;
import org.otfeed.protocol.ErrorEnum;

/**
 * Implementation of {@link IConnectionStateListener}
 * that prints events to the <code>PrintWriter</code>.
 * Default destination for printing is standard error 
 * output stream.
 */
public class SimpleConnectionStateListener implements IConnectionStateListener {

	// set autoflush to true
	private PrintWriter out = new PrintWriter(System.err, true);

	/**
	 * Determines the print destination.
	 * default is System.err.
	 * 
	 * @return output destination.
	 */
	public PrintWriter getPrintWriter() {
		return out;
	}

	/**
	 * Sets the destination.
	 * 
	 * @param val destination.
	 */
	public void setPrintWriter(PrintWriter val) {
		out = val;
	}

	public void onConnected() {
		out.println("Connected");
	}

	public void onConnecting(OTHost arg0) {
		out.println("Connecting to: " + arg0);
	}

	public void onError(OTError arg0) {
		if(arg0.getCode() == ErrorEnum.E_OTFEED_OK.code) {
			out.println("All done");
		} else {
			out.println("Error: " + arg0);
		}
	}

	public void onLogin() {
		out.println("Logged in");
	}

	public void onRedirect(OTHost arg0) {
		out.println("Redirected to: " + arg0);
	}
}
