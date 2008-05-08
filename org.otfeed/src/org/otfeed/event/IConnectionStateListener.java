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

/**
 * Listener to monitor connection progress.
 *
 * Connection process goes thru the following states:
 * <ol>
 *   <li> Connecting: a host/port has been picked from the
 *                    pool and system is trying to connect.
 *                    Next state could be either Error (if a fatal
 *                    error occurs), Connected (if connectred successfully),
 *                    or Connecting (if could not connect, but there are other
 *                    hosts to try).
 *   <li> Connected:  successfully connected and received login response.
 *                    Next state could be Error (if login failed), Redirect
 *                    (if we were asked to redirect to another host), or
 *                    Login (if login succeeded). Note that login errors are 
 *                    fatal: system will not attempt to use another host in
 *                    the pool.
 *   <li> Redirect:   We were asked to re-direct to the different host.
 *                    Next state would be Connecting.
 *   <li> Login:      login was successfull. Next state is Error (when
 *                    connection breaks due to communication error or
 *                    user request to close/shutdown it).
 *   <li> Error:      Final state. Always reached.
 * </ol>
 *
 */
public interface IConnectionStateListener {

	/**
	 * Is called when connection enters "Connecting" state.
	 *
	 * @param addr server address.
	 */
	public void onConnecting(OTHost addr);

	/**
	 * Is called when connection enters "Connected" state.
	 */
	public void onConnected();

	/**
	 * Is called when connection enters "Redirect" state.
	 *
	 * @param addr redirect address.
	 */
	public void onRedirect(OTHost addr);

	/**
	 * Is called when connection enters "Login" state.
	 */
	public void onLogin();

	/**
	 * Is called when connection terminates.
	 */
	public void onError(OTError error);
}
