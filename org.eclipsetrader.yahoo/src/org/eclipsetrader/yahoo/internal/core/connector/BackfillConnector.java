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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.Dividend;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.yahoo.internal.YahooActivator;
import org.eclipsetrader.yahoo.internal.core.Util;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension {

    private String id;
    private String name;

    private SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd"); //$NON-NLS-1$
    private SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
    private SimpleDateFormat dfAlt = new SimpleDateFormat("dd-MMM-yy", Locale.US); //$NON-NLS-1$
    private NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private NumberFormat pf = NumberFormat.getInstance(Locale.US);

    public BackfillConnector() {
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
     * @see org.eclipsetrader.core.feed.IBackfillConnector#canBackfill(org.eclipsetrader.core.feed.IFeedIdentifier, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public boolean canBackfill(IFeedIdentifier identifier, TimeSpan timeSpan) {
        if (timeSpan.getUnits() == Units.Days && timeSpan.getLength() == 1) {
            return true;
        }

        String symbol = Util.getSymbol(identifier);
        if (symbol.startsWith("^")) {
            return false;
        }

        if (timeSpan.getUnits() == Units.Minutes && timeSpan.getLength() == 1) {
            return true;
        }
        if (timeSpan.getUnits() == Units.Minutes && timeSpan.getLength() == 5) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillHistory(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
        try {
            if (TimeSpan.days(1).equals(timeSpan)) {
                return backfillDailyHistory(identifier, from, to);
            }
            else if (TimeSpan.minutes(1).equals(timeSpan)) {
                return backfill1DayHistory(identifier);
            }
            else if (TimeSpan.minutes(5).equals(timeSpan)) {
                return backfill5DayHistory(identifier);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
            YahooActivator.log(status);
        }
        return null;
    }

    private IOHLC[] backfillDailyHistory(IFeedIdentifier identifier, Date from, Date to) {
        List<OHLC> list = new ArrayList<OHLC>();

        Calendar c = Calendar.getInstance();
        c.setTime(from);
        int firstYear = c.get(Calendar.YEAR);
        c.setTime(to);
        int lastYear = c.get(Calendar.YEAR);

        HttpClient client = new HttpClient();
        for (int ys = firstYear; ys <= lastYear; ys++) {
            try {
                HttpMethod method = Util.get1YearHistoryFeedMethod(identifier, ys);
                Util.setupProxy(client, method.getURI().getHost());
                client.executeMethod(method);

                BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                readDailyBackfillStream(list, in);
                in.close();

            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
                YahooActivator.log(status);
            }
        }

        Collections.sort(list, new Comparator<OHLC>() {

            @Override
            public int compare(OHLC o1, OHLC o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        for (Iterator<OHLC> iter = list.iterator(); iter.hasNext();) {
            OHLC ohlc = iter.next();
            if (ohlc.getDate().before(from) || ohlc.getDate().after(to)) {
                iter.remove();
            }
        }

        return list.toArray(new IOHLC[list.size()]);
    }

    private IOHLC[] backfill1DayHistory(IFeedIdentifier identifier) throws Exception {
        List<OHLC> list = new ArrayList<OHLC>();

        HttpMethod method = Util.get1DayHistoryFeedMethod(identifier);
        method.setFollowRedirects(true);

        HttpClient client = new HttpClient();
        Util.setupProxy(client, method.getURI().getHost());
        client.executeMethod(method);

        BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
        read1DayBackfillStream(list, in);
        in.close();

        return list.toArray(new IOHLC[list.size()]);
    }

    private IOHLC[] backfill5DayHistory(IFeedIdentifier identifier) throws Exception {
        List<OHLC> list = new ArrayList<OHLC>();

        HttpMethod method = Util.get5DayHistoryFeedMethod(identifier);
        method.setFollowRedirects(true);

        HttpClient client = new HttpClient();
        Util.setupProxy(client, method.getURI().getHost());
        client.executeMethod(method);

        BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
        read1DayBackfillStream(list, in);
        in.close();

        return list.toArray(new IOHLC[list.size()]);
    }

    void readBackfillStream(List<OHLC> list, BufferedReader in) throws IOException {
        String inputLine = in.readLine();
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.startsWith("<")) {
                continue;
            }

            try {
                OHLC bar = parseResponseLine(inputLine);
                if (bar != null) {
                    list.add(bar);
                }
            } catch (ParseException e) {
                Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
                YahooActivator.log(status);
            }
        }
    }

    void read1DayBackfillStream(List<OHLC> list, BufferedReader in) throws IOException {
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            if (!Character.isDigit(inputLine.charAt(0))) {
                continue;
            }

            try {
                OHLC bar = parse1DayResponseLine(inputLine);
                if (bar != null) {
                    list.add(bar);
                }
            } catch (ParseException e) {
                Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
                YahooActivator.log(status);
            }
        }
    }

    void readDailyBackfillStream(List<OHLC> list, BufferedReader in) throws IOException {
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            if (!Character.isDigit(inputLine.charAt(0))) {
                continue;
            }

            try {
                OHLC bar = parseResponseLine(inputLine);
                if (bar != null) {
                    list.add(bar);
                }
            } catch (ParseException e) {
                Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
                YahooActivator.log(status);
            }
        }
    }

    protected OHLC parseResponseLine(String inputLine) throws ParseException {
        String[] item = inputLine.split(","); //$NON-NLS-1$
        if (item.length < 6) {
            return null;
        }

        Calendar day = Calendar.getInstance();
        try {
            day.setTime(df.parse(item[0]));
        } catch (ParseException e) {
            try {
                day.setTime(df2.parse(item[0]));
            } catch (ParseException e1) {
                try {
                    day.setTime(dfAlt.parse(item[0]));
                } catch (ParseException e2) {
                    throw e2;
                }
            }
        }
        day.set(Calendar.HOUR, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        double close = pf.parse(item[1].replace(',', '.')).doubleValue();
        double high = pf.parse(item[2].replace(',', '.')).doubleValue();
        double low = pf.parse(item[3].replace(',', '.')).doubleValue();
        double open = pf.parse(item[4].replace(',', '.')).doubleValue();
        long volume = nf.parse(item[5]).longValue();

        OHLC bar = new OHLC(day.getTime(), open, high, low, close, volume);

        return bar;
    }

    protected OHLC parse1DayResponseLine(String inputLine) throws ParseException {
        String[] item = inputLine.split(","); //$NON-NLS-1$
        if (item.length < 6) {
            return null;
        }

        Date date = new Date(Long.parseLong(item[0]) * 1000);
        double close = pf.parse(item[1].replace(',', '.')).doubleValue();
        double high = pf.parse(item[2].replace(',', '.')).doubleValue();
        double low = pf.parse(item[3].replace(',', '.')).doubleValue();
        double open = pf.parse(item[4].replace(',', '.')).doubleValue();
        long volume = nf.parse(item[5]).longValue();

        OHLC bar = new OHLC(date, open, high, low, close, volume);

        return bar;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillDividends(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
     */
    @Override
    public IDividend[] backfillDividends(IFeedIdentifier identifier, Date from, Date to) {
        List<IDividend> list = new ArrayList<IDividend>();

        try {
            HttpMethod method = Util.getDividendsHistoryMethod(identifier, from, to);
            method.setFollowRedirects(true);

            HttpClient client = new HttpClient();
            Util.setupProxy(client, method.getURI().getHost());
            client.executeMethod(method);

            BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

            // The first line is the header, ignoring
            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("<")) {
                    continue;
                }

                try {
                    Dividend dividend = parseDividendsResponseLine(inputLine);
                    if (dividend != null) {
                        list.add(dividend);
                    }
                } catch (ParseException e) {
                    Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e);
                    YahooActivator.log(status);
                }
            }

            in.close();

        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, YahooActivator.PLUGIN_ID, 0, "Error reading data", e);
            YahooActivator.log(status);
        }

        return list.toArray(new IDividend[list.size()]);
    }

    protected Dividend parseDividendsResponseLine(String inputLine) throws ParseException {
        String[] item = inputLine.split(","); //$NON-NLS-1$
        if (item.length < 2) {
            return null;
        }

        Calendar day = Calendar.getInstance();
        try {
            day.setTime(df.parse(item[0]));
        } catch (ParseException e) {
            try {
                day.setTime(dfAlt.parse(item[0]));
            } catch (ParseException e1) {
                throw e1;
            }
        }
        day.set(Calendar.HOUR, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        return new Dividend(day.getTime(), pf.parse(item[1].replace(',', '.')).doubleValue());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillSplits(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
     */
    @Override
    public ISplit[] backfillSplits(IFeedIdentifier identifier, Date from, Date to) {
        return null;
    }
}
