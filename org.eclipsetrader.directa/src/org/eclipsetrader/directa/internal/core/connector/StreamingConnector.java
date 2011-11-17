/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector2;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.TimeSpan;
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

    private static final String INFO = "info"; //$NON-NLS-1$

    private static final String PREZZO = "[L=]"; //$NON-NLS-1$
    private static final String TRADE = "[LC=]"; //$NON-NLS-1$
    private static final String ORA = "[TLT]"; //$NON-NLS-1$
    private static final String DATA = "[DATA_ULT]"; //$NON-NLS-1$
    private static final String VOLUME = "[CV]"; //$NON-NLS-1$
    private static final String MINIMO = "[LO]"; //$NON-NLS-1$
    private static final String MASSIMO = "[HI]"; //$NON-NLS-1$
    private static final String APERTURA = "[OP1]"; //$NON-NLS-1$
    private static final String PRECEDENTE = "[LIE]"; //$NON-NLS-1$
    private static final String BID_QUANTITA = "[BS1]"; //$NON-NLS-1$
    private static final String BID_PREZZO = "[BP1]"; //$NON-NLS-1$
    private static final String ASK_QUANTITA = "[AS1]"; //$NON-NLS-1$
    private static final String ASK_PREZZO = "[AP1]"; //$NON-NLS-1$

    private String id;
    private String name;

    private Map<String, FeedSubscription> symbolSubscriptions;
    private Map<String, FeedSubscription2> symbolSubscriptions2;
    private boolean subscriptionsChanged = false;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private TimeZone timeZone;
    private SimpleDateFormat df;
    private SimpleDateFormat df2;
    private SimpleDateFormat df3;

    private Thread thread;
    private Thread notificationThread;
    private boolean stopping = false;

    private String streamingServer = "213.92.13.41"; //$NON-NLS-1$
    private int streamingPort = 8002;
    private String streamingVersion = "3.0"; //$NON-NLS-1$
    private Socket socket;
    private OutputStream os;
    private DataInputStream is;
    private Set<String> sTit;
    private Set<String> sTit2;

    private Log logger = LogFactory.getLog(getClass());

    private Runnable notificationRunnable = new Runnable() {

        @Override
        public void run() {
            synchronized (notificationThread) {
                while (!isStopping()) {
                    FeedSubscription[] subscriptions;
                    synchronized (symbolSubscriptions) {
                        Collection<FeedSubscription> c = symbolSubscriptions.values();
                        subscriptions = c.toArray(new FeedSubscription[c.size()]);
                    }
                    for (int i = 0; i < subscriptions.length; i++) {
                        subscriptions[i].fireNotification();
                    }

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

        timeZone = TimeZone.getTimeZone("Europe/Rome"); //$NON-NLS-1$

        df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
        df.setTimeZone(timeZone);
        df2 = new SimpleDateFormat("dd.MM.yyyy HHmmss"); //$NON-NLS-1$
        df2.setTimeZone(timeZone);
        df3 = new SimpleDateFormat("yyyyMMdd HH:mm:ss"); //$NON-NLS-1$
        df3.setTimeZone(timeZone);
    }

    public synchronized static StreamingConnector getInstance() {
        if (instance == null) {
            instance = new StreamingConnector();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
        instance = this;
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
        synchronized (symbolSubscriptions) {
            IdentifierType identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
            FeedSubscription subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }

                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (identifierType.getIdentifier() == null) {
                identifierType.setIdentifier(identifier);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
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
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(this);
                    }
                }

                symbolSubscriptions.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector2#subscribeLevel2(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    @Override
    public IFeedSubscription2 subscribeLevel2(IFeedIdentifier identifier) {
        FeedSubscription subscription;
        IdentifierType identifierType;

        synchronized (symbolSubscriptions) {
            identifierType = IdentifiersList.getInstance().getIdentifierFor(identifier);
            subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);

                PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) identifier.getAdapter(PropertyChangeSupport.class);
                if (propertyChangeSupport != null) {
                    propertyChangeSupport.addPropertyChangeListener(this);
                }

                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
        }

        synchronized (symbolSubscriptions2) {
            FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
            if (subscription2 == null) {
                subscription2 = new FeedSubscription2(this, subscription);
                symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
                subscriptionsChanged = true;
            }
            if (subscription.incrementLevel2InstanceCount() == 1) {
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
        FeedSubscription subscription;
        IdentifierType identifierType;

        synchronized (symbolSubscriptions) {
            identifierType = IdentifiersList.getInstance().getIdentifierFor(symbol);
            subscription = symbolSubscriptions.get(identifierType.getSymbol());
            if (subscription == null) {
                subscription = new FeedSubscription(this, identifierType);

                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                subscriptionsChanged = true;
            }
            if (subscription.incrementInstanceCount() == 1) {
                subscriptionsChanged = true;
            }
        }

        synchronized (symbolSubscriptions2) {
            FeedSubscription2 subscription2 = symbolSubscriptions2.get(identifierType.getSymbol());
            if (subscription2 == null) {
                subscription2 = new FeedSubscription2(this, subscription);
                symbolSubscriptions2.put(identifierType.getSymbol(), subscription2);
                subscriptionsChanged = true;
            }
            if (subscription.incrementLevel2InstanceCount() == 1) {
                subscriptionsChanged = true;
            }
            return subscription;
        }
    }

    protected void disposeSubscription2(FeedSubscription subscription, FeedSubscription2 subscription2) {
        synchronized (symbolSubscriptions2) {
            if (subscription.decrementLevel2InstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();
                symbolSubscriptions2.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();

                if (subscription.getIdentifier() != null) {
                    PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subscription.getIdentifier().getAdapter(PropertyChangeSupport.class);
                    if (propertyChangeSupport != null) {
                        propertyChangeSupport.removePropertyChangeListener(this);
                    }
                }

                symbolSubscriptions.remove(identifierType.getSymbol());
                subscriptionsChanged = true;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public synchronized void connect() {
        WebConnector.getInstance().login();

        stopping = false;

        if (notificationThread == null || !notificationThread.isAlive()) {
            notificationThread = new Thread(notificationRunnable, name + " - Notification"); //$NON-NLS-1$
            notificationThread.start();
        }

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, name + " - Data Reader"); //$NON-NLS-1$
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public synchronized void disconnect() {
        stopping = true;

        if (thread != null) {
            try {
                thread.join(30 * 1000);
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error stopping thread", e); //$NON-NLS-1$
                Activator.log(status);
            }
            thread = null;
        }

        if (notificationThread != null) {
            try {
                synchronized (notificationThread) {
                    notificationThread.notify();
                }
                notificationThread.join(30 * 1000);
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error stopping notification thread", e); //$NON-NLS-1$
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
    @Override
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public void run() {
        int n = 0;
        byte bHeader[] = new byte[4];

        sTit = new HashSet<String>();
        sTit2 = new HashSet<String>();

        // Apertura del socket verso il server
        try {
            Proxy socksProxy = Proxy.NO_PROXY;
            if (Activator.getDefault() != null) {
                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
                if (reference != null) {
                    IProxyService proxyService = (IProxyService) context.getService(reference);
                    IProxyData[] proxyData = proxyService.select(new URI(null, streamingServer, null, null));
                    for (int i = 0; i < proxyData.length; i++) {
                        if (IProxyData.SOCKS_PROXY_TYPE.equals(proxyData[i].getType()) && proxyData[i].getHost() != null) {
                            socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyData[i].getHost(), proxyData[i].getPort()));
                            break;
                        }
                    }
                    context.ungetService(reference);
                }
            }
            socket = new Socket(socksProxy);
            socket.connect(new InetSocketAddress(streamingServer, streamingPort));
            os = socket.getOutputStream();
            is = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server", e)); //$NON-NLS-1$
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e1) {
                // Do nothing
            }
            return;
        }

        // Login
        try {
            os.write(CreaMsg.creaLoginMsg(WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt(), "flashBook", streamingVersion)); //$NON-NLS-1$
            os.flush();

            byte bHeaderLogin[] = new byte[4];
            n = is.read(bHeaderLogin);
            int lenMsg = Util.getMessageLength(bHeaderLogin, 2);
            if ((char) bHeaderLogin[0] != '#' && n != -1) {
                return;
            }

            byte msgResp[] = new byte[lenMsg];
            is.read(msgResp);
            if (Util.byteToInt(bHeaderLogin[1]) == CreaMsg.ERROR_MSG) {
                ErrorMessage eMsg = new ErrorMessage(msgResp);
                Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server: " + eMsg.sMessageError, null)); //$NON-NLS-1$
                return;
            }
            try {
                os.write(CreaMsg.creaStartDataMsg());
                os.flush();
            } catch (Exception e) {
                thread = null;
                Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error starting data stream", e)); //$NON-NLS-1$
                return;
            }
        } catch (Exception e) {
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to streaming server", e)); //$NON-NLS-1$
            return;
        }

        // Forces the subscriptions update on startup
        subscriptionsChanged = true;

        while (!isStopping()) {
            if (subscriptionsChanged) {
                try {
                    updateStreamSubscriptions();
                } catch (Exception e) {
                    thread = null;
                    Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error updating stream subscriptions", e)); //$NON-NLS-1$
                    break;
                }
            }

            // Legge l'header di un messaggio (se c'e')
            try {
                if ((n = is.read(bHeader)) == -1) {
                    continue;
                }
                while (n < 4) {
                    int r = is.read(bHeader, n, 4 - n);
                    n += r;
                }
            } catch (Exception e) {
                Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e)); //$NON-NLS-1$
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
                    Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Message from server: " + eMsg.sMessageError, null)); //$NON-NLS-1$
                }
                else if (h.tipo == Message.TIP_ECHO) {
                    try {
                        os.write(new byte[] {
                            bHeader[0], bHeader[1], bHeader[2], bHeader[3], mes[0], mes[1]
                        });
                        os.flush();
                    } catch (Exception e) {
                        // Do nothing
                    }
                }
                else if (h.len > 0) {
                    DataMessage obj;
                    try {
                        obj = Message.decodeMessage(mes);
                        if (obj == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error decoding incoming message", e)); //$NON-NLS-1$
                        continue;
                    }

                    processMessage(obj);
                }
            }
        }

        try {
            os.write(CreaMsg.creaStopDataMsg());
            os.flush();
        } catch (Exception e) {
            Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error stopping data stream", e)); //$NON-NLS-1$
        }

        try {
            os.write(CreaMsg.creaLogoutMsg());
            os.flush();
        } catch (Exception e) {
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error closing connection to streaming server", e)); //$NON-NLS-1$
        }

        try {
            os.close();
            is.close();
            socket.close();
        } catch (Exception e) {
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error closing connection to streaming server", e)); //$NON-NLS-1$
        }

        os = null;
        is = null;
        socket = null;

        if (!isStopping()) {
            thread = new Thread(this, name + " - Data Reader"); //$NON-NLS-1$
            try {
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
                // Do nothing
            }
            thread.start();
        }
    }

    void updateStreamSubscriptions() throws IOException {
        Set<String> toAdd = new HashSet<String>();
        Set<String> toRemove = new HashSet<String>();
        Set<String> toAdd2 = new HashSet<String>();
        Set<String> toRemove2 = new HashSet<String>();

        synchronized (symbolSubscriptions) {
            for (FeedSubscription s : symbolSubscriptions.values()) {
                if (!sTit.contains(s.getIdentifierType().getSymbol())) {
                    toAdd.add(s.getIdentifierType().getSymbol());
                }
                if (s.getLevel2InstanceCount() != 0) {
                    if (!sTit2.contains(s.getIdentifierType().getSymbol())) {
                        toAdd2.add(s.getIdentifierType().getSymbol());
                    }
                }
            }
            for (String s : sTit) {
                if (!symbolSubscriptions.containsKey(s)) {
                    toRemove.add(s);
                }
            }
            for (String s : sTit2) {
                if (!symbolSubscriptions2.containsKey(s)) {
                    toRemove2.add(s);
                }
            }
            subscriptionsChanged = false;
        }

        if (toRemove.size() != 0) {
            logger.info("Removing " + toRemove); //$NON-NLS-1$
            int flag[] = new int[toRemove.size()];
            for (int i = 0; i < flag.length; i++) {
                flag[i] = 0;
            }
            os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_DEL, toRemove.toArray(new String[toRemove.size()]), flag));
            os.flush();
        }

        if (toAdd.size() != 0) {
            logger.info("Adding " + toAdd); //$NON-NLS-1$
            String s[] = toAdd.toArray(new String[toAdd.size()]);
            int flag[] = new int[s.length];
            for (int i = 0; i < flag.length; i++) {
                flag[i] = sTit2.contains(s[i]) || toAdd2.contains(s[i]) ? 105 : 0;
            }
            os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_ADD, s, flag));
            os.flush();
        }

        if (toAdd2.size() != 0 || toRemove2.size() != 0) {
            Map<String, Integer> toMod = new HashMap<String, Integer>();
            for (String s : toAdd2) {
                if (!toAdd.contains(s)) {
                    toMod.put(s, new Integer(105));
                }
            }
            for (String s : toRemove2) {
                toMod.put(s, new Integer(0));
            }

            if (toMod.size() != 0) {
                logger.info("Modifying " + toMod); //$NON-NLS-1$

                String s[] = toMod.keySet().toArray(new String[toMod.keySet().size()]);
                int flag[] = new int[s.length];
                for (int i = 0; i < flag.length; i++) {
                    flag[i] = toMod.get(s[i]).intValue();
                }
                os.write(CreaMsg.creaPortMsg(CreaMsg.PORT_MOD, s, flag));
                os.flush();
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

    void processMessage(DataMessage obj) {
        FeedSubscription subscription = symbolSubscriptions.get(obj.head.key);
        if (subscription == null) {
            return;
        }

        PriceDataType priceData = subscription.getIdentifierType().getPriceData();

        if (obj instanceof Price) {
            Price pm = (Price) obj;

            priceData.setLast(pm.val_ult);
            priceData.setLastSize(pm.qta_ult);
            priceData.setVolume(pm.qta_prgs);
            priceData.setTime(new Date(pm.ora_ult));
            subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume()));

            priceData.setHigh(pm.max);
            priceData.setLow(pm.min);

            if ((priceData.getOpen() == null || priceData.getOpen() == 0.0) && pm.val_ult != 0.0) {
                priceData.setOpen(pm.val_ult);

                Calendar c = Calendar.getInstance();
                c.setTime(priceData.getTime());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                BarOpen bar = new BarOpen(c.getTime(), TimeSpan.days(1), priceData.getOpen());
                subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), null, bar));
            }

            if (priceData.getOpen() != null && priceData.getOpen() != 0.0 && priceData.getHigh() != 0.0 && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }
        }
        else if (obj instanceof Book) {
            Book bm = (Book) obj;

            IBook oldValue = subscription.getBook();

            int levels = bm.offset + 5;
            if (oldValue != null) {
                IBookEntry[] oldEntry = oldValue.getBidProposals();
                if (oldEntry != null) {
                    levels = Math.max(levels, oldEntry.length);
                }
                oldEntry = oldValue.getAskProposals();
                if (oldEntry != null) {
                    levels = Math.max(levels, oldEntry.length);
                }
            }

            IBookEntry[] bidEntry = new IBookEntry[levels];
            IBookEntry[] askEntry = new IBookEntry[levels];

            if (oldValue != null) {
                IBookEntry[] oldEntry = oldValue.getBidProposals();
                if (oldEntry != null) {
                    System.arraycopy(oldEntry, 0, bidEntry, 0, oldEntry.length);
                }
                oldEntry = oldValue.getAskProposals();
                if (oldEntry != null) {
                    System.arraycopy(oldEntry, 0, askEntry, 0, oldEntry.length);
                }
            }

            int index = bm.offset;
            for (int i = 0; i < 5; i++) {
                bidEntry[index + i] = new BookEntry(null, bm.val_c[i], new Long(bm.q_pdn_c[i]), new Long(bm.n_pdn_c[i]), null);
                askEntry[index + i] = new BookEntry(null, bm.val_v[i], new Long(bm.q_pdn_v[i]), new Long(bm.n_pdn_v[i]), null);
            }
            IBook newValue = new org.eclipsetrader.core.feed.Book(bidEntry, askEntry);
            subscription.setBook(newValue);
        }
        else if (obj instanceof BidAsk) {
            BidAsk bam = (BidAsk) obj;

            priceData.setBid(bam.bid);
            priceData.setBidSize(bam.num_bid);
            priceData.setAsk(bam.ask);
            priceData.setAskSize(bam.num_ask);
            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));
        }
        else if (obj instanceof AstaApertura) {
            AstaApertura ap = (AstaApertura) obj;

            if (ap.val_aper != 0.0) {
                subscription.setPrice(new org.eclipsetrader.core.feed.Price(new Date(ap.ora_aper), ap.val_aper));
            }

            if (priceData.getClose() != null) {
                priceData.setLastClose(priceData.getClose());
                priceData.setClose(null);
                subscription.setLastClose(new LastClose(priceData.getLastClose(), null));
            }
            if (priceData.getOpen() != null) {
                priceData.setOpen(null);
                priceData.setHigh(null);
                priceData.setLow(null);
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }
        }
        else if (obj instanceof AstaChiusura) {
            AstaChiusura ac = (AstaChiusura) obj;

            if (priceData.getClose() == null && priceData.getLast() != null) {
                priceData.setClose(priceData.getLast());

                Calendar c = Calendar.getInstance();
                c.setTime(priceData.getTime());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                Bar bar = new Bar(c.getTime(), TimeSpan.days(1), priceData.getOpen(), priceData.getHigh(), priceData.getLow(), priceData.getClose(), priceData.getVolume());
                subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), null, bar));
            }

            if (ac.val_chiu != 0.0) {
                subscription.setPrice(new org.eclipsetrader.core.feed.Price(new Date(ac.ora_chiu), ac.val_chiu));
            }
        }

        if (subscription.hasPendingChanges()) {
            wakeupNotifyThread();
        }
    }

    protected IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    protected void fetchLatestBookSnapshot(String[] sTit) {
        Hashtable<String, String[]> hashtable = new Hashtable<String, String[]>();

        try {
            HttpMethod method = createMethod(sTit, "t", streamingServer, WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt()); //$NON-NLS-1$
            method.setFollowRedirects(true);

            HttpClient client = new HttpClient();
            setupProxy(client, streamingServer);
            client.executeMethod(method);

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

            String s5;
            while ((s5 = bufferedreader.readLine()) != null) {
                String[] campo = s5.split("\\;"); //$NON-NLS-1$
                if (campo.length != 0) {
                    hashtable.put(campo[0], campo);
                }
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e); //$NON-NLS-1$
            Activator.log(status);
        }

        for (String symbol : sTit) {
            String sVal[] = hashtable.get(symbol);
            if (sVal == null) {
                continue;
            }
            FeedSubscription subscription = symbolSubscriptions.get(symbol);
            if (subscription == null) {
                continue;
            }

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
                Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e)); //$NON-NLS-1$
            }
        }

        wakeupNotifyThread();
    }

    protected void fetchLatestSnapshot(String[] sTit) {
        int flag[] = new int[sTit.length];
        for (int i = 0; i < flag.length; i++) {
            flag[i] = 1;
        }

        try {
            String s = "[!QUOT]"; //$NON-NLS-1$
            byte byte0 = 43;

            Hashtable<String, Map<String, String>> hashTable = new Hashtable<String, Map<String, String>>();
            try {
                HttpMethod method = createSnapshotMethod(sTit, INFO, streamingServer, WebConnector.getInstance().getUrt(), WebConnector.getInstance().getPrt());
                method.setFollowRedirects(true);
                logger.debug(method.getURI().toString());

                HttpClient client = new HttpClient();
                setupProxy(client, streamingServer);
                client.executeMethod(method);

                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

                String s5;
                while ((s5 = bufferedreader.readLine()) != null && !s5.startsWith(s)) {
                    logger.debug(s5);
                }

                if (s5 != null) {
                    do {
                        logger.debug(s5);
                        if (s5.startsWith(s)) {
                            Map<String, String> as = new HashMap<String, String>();

                            StringTokenizer stringtokenizer = new StringTokenizer(s5, ",\t"); //$NON-NLS-1$
                            String s2 = stringtokenizer.nextToken();
                            s2 = s2.substring(s2.indexOf(s) + s.length()).trim();
                            for (int j = 0; j < byte0; j++) {
                                String s4;
                                try {
                                    s4 = stringtokenizer.nextToken().trim();
                                    int sq = s4.indexOf("]");
                                    as.put(s4.substring(0, sq + 1), s4.substring(sq + 1));
                                } catch (NoSuchElementException nosuchelementexception) {
                                    hashTable.put(s2, as);
                                    break;
                                }
                            }

                            hashTable.put(s2, as);
                        }
                    } while ((s5 = bufferedreader.readLine()) != null);
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e); //$NON-NLS-1$
                Activator.log(status);
            }

            processSnapshotData(sTit, hashTable);

            wakeupNotifyThread();
        } catch (Exception e) {
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading snapshot data", e)); //$NON-NLS-1$
        }
    }

    void processSnapshotData(String[] sTit, Hashtable<String, Map<String, String>> hashTable) {
        for (int i = 0; i < sTit.length; i++) {
            Map<String, String> sVal = hashTable.get(sTit[i]);
            if (sVal == null) {
                continue;
            }
            FeedSubscription subscription = symbolSubscriptions.get(sTit[i]);
            if (subscription == null) {
                continue;
            }

            IdentifierType identifierType = subscription.getIdentifierType();
            PriceDataType priceData = identifierType.getPriceData();

            try {
                if ("0".equals(sVal.get(DATA))) {
                    sVal.put(DATA, new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime())); //$NON-NLS-1$
                }
                priceData.setTime(df3.parse(sVal.get(DATA) + " " + sVal.get(ORA))); //$NON-NLS-1$
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error parsing date: " + " (DATE='" + sVal.get(DATA) + "', TIME='" + sVal.get(ORA) + "')", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                Activator.log(status);
            }

            priceData.setBid(Double.parseDouble(sVal.get(BID_PREZZO)));
            priceData.setBidSize(Long.parseLong(sVal.get(BID_QUANTITA)));
            priceData.setAsk(Double.parseDouble(sVal.get(ASK_PREZZO)));
            priceData.setAskSize(Long.parseLong(sVal.get(ASK_QUANTITA)));
            priceData.setVolume(Long.parseLong(sVal.get(VOLUME)));
            priceData.setLastClose(Double.parseDouble(sVal.get(PRECEDENTE)));
            priceData.setOpen(Double.parseDouble(sVal.get(APERTURA)));
            priceData.setHigh(Double.parseDouble(sVal.get(MASSIMO)));
            priceData.setLow(Double.parseDouble(sVal.get(MINIMO)));

            double tradePrice = Double.parseDouble(sVal.get(TRADE));
            if (tradePrice != 0.0) {
                priceData.setLast(tradePrice);
                subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), priceData.getLastSize(), priceData.getVolume()));
            }
            else {
                subscription.setPrice(new org.eclipsetrader.core.feed.Price(priceData.getTime(), Double.parseDouble(sVal.get(PREZZO))));
            }

            if (priceData.getLast() == null || priceData.getLast() == 0.0) {
                priceData.setLast(priceData.getLastClose());
            }

            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));
            if (priceData.getOpen() != 0.0 && priceData.getHigh() != 0.0 && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }
            subscription.setLastClose(new LastClose(priceData.getLastClose(), null));
        }
    }

    public void wakeupNotifyThread() {
        if (notificationThread != null) {
            synchronized (notificationThread) {
                notificationThread.notifyAll();
            }
        }
    }

    private HttpMethod createMethod(String as[], String mode, String host, String urt, String prt) throws MalformedURLException {
        GetMethod method = new GetMethod("http://" + host + "/preqs/getdata.php"); //$NON-NLS-1$ //$NON-NLS-2$

        StringBuffer s = new StringBuffer();
        for (int i = 0; i < as.length; i++) {
            s.append(as[i]);
            s.append("|"); //$NON-NLS-1$
        }

        method.setQueryString(new NameValuePair[] {
            new NameValuePair("modo", mode), //$NON-NLS-1$
            new NameValuePair("u", urt), //$NON-NLS-1$
            new NameValuePair("p", prt), //$NON-NLS-1$
            new NameValuePair("out", "p"), //$NON-NLS-1$ //$NON-NLS-2$
            new NameValuePair("listaid", s.toString()), //$NON-NLS-1$
            new NameValuePair("lb", "20"), //$NON-NLS-1$ //$NON-NLS-2$
        });

        return method;
    }

    private HttpMethod createSnapshotMethod(String as[], String mode, String host, String urt, String prt) throws Exception {
        StringBuilder s = new StringBuilder();
        s.append("/mpush.php?"); //$NON-NLS-1$
        s.append("modo=" + mode); //$NON-NLS-1$
        s.append("&cod=A"); //$NON-NLS-1$
        s.append("&stcmd="); //$NON-NLS-1$
        for (int i = 0; i < as.length; i++) {
            s.append("+"); //$NON-NLS-1$
            s.append(as[i]);
            s.append("|"); //$NON-NLS-1$
        }
        s.append("&u=" + urt); //$NON-NLS-1$
        s.append("&p=" + prt); //$NON-NLS-1$

        GetMethod method = new GetMethod();
        method.setURI(new org.apache.commons.httpclient.URI("http://" + host + s.toString(), false));

        return method;
    }

    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    private void setupProxy(HttpClient client, String host) throws URISyntaxException {
        if (Activator.getDefault() != null) {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
            if (reference != null) {
                IProxyService proxyService = (IProxyService) context.getService(reference);
                IProxyData[] proxyData = proxyService.select(new URI(null, host, null, null));
                for (int i = 0; i < proxyData.length; i++) {
                    if (IProxyData.HTTP_PROXY_TYPE.equals(proxyData[i].getType())) {
                        IProxyData data = proxyData[i];
                        if (data.getHost() != null) {
                            client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
                        }
                        if (data.isRequiresAuthentication()) {
                            client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
                        }
                        break;
                    }
                }
                context.ungetService(reference);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof IFeedIdentifier) {
            IFeedIdentifier identifier = (IFeedIdentifier) evt.getSource();
            synchronized (symbolSubscriptions) {
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
}
