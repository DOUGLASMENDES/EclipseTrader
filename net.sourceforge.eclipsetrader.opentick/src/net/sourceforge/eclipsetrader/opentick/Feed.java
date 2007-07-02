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

package net.sourceforge.eclipsetrader.opentick;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.sourceforge.eclipsetrader.core.IFeed;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.opentick.internal.Client;
import net.sourceforge.eclipsetrader.opentick.internal.ClientAdapter;

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

import com.opentick.OTBBO;
import com.opentick.OTConstants;
import com.opentick.OTDataEntity;
import com.opentick.OTEquityInit;
import com.opentick.OTError;
import com.opentick.OTException;
import com.opentick.OTQuote;
import com.opentick.OTTodaysOHL;
import com.opentick.OTTrade;

public class Feed implements IFeed {
	boolean running = false;

	Set subscribedSecurities = new HashSet();

	Map streams = new HashMap();

	Set pendingStreams = new HashSet();

	Client client = Client.getInstance();

	private Log log = LogFactory.getLog(getClass());

	ClientAdapter clientListener = new ClientAdapter() {
		@Override
		public void onEquityInit(OTEquityInit msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestId()));
			if (security != null) {
				if (msg.getPrevClosePrice() != 0)
					security.setClose(new Double(msg.getPrevClosePrice()));
			} else
				log.warn("Unknown security for request id " + msg.getRequestId());
			streams.remove(String.valueOf(msg.getRequestId()));
		}

		@Override
		public void onError(OTError msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestId()));
			if (security != null) {
				log.error(msg.getRequestId() + " / " + msg.getDescription() + " (ticks) - " + security);
				streams.remove(String.valueOf(msg.getRequestId()));
			}
		}

		@Override
		public void onRealtimeQuote(OTQuote msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestID()));
			if (security != null) {
				Quote values = new Quote(security.getQuote());
				values.setDate(new Date(msg.getTimestamp() * 1000L));
				values.setBid(msg.getBidPrice());
				values.setBidSize(msg.getBidSize());
				values.setAsk(msg.getAskPrice());
				values.setAskSize(msg.getAskSize());
				security.setQuote(values);
			}
		}

		@Override
		public void onRealtimeBBO(OTBBO msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestID()));
			if (security != null) {
				Quote quote = new Quote(security.getQuote());
				quote.setDate(new Date(msg.getTimestamp() * 1000L));
				if (msg.getSide() == 'B') {
					quote.setBid(msg.getPrice());
					quote.setBidSize(msg.getSize());
				} else if (msg.getSide() == 'A' || msg.getSide() == 'S') {
					quote.setAsk(msg.getPrice());
					quote.setAskSize(msg.getSize());
				}
				security.setQuote(quote);
			}
		}

		@Override
		public void onRealtimeTrade(OTTrade msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestID()));
			if (security != null) {
				Quote quote = new Quote(security.getQuote());
				quote.setDate(new Date(msg.getTimestamp() * 1000L));
				quote.setLast(msg.getPrice());
				quote.setVolume(msg.getVolume());
				security.setQuote(quote);

				if (msg.isOpen())
					security.setOpen(new Double(msg.getPrice()));
				if (msg.isHigh())
					security.setHigh(new Double(msg.getPrice()));
				if (msg.isLow())
					security.setLow(new Double(msg.getPrice()));
				if (msg.isClose())
					security.setClose(new Double(msg.getPrice()));
			}
		}

		@Override
		public void onTodaysOHL(OTTodaysOHL msg) {
			Security security = (Security) streams.get(String.valueOf(msg.getRequestID()));
			if (security != null) {
				if (msg.getOpenPrice() != 0)
					security.setOpen(new Double(msg.getOpenPrice()));
				if (msg.getHighPrice() != 0)
					security.setHigh(new Double(msg.getHighPrice()));
				if (msg.getLowPrice() != 0)
					security.setLow(new Double(msg.getLowPrice()));
			}
		}

		@Override
		public void onRestoreConnection() {
			Object[] s = streams.values().toArray();
			streams.clear();

			try {
				for (int i = 0; i < s.length; i++)
					requestTickStream((Security) s[i]);
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	};

	public Feed() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#subscribe(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void subscribe(Security security) {
		if (!subscribedSecurities.contains(security)) {
			subscribedSecurities.add(security);

			try {
				if (running)
					requestTickStream(security);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).error(e, e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#unSubscribe(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public void unSubscribe(Security security) {
		if (subscribedSecurities.contains(security)) {
			subscribedSecurities.remove(security);

			try {
				if (running)
					cancelTickStream(security);
			} catch (Exception e) {
				LogFactory.getLog(getClass()).error(e, e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#start()
	 */
	public void start() {
		if (!running) {
			streams.clear();
			pendingStreams.clear();

			client.addListener(clientListener);
			try {
				client.login(15 * 1000);
				for (Iterator iter = subscribedSecurities.iterator(); iter.hasNext();)
					requestTickStream((Security) iter.next());
			} catch (Exception e) {
				log.error(e, e);
			}

			running = true;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#stop()
	 */
	public void stop() {
		if (running && client.isLoggedIn()) {
			client.removeListener(clientListener);
			try {
				for (Iterator iter = subscribedSecurities.iterator(); iter.hasNext();)
					cancelTickStream((Security) iter.next());
			} catch (Exception e) {
				log.error(e, e);
			}

			streams.clear();
			pendingStreams.clear();
			running = false;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IFeed#snapshot()
	 */
	public void snapshot() {
		SimpleDateFormat usDateTimeParser = new SimpleDateFormat("MM/dd/yyyy h:mma");
		SimpleDateFormat usDateParser = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat usTimeParser = new SimpleDateFormat("h:mma");
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

		// Builds the url for quotes download
		String host = "quote.yahoo.com";
		StringBuffer url = new StringBuffer("http://" + host + "/download/javasoft.beans?symbols=");
		for (Iterator iter = subscribedSecurities.iterator(); iter.hasNext();) {
			Security security = (Security) iter.next();
			url = url.append(security.getCode() + "+");
		}
		if (url.charAt(url.length() - 1) == '+')
			url.deleteCharAt(url.length() - 1);
		url.append("&format=sl1d1t1c1ohgvbap");

		// Read the last prices
		String line = "";
		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			BundleContext context = OpenTickPlugin.getDefault().getBundle().getBundleContext();
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
				String[] item = line.split(",");
				if (line.indexOf(";") != -1)
					item = line.split(";");

				Double open = null, high = null, low = null, close = null;
				Quote quote = new Quote();

				// 2 = Date
				// 3 = Time
				try {
					GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("EST"), Locale.US);
					usDateTimeParser.setTimeZone(c.getTimeZone());
					usDateParser.setTimeZone(c.getTimeZone());
					usTimeParser.setTimeZone(c.getTimeZone());

					String date = stripQuotes(item[2]);
					if (date.indexOf("N/A") != -1)
						date = usDateParser.format(Calendar.getInstance().getTime());
					String time = stripQuotes(item[3]);
					if (time.indexOf("N/A") != -1)
						time = usTimeParser.format(Calendar.getInstance().getTime());
					c.setTime(usDateTimeParser.parse(date + " " + time));
					c.setTimeZone(TimeZone.getDefault());
					quote.setDate(c.getTime());
				} catch (Exception e) {
					System.out.println(e.getMessage() + ": " + line);
				}
				// 1 = Last price or N/A
				if (item[1].equalsIgnoreCase("N/A") == false)
					quote.setLast(numberFormat.parse(item[1]).doubleValue());
				// 4 = Change
				// 5 = Open
				if (item[5].equalsIgnoreCase("N/A") == false)
					open = new Double(numberFormat.parse(item[5]).doubleValue());
				// 6 = Maximum
				if (item[6].equalsIgnoreCase("N/A") == false)
					high = new Double(numberFormat.parse(item[6]).doubleValue());
				// 7 = Minimum
				if (item[7].equalsIgnoreCase("N/A") == false)
					low = new Double(numberFormat.parse(item[7]).doubleValue());
				// 8 = Volume
				if (item[8].equalsIgnoreCase("N/A") == false)
					quote.setVolume(numberFormat.parse(item[8]).intValue());
				// 9 = Bid Price
				if (item[9].equalsIgnoreCase("N/A") == false)
					quote.setBid(numberFormat.parse(item[9]).doubleValue());
				// 10 = Ask Price
				if (item[10].equalsIgnoreCase("N/A") == false)
					quote.setAsk(numberFormat.parse(item[10]).doubleValue());
				// 11 = Close Price
				if (item[11].equalsIgnoreCase("N/A") == false)
					close = new Double(numberFormat.parse(item[11]).doubleValue());

				// 0 = Code
				String symbol = stripQuotes(item[0]);
				for (Iterator iter = subscribedSecurities.iterator(); iter.hasNext();) {
					Security security = (Security) iter.next();
					if (symbol.equalsIgnoreCase(security.getCode()))
						security.setQuote(quote, open, high, low, close);
				}
			}
			in.close();
		} catch (Exception e) {
			System.out.println(e.getMessage() + ": " + line);
			e.printStackTrace();
		}
	}

	private String stripQuotes(String s) {
		if (s.startsWith("\""))
			s = s.substring(1);
		if (s.endsWith("\""))
			s = s.substring(0, s.length() - 1);
		return s;
	}

	void requestTickStream(Security security) throws OTException {
		if (!client.isLoggedIn())
			pendingStreams.add(security);
		else {
			String symbol = security.getQuoteFeed().getSymbol();
			if (symbol == null || symbol.length() == 0)
				symbol = security.getCode();
			String exchange = security.getQuoteFeed().getExchange();
			if (exchange == null || exchange.length() == 0)
				exchange = "Q";

			int id = client.requestEquityInit(new OTDataEntity(exchange, symbol));
			streams.put(String.valueOf(id), security);
			log.debug(String.valueOf(id) + " / Request Equity Init " + security);

			id = client.requestTodaysOHL(new OTDataEntity(exchange, symbol));
			streams.put(String.valueOf(id), security);
			log.debug(String.valueOf(id) + " / Request Today's OHL " + security);

			id = client.requestTickStream(new OTDataEntity(exchange, symbol), OTConstants.OT_TICK_TYPE_LEVEL1);
			streams.put(String.valueOf(id), security);
			log.debug(String.valueOf(id) + " / Request Tick stream " + security);
		}
	}

	public void cancelTickStream(Security security) throws OTException {
		String[] keys = (String[]) streams.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			if (security.equals(streams.get(keys[i]))) {
				client.cancelTickStream(Integer.parseInt(keys[i]));
				streams.remove(keys[i]);
				log.debug(String.valueOf(keys[i]) + " / Request cancel Tick stream " + security);
			}
		}
		pendingStreams.remove(security);
	}
}
