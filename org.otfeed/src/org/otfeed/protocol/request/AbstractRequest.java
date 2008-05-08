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
import org.otfeed.protocol.MessageEnum;
import org.otfeed.protocol.CommandEnum;
import org.otfeed.protocol.StatusEnum;
import org.otfeed.protocol.ProtocolException;

import java.nio.ByteBuffer;

/**
 * Common base for all OpenTick requests.
 */
public abstract class AbstractRequest extends RequestJob {

	/**
	 * Request status. Returned by <code>handleMessage</code> to
	 * indicate whether more messages are expected.
	 */
	public enum JobStatus {
		/**
		 * Request active, more messages may follow.
		 */
		ACTIVE, 

		/**
		 * Request finished. No more messages expected.
		 */
		FINISHED 
	}

	private final CommandEnum command;
	private final int requestId;

	/**
	 * Returns command type of this request.
	 */
	public final CommandEnum getCommand() { return command; }

	/**
	 * Returns request id number.
	 */
	public final int getRequestId() { return requestId; }

	/**
	 * Creates request.
	 *
	 * @param command command type.
	 * @param requestId request id number.
	 */
	AbstractRequest(CommandEnum command, 
			int requestId,
			ICompletionDelegate completionDelegate) {
		super(completionDelegate);
		
		Check.notNull(command,  "command");

		this.command   = command;
		this.requestId = requestId;
	}

	/**
	 * Abstract method that writes out request body.
	 *
	 * This implementation only writes the request header.
	 * It is meant to be overwritten by concrete request implementation.
	 * Concrete implementation must call <code>super.writeRequest</code>
	 * at the top of its method body.
	 *
	 * @param out output buffer.
	 */
	public void writeRequest(ByteBuffer out) {
		Header header = new Header(
			MessageEnum.REQUEST,
			StatusEnum.OK,
			command,
			requestId);

		header.writeOut(out);
	}

	/**
	 * Abstract method to handle response message.
	 *
	 * This implementation only validates request and command.
	 * Must be overwritten by concrete implementation.
	 * Some concrete implementations may call this one at the beginning to
	 * check the sanity of the response.
	 *
	 * @param header parsed response header.
	 * @param in     input buffer with the rest of the 
	 *               data (after header has been parsed).
	 */
	public JobStatus handleMessage(Header header, ByteBuffer in) {

		if(header.getCommand() != command) {
			throw new ProtocolException("mismatch in command type: " 
				+ header.getCommand() + ", expected: " + command,
				in);
		}

		if(header.getRequestId() != requestId) {
			throw new ProtocolException("mismatch in requestId: " 
				+ header.getRequestId() + ", expected: " + requestId,
				in);
		}

		return JobStatus.FINISHED;
	}

	@Override
	public String toString() {
		return "Request: requestId=" + requestId + ", command=" + command;
	}
}
