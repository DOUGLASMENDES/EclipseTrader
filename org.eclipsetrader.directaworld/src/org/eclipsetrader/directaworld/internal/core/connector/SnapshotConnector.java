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

package org.eclipsetrader.directaworld.internal.core.connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.LastClose;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.QuoteDelta;
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
    private static SnapshotConnector instance;

    private static final String HOST = "registrazioni.directaworld.it";

    private String id;
    private String name;

	protected Map<String,FeedSubscription> symbolSubscriptions;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	protected TimeZone timeZone;
	private SimpleDateFormat dateTimeParser;
	private SimpleDateFormat timeParser;
	private NumberFormat numberFormat;

	private HttpClient client;
	private String userName;
	private String password;

	private Thread thread;
	private boolean stopping = false;
	private int requiredDelay = 15;

	public SnapshotConnector() {
		symbolSubscriptions = new HashMap<String,FeedSubscription>();

		timeZone = TimeZone.getTimeZone("Europe/Rome");

		dateTimeParser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
        dateTimeParser.setTimeZone(timeZone);
		timeParser = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
        timeParser.setTimeZone(timeZone);

		numberFormat = NumberFormat.getInstance(Locale.ITALY);
	}

	public synchronized static SnapshotConnector getInstance() {
		if (instance == null)
			instance = new SnapshotConnector();
    	return instance;
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
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
	    	FeedSubscription subscription = symbolSubscriptions.get(identifier.getSymbol());
	    	if (subscription == null) {
	    		subscription = new FeedSubscription(this, identifierType);
	    		symbolSubscriptions.put(identifier.getSymbol(), subscription);
	    	}
	    	subscription.incrementInstanceCount();
		    return subscription;
		}
    }

    protected void disposeSubscription(FeedSubscription subscription) {
		synchronized(symbolSubscriptions) {
			if (subscription.decrementInstanceCount() <= 0) {
				IdentifierType identifierType = subscription.getIdentifierType();
				symbolSubscriptions.remove(identifierType.getSymbol());
			}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    public synchronized void connect() {
		final IPreferenceStore preferences = getPreferenceStore();
		userName = preferences.getString(Activator.PREFS_USERNAME);
		password = preferences.getString(Activator.PREFS_PASSWORD);

		if (userName.length() == 0 || password.length() == 0) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() : null;
					LoginDialog dlg = new LoginDialog(shell, userName, password);
					if (dlg.open() == LoginDialog.OK) {
						userName = dlg.getUserName();
						password = dlg.getPassword();
						preferences.setValue(Activator.PREFS_USERNAME, userName);
						preferences.setValue(Activator.PREFS_PASSWORD, dlg.isSavePassword() ? password : "");
					}
				}
			});
			if (userName.length() == 0 || password.length() == 0)
				return;
		}

		if (client == null) {
			client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		}

		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
			if (reference != null) {
				IProxyService proxy = (IProxyService) context.getService(reference);
				IProxyData data = proxy.getProxyDataForHost(HOST, IProxyData.HTTP_PROXY_TYPE);
				if (data != null) {
					if (data.getHost() != null)
						client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
					if (data.isRequiresAuthentication())
						client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
				}
				context.ungetService(reference);
			}
		}

		if (thread == null) {
			stopping = false;
			thread = new Thread(this);
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
				synchronized(thread) {
					thread.notify();
				}
				thread.join(30 * 1000);
			} catch (InterruptedException e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error stopping thread", e);
				Activator.log(status);
			}
			thread = null;
		}
    }

    public boolean isRunning() {
    	return thread != null;
    }

	public boolean isStopping() {
    	return stopping;
    }

	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
		synchronized(thread) {
			while (!isStopping()) {
				fetchLatestSnapshot();
				try {
					thread.wait(requiredDelay * 1000);
				} catch (InterruptedException e) {
					// Ignore exception, not important at this time
				}
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
			synchronized(symbolSubscriptions) {
				symbols = symbolSubscriptions.keySet().toArray(new String[symbolSubscriptions.size()]);
			}

			StringBuilder url = new StringBuilder("http://" + HOST + "/cgi-bin/qta?idx=alfa&modo=t&appear=n");
			int x = 0;
			for (; x < symbols.length; x++)
				url.append("&id" + (x + 1) + "=" + symbols[x]);
			for (; x < 30; x++)
				url.append("&id" + (x + 1) + "="); //$NON-NLS-1$ //$NON-NLS-2$
			url.append("&u=" + userName + "&p=" + password); //$NON-NLS-1$ //$NON-NLS-2$

			HttpMethod method = new GetMethod(url.toString());
			method.setFollowRedirects(true);

			client.executeMethod(method);
			requiredDelay = 15;

			String inputLine;
			in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.indexOf("<!--QT START HERE-->") != -1) {
					while ((inputLine = in.readLine()) != null) {
						if (inputLine.indexOf("<!--QT STOP HERE-->") != -1)
							break;
						try {
							parseLine(inputLine);
						} catch (Exception e) {
							Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing line: " + inputLine, e);
							Activator.log(status);
						}
					}
				}
				else if (inputLine.indexOf("Sara' possibile ricaricare la pagina tra") != -1) {
					System.err.println(inputLine);
					int beginIndex = inputLine.indexOf("tra ") + 4;
					int endIndex = inputLine.indexOf("sec") - 1;
					try {
						requiredDelay = Integer.parseInt(inputLine.substring(beginIndex, endIndex)) + 1;
					} catch (Exception e) {
						Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing required delay", e);
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
			for (int i = 0; i < subscriptions.length; i++)
				subscriptions[i].fireNotification();

		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e);
			Activator.log(status);
		} finally {
			try {
	            if (in != null)
	            	in.close();
            } catch (Exception e) {
            	// We can't do anything at this time, ignore
            }
		}
	}

	public void parseLine(String line) throws ParseException {
		String[] item = line.split(";"); //$NON-NLS-1$

		FeedSubscription subscription = symbolSubscriptions.get(item[I_SYMBOL]);
		if (subscription != null) {
			IdentifierType identifierType = subscription.getIdentifierType();
			PriceDataType priceData = identifierType.getPriceData();

			Object oldValue = identifierType.getTrade();
			try {
				if (item[I_TIME].length() == 7)
					item[I_TIME] = item[I_TIME].charAt(0) + ":" + item[I_TIME].charAt(1) + item[I_TIME].charAt(3) + ":" + item[5].charAt(4) + item[I_TIME].charAt(6); //$NON-NLS-1$ //$NON-NLS-2$
				if ("".equals(item[I_DATE]) || " ".equals(item[I_DATE]))
					priceData.setTime(timeParser.parse(item[I_TIME]));
				else
					priceData.setTime(dateTimeParser.parse(item[I_DATE] + " " + item[I_TIME]));
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing date in line: " + line, e);
				Activator.log(status);
			}
			priceData.setLast(numberFormat.parse(item[I_LAST]).doubleValue());
			priceData.setVolume(numberFormat.parse(item[I_VOLUME]).longValue());
			Object newValue = new Trade(priceData.getTime(), priceData.getLast(), null, priceData.getVolume());
			if (!newValue.equals(oldValue)) {
				subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setTrade((ITrade) newValue);
			}

			oldValue = identifierType.getQuote();
			priceData.setBid(numberFormat.parse(item[I_BID]).doubleValue());
			priceData.setBidSize(numberFormat.parse(item[I_BID_SIZE]).longValue());
			priceData.setAsk(numberFormat.parse(item[I_ASK]).doubleValue());
			priceData.setAskSize(numberFormat.parse(item[I_ASK_SIZE]).longValue());
			newValue = new Quote(priceData.getBid(), priceData.getAsk(), priceData.getBidSize(), priceData.getAskSize());
			if (!newValue.equals(oldValue)) {
				subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setQuote((IQuote) newValue);
			}

			oldValue = identifierType.getTodayOHL();
			priceData.setOpen(numberFormat.parse(item[I_OPEN]).doubleValue());
			priceData.setHigh(numberFormat.parse(item[I_HIGH]).doubleValue());
			priceData.setLow(numberFormat.parse(item[I_LOW]).doubleValue());
			newValue = new TodayOHL(priceData.getOpen(), priceData.getHigh(), priceData.getLow());
			if (!newValue.equals(oldValue)) {
				subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setTodayOHL((ITodayOHL) newValue);
			}

			oldValue = identifierType.getLastClose();
			priceData.setClose(numberFormat.parse(item[I_CLOSE]).doubleValue());
			newValue = new LastClose(priceData.getClose(), null);
			if (!newValue.equals(oldValue)) {
				subscription.addDelta(new QuoteDelta(identifierType.getIdentifier(), oldValue, newValue));
				identifierType.setLastClose((ILastClose) newValue);
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
