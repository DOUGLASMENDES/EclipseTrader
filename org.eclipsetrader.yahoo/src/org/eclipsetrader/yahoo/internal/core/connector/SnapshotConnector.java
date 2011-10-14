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

package org.eclipsetrader.yahoo.internal.core.connector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TodayOHL;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.core.Util;
import org.eclipsetrader.yahoo.internal.core.repository.IdentifierType;
import org.eclipsetrader.yahoo.internal.core.repository.IdentifiersList;
import org.eclipsetrader.yahoo.internal.core.repository.PriceDataType;

public class SnapshotConnector implements Runnable, IFeedConnector, IExecutableExtension, PropertyChangeListener {

    private static final int I_CODE = 0;
    private static final int I_LAST = 1;
    private static final int I_DATE = 2;
    private static final int I_TIME = 3;
    //private static final int I_CHANGE = 4;
    private static final int I_OPEN = 5;
    private static final int I_HIGH = 6;
    private static final int I_LOW = 7;
    private static final int I_VOLUME = 8;
    private static final int I_BID = 9;
    private static final int I_ASK = 10;
    private static final int I_CLOSE = 11;
    //private static final int I_BID_SIZE = 12;
    //private static final int I_ASK_SIZE = 13;
    private static SnapshotConnector instance;
    private String id;
    private String name;

    protected Map<String, FeedSubscription> symbolSubscriptions;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    protected TimeZone timeZone;
    private SimpleDateFormat dateTimeParser;
    private SimpleDateFormat dateParser;
    private SimpleDateFormat timeParser;
    private NumberFormat numberFormat;

    protected Thread thread;
    private boolean stopping = false;
    private boolean subscriptionsChanged = false;

    public SnapshotConnector() {
        symbolSubscriptions = new HashMap<String, FeedSubscription>();

        timeZone = TimeZone.getTimeZone("America/New_York");

        dateTimeParser = new SimpleDateFormat("MM/dd/yyyy h:mma"); //$NON-NLS-1$
        dateTimeParser.setTimeZone(timeZone);

        dateParser = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
        dateParser.setTimeZone(timeZone);

        timeParser = new SimpleDateFormat("h:mma"); //$NON-NLS-1$
        timeParser.setTimeZone(timeZone);

        numberFormat = NumberFormat.getInstance(Locale.US);
    }

    public synchronized static SnapshotConnector getInstance() {
        if (instance == null) {
            instance = new SnapshotConnector();
        }
        return instance;
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
                setSubscriptionsChanged(true);
            }
            subscription.incrementInstanceCount();
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
                setSubscriptionsChanged(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public void connect() {
        if (thread == null || !thread.isAlive()) {
            stopping = false;
            thread = new Thread(this, name + " - Data Reader");
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
                synchronized (thread) {
                    thread.notify();
                }
                thread.join(30 * 1000);
            } catch (InterruptedException e) {
                Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error stopping thread", e);
                YahooActivator.log(status);
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
    @Override
    public void run() {
        try {
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            Util.setupProxy(client, Util.snapshotFeedHost);

            synchronized (thread) {
                while (!isStopping()) {
                    synchronized (symbolSubscriptions) {
                        if (symbolSubscriptions.size() != 0) {
                            String[] symbols = symbolSubscriptions.keySet().toArray(new String[symbolSubscriptions.size()]);
                            fetchLatestSnapshot(client, symbols, false);
                            setSubscriptionsChanged(false);
                        }
                    }

                    try {
                        thread.wait(5000);
                    } catch (InterruptedException e) {
                        // Ignore exception, not important at this time
                    }
                }
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
            YahooActivator.log(status);
        }
    }

    protected void fetchLatestSnapshot(HttpClient client, String[] symbols, boolean isStaleUpdate) {
        HttpMethod method = null;
        BufferedReader in = null;
        String line = ""; //$NON-NLS-1$

        try {
            method = Util.getSnapshotFeedMethod(symbols);

            client.executeMethod(method);

            in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            while ((line = in.readLine()) != null) {
                processSnapshotData(line, isStaleUpdate);
            }

            FeedSubscription[] subscriptions;
            synchronized (symbolSubscriptions) {
                Collection<FeedSubscription> c = symbolSubscriptions.values();
                subscriptions = c.toArray(new FeedSubscription[c.size()]);
            }
            for (int i = 0; i < subscriptions.length; i++) {
                subscriptions[i].fireNotification();
            }

        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
            YahooActivator.log(status);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (method != null) {
                    method.releaseConnection();
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.WARNING, YahooActivator.PLUGIN_ID, 0, "Connection wasn't closed cleanly", e);
                YahooActivator.log(status);
            }
        }
    }

    void processSnapshotData(String line, boolean isStaleUpdate) {
        String[] elements;
        if (line.indexOf(";") != -1) {
            elements = line.split(";"); //$NON-NLS-1$
        }
        else {
            elements = line.split(","); //$NON-NLS-1$
        }

        String symbol = stripQuotes(elements[I_CODE]);
        FeedSubscription subscription = symbolSubscriptions.get(symbol);
        if (subscription != null) {
            IdentifierType identifierType = subscription.getIdentifierType();
            PriceDataType priceData = identifierType.getPriceData();

            priceData.setTime(getDateValue(elements[I_DATE], elements[I_TIME]));
            priceData.setLast(getDoubleValue(elements[I_LAST]));
            priceData.setVolume(getLongValue(elements[I_VOLUME]));
            subscription.setTrade(new Trade(priceData.getTime(), priceData.getLast(), null, priceData.getVolume()));

            priceData.setBid(getDoubleValue(elements[I_BID]));
            if (!isStaleUpdate) {
                priceData.setBidSize(null); // getLongValue(elements[I_BID_SIZE]));
            }
            priceData.setAsk(getDoubleValue(elements[I_ASK]));
            if (!isStaleUpdate) {
                priceData.setAskSize(null); // getLongValue(elements[I_ASK_SIZE]));
            }
            subscription.setQuote(new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize()));

            priceData.setOpen(getDoubleValue(elements[I_OPEN]));
            priceData.setHigh(getDoubleValue(elements[I_HIGH]));
            priceData.setLow(getDoubleValue(elements[I_LOW]));
            if (priceData.getOpen() != null && priceData.getOpen() != 0.0 && priceData.getHigh() != null && priceData.getHigh() != 0.0 && priceData.getLow() != null && priceData.getLow() != 0.0) {
                subscription.setTodayOHL(new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow()));
            }

            priceData.setClose(getDoubleValue(elements[I_CLOSE]));
            subscription.setLastClose(new LastClose(priceData.getClose(), null));
        }
    }

    protected Date getDateValue(String dateValue, String timeValue) {
        String date = stripQuotes(dateValue);
        String time = stripQuotes(timeValue);

        if (date.indexOf("N/A") != -1 && time.indexOf("N/A") != -1) {
            return null;
        }

        try {
            if (date.indexOf("N/A") != -1) {
                date = dateParser.format(Calendar.getInstance(timeZone).getTime());
            }
            if (time.indexOf("N/A") != -1) {
                time = timeParser.format(Calendar.getInstance(timeZone).getTime());
            }

            Calendar c = Calendar.getInstance();
            c.setTime(dateTimeParser.parse(date + " " + time)); //$NON-NLS-1$
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.setTimeZone(TimeZone.getDefault());
            if (c.get(Calendar.YEAR) < 70) {
                c.add(Calendar.YEAR, 2000);
            }

            return c.getTime();
        } catch (ParseException e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing date/time values", e);
            YahooActivator.log(status);
        }

        return null;
    }

    protected Double getDoubleValue(String value) {
        try {
            if (!value.equals("") && !value.equalsIgnoreCase("N/A")) {
                return numberFormat.parse(value).doubleValue();
            }
        } catch (ParseException e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing number", e);
            YahooActivator.log(status);
        }
        return null;
    }

    protected Long getLongValue(String value) {
        try {
            if (!value.equals("") && !value.equalsIgnoreCase("N/A")) {
                return numberFormat.parse(value).longValue();
            }
        } catch (ParseException e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing number", e);
            YahooActivator.log(status);
        }
        return null;
    }

    protected String stripQuotes(String s) {
        if (s.startsWith("\"")) {
            s = s.substring(1);
        }
        if (s.endsWith("\"")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    protected boolean isSubscriptionsChanged() {
        return subscriptionsChanged;
    }

    protected void setSubscriptionsChanged(boolean subscriptionsChanged) {
        this.subscriptionsChanged = subscriptionsChanged;
    }

    Map<String, FeedSubscription> getSymbolSubscriptions() {
        return symbolSubscriptions;
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
                        setSubscriptionsChanged(true);
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
