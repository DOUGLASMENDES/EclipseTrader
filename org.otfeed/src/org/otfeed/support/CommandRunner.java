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

package org.otfeed.support;

import java.util.LinkedList;
import java.util.List;

import org.otfeed.IConnection;
import org.otfeed.IConnectionFactory;
import org.otfeed.IRequest;
import org.otfeed.event.IConnectionStateListener;
import org.otfeed.protocol.ICommand;

/**
 * Class that runs a list of commands, then waits 
 * for the completion. Sometimes useful in scripts.
 */
public class CommandRunner {

	/**
	 * Creates new CommandRunner.
	 */
	public CommandRunner() { }

	/**
	 * Creates new CommandRunner from a given connection 
	 * factory, with the given list of commands.
	 * 
	 * @param cf connection factory.
	 * @param commands list of commands.
	 */
	public CommandRunner(IConnectionFactory cf, List<ICommand> commands) {
		setConnectionFactory(cf);
		setCommandList(commands);
	}
	
	private IConnectionFactory connectionFactory;
	
	/**
	 * Connection factory to use.
	 * 
	 * @return connection factory.
	 */
	public IConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}
	
	/**
	 * Sets the connection factory.
	 * 
	 * @param val connection factory.
	 */
	public void setConnectionFactory(IConnectionFactory val) {
		connectionFactory = val;
	}
	
	private List<ICommand> commandList = new LinkedList<ICommand>();
	
	/**
	 * List of commands to execute.
	 * If empty, the runner will just do connect, login, and disconnect.
	 * 
	 * @return lsit of commands.
	 */
	public List<ICommand> getCommandList() {
		return commandList;
	}
	
	/**
	 * Sets list of commands.
	 * 
	 * @param val list of commands.
	 */
	public void setCommandList(List<ICommand> val) {
		commandList = val;
	}
	
	private IConnectionStateListener connectionStateListener
		= new SimpleConnectionStateListener();

	/**
	 * Listener that watches connection-level events.
	 * Defaul is an instance of {@link SimpleConnectionStateListener},
	 * which will print the events to the System.err.
	 * 
	 * @return connection state listener. 
	 */
	public IConnectionStateListener getConnectionStateListener() {
		return connectionStateListener;
	}

	/**
	 * Sets connection state listener.
	 * 
	 * @param val connection state listener.
	 */
	public void setConnectionStateListener(IConnectionStateListener val) {
		connectionStateListener = val;
	}
	
	/**
	 * Main method: connects to the server and executes all
	 * commands. Blocks untill the processing is finished.
	 *
	 * @throws Exception is something is not 
	 * configured correctly, or something goes extremely
	 * wrong at the runtime.
	 */
	public void runCommands() throws Exception {
		runCommands(Integer.MAX_VALUE);
	}
	
	/**
	 * Main method: connects to the server and executes all
	 * commands. Blocks for the given timeout value
	 * waiting for the requests to complete. If timeout expires,
	 * the connection is shutdown, all pending requests are 
	 * aborted with error.
	 * 
	 * @param timeout how long to wait for the request to complete (in milliseconds).
	 * @throws Exception if something is not 
	 *    configured correctly, or something goes extremely
	 *    wrong at the runtime.
	 */
	public void runCommands(long timeout) throws Exception {
		if(connectionFactory == null) {
			throw new NullPointerException("connectionFactory property must be set");
		}
		
		if(commandList == null) {
			throw new NullPointerException("commandList property must be set");
		}

		long target = System.currentTimeMillis() + timeout;

		IConnection connection = connectionFactory.connect(connectionStateListener);

		try {
			List<IRequest> requestList = new LinkedList<IRequest>();
			for(ICommand command : commandList) {
				IRequest r = connection.prepareRequest(command);
				r.submit();
				requestList.add(r);
			}

			for(IRequest r : requestList) {
				long delay = target - System.currentTimeMillis();
				if(delay > 0) {
					r.waitForCompletion(delay);
				}
			}
		} finally {
			connection.shutdown();
			connection.waitForCompletion();
		}
	}
}
