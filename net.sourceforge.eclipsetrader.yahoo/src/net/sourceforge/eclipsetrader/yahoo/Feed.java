/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.yahoo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.History;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Feed implements IFeed, Runnable {
	private Map map = new HashMap();

	private Thread thread;

	private boolean stopping = false;

	private SimpleDateFormat usDateTimeParser = new SimpleDateFormat("MM/dd/yyyy h:mma"); //$NON-NLS-1$

	private SimpleDateFormat usDateParser = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$

	private SimpleDateFormat usTimeParser = new SimpleDateFormat("h:mma"); //$NON-NLS-1$

	private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

	private Log log = LogFactory.getLog(getClass());

	public Feed() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#subscribe(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void subscribe(Security security) {
		String symbol = security.getQuoteFeed().getSymbol();
		if (symbol == null || symbol.length() == 0)
			symbol = security.getCode();
		map.put(security, symbol);
		log.info("Subscribed to " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#unSubscribe(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void unSubscribe(Security security) {
		map.remove(security);
		log.info("Unsubscribed from " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#start()
	 */
	public void start() {
		if (thread == null) {
			stopping = false;
			thread = new Thread(this);
			thread.start();
			log.info("Thread started"); //$NON-NLS-1$
		} else
			log.warn("Thread already instantiated"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#stop()
	 */
	public void stop() {
		stopping = true;
		if (thread != null) {
			try {
				thread.join(30 * 1000);
				log.info("Thread stopped"); //$NON-NLS-1$
			} catch (InterruptedException e) {
				log.error(e, e);
			}
			thread = null;
		} else
			log.warn("Thread not yet instantiated"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#snapshot()
	 */
	public void snapshot() {
		log.info("Snapshot update"); //$NON-NLS-1$
		update();

		boolean updateHistory = YahooPlugin.getDefault().getPreferenceStore().getBoolean(YahooPlugin.PREFS_UPDATE_HISTORY);
		if (updateHistory)
			updateHistory();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		long nextRun = System.currentTimeMillis() + 2 * 1000;

		while (!stopping) {
			if (System.currentTimeMillis() >= nextRun) {
				update();
				nextRun = System.currentTimeMillis() + 10 * 1000;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore exception, not important at this time
			}
		}
	}

	private void update() {
		// Builds the url for quotes download
		String host = "quote.yahoo.com";
		StringBuffer url = new StringBuffer("http://" + host + "/download/javasoft.beans?symbols="); //$NON-NLS-1$
		for (Iterator iter = map.values().iterator(); iter.hasNext();)
			url = url.append((String) iter.next() + "+"); //$NON-NLS-1$
		if (url.charAt(url.length() - 1) == '+')
			url.deleteCharAt(url.length() - 1);
		url.append("&format=sl1d1t1c1ohgvbap"); //$NON-NLS-1$
		log.debug(url.toString());

		// Read the last prices
		String line = ""; //$NON-NLS-1$
		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			BundleContext context = YahooPlugin.getDefault().getBundle().getBundleContext();
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
			}

			HttpMethod method = new GetMethod(url.toString());
			method.setFollowRedirects(true);
			client.executeMethod(method);

			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			while ((line = in.readLine()) != null) {
				String[] item = line.split(","); //$NON-NLS-1$
				if (line.indexOf(";") != -1) //$NON-NLS-1$
					item = line.split(";"); //$NON-NLS-1$

				Double open = null, high = null, low = null, close = null;
				Quote quote = new Quote();

				// 2 = Date
				// 3 = Time
				try {
					GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("EST"), Locale.US); //$NON-NLS-1$
					usDateTimeParser.setTimeZone(c.getTimeZone());
					usDateParser.setTimeZone(c.getTimeZone());
					usTimeParser.setTimeZone(c.getTimeZone());

					String date = stripQuotes(item[2]);
					if (date.indexOf("N/A") != -1) //$NON-NLS-1$
						date = usDateParser.format(Calendar.getInstance().getTime());
					String time = stripQuotes(item[3]);
					if (time.indexOf("N/A") != -1) //$NON-NLS-1$
						time = usTimeParser.format(Calendar.getInstance().getTime());
					c.setTime(usDateTimeParser.parse(date + " " + time)); //$NON-NLS-1$
					c.setTimeZone(TimeZone.getDefault());
					quote.setDate(c.getTime());
				} catch (Exception e) {
					log.error(e.getMessage() + ": " + line); //$NON-NLS-1$
				}
				// 1 = Last price or N/A
				if (item[1].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					quote.setLast(numberFormat.parse(item[1]).doubleValue());
				// 4 = Change
				// 5 = Open
				if (item[5].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					open = new Double(numberFormat.parse(item[5]).doubleValue());
				// 6 = Maximum
				if (item[6].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					high = new Double(numberFormat.parse(item[6]).doubleValue());
				// 7 = Minimum
				if (item[7].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					low = new Double(numberFormat.parse(item[7]).doubleValue());
				// 8 = Volume
				if (item[8].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					quote.setVolume(numberFormat.parse(item[8]).intValue());
				// 9 = Bid Price
				if (item[9].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					quote.setBid(numberFormat.parse(item[9]).doubleValue());
				// 10 = Ask Price
				if (item[10].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					quote.setAsk(numberFormat.parse(item[10]).doubleValue());
				// 11 = Close Price
				if (item[11].equalsIgnoreCase("N/A") == false) //$NON-NLS-1$
					close = new Double(numberFormat.parse(item[11]).doubleValue());

				// 0 = Code
				String symbol = stripQuotes(item[0]);
				for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
					Security security = (Security) iter.next();
					if (symbol.equalsIgnoreCase((String) map.get(security))) {
						security.setQuote(quote, open, high, low, close);
					}
				}
			}
			in.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	private String stripQuotes(String s) {
		if (s.startsWith("\"")) //$NON-NLS-1$
			s = s.substring(1);
		if (s.endsWith("\"")) //$NON-NLS-1$
			s = s.substring(0, s.length() - 1);
		return s;
	}

	protected void updateHistory() {
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			Security security = (Security) iter.next();
			updateHistory(security, security.getQuote());
		}
	}

	/**
	 * Updates security's history with quote data if:
	 * <li>security's history is not empty
	 * <li>quote's date is one day after the last history bar
	 * <li>current time is after the end of the financial day as per the security's end date.
	 * 
	 * @param security
	 * @param quote
	 */
	protected void updateHistory(Security security, Quote quote) {
		// update only if quote's date is the last history date + 1, and the current time is later than the end time in security settings
		History history = security.getHistory();
		if (quote.getDate() != null && (!history.isEmpty())) {
			Calendar quoteCalendar = Calendar.getInstance();
			quoteCalendar.setTime(quote.getDate());
			quoteCalendar = new GregorianCalendar(quoteCalendar.get(Calendar.YEAR), quoteCalendar.get(Calendar.MONTH), quoteCalendar.get(Calendar.DAY_OF_MONTH));

			Bar lastBar = history.get(history.size() - 1);

			Calendar lastBarDate = Calendar.getInstance();
			lastBarDate.setTime(lastBar.getDate());

			Calendar securityEndTime = Calendar.getInstance();
			securityEndTime.setTime(quote.getDate());
			securityEndTime.set(Calendar.HOUR_OF_DAY, security.getEndTime() / 60);
			securityEndTime.set(Calendar.MINUTE, security.getEndTime() % 60);
			securityEndTime.set(Calendar.SECOND, 0);
			securityEndTime.set(Calendar.MILLISECOND, 0);

			Calendar today = Calendar.getInstance();

			if (quoteCalendar.getTime().after(lastBarDate.getTime()) && today.after(securityEndTime)) {
				Bar bar = new Bar();
				bar.setDate(quoteCalendar.getTime());
				bar.setOpen(security.getOpen().doubleValue());
				bar.setHigh(security.getHigh().doubleValue());
				bar.setLow(security.getLow().doubleValue());
				bar.setClose(quote.getLast());
				bar.setVolume(quote.getVolume());
				history.add(bar);
				CorePlugin.getRepository().save(history);
			}
		}
	}
}
