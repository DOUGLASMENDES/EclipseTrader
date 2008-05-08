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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.nio.ByteBuffer;

import java.util.concurrent.atomic.AtomicBoolean;

import org.otfeed.support.IBufferAllocator;

class SocketStreamerFactory implements IStreamerFactory {

	private final static int BUFFER_SIZE = 1024;

	private final IBufferAllocator allocator;

	public SocketStreamerFactory(IBufferAllocator allocator) {
		this.allocator = allocator;
	}

	public IStreamer connect(String host, int port) throws IOException {

		final Socket socket = new Socket(host, port);

		final InputStream  input  = socket.getInputStream();
		final OutputStream output = socket.getOutputStream();

		return new IStreamer() {

			private ByteBuffer inputBuffer  = allocator.allocate(BUFFER_SIZE);
			private ByteBuffer outputBuffer = allocator.allocate(BUFFER_SIZE);

			private final AtomicBoolean isClosed
					= new AtomicBoolean(false);

			public void close() {

				if(isClosed.getAndSet(true)) {
					return;
				}

				try {
					socket.close();
				} catch(IOException ex) {
					// who cares?
				}

// System.out.println("socket closed");
			}

			public void write(ByteBuffer buffer) throws IOException {

				outputBuffer.clear();
				outputBuffer.putInt(buffer.limit() - buffer.position());
				outputBuffer.flip();

				output.write(outputBuffer.array(), 0, 4);

				output.write(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
				buffer.position(buffer.limit());
			}

			public ByteBuffer read() throws IOException {
				inputBuffer.clear();

				readCompletely(input, inputBuffer.array(), 0, 4);

				inputBuffer.limit(4);

				int length = inputBuffer.getInt();

				ByteBuffer in = allocator.allocate(length);

				readCompletely(input, in.array(), 0, length);
				in.limit(length);

				return in;
			}
		};
	}

	private static void readCompletely(InputStream in,
			byte [] data, int offset, int size) throws IOException {

		int actual = 0;
		while(actual < size) {
			int bytes = in.read(data, offset + actual, size - actual);
			if(bytes < 0) throw new IOException("read error");
			actual += bytes;
		}
	}
}
