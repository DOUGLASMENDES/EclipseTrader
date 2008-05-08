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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.otfeed.protocol.connector.IStreamer;
import org.otfeed.protocol.connector.IStreamerFactory;
import org.otfeed.support.BufferFormat;
import org.otfeed.support.IFormat;

/**
 * Mock streaming layer for offline testing.
 * <p/>
 * This object facilitates offline testing of object 
 * marshalling code
 * by returning a pre-recorded sequence of frames.
 * <p/>
 * Use this object to substitute {@link org.otfeed.OTConnectionFactory#getStreamerFactory() streamerFactory}
 * of {@link org.otfeed.OTConnectionFactory OTConnectionFactory}.
 * <p/>
 * <em>IMPORTANT</em>: set {@link org.otfeed.OTConnectionFactory#setHeartbeatIntervalMillis(long) heartbeatInterval}
 * property of <code>OTConnectionListener</code> to reasonably
 * large value to make sure that heartbeat frames are not
 * sent to the streamer layer. Otherwise, programs that
 * <code>MockStreamerFactory</code> executes may be confused by
 * receiving a heartbeat packet.
 * <p/>
 * <em>IMPORTANT</em>: recorded packets must contain correct requestId. Otherwise
 * they will be silently dropped. Request id starts with zero (login request)
 * and is incremented for every command prepared (event if not submitted).
 * Thus, first request will have id of one, second - id of two, etc.
 * Practically, its simpler to restrict testing to just a single command
 * and then destroy the connection. This way, all frames for a command
 * to be tested must have request id of 1.
 * <p/>
 * Next, build a list of operations that will be the "program"
 * for the streamer. Assign this list to the {@link #setOpList(List) opList}
 * property of <code>MockStreamerFactory</code>.
 * <p/>
 * Example program may look like this:
 * <pre>
 * // EXPECT ANY (will be login request frame)
 * mockStreamerFactory.getOpList().add(MockStreamerFactory.expectAny());
 * // SEND "login OK" response
 * mockStreamerFactory.getOpList().add(MockStreamerFactory.sendLoginOK());
 * // WAIT for any buffer
 * mockStreamerFactory.getOpList().add(MockStreamerFactory.expectAny());
 * // No more elements in the list: streamer will emulate
 * // disconnect from the server (read error).  
 * </pre>
 */
public class MockStreamerFactory implements IStreamerFactory {

	public interface Op { } // marker interface
	
	private static final IFormat<ByteBuffer> FORMAT = new BufferFormat();

	private static final String FRAME_LOGIN_OK =
		"% reply from server 'successfull login'\n"
		+ "02010000 01000000 00000000 61663765	%............af7e\n"
		+ "61326137 38643533 35333934 64343839	%a2a78d535394d489\n"
		+ "62306135 32633464 30643631 34653834	%b0a52c4d0d614e84\n"
		+ "36326132 37313064 61386534 66646261	%62a2710da8e4fdba\n"
		+ "62643130 35353138 38613035 00000000	%bd1055188a05....\n"
		+ "00000000 00000000 00000000 00000000	%................\n"
		+ "00000000 00000000 00000000 00000000	%................\n"
		+ "00000000 00000000 00000000 00000000	%................\n"
		+ "00000000 00000000 00000000 000000  	%...............\n";

	private static final String FRAME_LOGIN_FAILED =
		"% reply from server 'login failed'\n"
		+ "0202f7da 01000000 00000000 e9031900	%................\n"
		+ "42616420 75736572 6e616d65 206f7220	%Bad.username.or.\n"
		+ "70617373 776f7264 00               	%password.\n";
	
	/**
	 * Helper: creates a <code>ByteBuffer</code> from
	 * its hex-dump String representation.
	 * 
	 * @param bufferString string in the hex format.
	 * @return buffer.
	 */
	public static ByteBuffer parse(String bufferString) {
		return FORMAT.parse(bufferString);
	}
	
	/**
	 * Helper: creates a <code>ByteBuffer</code> by parsing
	 * a resource file.
	 * 
	 * @param resourceName name of the resource.
	 * @return buffer.
	 * @throws IOException on IO error.
	 */
	public static ByteBuffer parseResource(String resourceName) throws IOException {
		InputStream input = MockStreamerFactory.class.getClassLoader().getResourceAsStream(resourceName);
		if(input == null) {
			throw new IOException("resource named [" + resourceName + "] not found");
		}
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						input, "UTF-8"));
		StringWriter writer = new StringWriter();
		String line = null;
		while((line = reader.readLine()) != null) {
			writer.write(line + "\n");
		}

		return parse(writer.toString());
	}
	
	/**
	 * Helper: creates a "SEND" operation with the buffer
	 * telling "login was OK".
	 * 
	 * @return send operation.
	 */
	public static Op sendLoginOK() {
		return send(parse(FRAME_LOGIN_OK));
	}
	
	/**
	 * Helper: creates a "SEND" operation with the buffer
	 * telling that "login failed".
	 * 
	 * @return send operation.
	 */
	public static Op sendLoginFailed() {
		return send(parse(FRAME_LOGIN_FAILED));
	}
	
	/**
	 * Creates new mock streamer. 
	 * 
	 */
	public MockStreamerFactory() { }
	
	private IFormat<ByteBuffer> format = new BufferFormat();
	
	/**
	 * Format to use when parsing ByteBuffer s.
	 * Defaults to {@link BufferFormat}.
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
	
	private static class ExpectOp implements Op {
		ByteBuffer expect;
		private ExpectOp(ByteBuffer b) {
			expect = b;
		}
		private ExpectOp() {
			this(null);
		}
	}
	
	private static class SendOp implements Op {
		ByteBuffer send;
		
		public SendOp(ByteBuffer s) {
			send = s;
		}
	}
	
	/**
	 * Creates an operation of "EXPECT ANY BUFFER" type.
	 * This one will match any received buffer.
	 * 
	 * @return operation.
	 */
	public static Op expectAny() {
		return new ExpectOp();
	}
	
	/**
	 * Creates an operation of "EXPECT A BUFFER" type.
	 * This one will try to match the model buffer
	 * against the actual one received from the upper layer.
	 * 
	 * @param e - model buffer.
	 * @return operation.
	 */
	public static Op expectBuffer(ByteBuffer e) {
		return new ExpectOp(e);
	}
	
	/**
	 * Creates an operation of "SEND" type.
	 * This one sends the buffer back to the driver (and, ultimately,
	 * to the client application as a set of events).
	 * 
	 * @param s buffer to send.
	 * @return operation.
	 */
	public static Op send(ByteBuffer s) {
		return new SendOp(s);
	}

	private List<Op> opList = new LinkedList<Op>();

	/**
	 * List of operations that drive the mock streamer.
	 * <p/>
	 * List of operations specifies program that mock streamer will
	 * execute. Following operations are defined:
	 * <ul>
	 * <li>expect(ByteBuffer b): waits for the user action
	 * (i.e. a buffer that upper layer sends to the
	 * streamer. Compares that buffer sent matches exactly the 
	 * one that is supplied to expect operation. If matches,
	 * continues the execution. if does not match, throws an 
	 * exception. To create this kind of operation, use
	 * {@link #expectBuffer(ByteBuffer)} helper.
	 * <li>expect(): wait for a buffer and then continues
	 * (ignores the buffer content). To create this kind of operation
	 * use {@link #expectAny()} helper.
	 * <li>send(ByteBuffer b): sends the buffer back to the upper layer
	 * for decoding and presenting to the user as a an event of
	 * a set of events. To create this kind of operation
	 * user {@link #send(ByteBuffer)} helper.
	 * </ul>
	 * 
	 * @return op list.
	 */
	public List<Op> getOpList() {
		return opList;
	}
	
	/**
	 * Sets op list.
	 * 
	 * @param val op list.
	 */
	public void setOpList(List<Op> val) {
		opList = val;
	}
	
	private boolean trace = false;
	
	/**
	 * Trace flag makes streamer print all frames
	 * to screen.
	 * 
	 * @return trace flag.
	 */
	public boolean getTrace() {
		return trace;
	}
	
	/**
	 * Sets trace flag.
	 * 
	 * @param val trace value.
	 */
	public void setTrace(boolean val) {
		trace = val;
	}
	
	private void trace(String info) {
		if(trace) { 
			System.out.println("[" + id + "] " + info);
		}
	}
	
	private String id;
	
	/**
	 * Identification string.
	 * 
	 * @return id string.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets identification string.
	 * 
	 * @param val id string.
	 */
	public void setId(String val) {
		id = val;
	}
	
	private void trace(String info, ByteBuffer buffer) {
		if(trace) {
			System.out.println("[" + id + "] " + info + "\n" + FORMAT.format(buffer));
		}
	}

	private static ByteBuffer copyBuffer(ByteBuffer in) {
		ByteBuffer copy = ByteBuffer.allocate(in.capacity());
		copy.put(in.duplicate());
		copy.flip();
		return copy;
	}

	public IStreamer connect(String host, int port) throws IOException {
		
		trace("connected");
		
		final Iterator<Op> iterator = opList.iterator();
		
		return new IStreamer() {
			
			private final AtomicBoolean isClosed = new AtomicBoolean(false);
			private final Queue<ByteBuffer> sentQueue = new ConcurrentLinkedQueue<ByteBuffer>();

			public void close() { 
				isClosed.set(true);
				synchronized(isClosed) {
					isClosed.notifyAll();
				}
				
				trace("close() called");
			}

			public ByteBuffer read() throws IOException {
		
				trace("read");

				while(iterator.hasNext()) {
					Op op = iterator.next();
					if(op instanceof ExpectOp) {
						ExpectOp eop = (ExpectOp) op;
						ByteBuffer buffer = null;

						trace("read: EXPECT is waiting for buffer");
						while(!isClosed.get() 
								&& (buffer = sentQueue.poll()) == null) {
							synchronized(isClosed) {
								try {
									isClosed.wait();
								} catch(InterruptedException ex) {
									throw new IOException("interrupted?");
								}
							}
						}
					
						if(isClosed.get()) {
							trace("read: closed");
							throw new IOException("closed");
						}
					
						trace("read: EXPECT received buffer:", buffer);

						if(eop.expect == null) {
							trace("read: buffer matched (wildcard)");
						} else if(eop.expect.compareTo(buffer) != 0) {
							trace("read: model and actual buffer are not identical. Expected: ", eop.expect);
							throw new IOException("model and actual buffers are not identical");
						} else {
							trace("read: buffer matched");
						}
					} else if(op instanceof SendOp) {
						SendOp sop = (SendOp) op;

						trace("read: SEND operation processed:", sop.send);
						
						return sop.send;
					} else {
						throw new AssertionError("unexpected op: " + op);
					}
				}
				
				trace("read: no more ops");
				throw new IOException("eof: no more ops");
			}

			public void write(ByteBuffer out) throws IOException {
				// need to copy, because caller may re-use this buffer for the next frame
				ByteBuffer copy = copyBuffer(out);
				trace("write:\n" + FORMAT.format(copy));
				
				sentQueue.offer(copy);
				synchronized(isClosed) {
					isClosed.notifyAll();
				}
			}
		};
	}
}
