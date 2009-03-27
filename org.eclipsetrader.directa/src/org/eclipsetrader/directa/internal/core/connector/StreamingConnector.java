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

package org.eclipsetrader.directa.internal.core.connector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.WebConnector;
import org.eclipsetrader.directa.internal.core.messages.AstaApertura;
import org.eclipsetrader.directa.internal.core.messages.AstaChiusura;
import org.eclipsetrader.directa.internal.core.messages.BidAsk;
import org.eclipsetrader.directa.internal.core.messages.Book;
import org.eclipsetrader.directa.internal.core.messages.CreaMsg;
import org.eclipsetrader.directa.internal.core.messages.DataMessage;
import org.eclipsetrader.directa.internal.core.messages.ErrorMessage;
import org.eclipsetrader.directa.internal.core.messages.Header;
import org.eclipsetrader.directa.internal.core.messages.Message;
import org.eclipsetrader.directa.internal.core.messages.Price;
import org.eclipsetrader.directa.internal.core.messages.Util;
import org.eclipsetrader.directa.internal.core.repository.IdentifierType;
import org.eclipsetrader.directa.internal.core.repository.IdentifiersList;
import org.eclipsetrader.directa.internal.core.repository.PriceDataType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StreamingConnector implements Runnable, IFeedConnector2, IExecutableExtension, PropertyChangeListener {
    private static StreamingConnector instance;

	public static final int PREZZO = 0;
	public static final int VARIAZIONE_PERC = 1;
	public static final int ORA = 2;
	public static final int DATA = 3;
	public static final int VOLUME = 4;
	public static final int MINIMO = 5;
	public static final int MASSIMO = 6;
	public static final int APERTURA = 7;
	public static final int PRECEDENTE = 8;
	public static final int INIZIO_BOOK = 9;
	public static final int BID_QUANTITA = 39;
	public static final int BID_PREZZO = 40;
	public static final int ASK_QUANTITA = 41;
	public static final int ASK_PREZZO = 42;
    //private static final String DESCRIPTION = "d";
    private static final String DATI = "t2";
    //private static final String GRAPH = "g";

    private String id;
    private String name;

    private Map<String, FeedSubscription> symbolSubscriptions;
	private Map<String, FeedSubscription2> symbolSubscriptions2;
	private boolean subscriptionsChanged = false;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private TimeZone timeZone;
	private SimpleDateFormat df;
	private SimpleDateFormat df2;

	private Thread thread;
	private Thread notificationThread;
	private boolean stopping = false;

	private String streamingServer = "213.92.13.44";
	private int streamingPort = 8002;
	private String streamingVersion = "3.0";
	private Socket socket;
	private OutputStream os;
	private DataInputStream is;

	private Log logger = LogFactory.getLog(getClass());

	private Runnable notificationRunnable = new Runnable() {
		public void run() {
			synchronized(notificationThread) {
				while (!isStopping()) {
					FeedSubscription[] subscriptions;
					synchronized (symbolSubscriptions) {
						Collection<FeedSubscription> c = symbolSubscriptions.values();
						subscriptions = c.toArray(new FeedSubscription[c.size()]);
					}
					for (int i = 0; i < subscriptions.length; i++)
						subscriptions[i].fireNotification();

					try {
						notificationThread.wait();
					} catch (InterruptedException e) {
						// Ignore exception, not important at this time
					}
				}
			}
		}
	};

	public StreamingConnector() {
		symbolSubscriptions = new HashMap<String, FeedSubscription>();
		symbolSubscriptions2 = new HashMap<String, FeedSubscription2>();

		timeZone = TimeZone.getTimeZone("Europe/Rome");

		df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        df.setTimeZone(timeZone);
		df2 = new SimpleDateFormat("dd.MM.yyyy HHmmss");
        df2.setTimeZone(timeZone);
	}

	public synchronized static StreamingConnector getInstance() {
		if (instance == null)
			instance = new StreamingConnector();
    	return instance;
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    	instance = this;
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
		synchronized(symbolSubscriptions) {
	    	IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
	    	FeedSubscription subscription = symbolSubscriptions.get(identifierType.getSymbol());
	    	if (subscription == null) {
	    		subscription = new FeedSubscription(this, identifierType);

	    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
	    	    if (propertyChangeSupport != null)
	    	    	propertyChangeSupport.addPropertyChangeListener(this);

	    		symbolSubscriptions.put(identifierType.getSymbol(), subscription);
	        	subscriptionsChanged = true;
	    	}
	    	if (identifierType.getIdentifier() == null) {
	    		identifierType.setIdentifier(identifier);

	    		PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
	    	    if (propertyChangeSupport != null)
	    	    	propertyChangeSupport.addPropertyChangeListener(this);
	    	}
	    	if (subscription.incrementInstanceCount() == 1)
	        	subscriptionsChanged = true;
		    return subscription;
		}
    }

    protected void disposeSubscription(FeedSubscription subscription) {
		synchronized(symbolSubscriptions) {
	    	if (subscription.decrementInstanceCount() <= 0) {
		    	IdentifierType identifierType = subscription.getIdentifierType();

		    	if (subscription.getIdentifier() != null) {
		    	    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
		    	    if (propertyChangeSupport != null)
		    	    	propertyChangeSupport.removePropertyChangeListener(this);
		    	}

	    		symbolSubscriptions.remove(identifierType.getSymbol());
	        	subscriptionsChanged = true;
	    	}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
    	FeedSubscription subscription;
    	IdentifierType identifierType;

    	synchronized(symbolSubscriptions) {
	    	identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
	    	subscription = symbolSubscriptions.get(identifierType.getSymbol());
	    	if (subscription == null) {
	    		subscription = new FeedSubscription(this, identifierType);

	    	    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
	    	    if (propertyChangeSupport != null)
	    	    	propertyChangeSupport.addPropertyChangeListener(this);

	    		symbolSubscriptions.put(identifierType.getSymbol(), subscription);
	        	subscriptionsChanged = true;
	    	}
	    	if (subscription.incrementInstanceCount() == 1)
	        	subscriptionsChanged = true;
		}

		synchronized(symbolSubscriptions2) {
	    	FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
	    	if (subscription2 == null) {
	    		subscription2 = new FeedSubscription2(this, subscription);
	    		symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
	        	subscriptionsChanged = true;
	    	}
	    	if (subscription.incrementLevel2InstanceCount() == 1)
	        	subscriptionsChanged = true;
		    return subscription;
		}
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(java.lang.String)
     */
    public IFeedSubscription2 subscribeLevel2(String symbol) {
    	FeedSubscription subscription;
    	IdentifierType identifierType;

    	synchronized(symbolSubscriptions) {
	    	identifierType = IdentifiersList.getInstance().getIdentifierFor(symbol);
	    	subscription = symbolSubscriptions.get(identifierType.getSymbol());
	    	if (subscription == null) {
	    		subscription = new FeedSubscription(this, identifierType);

	    		symbolSubscriptions.put(identifierType.getSymbol(), subscription);
	        	subscriptionsChanged = true;
	    	}
	    	if (subscription.incrementInstanceCount() == 1)
	        	subscriptionsChanged = true;
		}

		synchronized(symbolSubscriptions2) {
	    	FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
	    	if (subscription2 == null) {
	    		subscription2 = new FeedSubscription2(this, subscription);
	    		symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
	        	subscriptionsChanged = true;
	    	}
	    	if (subscription.incrementLevel2InstanceCount() == 1)
	        	subscriptionsChanged = true;
		    return subscription;
		}
    }

	protected void disposeSubscription2(FeedSubscription subscription, FeedSubscription2 subscription2) {
		synchronized(symbolSubscriptions2) {
	    	if (subscription.decrementLevel2InstanceCount() <= 0) {
		    	IdentifierType identifierType = subscription.getIdentifierType();
	    		symbolSubscriptions2.remove(identifierType.getSymbol());
	        	subscriptionsChanged = true;
	    	}
		}
		synchronized(symbolSubscriptions) {
	    	if (subscription.decrementInstanceCount() <= 0) {
		    	IdentifierType identifierType = subscription.getIdentifierType();

		    	if (subscription.getIdentifier() != null) {
		    	    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
		    	    if (propertyChangeSupport != null)
		    	    	propertyChangeSupport.removePropertyChangeListener(this);
		    	}

	    		symbolSubscriptions.remove(identifierType.getSymbol());
	        	subscriptionsChanged = true;
	    	}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    public synchronized void connect() {
    	WebConnector.getInstance().login();

    	stopping = false;

		if (notificationThread == null || !notificationThread.isAlive()) {
			notificationThread = new Thread(notificationRunnable, name + " - Notification");
			notificationThread.start();
		}

		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this, name + " - Data Reader");
			thread.start();
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    public synchronized void disconnect() {
        stopping = true;

        if (thread != null) {
			try {
				thread.join(30 * 1000);
			} catch (InterruptedException e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error stopping thread", e);
				Activator.log(status);
			}
			thread = null;
		}

        if (notificationThread != null) {
			try {
				synchronized(notificationThread) {
					notificationThread.notify();
				}
				notificationThread.join(30 * 1000);
			} catch (InterruptedException e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error stopping notification thread", e);
				Activator.log(status);
			}
			notificationThread = null;
		}
    }

	public boolean isStopping() {
    	return stopping;
    }

	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
		int n = 0;
		byte bHeader[] = new byte[4];
		Set<String> sTit = new HashSet<String>();
		Set<String> sTit2 = new HashSet<String>();

		// Apertura del socket verso il server
		try {
			Proxy socksProxy = Proxy.NO_PROXY;
			if (Activator.getDefault() != null) {
				BundleContext context = Activator.getDefault().getBundle().getBundleContext();
				ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
				if (reference != null) {
					IProxyService proxy = (IProxyService) context.getService(reference);
					IProxyData data = proxy.getProxyDataForHost(streamingServer, IProxyData.SOCKS_PROXY_TYPE);
					if (data != null) {
						if (data.getHost() != null)
							socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(data.getHost(), data.getPort()));
					}
					context.ungetService(reference);
				}
			}
			socket = new Socket(socksProxy);
			socket.connect(new InetSocketAddress(streamingServer, streamingPort));
			os = socket.getOutputStream();
			is = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server", e));
			try {
				if (socket != null) {
					socket.close();
					socket = null;
				}
			} catch(Exception e1) {
				// Do nothing
			}
			return;
		}

		// Login
		try {
			os.write(CreaMsg.creaLoginMsg(WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt(), "flashBook", streamingVersion));
			os.flush();

			byte bHeaderLogin[] = new byte[4];
			n = is.read(bHeaderLogin);
			int lenMsg = Util.getMessageLength(bHeaderLogin, 2);
			if ((char) bHeaderLogin[0] != '#' && n != -1)
				return;

			byte msgResp[] = new byte[lenMsg];
			is.read(msgResp);
			if (Util.byteToInt(bHeaderLogin[1]) == CreaMsg.ERROR_MSG) {
				ErrorMessage eMsg = new ErrorMessage(msgResp);
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server: " + eMsg.sMessageError, null));
				return;
			}
			try {
				os.write(CreaMsg.creaStartDataMsg());
				os.flush();
			} catch (Exception e) {
				thread = null;
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error starting data stream", e));
				return;
			}
		} catch (Exception e) {
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server", e));
			return;
		}

		// Forces the subscriptions update on startup
		subscriptionsChanged = true;

		while (!isStopping()) {
			if (subscriptionsChanged) {
	        	Set<String> toAdd = new HashSet<String>();
	        	Set<String> toRemove = new HashSet<String>();
	        	Set<String> toAdd2 = new HashSet<String>();
	        	Set<String> toRemove2 = new HashSet<String>();

	        	synchronized(symbolSubscriptions) {
		        	for (FeedSubscription s : symbolSubscriptions.values()) {
		        		if (!sTit.contains(s.getIdentifierType().getSymbol()))
		        			toAdd.add(s.getIdentifierType().getSymbol());
		        		if (s.getLevel2InstanceCount() != 0) {
			        		if (!sTit2.contains(s.getIdentifierType().getSymbol()))
			        			toAdd2.add(s.getIdentifierType().getSymbol());
		        		}
		        	}
		        	for (String s : sTit) {
		        		if (!symbolSubscriptions.containsKey(s))
		        			toRemove.add(s);
		        	}
		        	for (String s : sTit2) {
		        		if (!symbolSubscriptions2.containsKey(s))
		        			toRemove2.add(s);
		        	}
		        	subscriptionsChanged = false;
				}

				if (toRemove.size() != 0) {
					try {
						logger.info("Removing " + toRemove);
						int flag[] = new int[toRemove.size()];
						for (int i = 0; i < flag.length; i++)
							flag[i] = 0;
						os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_DEL, toRemove.toArray(new String[toRemove.size()]), flag));
						os.flush();
					} catch (Exception e) {
						thread = null;
						Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error removing symbols from stream", e));
						break;
					}
				}

				if (toAdd.size() != 0) {
					try {
						logger.info("Adding " + toAdd);
						String s[] = toAdd.toArray(new String[toAdd.size()]);
						int flag[] = new int[s.length];
						for (int i = 0; i < flag.length; i++)
							flag[i] = sTit2.contains(s[i]) || toAdd2.contains(s[i]) ? 105 : 0;
						os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_ADD, s, flag));
						os.flush();
					} catch (Exception e) {
						thread = null;
						Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error adding symbols from stream", e));
						break;
					}
				}

				if (toAdd2.size() != 0 || toRemove2.size() != 0) {
					try {
			        	Map<String, Integer> toMod = new HashMap<String, Integer>();
			        	for (String s : toAdd2) {
			        		if (!toAdd.contains(s))
			        			toMod.put(s, new Integer(105));
			        	}
			        	for (String s : toRemove2)
		        			toMod.put(s, new Integer(0));

			        	if (toMod.size() != 0) {
							logger.info("Modifying " + toMod);

				        	String s[] = toMod.keySet().toArray(new String[toMod.keySet().size()]);
							int flag[] = new int[s.length];
							for (int i = 0; i < flag.length; i++)
								flag[i] = toMod.get(s[i]).intValue();
							os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_MOD, s, flag));
							os.flush();
			        	}
					} catch (Exception e) {
						thread = null;
						Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error adding symbols from stream", e));
						break;
					}
				}

				sTit.removeAll(toRemove);
				sTit.addAll(toAdd);
				sTit2.removeAll(toRemove2);
				sTit2.addAll(toAdd2);

				if (toAdd.size() != 0) {
					final String[] addSymbols = toAdd.toArray(new String[toAdd.size()]);
					fetchLatestBookSnapshot(addSymbols);
					fetchLatestSnapshot(addSymbols);
				}
			}

			// Legge l'header di un messaggio (se c'e')
			try {
				if ((n = is.read(bHeader)) == -1)
					continue;
				while (n < 4) {
					int r = is.read(bHeader, n, 4 - n);
					n += r;
				}
			} catch (Exception e) {
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e));
				break;
			}

			// Verifica la correttezza dell'header e legge il resto del messaggio
			Header h = new Header();
			h.start = (char) Util.byteToInt(bHeader[0]);
			if (h.start == '#') {
				h.tipo = Util.getByte(bHeader[1]);
				h.len = Util.getMessageLength(bHeader, 2);
				byte mes[] = new byte[h.len];
				try {
					n = is.read(mes);
					while (n < h.len) {
						int r = is.read(mes, n, h.len - n);
						n += r;
					}
				} catch (Exception e) {
				}

				if (h.tipo == CreaMsg.ERROR_MSG) {
					ErrorMessage eMsg = new ErrorMessage(mes);
					Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Message from server: " + eMsg.sMessageError, null));
				}
				else if (h.tipo == Message.TIP_ECHO) {
					try {
						os.write(new byte[] { bHeader[0], bHeader[1], bHeader[2], bHeader[3], mes[0], mes[1] });
						os.flush();
					} catch (Exception e) {
						// Do nothing
					}
				}
				else if (h.len > 0) {
					DataMessage obj;
					try {
						obj = Message.decodeMessage(mes);
						if (obj == null)
							continue;
					} catch(Exception e) {
						Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error decoding incoming message", e));
						continue;
					}

					FeedSubscription subscription = symbolSubscriptions.get(obj.head.key);
					if (subscription != null) {
						PriceDataType priceData = subscription.getIdentifierType().getPriceData();

						if (obj instanceof Price) {
							Price pm = (Price) obj;

							Object oldValue = subscription.getQuote();
							priceData.setLast(pm.val_ult);
							priceData.setLastSize(pm.qta_ult);
							priceData.setVolume(pm.qta_prgs);
							priceData.setTime(new Date(pm.ora_ult));
							Object newValue = new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume());
							if (!newValue.equals(oldValue)) {
								subscription.setTrade((ITrade) newValue);
								subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
								wakeupNotifyThread();
							}

							oldValue = subscription.getTodayOHL();
							priceData.setHigh(pm.max);
							priceData.setLow(pm.min);
							newValue = new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow());
							if (!newValue.equals(oldValue)) {
								subscription.setTodayOHL((ITodayOHL) newValue);
								subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
								wakeupNotifyThread();
							}
						}
						else if (obj instanceof Book) {
							Book bm = (Book) obj;

							IBookEntry[] bidEntry = new IBookEntry[0];
							IBookEntry[] askEntry = new IBookEntry[0];

							IBook oldValue = subscription.getBook();
							if (oldValue != null) {
								if (oldValue.getBidProposals() != null)
									bidEntry = oldValue.getBidProposals();
								if (oldValue.getAskProposals() != null)
									askEntry = oldValue.getAskProposals();
							}

							int levels = bm.offset + 5;
							if (bidEntry.length < levels) {
								IBookEntry[] newEntry = new IBookEntry[levels];
								for (int i = 0; i < bidEntry.length; i++)
									newEntry[i] = bidEntry[i];
								bidEntry = newEntry;
							}
							if (askEntry.length < levels) {
								IBookEntry[] newEntry = new IBookEntry[levels];
								for (int i = 0; i < askEntry.length; i++)
									newEntry[i] = askEntry[i];
								askEntry = newEntry;
							}

							int index = bm.offset;
							for (int i = 0; i < 5; i++) {
								bidEntry[index + i] = new BookEntry(null, bm.val_c[i], new Long(bm.q_pdn_c[i]), new Long(bm.n_pdn_c[i]), null);
								askEntry[index + i] = new BookEntry(null, bm.val_v[i], new Long(bm.q_pdn_v[i]), new Long(bm.n_pdn_v[i]), null);
							}
							IBook newValue = new org.eclipsetrader.core.feed.Book(bidEntry, askEntry);
							subscription.setBook(newValue);
							subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
							wakeupNotifyThread();
						}
						else if (obj instanceof BidAsk) {
							BidAsk bam = (BidAsk) obj;

							Object oldValue = subscription.getQuote();
							priceData.setBid(bam.bid);
							priceData.setBidSize(bam.num_bid);
							priceData.setAsk(bam.ask);
							priceData.setAskSize(bam.num_ask);
							Object newValue = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
							if (!newValue.equals(oldValue)) {
								subscription.setQuote((IQuote) newValue);
								subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
								wakeupNotifyThread();
							}
						}
						else if (obj instanceof AstaApertura) {
							AstaApertura ap = (AstaApertura) obj;

							Object oldValue = subscription.getTrade();
							priceData.setLast(ap.val_aper);
							priceData.setVolume(ap.qta_aper);
							priceData.setTime(new Date(ap.ora_aper));
							Object newValue = new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume());
							if (!newValue.equals(oldValue)) {
								subscription.setTrade((ITrade) newValue);
								subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
								wakeupNotifyThread();
							}
						}
						else if (obj instanceof AstaChiusura) {
							AstaChiusura ac = (AstaChiusura) obj;

							Object oldValue = subscription.getTrade();
							priceData.setLast(ac.val_chiu);
							priceData.setTime(new Date(ac.ora_chiu));
							Object newValue = new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume());
							if (!newValue.equals(oldValue)) {
								subscription.setTrade((ITrade) newValue);
								subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
								wakeupNotifyThread();
							}
						}
					}
				}
			}
		}

		try {
			os.write(CreaMsg.creaStopDataMsg());
			os.flush();
		} catch (Exception e) {
			Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error stopping data stream", e));
		}

		try {
			os.write(CreaMsg.creaLogoutMsg());
			os.flush();
		} catch (Exception e) {
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error closing connection to streaming server", e));
		}

		try {
			os.close();
			is.close();
			socket.close();
		} catch (Exception e) {
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error closing connection to streaming server", e));
		}

		os = null;
		is = null;
		socket = null;

		if (!isStopping()) {
			thread = new Thread(this, name + " - Data Reader");
			try {
				Thread.sleep(2 * 1000);
			} catch(Exception e) {
				// Do nothing
			}
			thread.start();
		}
    }

	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

    protected void fetchLatestBookSnapshot(String[] sTit) {
		Hashtable<String, String[]> hashtable = new Hashtable<String, String[]>();

		try {
			HttpMethod method = createMethod(sTit, "t", streamingServer, WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt());
			method.setFollowRedirects(true);

			HttpClient client = new HttpClient();
			setupProxy(client, streamingServer);
			client.executeMethod(method);

			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

			String s5;
			while ((s5 = bufferedreader.readLine()) != null) {
				String[] campo = s5.split("\\;");
				if (campo.length != 0)
					hashtable.put(campo[0], campo);
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e);
			Activator.log(status);
		}

		for (String symbol : sTit) {
			String sVal[] = hashtable.get(symbol);
			if (sVal == null)
				continue;
			FeedSubscription subscription = symbolSubscriptions.get(symbol);
			if (subscription == null)
				continue;

			try {
				Object oldValue = subscription.getBook();
				IBookEntry[] bidEntry = new IBookEntry[20];
				IBookEntry[] askEntry = new IBookEntry[20];
				for (int x = 0, k = 9; x < 20; x++, k += 6) {
					bidEntry[x] = new BookEntry(null, Double.parseDouble(sVal[k + 2]), Long.parseLong(sVal[k + 1]), Long.parseLong(sVal[k]), null);
					askEntry[x] = new BookEntry(null, Double.parseDouble(sVal[k + 5]), Long.parseLong(sVal[k + 4]), Long.parseLong(sVal[k + 3]), null);
				}
				Object newValue = new org.eclipsetrader.core.feed.Book(bidEntry, askEntry);
				subscription.setBook((IBook) newValue);
				subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
			} catch (Exception e) {
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e));
			}
		}

		wakeupNotifyThread();
    }

    protected void fetchLatestSnapshot(String[] sTit) {
		int flag[] = new int[sTit.length];
		for (int i = 0; i < flag.length; i++)
			flag[i] = 1;

		try {
			String s = "[!QUOT]";
			byte byte0 = 43;

			Hashtable<String, String[]> hashtable = new Hashtable<String, String[]>();
			try {
				HttpMethod method = createMethodOld(sTit, DATI, "213.92.13.41", WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt());
				method.setFollowRedirects(true);

				HttpClient client = new HttpClient();
				setupProxy(client, streamingServer);
				client.executeMethod(method);

				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

				String s5;
				while ((s5 = bufferedreader.readLine()) != null && !s5.startsWith(s))
					;

				if (s5 != null) {
					do {
						if (s5.startsWith(s)) {
							String as[] = new String[43];
							for (int i = 0; i < 43; i++)
								as[i] = "0";

							StringTokenizer stringtokenizer = new StringTokenizer(s5, ",\t");
							String s2 = stringtokenizer.nextToken();
							s2 = s2.substring(s2.indexOf(s) + s.length()).trim();
							for (int j = 0; j < byte0; j++) {
								String s4;
								try {
									s4 = stringtokenizer.nextToken().trim();
									if (s4.equals(""))
										s4 = "0";
								} catch (NoSuchElementException nosuchelementexception) {
									hashtable.put(s2, as);
									break;
								}
								as[j] = s4;
							}

							if (as[2].length() == 6)
								as[2] = as[2].substring(0, 2) + ":" + as[2].substring(2, 4) + ":" + as[2].substring(4, 6);
							if (as[3].length() == 8)
								as[3] = as[3].substring(6, 8) + "." + as[3].substring(4, 6) + "." + as[3].substring(0, 4);
							hashtable.put(s2, as);
						}
					} while ((s5 = bufferedreader.readLine()) != null);
				}
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e);
				Activator.log(status);
			}

			for (String symbol : sTit) {
				String sVal[] = hashtable.get(symbol);
				if (sVal == null)
					continue;
				FeedSubscription subscription = symbolSubscriptions.get(symbol);
				if (subscription == null)
					continue;

				IdentifierType identifierType = subscription.getIdentifierType();
				PriceDataType priceData = identifierType.getPriceData();

				Object oldValue = identifierType.getTrade();
				priceData.setLast(Double.parseDouble(sVal[PREZZO]));
				priceData.setVolume(Long.parseLong(sVal[VOLUME]));
				try {
					if (sVal[DATA].equals("0"))
						sVal[DATA] = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());

					if (sVal[ORA].indexOf(':') == -1) {
						if (sVal[ORA].length() < 6)
							sVal[ORA] = "0" + sVal[ORA];
						priceData.setTime(df2.parse(sVal[DATA] + " " + sVal[ORA]));
					}
					else
						priceData.setTime(df.parse(sVal[DATA] + " " + sVal[ORA]));
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing date: " + " (DATE='" + sVal[DATA] + "', TIME='" + sVal[ORA] + "')", e);
					Activator.log(status);
				}
				Object newValue = new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume());
				if (!newValue.equals(oldValue)) {
					subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
					identifierType.setTrade((ITrade) newValue);
				}

				oldValue = identifierType.getQuote();
				priceData.setBid(Double.parseDouble(sVal[BID_PREZZO]));
				priceData.setBidSize(Long.parseLong(sVal[BID_QUANTITA]));
				priceData.setAsk(Double.parseDouble(sVal[ASK_PREZZO]));
				priceData.setAskSize(Long.parseLong(sVal[ASK_QUANTITA]));
				newValue = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
				if (!newValue.equals(oldValue)) {
					subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
					identifierType.setQuote((IQuote) newValue);
				}

				oldValue = identifierType.getTodayOHL();
				priceData.setOpen(Double.parseDouble(sVal[APERTURA]));
				priceData.setHigh(Double.parseDouble(sVal[MASSIMO]));
				priceData.setLow(Double.parseDouble(sVal[MINIMO]));
				newValue = new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow());
				if (!newValue.equals(oldValue)) {
					subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
					identifierType.setTodayOHL((ITodayOHL) newValue);
				}

				oldValue = identifierType.getLastClose();
				priceData.setClose(Double.parseDouble(sVal[PRECEDENTE]));
				newValue = new LastClose(priceData.getClose(), null);
				if (!newValue.equals(oldValue)) {
					subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
					identifierType.setLastClose((ILastClose) newValue);
				}
			}

			wakeupNotifyThread();
		} catch (Exception e) {
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e));
		}
	}

    public void wakeupNotifyThread() {
    	if (notificationThread != null) {
    		synchronized(notificationThread) {
    			notificationThread.notifyAll();
    		}
    	}
    }

    private HttpMethod createMethod(String as[], String mode, String host, String urt, String prt) throws MalformedURLException {
		GetMethod method = new GetMethod("http://" + host + "/preqs/getdata.php");

		StringBuffer s = new StringBuffer();
		for (int i = 0; i < as.length; i++) {
			s.append(as[i]);
			s.append("|");
		}

		method.setQueryString(new NameValuePair[] {
				new NameValuePair("modo", mode),
				new NameValuePair("u", urt),
				new NameValuePair("p", prt),
				new NameValuePair("out", "p"),
				new NameValuePair("listaid", s.toString()),
				new NameValuePair("lb", "20"),
			});

		return method;
	}

    private HttpMethod createMethodOld(String as[], String mode, String host, String urt, String prt) throws MalformedURLException {
		GetMethod method = new GetMethod("http://" + host + "/cgi-bin/preqa.fcgi");

		StringBuffer s = new StringBuffer();
		for (int i = 0; i < as.length; i++) {
			s.append(as[i]);
			s.append("|");
		}

		method.setQueryString(new NameValuePair[] {
				new NameValuePair("modo", mode),
				new NameValuePair("stcmd", s.toString()),
				new NameValuePair("u", urt),
				new NameValuePair("p", prt),
			});

		return method;
	}

    protected void setupProxy(HttpClient client, String host) {
		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
			if (reference != null) {
				IProxyService proxy = (IProxyService) context.getService(reference);
				IProxyData data = proxy.getProxyDataForHost(host, IProxyData.HTTP_PROXY_TYPE);
				if (data != null) {
					if (data.getHost() != null)
						client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
					if (data.isRequiresAuthentication())
						client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
				}
				context.ungetService(reference);
			}
		}
    }

	/* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
    	if (evt.getSource() instanceof IFeedIdentifier) {
    		IFeedIdentifier identifier = (IFeedIdentifier) evt.getSource();
			synchronized(symbolSubscriptions) {
				for (FeedSubscription subscription : symbolSubscriptions.values()) {
					if (subscription.getIdentifier() == identifier) {
						symbolSubscriptions.remove(subscription.getIdentifierType().getSymbol());
				    	IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
				    	subscription.setIdentifierType(identifierType);
			    		symbolSubscriptions.put(identifierType.getSymbol(), subscription);
			        	subscriptionsChanged = true;
			        	break;
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
}
