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

package org.otfeed;

import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTHost;
import org.otfeed.protocol.connector.IStreamerFactory;
import org.otfeed.protocol.connector.LoginStreamerFactory;
import org.otfeed.protocol.connector.OTEngine;
import org.otfeed.support.ByteReverseBufferAllocator;
import org.otfeed.support.ConnectionStateListener;
import org.otfeed.support.IBufferAllocator;

import java.util.List;

/**
 * Factory of connections to the OpenTick service.
 * This is the root object of the OpenTick client API. It is used to
 * establish a connection to the OpenTick service. 
 *
 * <p>
 * Before calling the {@link #connect} method, one have to set 
 * {@link #setUsername username}/{@link #setPassword password}
 * and at least one host in the {@link #setHostList(List) hostList}.
 *
 * <h3>Sample usage</h3>
 * Sample usage can be like this:
 * <pre>
 * OTConnectionFactory factory = new OTConnectionFactory();
 *
 * factory.setUsername("super-trooper");
 * factory.setPassword("kick-me");
 * factory.getHostList().add(new OTHost("feed1.opentick.com", 10015));
 *
 * IConnection connection = factory.connect(null);
 *
 * ListExchangesCommand command = new ListExchangesCommand(new IDataDelegate<OTExchange> {
 *         public void onData(OTExchange exchange) {
 *                 System.out.println(exchange);
 *         }
 * });
 * 
 * IRequest request = connection.prepareRequest(command);
 * request.submit();
 * request.waitForCompletion();
 *
 * connection.shutdown();
 * connection.waitForCompletion();
 * </pre>
 *
 * <h3>Avoid starting more than one simultaneous connection</h3>
 * Note that one can establish as many connection as she wishes, by calling
 * {@link #connect} method multiple times. However, <b>this is not 
 * recommended</b>. The reasons are: the avoidance of synchronization issues, and
 * conservation of resources.
 * <p>
 * Synchronization is a concern, because listener methods are called
 * by a separate <i>event-dispatching</i> thread. Every connection starts its
 * own event-displatching thread. 
 * <p>
 * Resources are a concern, because each connection takes a significant 
 * amount of resources. For example, current implementation
 * creates three threads and one client socket per connection.
 * <p>
 * Therefore, the recommentded practice is to limit number of connections
 * (e.g. work with a single connection per whole application). Each connection
 * can handle unlimited number of simultaneous requests very efficiently.
 * Another benefit of having just a single connection per application is
 * that one can avoid need for synchronization by assuming that all business
 * logic is done by the event-dispatching thread. When needed, calls from
 * outside can be translated to the event-dispatching thread by calling
 * {@link IConnection#runInEventThread} method on the connection object.
 * <p>
 * {@link OTPooledConnectionFactory} class can be used to simulate "simultaneous"
 * connections while keeping only a single actual connection to the Opentick server.
 *
 * <h3>No automatic reconnect after connection was lost</h3>
 * This API does not provide the feature of automatic re-connect on connection
 * loss. This is because the underlying protocol does not allow for reliable
 * re-start of ongoing requests (there is no notion of request "progress", that
 * can be used to re-submit the request). Original driver provided by
 * OpenTick (see http://www.opentick.com) does attempt to do such a reconnect,
 * with the risk of loosing some data, and getting duplicate historic data.
 * <p/>
 * We feel that since re-connect can easily lead to surprising and 
 * often not obvious data problems, its much safer to terminate all pending
 * requests, and let the application to decide what to do next (maybe 
 * terminate, or reconnect).
 * <p/>
 * Following outlines the steps that application have to take in order
 * to recover gracefully from lost connection connection. We assume that
 * it was a historical data request.
 * <ol>
 * 	<li>Remember the latest data timestamp.
 *  <li>Discard all the data that has this timestamp (because
 *  	it may be incomplete).
 *  <li>Re-connect to the server.
 *  <li>Re-issue the request using the remembered timestamp as 
 *  the starting date.
 * </ol>
 * Apparently, this is better be done by the application level, 
 * not the driver.
 */
public class OTConnectionFactory implements IConnectionFactory {

	/**
	 * Creates new OTConnectionFactory.
	 */
	public OTConnectionFactory() { }
	
	/**
	 * Creates new OTConnectionFactory and initializes 
	 * all its properties.
	 * 
	 * @param username username.
	 * @param password password.
	 * @param hostList list of hosts.
	 */
	public OTConnectionFactory(String username, String password,
			List<OTHost> hostList) {
		setUsername(username);
		setPassword(password);
		setHostList(hostList);
	}

	/**
	 * Sets login name.
	 * Username must be set before calling {@link #connect}
	 *
	 * @param val login name.
	 */
	public void setUsername(String val) {
		factory.setUsername(val);
	}

	/**
	 * Gets login name.
	 *
	 * @return username login name.
	 */
	public String getUsername() {
		return factory.getUsername();
	}

	/**
	 * Sets login password.
	 * Password must be set before calling {@link #connect}
	 *
	 * @param val login password.
	 */
	public void setPassword(String val) {
		factory.setPassword(val);
	}

	/**
	 * Gets login password.
	 *
	 * @return login password.
	 */
	public String getPassword() {
		return factory.getPassword();
	}

	/**
	 * Returns list of servers.
	 * Allows client to control this list by doing the following:
	 *
	 * <pre>
	 *    session.getHostList().add(new OTHost("feed1.opentick.com", 10015));
	 *    session.getHostList().add(new OTHost("feed2.opentick.com", 10015));
	 * </pre>
	 *
	 * Client must configure at least one server address before calling
	 * {@link #connect} method. Initially, this list is empty (but not null).
	 *
	 * @return modifiable list of server addresses.
	 */
	public List<OTHost> getHostList() {
		return factory.getHostList();
	}

	/**
	 * Sets list of servers.
	 * Allows client to control list of hosts by doing the following:
	 *
	 * <pre>
	 *    List<OTHost> hosts = new LinkedList<OTHost>();
	 *    hosts.add(new OTHost("feed1.opentick.com", 10015));
	 *    hosts.add(new OTHost("feed2.opentick.com", 10015));
	 *    session.setHostList(hosts);
	 * </pre>
	 *
	 * Client must configure at least one server address before calling
	 * {@link #connect} method.
	 *
	 * @param val list of server addresses.
	 */
	public void setHostList(List<OTHost> val) {
		factory.setHostList(val);
	}

	/**
	 * Sets connection timeout (in milliseconds).
	 *
	 * Default value is 20 000, which is 20secs. This value can be
	 * useful only if there are many "bad" servers in the hosts list,
	 * so that it takes significant time to reach the one that answers
	 * the connection request. There is rarely any need to change this
	 * value.
	 *
	 * @param val connection timeout value.
	 */
	public void setConnectTimeoutMillis(long val) {
		factory.setConnectTimeoutMillis(val);
	}

	/**
	 * Gets connection timeout (in milliseconds).
	 *
	 * @return connection timeout value.
	 */
	public long getConnectTimeoutMillis() {
		return factory.getConnectTimeoutMillis();
	}
	
	private long heartbeatIntervalMillis = 10000;

	/**
	 * Sets heartbeat interval value (in milliseconds).
	 * When connected, client periodically
	 * sends heartbeat messages to the server. If server does not receive
	 * any message from client for some time, it will close the connection.
	 *
	 * OpenTick recommends the heartbeat to be between 1sec and 10sec.
	 * Default value is 10 000, which is 10secs.
	 *
	 * @param val heartbeat interval value.
	 */
	public void setHeartbeatIntervalMillis(long val) {
		heartbeatIntervalMillis = val;
	}

	/**
	 * Gets heartbeat interval value (in milliseconds).
	 *
	 * @return heartbeat interval value.
	 */
	public long getHeartbeatIntervalMillis() {
		return heartbeatIntervalMillis;
	}

	private static final IBufferAllocator allocator = new ByteReverseBufferAllocator();

	private final LoginStreamerFactory factory
			= new LoginStreamerFactory(allocator);
	
	/**
	 * Low-level IO object, responsible for connecting and 
	 * delivering/receiving raw frames. Normally, you will not
	 * need to change this.
	 * <p/>
	 * The default value is the "correct" real-life
	 * streamer implementation, see
	 * {@link LoginStreamerFactory#getStreamerFactory()}.
	 * <p/>
	 * You may want to change this only for the puproses of
	 * testing, debugging, or mocking.
	 * 
	 * @return streamer factory.
	 */
	public IStreamerFactory getStreamerFactory() {
		return factory.getStreamerFactory();
	}
	
	/**
	 * Sets streamer factory.
	 * 
	 * @param val stremaer factory.
	 */
	public void setStreamerFactory(IStreamerFactory val) {
		factory.setStreamerFactory(val);
	}

	/**
	 * Starts asynchronous connection process. This method
	 * does not block, it returns {@link IConnection} object
	 * immediately.
	 *
	 * Use <code>list</code> paramater to monitor the connection progress.
	 * For more details, see {@link IConnectionStateListener}.
	 *
	 * If caller is not interested in monitoring connection progress,
	 * it can pass null as <code>list</code> paramater.
	 *
	 * Valid {@link #setUsername username} and {@link #setPassword password},
	 * and non-empty {@link #setHostList hostList} must be set before 
	 * calling this method.
	 *
	 * @param list connection state listener.
	 * @return connection object.
	 * @throws IllegalStateException if username/password pair is not set, 
	 *         or if hostList is empty.
	 */
	public IConnection connect(IConnectionStateListener list) {

		if(list == null) list = new ConnectionStateListener();

		return new OTEngine(factory, allocator, 
				heartbeatIntervalMillis, list);
	}
}

