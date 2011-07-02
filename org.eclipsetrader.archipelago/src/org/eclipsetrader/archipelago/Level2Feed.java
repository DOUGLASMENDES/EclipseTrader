/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.archipelago;

import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Level2Feed implements IFeedConnector2, Runnable, IExecutableExtension, IExecutableExtensionFactory {

    private static Level2Feed instance;

    private String id;
    private String name;

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private Map<String, FeedSubscription> symbolSubscriptions;
    private boolean subscriptionsChanged;

    private Thread thread;
    private boolean stopping;
    private Socket socket;
    private BufferedOutputStream os;
    private BufferedReader is;

    private Log logger = LogFactory.getLog(getClass());

    public Level2Feed() {
        if (instance == null) {
            symbolSubscriptions = new HashMap<String, FeedSubscription>();
            instance = this;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    @Override
    public Object create() throws CoreException {
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    @Override
    public IFeedSubscription subscribe(IFeedIdentifier identifier) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    @Override
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
        synchronized (symbolSubscriptions) {
            FeedSubscription subscription = symbolSubscriptions.get(identifier.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifier);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    ; // TODO propertyChangeSupport.addPropertyChangeListener(this);
                }

                symbolSubscriptions.put(identifier.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (subscription.getIdentifier() == null) {
                subscription.setIdentifier(identifier);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    ; // TODO propertyChangeSupport.addPropertyChangeListener(this);
                }
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(java.lang.String)
     */
    @Override
    public IFeedSubscription2 subscribeLevel2(String symbol) {
        synchronized (symbolSubscriptions) {
            FeedSubscription subscription = symbolSubscriptions.get(symbol);
            if (subscription == null) {
                subscription = new FeedSubscription(this, symbol);
                symbolSubscriptions.put(symbol, subscription);
                subscriptionsChanged = true;
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    public void disposeSubscription(FeedSubscription subscription) {
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                if (subscription.getIdentifier() != null) {
                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        ; // TODO propertyChangeSupport.removePropertyChangeListener(this);
                    }
                }

                symbolSubscriptions.remove(subscription.getSymbol());
                subscriptionsChanged = true;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public void connect() {
        if (thread == null) {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public void disconnect() {
        stopping = true;
        if (thread != null) {
            try {
                thread.join(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void addConnectorListener(IConnectorListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void removeConnectorListener(IConnectorListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        String HOST = "datasvr.tradearca.com";
        Set<String> sTit = new HashSet<String>();

        for (int i = 0; i < 5 && !stopping; i++) {
            try {
                Proxy socksProxy = Proxy.NO_PROXY;
                if (ArchipelagoPlugin.getDefault() != null) {
                    BundleContext context = ArchipelagoPlugin.getDefault().getBundle().getBundleContext();
                    ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
                    if (reference != null) {
                        IProxyService proxy = (IProxyService) context.getService(reference);
                        IProxyData data = proxy.getProxyDataForHost(HOST, IProxyData.SOCKS_PROXY_TYPE);
                        if (data != null) {
                            if (data.getHost() != null) {
                                socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(data.getHost(), data.getPort()));
                            }
                        }
                        context.ungetService(reference);
                    }
                }
                socket = new Socket(socksProxy);
                socket.connect(new InetSocketAddress(HOST, 80));
                os = new BufferedOutputStream(socket.getOutputStream());
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                os.write("GET http://datasvr.tradearca.com/zrepeaterz/ HTTP/1.1\r\n\r\n".getBytes());
                os.write("LogonRequest=DISABLED\r\n".getBytes());
                os.flush();
                break;
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, ArchipelagoPlugin.PLUGIN_ID, 0, "Error connecting to server", e);
                ArchipelagoPlugin.log(status);

                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e1) {
                }
                socket = null;
                is = null;
                os = null;
            }
        }
        if (socket == null || os == null || is == null) {
            thread = null;
            return;
        }

        while (!stopping) {
            try {
                if (subscriptionsChanged) {
                    Set<String> toAdd = new HashSet<String>();
                    Set<String> toRemove = new HashSet<String>();

                    synchronized (symbolSubscriptions) {
                        for (String s : symbolSubscriptions.keySet()) {
                            if (!sTit.contains(s)) {
                                toAdd.add(s);
                            }
                        }
                        for (String s : sTit) {
                            if (!symbolSubscriptions.containsKey(s)) {
                                toRemove.add(s);
                            }
                        }
                        subscriptionsChanged = false;
                    }

                    if (toRemove.size() != 0) {
                        for (String s : toRemove) {
                            os.write("MsgType=UnregisterBook&Symbol=".getBytes());
                            os.write(s.getBytes());
                            os.write("\r\n".getBytes());
                        }
                        logger.info("Removing " + toRemove);
                        os.flush();
                    }

                    if (toAdd.size() != 0) {
                        for (String s : toAdd) {
                            os.write("MsgType=RegisterBook&Symbol=".getBytes());
                            os.write(s.getBytes());
                            os.write("\r\n".getBytes());
                        }
                        logger.info("Adding " + toAdd);
                        os.flush();
                    }

                    sTit.removeAll(toRemove);
                    sTit.addAll(toAdd);
                }

                if (!is.ready()) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    continue;
                }

                String inputLine = is.readLine();
                if (inputLine.startsWith("BK&")) {
                    String[] sections = inputLine.split("&");
                    if (sections.length < 4) {
                        continue;
                    }

                    String symbol = sections[1];

                    int index = 0, item = 0;
                    String[] elements = sections[2].split("#");

                    List<BookEntry> bid = new ArrayList<BookEntry>();
                    while (index < elements.length) {
                        Double price = new Double(elements[index++]);
                        Long quantity = new Long(elements[index++]);
                        index++; // Time
                        String id = elements[index++];
                        bid.add(new BookEntry(null, price, quantity, 1L, id));
                        item++;
                    }

                    index = 0;
                    item = 0;
                    elements = sections[3].split("#");
                    List<BookEntry> ask = new ArrayList<BookEntry>();
                    while (index < elements.length) {
                        Double price = new Double(elements[index++]);
                        Long quantity = new Long(elements[index++]);
                        index++; // Time
                        String id = elements[index++];
                        ask.add(new BookEntry(null, price, quantity, 1L, id));
                        item++;
                    }

                    FeedSubscription subscription = symbolSubscriptions.get(symbol);
                    if (subscription != null) {
                        IBook oldValue = subscription.getBook();
                        IBook newValue = new org.eclipsetrader.core.feed.Book(bid.toArray(new IBookEntry[bid.size()]), ask.toArray(new IBookEntry[ask.size()]));
                        subscription.setBook(newValue);
                        subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), oldValue, newValue));
                        subscription.fireNotification();
                    }
                }
            } catch (SocketException e) {
                for (int i = 0; i < 5 && !stopping; i++) {
                    try {
                        socket = new Socket("datasvr.tradearca.com", 80);
                        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        os = new BufferedOutputStream(socket.getOutputStream());

                        os.write("GET http://datasvr.tradearca.com/zrepeaterz/ HTTP/1.1\r\n\r\n".getBytes());
                        os.write("LogonRequest=DISABLED\r\n".getBytes());
                        os.flush();

                        for (String s : sTit) {
                            os.write("MsgType=RegisterBook&Symbol=".getBytes());
                            os.write(s.getBytes());
                            os.write("\r\n".getBytes());
                        }
                        os.flush();
                        break;
                    } catch (Exception e1) {
                        Status status = new Status(IStatus.ERROR, ArchipelagoPlugin.PLUGIN_ID, 0, "Error connecting to server", e);
                        ArchipelagoPlugin.log(status);
                    }
                }
                if (socket == null || os == null || is == null) {
                    thread = null;
                    return;
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, ArchipelagoPlugin.PLUGIN_ID, 0, "Error receiving stream", e);
                ArchipelagoPlugin.log(status);
                break;
            }
        }

        try {
            if (socket != null) {
                socket.close();
            }
            socket = null;
            os = null;
            is = null;
        } catch (Exception e) {
            // Do nothing
        }

        thread = null;
    }
}
