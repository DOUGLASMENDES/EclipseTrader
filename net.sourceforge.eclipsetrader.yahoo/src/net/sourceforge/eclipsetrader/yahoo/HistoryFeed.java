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
import java.util.Date;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.History;
import net.sourceforge.eclipsetrader.core.db.Security;

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

import sun.misc.GC.LatencyRequest;

public class HistoryFeed implements IHistoryFeed {
	SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy", Locale.US); //$NON-NLS-1$

	SimpleDateFormat dfAlt = new SimpleDateFormat("yy-MM-dd"); //$NON-NLS-1$

	NumberFormat nf = NumberFormat.getInstance(Locale.US);

	NumberFormat pf = NumberFormat.getInstance(Locale.US);

	private Log log = LogFactory.getLog(getClass());

	public HistoryFeed() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.IHistoryFeed#updateHistory(net.sourceforge.eclipsetrader.core.db.Security, int)
	 */
	public void updateHistory(Security security, int interval) {
		if (interval == IHistoryFeed.INTERVAL_DAILY) {
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			History history = security.getHistory();
			if (history.size() == 0)
				from.add(Calendar.YEAR, -CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
			else {
				Bar cd = history.getLast();
				if (cd != null) {
					from.setTime(cd.getDate());
					from.add(Calendar.DATE, 1);
				}
			}

			try {
				HttpClient client = new HttpClient();
				client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
				String host = "ichart.finance.yahoo.com"; //$NON-NLS-1$
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
			
				String symbol = null;
				if (security.getHistoryFeed() != null)
					symbol = security.getHistoryFeed().getSymbol();
				if (symbol == null || symbol.length() == 0)
					symbol = security.getCode();
				
				// If the last bar from the data is from a date before today, then
				// download the historical data, otherwise it's enough to download the data for today.
				to.add(Calendar.DAY_OF_MONTH, -1);
				to.set(Calendar.HOUR, 0);
				to.set(Calendar.MINUTE, 0);
				to.set(Calendar.SECOND, 0);
				to.set(Calendar.MILLISECOND, 0);
				Bar lastHistoryBar = history.getLast();
				Date lastDate;
				if (lastHistoryBar == null) lastDate = from.getTime(); else lastDate = lastHistoryBar.getDate();
				if (lastDate.before(to.getTime())) {
					log.info("Updating historical data for " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
	
					StringBuffer url = new StringBuffer("http://" + host + "/table.csv" + "?s="); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					url.append(symbol);
					url.append("&d=" + to.get(Calendar.MONTH) + "&e=" + to.get(Calendar.DAY_OF_MONTH) + "&f=" + to.get(Calendar.YEAR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					url.append("&g=d"); //$NON-NLS-1$
					url.append("&a=" + from.get(Calendar.MONTH) + "&b=" + from.get(Calendar.DAY_OF_MONTH) + "&c=" + from.get(Calendar.YEAR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					url.append("&ignore=.csv"); //$NON-NLS-1$
					log.debug(url);
					
					HttpMethod method = new GetMethod(url.toString());
					method.setFollowRedirects(true);
					client.executeMethod(method);
	
					BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
	
					// The first line is the header, ignoring
					String inputLine = in.readLine();
					log.trace(inputLine);
	
					while ((inputLine = in.readLine()) != null) {
						log.trace(inputLine);
						if (inputLine.startsWith("<")) //$NON-NLS-1$
							continue;
						String[] item = inputLine.split(","); //$NON-NLS-1$
						if (item.length < 6)
							continue;
	
						Calendar day = Calendar.getInstance();
						try {
							day.setTime(df.parse(item[0]));
						} catch (Exception e) {
							try {
								day.setTime(dfAlt.parse(item[0]));
							} catch (Exception e1) {
								log.error(e1, e1);
							}
						}
						day.set(Calendar.HOUR, 0);
						day.set(Calendar.MINUTE, 0);
						day.set(Calendar.SECOND, 0);
						day.set(Calendar.MILLISECOND, 0);
	
						Bar bar = new Bar();
						bar.setDate(day.getTime());
						bar.setOpen(Double.parseDouble(item[1].replace(',', '.')));
						bar.setHigh(Double.parseDouble(item[2].replace(',', '.')));
						bar.setLow(Double.parseDouble(item[3].replace(',', '.')));
						bar.setClose(Double.parseDouble(item[4].replace(',', '.')));
						bar.setVolume(Long.parseLong(item[5]));
	
						// Remove the old bar, if exists
						int index = history.indexOf(bar.getDate());
						if (index != -1)
							history.remove(index);
	
						history.add(bar);
					}
					in.close();
				}
			
				// Get the data for today (to-date) using a different URL at Yahoo!
				//Bar lastbar = history.getLast
				log.debug("Get data for today using a separate URL..."); //$NON-NLS-1$
				StringBuffer url = new StringBuffer("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=sl1d1t1c1ohgv&e=.csv"); //$NON-NLS-1$ //$NON-NLS-2$
				log.debug(url);
				
				HttpMethod method = new GetMethod(url.toString());
				method.setFollowRedirects(true);
				client.executeMethod(method);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
				String inputLine = in.readLine();
				log.trace(inputLine);
				String[] item = inputLine.split(","); //$NON-NLS-1$
				item[2] = item[2].replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US); //$NON-NLS-1$
				Calendar day = Calendar.getInstance();
				
				// Transfer the data from the string array to the Bar
				day.setTime(df.parse(item[2]));
				Bar bar = new Bar();
				bar.setDate(day.getTime());
				bar.setOpen(Double.parseDouble(item[5]));
				bar.setClose(Double.parseDouble(item[1]));
				bar.setHigh(Double.parseDouble(item[6]));
				bar.setLow(Double.parseDouble(item[7]));
				bar.setVolume(Long.parseLong(item[8]));
				
				// Remove the old bar, if exists
				int index = history.indexOf(bar.getDate());
				if (index != -1)
					history.remove(index);
				history.add(bar);
				
				in.close();
				
			} catch (Exception e) {
				log.error(e, e);
			}
			
			CorePlugin.getRepository().save(history);
		} else
			log.warn("Intraday data not supported for " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
