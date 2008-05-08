package org.otfeed;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.otfeed.IConnection;
import org.otfeed.IConnectionFactory;
import org.otfeed.IRequest;
import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.event.OTHost;
import org.otfeed.protocol.ICommand;

/**
 * Class that facilitates connection pooling (sharing).
 * This class maintains (at most) one connection to the
 * Opentick server and routes all {@link #connect(IConnectionStateListener)} requests
 * to this single connection. This is convenient as all clients
 * can view their copy of connection as an independant one. This
 * is efficient, because only minimal number of system resources
 * is consumed.
 * It is recommended that this class should be used whenever there is
 * a chance that factory's {@link #connect(IConnectionStateListener)} method can be called more
 * than once. 
 */
public class OTPooledConnectionFactory implements IConnectionFactory {
	
	private IConnectionFactory engine;
	
	/**
	 * The factory of shared connection (typically an instance of {@link org.otfeed.OTConnectionFactory}).
	 * @param engine
	 */
	public void setConnectionFactory(IConnectionFactory engine) {
		this.engine = engine;
	}
	public IConnectionFactory getConnectionFactory() {
		return engine;
	}
	
	private PooledConnection pooledConnection = null;
	private int refCount = 0;
	private boolean isGlobalShutdown = false; // global shutdown flag, object no longer usable if "on"
	
	public synchronized IConnection connect(IConnectionStateListener listener) {
		
		if(isGlobalShutdown) {
			throw new IllegalStateException("trying to connect after global shutdown");
		}
		
		if(refCount++ == 0) {
			pooledConnection = new PooledConnection();
		}
		
		pooledConnection.addConnectionStateListener(listener);

		// this wrapper ensures that shutdown is seen only once per connection
		// otherwise refcounting could be confused
		return new IConnection () {
			
			private final AtomicBoolean isShutdown = new AtomicBoolean();
			private final PooledConnection connection = pooledConnection;
			private final AtomicBoolean isLastToShutdown = new AtomicBoolean(false);

			public IRequest prepareRequest(ICommand arg0) {
				return connection.prepareRequest(arg0);
			}

			public void runInEventThread(Runnable arg0) {
				connection.runInEventThread(arg0);
			}

			public void shutdown() {
				if(isShutdown.getAndSet(true) == false) {
					System.out.println("shutting down " + this);
					synchronized(OTPooledConnectionFactory.this) {
						if(isGlobalShutdown) return;
						System.out.println("refcount=" + refCount);
						if(--refCount == 0) {
							connection.shutdown();
							isLastToShutdown.set(true);
							pooledConnection = null;
						}
					}
					
					synchronized(isShutdown) {
						isShutdown.notifyAll();
					}
				}
			}

			public void waitForCompletion() {
				waitForCompletion(Integer.MAX_VALUE);
			}

			public boolean waitForCompletion(long millis) {
				long target = System.currentTimeMillis() + millis;

				while(isShutdown.get() == false) {
					long toWait = target - System.currentTimeMillis();
					
					if(toWait <= 0) return false;
					
					synchronized(isShutdown) {
						try {
							isShutdown.wait(toWait);
						} catch(InterruptedException ex) {
							return false; // not sure
						}
					}
				}
				
				if(isLastToShutdown.get()) {
					return connection.waitForCompletion(target - System.currentTimeMillis());
				}
				
				return true;
			}

		};
	}
	
	/**
	 * Forces the shutdown of the pooled connection. Useful for hooking to the
	 * Spring's "deinit" bean lifecycle to make sure that even if users of this bean 
	 * did not shutdown their clones of the connection, this method will actually
	 * disconnect from Opentick and release all resources.
	 */
	public synchronized void shutdownAll() {
		if(isGlobalShutdown) return; // ignore repeated shutdowns
		
		if(pooledConnection != null) {
			pooledConnection.shutdown();
			pooledConnection = null;
		}
	}
	
	private interface IStateTransitionEvent {
		public void applyEvent(IConnectionStateListener l);
	}

	// since new connection state listener can be added at any time
	// during the life cycle of pooled connection, this class will keep
	// the history of all connection state events seen so far, and replay them
	// for the incoming listener.
	private class PooledConnection implements IConnection {

		private final IConnection connection;
		private final List<IConnectionStateListener> listeners = new LinkedList<IConnectionStateListener>();
		private final List<IStateTransitionEvent> events = new LinkedList<IStateTransitionEvent>();
		
		private final IConnectionStateListener stateListener = new IConnectionStateListener() {

			public void onConnected() {
				for(IConnectionStateListener l : listeners) {
					l.onConnected();
				}
			}

			public void onConnecting(final OTHost arg0) {
				for(IConnectionStateListener l : listeners) {
					l.onConnecting(arg0);
				}
			}

			public void onError(OTError error) {
				for(IConnectionStateListener l : listeners) {
					l.onError(error);
				}
			}

			public void onLogin() {
				for(IConnectionStateListener l : listeners) {
					l.onLogin();
				}
			}

			public void onRedirect(OTHost arg0) {
				for(IConnectionStateListener l : listeners) {
					l.onRedirect(arg0);
				}
			}
		};
		
		public PooledConnection() {
			listeners.add(new IConnectionStateListener() {

				public void onConnected() {
					events.add(new IStateTransitionEvent() {
						public void applyEvent(IConnectionStateListener l) {
							l.onConnected();
						}
					});
				}

				public void onConnecting(final OTHost arg0) {
					events.add(new IStateTransitionEvent() {
						public void applyEvent(IConnectionStateListener l) {
							l.onConnecting(arg0);
						}
					});
				}

				public void onError(final OTError arg0) {
					events.add(new IStateTransitionEvent() {
						public void applyEvent(IConnectionStateListener l) {
							l.onError(arg0);
						}
					});
				}

				public void onLogin() {
					events.add(new IStateTransitionEvent() {
						public void applyEvent(IConnectionStateListener l) {
							l.onLogin();
						}
					});
				}

				public void onRedirect(final OTHost arg0) {
					events.add(new IStateTransitionEvent() {
						public void applyEvent(IConnectionStateListener l) {
							l.onRedirect(arg0);
						}
					});
				}
			});
			
			connection = engine.connect(stateListener);
		}
		
		public void addConnectionStateListener(final IConnectionStateListener l) {
			
			connection.runInEventThread(new Runnable() { public void run() {
				// replay events that we missed
				for(IStateTransitionEvent event : events) {
					event.applyEvent(l);
				}
				// add me to the listeners
				listeners.add(l);
			}});
		}

		public IRequest prepareRequest(ICommand arg0) {
			return connection.prepareRequest(arg0);
		}

		public void runInEventThread(Runnable arg0) {
			connection.runInEventThread(arg0);
		}

		public void shutdown() {
			connection.shutdown();
		}

		public void waitForCompletion() {
			connection.waitForCompletion();
		}

		public boolean waitForCompletion(long arg0) {
			return connection.waitForCompletion(arg0);
		}
	}
}

