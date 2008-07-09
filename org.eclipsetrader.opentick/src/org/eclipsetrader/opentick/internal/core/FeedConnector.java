/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipsetrader.core.feed.ConnectorEvent;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.opentick.internal.Connector;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.IdentifierType;
import org.eclipsetrader.opentick.internal.core.repository.IdentifiersList;
import org.otfeed.IConnection;

public class FeedConnector implements IFeedConnector2, IExecutableExtension, IExecutableExtensionFactory, Runnable, PropertyChangeListener {
	private static FeedConnector instance;

	private String id;
	private String name;

	protected Map<String, FeedSubscription> symbolSubscriptions;
	protected Map<String, FeedSubscription2> symbolSubscriptions2;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private Thread thread;
	private boolean stopping = false;
	private IConnection connection;

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			if (OTActivator.PREFS_SERVER.equals(property) || OTActivator.PREFS_PORT.equals(property) || OTActivator.PREFS_PASSWORD.equals(property) || OTActivator.PREFS_USERNAME.equals(property)) {
				disconnect();
				connect();
			}
		}
	};

	public FeedConnector() {
		symbolSubscriptions = new HashMap<String, FeedSubscription>();
		symbolSubscriptions2 = new HashMap<String, FeedSubscription2>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id");
		name = config.getAttribute("name");
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    public Object create() throws CoreException {
    	if (instance == null)
    		instance = this;
	    return instance;
    }

	public static FeedConnector getInstance() {
    	return instance;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
	 */
	public IFeedSubscription subscribe(IFeedIdentifier identifier) {
		synchronized (symbolSubscriptions) {
			IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
			FeedSubscription subscription = symbolSubscriptions.get(identifierType.getCompoundSymbol());
			if (subscription == null) {
				subscription = new FeedSubscription(this, identifierType);
				symbolSubscriptions.put(identifierType.getCompoundSymbol(), subscription);

	    	    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
	    	    if (propertyChangeSupport != null)
	    	    	propertyChangeSupport.addPropertyChangeListener(this);
			}
	    	if (identifierType.getIdentifier() == null) {
	    		identifierType.setIdentifier(identifier);

	    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
	    	    if (propertyChangeSupport != null)
	    	    	propertyChangeSupport.addPropertyChangeListener(this);
	    	}
	    	if (subscription.incrementInstanceCount() == 1) {
				try {
					if (connection != null)
						subscription.submitRequests(connection);
                } catch (Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error submitting requests", e);
    				OTActivator.log(status);
                }
	    	}
			return subscription;
		}
	}

	protected void disposeSubscription(FeedSubscription subscription) {
		synchronized (symbolSubscriptions) {
			if (subscription.decrementInstanceCount() <= 0) {
				IdentifierType identifierType = subscription.getIdentifierType();

		    	if (subscription.getIdentifier() != null) {
		    	    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
		    	    if (propertyChangeSupport != null)
		    	    	propertyChangeSupport.removePropertyChangeListener(this);
		    	}

				symbolSubscriptions.remove(identifierType.getCompoundSymbol());
				try {
					subscription.cancelRequests();
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error canceling requests", e);
					OTActivator.log(status);
				}
			}
		}
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
		synchronized (symbolSubscriptions2) {
			IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);;
			FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getCompoundSymbol());
			if (subscription2 == null) {
				subscription2 = new FeedSubscription2(this, identifierType);
				symbolSubscriptions2.put(identifierType.getCompoundSymbol(), subscription2);
			}
	    	if (subscription2.incrementInstanceCount() == 1) {
				try {
					if (connection != null)
						subscription2.submitRequests(connection);
                } catch (Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error submitting requests", e);
    				OTActivator.log(status);
                }
	    	}
			return subscription2;
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(java.lang.String)
     */
    public IFeedSubscription2 subscribeLevel2(String symbol) {
		synchronized (symbolSubscriptions2) {
			IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(symbol);
			FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getCompoundSymbol());
			if (subscription2 == null) {
				subscription2 = new FeedSubscription2(this, identifierType);
				symbolSubscriptions2.put(identifierType.getCompoundSymbol(), subscription2);
			}
	    	if (subscription2.incrementInstanceCount() == 1) {
				try {
					if (connection != null)
						subscription2.submitRequests(connection);
                } catch (Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error submitting requests", e);
    				OTActivator.log(status);
                }
	    	}
			return subscription2;
		}
    }

	protected void disposeSubscription2(FeedSubscription2 subscription2) {
		synchronized(symbolSubscriptions2) {
	    	if (subscription2.decrementInstanceCount() <= 0) {
		    	IdentifierType identifierType = subscription2.getIdentifierType();
	    		symbolSubscriptions2.remove(identifierType.getCompoundSymbol());
				try {
					subscription2.cancelRequests();
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error canceling requests", e);
					OTActivator.log(status);
				}
	    	}
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
	 */
	public void connect() {
		Connector.getInstance().connect(this);
		connection = Connector.getInstance().getConnection();

		if (thread == null) {
			stopping = false;
			thread = new Thread(this, getName() + " - Notification");
			thread.start();
		}

		if (connection != null) {
			synchronized (symbolSubscriptions) {
				for (FeedSubscription subscription : symbolSubscriptions.values())
					subscription.submitRequests(connection);
				for (FeedSubscription2 subscription : symbolSubscriptions2.values())
					subscription.submitRequests(connection);
			}
		}

		if (OTActivator.getDefault() != null)
			OTActivator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	protected IPreferenceStore getPreferenceStore() {
		return OTActivator.getDefault().getPreferenceStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
	 */
	public void disconnect() {
		if (OTActivator.getDefault() != null)
			OTActivator.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);

		stopping = true;

		Connector.getInstance().disconnect();
		connection = null;

		if (thread != null) {
			try {
				synchronized (thread) {
					thread.notifyAll();
				}
				thread.join(30 * 1000);
			} catch (InterruptedException e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error stopping thread", e);
				OTActivator.log(status);
			}
			thread = null;
		}
	}

	public boolean isStopping() {
		return stopping;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		synchronized (thread) {
			while (!isStopping()) {
				FeedSubscription[] subscriptions;
				synchronized (symbolSubscriptions) {
					Collection<FeedSubscription> c = symbolSubscriptions.values();
					subscriptions = c.toArray(new FeedSubscription[c.size()]);
				}
				for (FeedSubscription s : subscriptions)
					s.fireNotification();

				FeedSubscription2[] subscriptions2;
				synchronized (symbolSubscriptions2) {
					Collection<FeedSubscription2> c = symbolSubscriptions2.values();
					subscriptions2 = c.toArray(new FeedSubscription2[c.size()]);
				}
				for (FeedSubscription2 s : subscriptions2)
					s.fireNotification();

				try {
					thread.wait();
				} catch (InterruptedException e) {
					// Ignore exception, not important at this time
				}
			}
		}
	}

	protected void wakeupNotifyThread() {
		if (thread != null) {
			synchronized (thread) {
				thread.notifyAll();
			}
		}
	}

	/* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
    	if (evt.getSource() instanceof IFeedIdentifier) {
    		IFeedIdentifier identifier = (IFeedIdentifier) evt.getSource();
			synchronized(symbolSubscriptions) {
				Collection<FeedSubscription> c = symbolSubscriptions.values();
				for (FeedSubscription subscription : c.toArray(new FeedSubscription[c.size()])) {
					if (subscription.getIdentifier() == identifier) {
						symbolSubscriptions.remove(subscription.getIdentifierType().getCompoundSymbol());
						try {
			                subscription.cancelRequests();
		                } catch (Exception e) {
		    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error canceling requests", e);
		    				OTActivator.log(status);
		                }

						IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
				    	subscription.setIdentifierType(identifierType);

				    	try {
							if (connection != null)
								subscription.submitRequests(connection);
		                } catch (Exception e) {
		    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error submitting requests", e);
		    				OTActivator.log(status);
		                }
				    	symbolSubscriptions.put(identifierType.getCompoundSymbol(), subscription);
					}
				}
			}
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    public void addConnectorListener(IConnectorListener listener) {
    	listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    public void removeConnectorListener(IConnectorListener listener) {
    	listeners.remove(listener);
    }

	public void fireConnectionEvent(ConnectorEvent event) {
		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			try {
				((IConnectorListener) l[i]).connectorStatusChange(event);
			} catch(Throwable e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error notifying connector status change", e);
				OTActivator.log(status);
			}
		}
    }
}
