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

import org.otfeed.event.IConnectionStateListener;

/**
 * Low-level communication interface: producer of session streamer service.
 */
public interface ISessionStreamerFactory {

	/**
	 * Creates a connection.
	 * This is an asynchronous call, that will not block. To monitor
	 * connection progress, use <code>listener</code> parameter.
	 *
	 * @param listener a listener to receive connection state change
	 *        events.
	 */
	public ISessionStreamer connect(IConnectionStateListener listener)
			throws IOException;

	/**
	 * Shuts down connection factory.
	 * If a connect is in progress, it
	 * will be interrupted.
	 */
	public void shutdown();
}
