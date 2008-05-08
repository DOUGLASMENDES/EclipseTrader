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

package org.otfeed.protocol.connector;

import java.io.IOException;

import org.otfeed.event.OTError;

/**
 * Exception thrown on a login error.
 * Wraps {@link OTError} object, that contains server response.
 */
class LoginFailureException extends IOException {

	private static final long serialVersionUID = 3894948536644732832L;

	/**
	 * OTError object, describing server response.
	 */
	public final OTError error;

	public LoginFailureException(OTError e) { super(); error = e; }
}
