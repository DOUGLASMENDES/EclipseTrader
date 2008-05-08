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
import org.otfeed.protocol.CommandEnum;

import java.nio.ByteBuffer;

/**
 * Common base for all OpenTick session requests. Note that only 
 * {@link LoginRequest} does not have session context, and therefore
 * is not derived from this class. All other requests are session requests.
 */
public abstract class AbstractSessionRequest extends AbstractRequest {

	private String sessionId = "";

	/**
	 * Gets session id string.
	 * @return session id string.
	 */
	public String getSessionId() { return sessionId; }

	/**
	 * Sets session id string.
	 * @param val session id string.
	 */
	public void setSessionId(String val) { sessionId = val; }

	AbstractSessionRequest(CommandEnum command, 
			int requestId, 
			ICompletionDelegate completionDelegate) {
		super(command, requestId, completionDelegate);
	}
	
	/**
	 * Writes out the request header and session string.
	 * Subclasses must override this if needed, and their 
	 * implementation must call this method as the first operation.
	 */
	@Override
	public void writeRequest(ByteBuffer out) {
		super.writeRequest(out);
		Util.writeString(out, sessionId, 64);
	}

	/**
	 * Returns cancel command type.
	 * Some requests can be cancelled by sending a {@link CancelRequest}
	 * to the server. Such requests must override this method
	 * to return the cancel command type.
	 */
	public CommandEnum getCancelCommand() {
		return null;
	}
}
