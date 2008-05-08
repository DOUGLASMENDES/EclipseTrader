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

package org.otfeed.support.mock;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import org.otfeed.protocol.connector.IStreamer;
import org.otfeed.protocol.connector.IStreamerFactory;
import org.otfeed.support.BufferFormat;
import org.otfeed.support.IFormat;

/**
 * Wrapper around {@link IStreamerFactory} to log content of
 * frames to the print stream. Useful for testing, tracing,
 * and debugging.
 */
public class SpyStreamerFactory implements IStreamerFactory {
	
	private final IStreamerFactory engine;

	/**
	 * Creates new spy with the given id string, wrapping an instance of {@link IStreamerFactory}.
	 *
	 * @param id id string.
	 * @param engine wrapped instance.
	 */
	public SpyStreamerFactory(String id, IStreamerFactory engine) {
		this.id = id;
		this.engine = engine;
	}
	
	/**
	 * Creates new spy with the empty id string, wrapping an instance of {@link IStreamerFactory}.
	 * 
	 * @param engine wrapped instance.
	 */
	public SpyStreamerFactory(IStreamerFactory engine) {
		this("", engine);
	}
	
	private PrintStream readLogStream = new PrintStream(System.out, true);

	/**
	 * Destination for logging the incoming frames.
	 * Defaults to System.out.
	 * 
	 * @return print stream.
	 */
	public PrintStream getReadLogStream() {
		return readLogStream;
	}

	/**
	 * Sets read log stream.
	 * 
	 * @param val print stream.
	 */
	public void setReadLogStream(PrintStream val) {
		readLogStream = val;
	}

	private PrintStream writeLogStream = new PrintStream(System.out, true);
	
	/**
	 * Destination for logging the outgoing frames.
	 * Defaults to System.out.
	 * 
	 * @return print stream.
	 */
	public PrintStream getWriteLogStream() {
		return writeLogStream;
	}

	/**
	 * Sets write log stream.
	 * 
	 * @param val print stream.
	 */
	public void setWriteLogStream(PrintStream val) {
		writeLogStream = val;
	}

	private String id = "";
	
	/**
	 * Identification string (optional).
	 * 
	 * This string is written to the comment area of
	 * generated hexdumps. You may want to set it if
	 * mustiple simultaneous connections are being spyied
	 * upon to distinguish between them.
	 * 
	 * Default value is empty string.
	 * 
	 * @return id string.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets identification sting.
	 * 
	 * @param val id string.
	 */
	public void setId(String val) {
		id = val;
	}
	
	private IFormat<ByteBuffer> format = new BufferFormat();
	
	/**
	 * Format to use when converting ByteBuffer s to human-readable
	 * form. Defaults to {@link BufferFormat}.
	 *
	 * @return format.
	 */
	public IFormat<ByteBuffer> getFormat() {
		return format;
	}
	
	/**
	 * Sets format.
	 * 
	 * @param val format.
	 */
	public void setFormat(IFormat<ByteBuffer> val) {
		format = val;
	}

	public IStreamer connect(String host, int port) throws IOException {
		
		final IStreamer streamer = engine.connect(host, port);
		
		return new IStreamer() {

			public void close() {
				streamer.close();
			}

			public ByteBuffer read() throws IOException {
				ByteBuffer out = streamer.read();
				
				readLogStream.println("% " + id);
				readLogStream.println("% read timestamp: " + System.currentTimeMillis());
				readLogStream.println(format.format(out));
				
				return out;
			}

			public void write(ByteBuffer out) throws IOException {
				
				writeLogStream.println("% " + id);
				writeLogStream.println("% write timestamp: " + System.currentTimeMillis());
				writeLogStream.println(format.format(out));

				streamer.write(out);
			}
		};
	}
}
