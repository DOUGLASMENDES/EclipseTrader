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

package org.eclipsetrader.yahoo.internal.core;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Util {

    public static final String snapshotFeedHost = "download.finance.yahoo.com"; //$NON-NLS-1$
    public static final String streamingFeedHost = "streamerapi.finance.yahoo.com"; //$NON-NLS-1$
    public static final String historyFeedHost = "ichart.finance.yahoo.com"; //$NON-NLS-1$

    private Util() {
    }

    public static String getSymbol(IFeedIdentifier identifier) {
        String symbol = identifier.getSymbol();

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            if (properties.getProperty("org.eclipsetrader.yahoo.symbol") != null) {
                symbol = properties.getProperty("org.eclipsetrader.yahoo.symbol");
            }
        }

        return symbol;
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
            if (i != 0) {
                s.append(" "); //$NON-NLS-1$
            }
            s.append(symbols[i]);
        }
        method.setQueryString(new NameValuePair[] {
                new NameValuePair("symbols", s.toString()),
                new NameValuePair("format", "sl1d1t1c1ohgvbapb6a5"),
        });
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
            if (i != 0) {
                s.append(","); //$NON-NLS-1$
            }
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
    public static HttpMethod getHistoryFeedMethod(IFeedIdentifier identifier, Date from, Date to) throws URIException {
        String symbol = getSymbol(identifier);

        Calendar fromDate = Calendar.getInstance();
        fromDate.setTime(from);

        Calendar toDate = Calendar.getInstance();
        toDate.setTime(to);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
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
        try {
            System.out.println(method.getURI().toString());
        } catch (URIException e) {
            e.printStackTrace();
        }

        return method;
    }

    public static HttpMethod get1DayHistoryFeedMethod(IFeedIdentifier identifier) throws URIException {
        String symbol = getSymbol(identifier);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote;range=1d/csv/";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);

        return method;
    }

    public static HttpMethod get5DayHistoryFeedMethod(IFeedIdentifier identifier) throws URIException {
        String symbol = getSymbol(identifier);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote;range=5d/csv/";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);

        return method;
    }

    public static HttpMethod get1YearHistoryFeedMethod(IFeedIdentifier identifier, int year) throws URIException {
        String symbol = getSymbol(identifier);

        String prefix = "/instrument/1.0/";
        String suffix = "/chartdata;type=quote;ys=" + year + ";yz=1/csv/";
        URI uri = new URI("http", "chartapi.finance.yahoo.com", prefix + symbol.toLowerCase() + suffix, "");

        GetMethod method = new GetMethod();
        method.setURI(uri);
        method.setFollowRedirects(true);

        return method;
    }

    public static HttpMethod getDividendsHistoryMethod(IFeedIdentifier identifier, Date from, Date to) {
        String symbol = getSymbol(identifier);

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

    public static URL getRSSNewsFeedForSecurity(ISecurity security) throws MalformedURLException, URIException, NullPointerException {
        IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
        if (identifier == null) {
            return null;
        }

        String symbol = getSymbol(identifier);

        URI feedUrl = new URI("http://finance.yahoo.com/rss/headline?s=" + symbol, false);

        return new URL(feedUrl.toString());
    }

    public static void setupProxy(HttpClient client, String host) throws URISyntaxException {
        if (YahooActivator.getDefault() == null) {
            return;
        }
        BundleContext context = YahooActivator.getDefault().getBundle().getBundleContext();
        ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
        if (reference != null) {
            IProxyService proxyService = (IProxyService) context.getService(reference);
            IProxyData[] proxyData = proxyService.select(new java.net.URI(IProxyData.HTTP_PROXY_TYPE, "//" + host, null));
            if (proxyData != null && proxyData.length != 0) {
                if (proxyData[0].getHost() != null) {
                    client.getHostConfiguration().setProxy(proxyData[0].getHost(), proxyData[0].getPort());
                }
                if (proxyData[0].isRequiresAuthentication()) {
                    client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyData[0].getUserId(), proxyData[0].getPassword()));
                }
            }
            context.ungetService(reference);
        }
    }
}
