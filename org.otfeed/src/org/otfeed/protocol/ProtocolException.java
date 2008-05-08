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

package org.otfeed.protocol;

import org.otfeed.support.BufferFormat;

import java.nio.ByteBuffer;

/**
 * Runtime exception type to be thrown when a protocol violation occurs.
 * This exception holds the frame that caused the problem. The frame content
 * will be printed if exception is printed.
 */
public class ProtocolException extends RuntimeException {

	private static final long serialVersionUID = -1858209629779954840L;

	private final ByteBuffer buffer;

	public ProtocolException(String reason, ByteBuffer bb) {
		super(reason);
		buffer = bb;
	}

	@Override
	public String toString() {

		String out = "ProtocolException: " + getMessage() + "\n";

		if(buffer != null) {
			BufferFormat format = new BufferFormat();
			out += "buffer: " + buffer;
			buffer.position(0);
			out += "\n";
			out += format.format(buffer);
		}

		return out;
	}
}
