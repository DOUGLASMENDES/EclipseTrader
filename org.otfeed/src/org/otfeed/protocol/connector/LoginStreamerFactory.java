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

import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTHost;
import org.otfeed.protocol.request.LoginRequest;
import org.otfeed.protocol.request.Header;
import org.otfeed.protocol.StatusEnum;
import org.otfeed.support.ByteReverseBufferAllocator;
import org.otfeed.support.ConnectionStateListener;
import org.otfeed.support.IBufferAllocator;


import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.otfeed.protocol.request.Util.readError;

public class LoginStreamerFactory implements ISessionStreamerFactory {

	private final static int BUFFER_SIZE = 1024;

	private List<OTHost> hostList = new LinkedList<OTHost>();

	public List<OTHost> getHostList() {
		return hostList;
	}

	public void setHostList(List<OTHost> val) {
		hostList = val;
	}

	private final AtomicBoolean isShutdown = new AtomicBoolean(false);

	private IBufferAllocator allocator = new ByteReverseBufferAllocator();

	public LoginStreamerFactory(IBufferAllocator all) {
		allocator = all;
	}
	
	public LoginStreamerFactory() { }

	private String username;
	public String getUsername() { return username; }
	public void setUsername(String val) { username = val; }

	private String password;
	public String getPassword() { return password; }
	public void setPassword(String val) { password = val; }

	private long connectTimeout = 20000; // 20 seconds
	public void setConnectTimeoutMillis(long val) { connectTimeout = val; }
	public long getConnectTimeoutMillis() { return connectTimeout; }
	
	private IStreamerFactory streamerFactory = new SocketStreamerFactory(allocator);

	public IStreamerFactory getStreamerFactory() {
		return streamerFactory;
	}
	public void setStreamerFactory(IStreamerFactory val) {
		streamerFactory = val;
	}

	private static class SessionStreamer implements ISessionStreamer {

		private final IStreamer connector;
		private final String sessionId;

		private SessionStreamer(IStreamer c, String sid) {
			connector = c;
			sessionId = sid;
		}

		public String getSessionId() {
			return sessionId;
		}

		public ByteBuffer read() throws IOException {
			return connector.read();
		}

		public void write(ByteBuffer bb) throws IOException {
			connector.write(bb);
		}

		public void close() {
			connector.close();
		}
	}

	public ISessionStreamer connect()
				throws IOException, LoginFailureException {
		return connect(new ConnectionStateListener());
	}

	public ISessionStreamer connect(IConnectionStateListener listener)
				throws IOException, LoginFailureException {

		if(username == null || password == null) {
			throw new IllegalStateException(
				"username and password must be set");
		}

		Set<OTHost> triedAddresses = new HashSet<OTHost>();

		LinkedList<OTHost> addressList = new LinkedList<OTHost>();
		addressList.addAll(this.hostList);

		if(addressList.size() == 0) {
			throw new IllegalStateException(
				"no connection addresses specified");
		}

		long started = System.currentTimeMillis();

		OTHost address;
		while((address = addressList.poll()) != null) {

			if(triedAddresses.contains(address)) {
				continue;
			}

			triedAddresses.add(address);

			listener.onConnecting(address);

			long now = System.currentTimeMillis();
			if(now > started + connectTimeout) {
				throw new IOException("connection timed out");
			}

			if(isShutdown.get()) {
				throw new IOException("shutdown");
			}

			IStreamer connector = null;

			try {
					connector = streamerFactory.connect(address.getHost(), address.getPort());
			} catch(IOException ex) {
//System.out.println(ex);
				continue;
			}

			// connected!

			LoginRequest request = new LoginRequest(0, username, password);

			ByteBuffer buffer = allocator.allocate(BUFFER_SIZE);

			request.writeRequest(buffer);

			buffer.flip();

			try {
				connector.write(buffer);
				buffer = connector.read();
			} catch(IOException ex) {
				connector.close();
				continue;
			} 

			Header header = new Header(buffer);

			listener.onConnected();

			if(header.getStatus() != StatusEnum.OK) {
				// if fatal error, stop trying
				connector.close();

				throw new LoginFailureException(readError(header.getRequestId(), buffer));
			}

			LoginRequest.Response response 
				= new LoginRequest.Response(buffer);

			if(!response.redirectFlag) {
				// success!
				listener.onLogin();
				return new SessionStreamer(
					connector, response.sessionId);
			}

			connector.close();

			OTHost redirectAddress = new OTHost(response.redirectHost, response.redirectPort);
			listener.onRedirect(redirectAddress);

			addressList.remove(redirectAddress);
			addressList.addFirst(redirectAddress);
			// strangely, opentick servers do circular redirects
			// e.g connect to l6 gives redirect to l4, then we
			// connect to l4 and get bounced back to l6.
			// therefore, never blacklist address that we've got as
			// redirect
			triedAddresses.remove(redirectAddress);
		}
//System.out.println("all failed to respond");

		// if here, all hosts failed. Rethrow last IO exception
		throw new IOException("all hosts failed to respond");
	}

	public void shutdown() {
		isShutdown.set(true);
	}
}
