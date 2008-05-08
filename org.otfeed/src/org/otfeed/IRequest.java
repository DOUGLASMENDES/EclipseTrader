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

import org.otfeed.event.OTError;

/**
 * Defines contract for the Request handler.
 *
 * Instance of {@link IRequest} is returned by 
 * {@link IConnection#prepareRequest(org.otfeed.protocol.ICommand)}
 * metod.
 * <p/>
 * Use this handle to actually {@link #submit} the asynchronous request, 
 * to {@link #cancel} it or to {@link #waitForCompletion() block} till request is complete.
 * <p/>
 * Implementations must be thread-safe. All implementation supplied by <code>org.otfeed</code>
 * are thread-safe.
 */
public interface IRequest {

	/**
	 * Submits the request.
	 * If request is already active, does nothing.
	 */
	public void submit();

	/**
	 * Cancels the request.
	 * If request is not active, does nothing.
	 */
	public void cancel();

	/**
	 * Returns request completion status.
	 * After request is completed, one may want to check 
	 * {@link #getError() error} property to see whether 
	 * completion was normal or an error has occured.
	 *
	 * @return true if request has completed, false otherwise.
	 */
	public boolean isCompleted();
	
	/**
	 * Returns error. If request is still running, or has
	 * completed normally, returns null.
	 * 
	 * @return error.
	 */
	public OTError getError();

	/**
	 * Waits for request to complete.
	 *
	 * @param  millis timeout value in millis.
	 * @return true if request completed or has been successfully
	 *         cancelled, false on timeout.
	 */
	public boolean waitForCompletion(long millis);

	/**
	 * Waits for request to complete (indefinetely).
	 */
	public void waitForCompletion();
}
