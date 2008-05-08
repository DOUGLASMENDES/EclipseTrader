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

package org.otfeed;

import org.otfeed.event.IConnectionStateListener;


/**
 * Defines contract for the connection factory service.
 * <p/>
 * This is the central interface of the <code>org.otfeed</code> API. 
 * It allows one to establish a connection to the
 * OpenTick server.
 * <p/>
 * Implementations must be thread-safe. All implementations provided
 * by <code>org.otfeed</code> driver are thread-safe.
 */
public interface IConnectionFactory {

	/**
	 * Connects to the server. This call does not block, the
	 * connection process is started asynchronously, in
	 * a separate thread (event-dispatching thread).
	 * <p/>
	 * To monitor connection progress, use <code>listener</code>
	 * parameter.
	 * 
	 * @param listener listener to receive connection state events. This parameter
	 *                 may be null, to indicate that there is no interest in monitoring
	 *                 connection status.
	 */
	public IConnection connect(IConnectionStateListener listener);
}
