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

import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.OTError;

/**
 * Base for synthetic requests: a snapshot request followed by
 * a stream request.
 */
public abstract class AbstractStreamWithSnapshotRequest extends RequestJob {

	final IConnection connection;

	private IRequest snapshotRequest;
	private IRequest streamRequest;

	abstract IRequest prepareSnapshotRequest(IConnection connection, ICompletionDelegate d);
	abstract IRequest prepareStreamRequest(IConnection connection, ICompletionDelegate d);

	/**
	 * Creates new object.
	 * 
	 * @param connection connection reference.
	 * @param completionDelegate client's completion delegate.
	 */
	AbstractStreamWithSnapshotRequest(
			IConnection connection, 
			ICompletionDelegate completionDelegate) {
		super(completionDelegate);

		Check.notNull(connection, "connection");
		this.connection = connection;
	}

	public final void prepareRequest() {

		this.snapshotRequest = prepareSnapshotRequest(connection, new ICompletionDelegate() {
			public void onDataEnd(OTError error) {
				if(error != null) {
					fireCompleted(error);
					streamRequest.cancel();
				}
			}
		});

		this.streamRequest   = prepareStreamRequest(connection, new ICompletionDelegate() {
			public void onDataEnd(OTError error) {
				fireCompleted(error);
				snapshotRequest.cancel();
			}
		});
	}

	public void submit() {
		snapshotRequest.submit();
		streamRequest.submit();
	}

	public void cancel() {
		snapshotRequest.cancel();
		streamRequest.cancel();
	}
}
