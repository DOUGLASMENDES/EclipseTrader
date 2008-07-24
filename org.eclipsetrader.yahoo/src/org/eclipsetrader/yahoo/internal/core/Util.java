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

package org.eclipsetrader.yahoo.internal.core;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;

public class Util {
	public static final String snapshotFeedHost = "quote.yahoo.com"; //$NON-NLS-1$
	public static final String streamingFeedHost = "streamerapi.finance.yahoo.com"; //$NON-NLS-1$
	public static final String historyFeedHost = "ichart.finance.yahoo.com"; //$NON-NLS-1$

	private Util() {
	}

	/**
	 * Builds the http method for live prices snapshot download.
	 *
	 * @return the method.
	 */
	public static HttpMethod getSnapshotFeedMethod(String[] symbols) {
		GetMethod method = new GetMethod("http://" + snapshotFeedHost + "/download/javasoft.beans");
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < symbols.length; i++) {
			if (i != 0)
				s.append("+"); //$NON-NLS-1$
			s.append(symbols[i]);
		}
		method.setQueryString(new NameValuePair[] {
				new NameValuePair("symbols", s.toString()),
				new NameValuePair("format", "sl1d1t1c1ohgvbapb6a5"),
			});
		method.setFollowRedirects(true);
		return method;
	}

	/**
	 * Builds the http method for streaming prices download.
	 *
	 * @return the method.
	 */
	public static HttpMethod getStreamingFeedMethod(String[] symbols) {
		GetMethod method = new GetMethod("http://" + streamingFeedHost + "/streamer/1.0");

		StringBuffer s = new StringBuffer();
		for (int i = 0; i < symbols.length; i++) {
			if (i != 0)
				s.append(","); //$NON-NLS-1$
			s.append(symbols[i]);
		}

		method.setQueryString(new NameValuePair[] {
				new NameValuePair("s", s.toString()),
				new NameValuePair("k", "a00,a50,b00,b60,g00,h00,j10,l10,t10,v00"),
				new NameValuePair("j", "c10,l10,p20,t10"),
				new NameValuePair("r", "0"),
				new NameValuePair("marketid", "us_market"),
				new NameValuePair("callback", "parent.yfs_u1f"),
				new NameValuePair("mktmcb", "parent.yfs_mktmcb"),
				new NameValuePair("gencallback", "parent.yfs_gencb"),
			});
		method.setFollowRedirects(true);

		return method;
	}

	/**
	 * Builds the http method for historycal prices download.
	 *
	 * @return the method.
	 */
	public static HttpMethod getHistoryFeedMethod(IFeedIdentifier identifier, Date from, Date to) {
		String symbol = identifier.getSymbol();

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty("org.eclipsetrader.yahoo.symbol") != null)
				symbol = properties.getProperty("org.eclipsetrader.yahoo.symbol");
		}

		Calendar fromDate = Calendar.getInstance();
		fromDate.setTime(from);

		Calendar toDate = Calendar.getInstance();
		toDate.setTime(to);

		GetMethod method = new GetMethod("http://" + historyFeedHost + "/table.csv");
        method.setQueryString(new NameValuePair[] {
        		new NameValuePair("s", symbol),
        		new NameValuePair("d", String.valueOf(toDate.get(Calendar.MONTH))),
        		new NameValuePair("e", String.valueOf(toDate.get(Calendar.DAY_OF_MONTH))),
        		new NameValuePair("f", String.valueOf(toDate.get(Calendar.YEAR))),
        		new NameValuePair("g", "d"),
        		new NameValuePair("a", String.valueOf(fromDate.get(Calendar.MONTH))),
        		new NameValuePair("b", String.valueOf(fromDate.get(Calendar.DAY_OF_MONTH))),
        		new NameValuePair("c", String.valueOf(fromDate.get(Calendar.YEAR))),
        		new NameValuePair("ignore", ".csv"),
        	});
		method.setFollowRedirects(true);

		return method;
	}

	public static HttpMethod getDividendsHistoryMethod(IFeedIdentifier identifier, Date from, Date to) {
		String symbol = identifier.getSymbol();

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty("org.eclipsetrader.yahoo.symbol") != null)
				symbol = properties.getProperty("org.eclipsetrader.yahoo.symbol");
		}

		Calendar fromDate = Calendar.getInstance();
		fromDate.setTime(from);

		Calendar toDate = Calendar.getInstance();
		toDate.setTime(to);

		GetMethod method = new GetMethod("http://" + historyFeedHost + "/table.csv");
        method.setQueryString(new NameValuePair[] {
        		new NameValuePair("s", symbol),
        		new NameValuePair("d", String.valueOf(toDate.get(Calendar.MONTH))),
        		new NameValuePair("e", String.valueOf(toDate.get(Calendar.DAY_OF_MONTH))),
        		new NameValuePair("f", String.valueOf(toDate.get(Calendar.YEAR))),
        		new NameValuePair("g", "v"),
        		new NameValuePair("a", String.valueOf(fromDate.get(Calendar.MONTH))),
        		new NameValuePair("b", String.valueOf(fromDate.get(Calendar.DAY_OF_MONTH))),
        		new NameValuePair("c", String.valueOf(fromDate.get(Calendar.YEAR))),
        		new NameValuePair("ignore", ".csv"),
        	});
		method.setFollowRedirects(true);

		return method;
	}
}
