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

import org.otfeed.protocol.ICommand;

/**
 * Defines contract for the connection to the OpenTick service.
 * <p/>
 * This is the central interface of the API. It allows one to
 * prepare and submit a request for data.
 * <p/>
 * Note that connection is asynchronous, which means that connection/login 
 * process might still
 * be in progress by the time user gets object implementing this interface.
 * But even if so, nothing prevents user from using it: submitted requests
 * will be queued and sent as soon as connection is established.
 * <p/>
 * Implementations must be thread-safe. All implementations 
 * provided by <code>org.otfeed</code> driver are thread-safe.
 */
public interface IConnection {

	/**
	 * Closes connection to the server.
	 *
	 * All pending requests will receive onError message.
	 */
	public void shutdown();

	/**
	 * Blocks till connection terminates (on error, or as a result of
	 * calling shutdown() method).
	 */
	public void waitForCompletion();

	/**
	 * Blocks for the earliest of (i) connection termination, or (ii)
	 * timeout expiration.
	 *
	 * @param millis for how long to block.
	 * @return true if connection has terminated, false if
	 *          timeout has expired.
	 */
	public boolean waitForCompletion(long millis);

	/**
	 * Schedules a job for execution in the event-dispatching thread.
	 * All listener methods are called by event-dispatching thread.
	 * To avoid a need to make listener objects thread-safe, programmer
	 * can instead use this method when there is a need to interact
	 * with listener objects from a different thread.
	 *
	 * Note that these jobs will be executed in order they were sumbitted.
	 * Also, these jobs are considered a priority for the event thread.
	 *
	 * @param job a job to be submitted.
	 */
	public void runInEventThread(Runnable job);

	/**
	 * Prepares a request. This is the main functionality
	 * of {@link IConnection} interface. It allowas caller to
	 * prepare a request to the OpenTick server.
	 * <p>
	 * Command parameter must be one of the known request 
	 * commands, see {@link org.otfeed.command} package.
	 * 
	 * See also: {@link IRequest}.
	 *
	 * @param command object describing request parameters. 
	 *
	 * @return IRequest handle.
	 */
	public IRequest prepareRequest(ICommand command);
}
