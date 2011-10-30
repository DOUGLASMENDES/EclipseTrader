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

package org.eclipsetrader.directaworld.internal.core.connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
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
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.directaworld.internal.Activator;
import org.eclipsetrader.directaworld.internal.core.repository.IdentifierType;
import org.eclipsetrader.directaworld.internal.core.repository.IdentifiersList;
import org.eclipsetrader.directaworld.internal.core.repository.PriceDataType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SnapshotConnector implements Runnable, IFeedConnector, IExecutableExtension {

    private static final int I_SYMBOL = 0;
    //private static final int I_NAME = 1;
    private static final int I_LAST = 2;
    //private static final int I_CHANGE = 3;
    private static final int I_VOLUME = 4;
    private static final int I_TIME = 5;
    private static final int I_DATE = 6;
    private static final int I_BID = 7;
    private static final int I_BID_SIZE = 8;
    private static final int I_ASK = 9;
    private static final int I_ASK_SIZE = 10;
    private static final int I_OPEN = 12;
    private static final int I_CLOSE = 13;
    private static final int I_LOW = 14;
    private static final int I_HIGH = 15;

    private static final String HOST = "registrazioni.directaworld.it"; //$NON-NLS-1$

    private String id;
    private String name;

    protected Map<String, FeedSubscription> symbolSubscriptions;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    protected TimeZone timeZone;
    private SimpleDateFormat dateTimeParser;
    private SimpleDateFormat timeParser;
    private NumberFormat numberFormat;

    private HttpClient client;
    private String userName;
    private String password;

    private Thread thread;
    private boolean connected;
    private boolean stopping;
    private int requiredDelay = 15;

    private Log logger = LogFactory.getLog(getClass());

    public SnapshotConnector() {
        symbolSubscriptions = new HashMap<String, FeedSubscription>();

        timeZone = TimeZone.getTimeZone("Europe/Rome"); //$NON-NLS-1$

        dateTimeParser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
        dateTimeParser.setTimeZone(timeZone);
        timeParser = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
        timeParser.setTimeZone(timeZone);

        numberFormat = NumberFormat.getInstance(Locale.ITALY);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id"); //$NON-NLS-1$
        name = config.getAttribute("name"); //$NON-NLS-1$
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
                symbolSubscriptions.put(identifierType.getSymbol(), subscription);
                if (connected) {
                    startThread();
                }
            }
            subscription.incrementInstanceCount();
            return subscription;
        }
    }

    protected void disposeSubscription(FeedSubscription subscription) {
        synchronized (symbolSubscriptions) {
            if (subscription.decrementInstanceCount() <= 0) {
                IdentifierType identifierType = subscription.getIdentifierType();
                symbolSubscriptions.remove(identifierType.getSymbol());
                if (symbolSubscriptions.size() == 0 && connected) {
                    stopThread();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public synchronized void connect() {
        connected = true;
        synchronized (symbolSubscriptions) {
            if (symbolSubscriptions.size() != 0) {
                startThread();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public synchronized void disconnect() {
        stopThread();
        connected = false;
    }

    protected void startThread() {
        if (thread == null) {
            stopping = false;
            thread = new Thread(this, name + " - Data Reader"); //$NON-NLS-1$
            thread.start();
        }
    }

    protected void stopThread() {
        stopping = true;

        if (thread != null) {
            try {
                synchronized (thread) {
                    thread.notify();
                }
                if (thread != null) {
                    thread.join(30 * 1000);
                }
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error stopping thread", e); //$NON-NLS-1$
                Activator.log(status);
            }
            thread = null;
        }
    }

    public boolean isRunning() {
        return thread != null;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

        final ISecurePreferences securePreferences;
        if (preferenceStore.getBoolean(Activator.PREFS_USE_SECURE_PREFERENCE_STORE)) {
            securePreferences = SecurePreferencesFactory.getDefault().node(Activator.PLUGIN_ID);
            try {
                if (userName == null) {
                    userName = securePreferences.get(Activator.PREFS_USERNAME, ""); //$NON-NLS-1$
                }
                if (password == null) {
                    password = securePreferences.get(Activator.PREFS_PASSWORD, ""); //$NON-NLS-1$
                }
            } catch (Exception e) {
                final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error accessing secure storage", e); //$NON-NLS-1$
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        Activator.log(status);
                        ErrorDialog.openError(null, null, null, status);
                    }
                });
            }
        }
        else {
            securePreferences = null;
            if (userName == null) {
                userName = preferenceStore.getString(Activator.PREFS_USERNAME);
            }
            if (password == null) {
                password = preferenceStore.getString(Activator.PREFS_PASSWORD);
            }
        }

        client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        try {
            setupProxy(client, HOST);
        } catch (URISyntaxException e) {
            final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error setting proxy", e); //$NON-NLS-1$
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    Activator.log(status);
                    ErrorDialog.openError(null, null, null, status);
                }
            });
        }

        do {
            if (userName == null || password == null || "".equals(userName) || "".equals(password)) { //$NON-NLS-1$ //$NON-NLS-2$
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        Shell shell = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() : null;
                        LoginDialog dlg = new LoginDialog(shell, userName, password);
                        if (dlg.open() == Window.OK) {
                            userName = dlg.getUserName();
                            password = dlg.getPassword();
                            if (dlg.isSavePassword()) {
                                if (preferenceStore.getBoolean(Activator.PREFS_USE_SECURE_PREFERENCE_STORE)) {
                                    try {
                                        securePreferences.put(Activator.PREFS_USERNAME, userName, true);
                                        securePreferences.put(Activator.PREFS_PASSWORD, dlg.isSavePassword() ? password : "", true); //$NON-NLS-1$
                                    } catch (Exception e) {
                                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error accessing secure storage", e); //$NON-NLS-1$
                                        Activator.log(status);
                                        ErrorDialog.openError(null, null, null, status);
                                    }
                                }
                                else {
                                    preferenceStore.putValue(Activator.PREFS_USERNAME, userName);
                                    preferenceStore.putValue(Activator.PREFS_PASSWORD, dlg.isSavePassword() ? password : ""); //$NON-NLS-1$
                                }
                            }
                        }
                        else {
                            userName = null;
                            password = null;
                        }
                    }
                });
                if (userName == null || password == null) {
                    client = null;
                    thread = null;
                    return;
                }
            }
        } while (!checkLogin());

        synchronized (thread) {
            while (!stopping) {
                if (symbolSubscriptions.size() != 0) {
                    fetchLatestSnapshot();
                }
                try {
                    thread.wait(requiredDelay * 1000);
                } catch (InterruptedException e) {
                    // Ignore exception, not important at this time
                }
            }
        }

        client = null;
        thread = null;
    }

    private void setupProxy(HttpClient client, String host) throws URISyntaxException {
        if (Activator.getDefault() != null) {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference<IProxyService> reference = context.getServiceReference(IProxyService.class);
            if (reference != null) {
                IProxyService proxyService = context.getService(reference);
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

    protected IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    protected void fetchLatestSnapshot() {
        BufferedReader in = null;
        try {
            String[] symbols;
            synchronized (symbolSubscriptions) {
                symbols = symbolSubscriptions.keySet().toArray(new String[symbolSubscriptions.size()]);
            }

            StringBuilder url = new StringBuilder("http://" + HOST + "/cgi-bin/qta?idx=alfa&modo=t&appear=n"); //$NON-NLS-1$ //$NON-NLS-2$
            int x = 0;
            for (; x < symbols.length; x++) {
                url.append("&id" + (x + 1) + "=" + symbols[x]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for (; x < 30; x++) {
                url.append("&id" + (x + 1) + "="); //$NON-NLS-1$ //$NON-NLS-2$
            }
            url.append("&u=" + userName + "&p=" + password); //$NON-NLS-1$ //$NON-NLS-2$

            HttpMethod method = new GetMethod(url.toString());
            method.setFollowRedirects(true);

            logger.debug(method.getURI().toString());
            client.executeMethod(method);
            requiredDelay = 15;

            String inputLine;
            in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            while ((inputLine = in.readLine()) != null) {
                logger.debug(inputLine);
                if (inputLine.indexOf("<!--QT START HERE-->") != -1) { //$NON-NLS-1$
                    while ((inputLine = in.readLine()) != null) {
                        logger.debug(inputLine);
                        if (inputLine.indexOf("<!--QT STOP HERE-->") != -1) { //$NON-NLS-1$
                            break;
                        }
                        try {
                            parseLine(inputLine);
                        } catch (Exception e) {
                            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error parsing line: " + inputLine, e); //$NON-NLS-1$
                            Activator.log(status);
                        }
                    }
                }
                else if (inputLine.indexOf("Sara' possibile ricaricare la pagina tra") != -1) { //$NON-NLS-1$
                    int beginIndex = inputLine.indexOf("tra ") + 4; //$NON-NLS-1$
                    int endIndex = inputLine.indexOf("sec") - 1; //$NON-NLS-1$
                    try {
                        requiredDelay = Integer.parseInt(inputLine.substring(beginIndex, endIndex)) + 1;
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error parsing required delay", e); //$NON-NLS-1$
                        Activator.log(status);
                    }
                }
            }
            in.close();

            FeedSubscription[] subscriptions;
            synchronized (symbolSubscriptions) {
                Collection<FeedSubscription> c = symbolSubscriptions.values();
                subscriptions = c.toArray(new FeedSubscription[c.size()]);
            }
            for (int i = 0; i < subscriptions.length; i++) {
                subscriptions[i].fireNotification();
            }

        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e); //$NON-NLS-1$
            Activator.log(status);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                // We can't do anything at this time, ignore
            }
        }
    }

    protected void parseLine(String line) throws ParseException {
        String[] item = line.split(";"); //$NON-NLS-1$

        FeedSubscription subscription = symbolSubscriptions.get(item[I_SYMBOL]);
        if (subscription != null) {
            IdentifierType identifierType = subscription.getIdentifierType();
            PriceDataType priceData = identifierType.getPriceData();

            try {
                if (item[I_TIME].length() == 7) {
                    item[I_TIME] = item[I_TIME].charAt(0) + ":" + item[I_TIME].charAt(1) + item[I_TIME].charAt(3) + ":" + item[5].charAt(4) + item[I_TIME].charAt(6); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if ("".equals(item[I_DATE]) || " ".equals(item[I_DATE]) || " 0/  /0".equals(item[I_DATE])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    priceData.setTime(timeParser.parse(item[I_TIME]));
                }
                else {
                    priceData.setTime(dateTimeParser.parse(item[I_DATE] + " " + item[I_TIME])); //$NON-NLS-1$
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error parsing date in line: " + line, e); //$NON-NLS-1$
                Activator.log(status);
            }
            priceData.setLast(numberFormat.parse(item[I_LAST]).doubleValue());
            priceData.setVolume(numberFormat.parse(item[I_VOLUME]).longValue());
            subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), null, priceData.getVolume()));

            priceData.setBid(numberFormat.parse(item[I_BID]).doubleValue());
            priceData.setBidSize(numberFormat.parse(item[I_BID_SIZE]).longValue());
            priceData.setAsk(numberFormat.parse(item[I_ASK]).doubleValue());
            priceData.setAskSize(numberFormat.parse(item[I_ASK_SIZE]).longValue());
            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));

            priceData.setOpen(numberFormat.parse(item[I_OPEN]).doubleValue());
            priceData.setHigh(numberFormat.parse(item[I_HIGH]).doubleValue());
            priceData.setLow(numberFormat.parse(item[I_LOW]).doubleValue());
            if (priceData.getOpen() != 0.0 && priceData.getHigh() != 0.0 && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }

            priceData.setClose(numberFormat.parse(item[I_CLOSE]).doubleValue());
            subscription.setLastClose(new LastClose(priceData.getClose(), null));
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

    protected boolean checkLogin() {
        boolean result = false;

        BufferedReader in = null;
        try {
            StringBuilder url = new StringBuilder("http://" + HOST + "/cgi-bin/qta?idx=alfa&modo=t&appear=n"); //$NON-NLS-1$ //$NON-NLS-2$
            int x = 0;
            for (; x < 30; x++) {
                url.append("&id" + (x + 1) + "="); //$NON-NLS-1$ //$NON-NLS-2$
            }
            url.append("&u=" + userName + "&p=" + password); //$NON-NLS-1$ //$NON-NLS-2$

            HttpMethod method = new GetMethod(url.toString());
            method.setFollowRedirects(true);

            client.executeMethod(method);
            requiredDelay = 15;

            String inputLine;
            in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.indexOf("<!--QT START HERE-->") != -1) { //$NON-NLS-1$
                    result = true;
                }
            }
            in.close();
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e); //$NON-NLS-1$
            Activator.log(status);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                // We can't do anything at this time, ignore
            }
        }

        return result;
    }
}
