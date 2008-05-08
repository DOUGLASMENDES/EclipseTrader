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

import org.otfeed.protocol.*;

import java.nio.ByteBuffer;


/**
 * OpenTick protocol header. 
 * This is common part to all OpenTick messages.
 */
public final class Header {

	private MessageEnum message;
	private StatusEnum  status;
	private CommandEnum command;
	private int         requestId;

	public Header() { }

	public Header(MessageEnum m, StatusEnum s, CommandEnum c, int r) {
		message = m;
		status  = s;
		command = c;
		requestId = r;
	}

	public Header(ByteBuffer in) {
		readIn(in);
	}

	public MessageEnum getMessage() { return message; }
	public void setMessage(MessageEnum val) { message = val; }

	public StatusEnum getStatus() { return status; }
	public void setStatus(StatusEnum val) { status= val; }

	public CommandEnum getCommand() { return command; }
	public void setCommand(CommandEnum val) { command = val; }

	public int getRequestId() { return requestId; }
	public void setRequestId(int val) { requestId = val; }

	public void writeOut(ByteBuffer out) {
		out.put((byte) message.code);
		out.put((byte) status.code);
		out.put((byte) 0);  // reserved
		out.put((byte) 0);  // reserved
		out.putInt(command.code);
		out.putInt(requestId);
	}

	public void readIn(ByteBuffer in) {
		message = Util.readMessageEnum(in);
		status  = Util.readStatusEnum(in);
		in.get();  // reserved
		in.get();  // reserved
		command = Util.readCommandEnum(in);
		requestId = in.getInt();
	}

	@Override
	public String toString() { 
		return "Header: message=" + message + ", status=" + status
			+ ", command=" + command + ", requestId=" + requestId;
	}
}
