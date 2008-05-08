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


import org.otfeed.event.ICompletionDelegate;
import org.otfeed.event.OTError;
import org.otfeed.protocol.CommandEnum;

import java.nio.ByteBuffer;

/**
 * Represents a request to cancel an existing tick, book, option chain, or
 * historical data request.
 */
public final class CancelRequest extends AbstractSessionRequest {

	private final int targetRequestId;

	public CancelRequest(CommandEnum cancelCommand,
				int requestId, 
				int targetRequestId,
				ICompletionDelegate completionDelegate) {
		super(cancelCommand, requestId, completionDelegate);
		this.targetRequestId = targetRequestId;
	}

	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);
		out.putInt(targetRequestId);
	}

	public void handleError(OTError error) { }
}
