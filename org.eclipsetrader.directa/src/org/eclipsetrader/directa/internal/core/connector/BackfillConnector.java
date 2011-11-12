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

import java.io.BufferedInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Inflater;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.WebConnector;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension {

    private String id;
    private String name;

    private String backfillServer = "213.92.13.41"; //$NON-NLS-1$
    private SimpleDateFormat df;

    private Log log = LogFactory.getLog(getClass());

    public BackfillConnector() {
        df = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
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
     * @see org.eclipsetrader.core.feed.IBackfillConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#getName()
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
        if (timeSpan.getUnits() == Units.Minutes) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillHistory(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
        String symbol = getSymbol(identifier);

        int days = 0;
        Calendar c = Calendar.getInstance();
        c.setTime(from);
        while (c.getTime().before(to)) {
            days++;
            c.add(Calendar.DATE, 1);
        }

        WebConnector connector = WebConnector.getInstance();
        if (!connector.isLoggedIn()) {
            connector.login();
        }

        List<OHLC> list = new ArrayList<OHLC>();

        if (timeSpan.getUnits() == TimeSpan.Units.Days) {
            StringBuilder s = new StringBuilder();
            s.append("/jchart/jwrap.php?"); //$NON-NLS-1$
            s.append("jmodo=dati"); //$NON-NLS-1$
            s.append(String.format("&codalfa=%s", symbol)); //$NON-NLS-1$
            s.append(String.format("&ngiorni=%d", days)); //$NON-NLS-1$
            s.append(String.format("&di=%s", df.format(from))); //$NON-NLS-1$
            s.append(String.format("&df=%s", df.format(to))); //$NON-NLS-1$

            HttpClient client = new HttpClient();
            try {
                GetMethod method = new GetMethod();
                method.setURI(new org.apache.commons.httpclient.URI("http://" + backfillServer + s.toString(), false)); //$NON-NLS-1$
                method.setFollowRedirects(true);
                log.debug(method.getURI().toString());

                client.executeMethod(method);

                BufferedInputStream in = new BufferedInputStream(method.getResponseBodyAsStream());
                parseEndOfDayStream(in, list);
                in.close();
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e); //$NON-NLS-1$
                Activator.log(status);
            }
        }
        else if (timeSpan.getUnits() == TimeSpan.Units.Minutes) {
            int size = timeSpan.getLength() * 60;

            if (days > 365) {
                days = 365;
            }

            StringBuilder s = new StringBuilder();
            s.append("/mpush.php?"); //$NON-NLS-1$
            s.append("modo=g"); //$NON-NLS-1$
            s.append("&u=" + WebConnector.getInstance().getUrt()); //$NON-NLS-1$
            s.append("&p=" + WebConnector.getInstance().getPrt()); //$NON-NLS-1$
            s.append("&cod=A"); //$NON-NLS-1$
            s.append(String.format("&stcmd=%s,,,%d,%d,0", symbol, days, size)); //$NON-NLS-1$

            HttpClient client = new HttpClient();
            try {
                GetMethod method = new GetMethod();
                method.setURI(new org.apache.commons.httpclient.URI("http://" + backfillServer + s.toString(), false)); //$NON-NLS-1$
                method.setFollowRedirects(true);
                log.debug(method.getURI().toString());

                client.executeMethod(method);

                BufferedInputStream in = new BufferedInputStream(method.getResponseBodyAsStream());
                parseIntradayStream(in, list);
                in.close();
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e); //$NON-NLS-1$
                Activator.log(status);
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
        log.info(String.format("%s: Downloaded %d %s-bars from %s to %s", symbol, list.size(), timeSpan, from, to)); //$NON-NLS-1$

        return list.toArray(new IOHLC[list.size()]);
    }

    public String getSymbol(IFeedIdentifier identifier) {
        String symbol = identifier.getSymbol();

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            for (int i = 0; i < WebConnector.PROPERTIES.length; i++) {
                if (properties.getProperty(WebConnector.PROPERTIES[i]) != null) {
                    symbol = properties.getProperty(WebConnector.PROPERTIES[i]);
                    break;
                }
            }
        }

        return symbol;
    }

    protected void parseEndOfDayStream(BufferedInputStream in, List<OHLC> list) throws Exception {
        byte[] buffer = new byte[24];

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        for (;;) {
            int len = 0, readed;
            while ((readed = in.read(buffer, len, buffer.length - len)) != -1) {
                len += readed;
                if (len == 24) {
                    break;
                }
            }
            if (readed == -1) {
                break;
            }

            long dateAsLong = getLong(buffer, 0);
            c.set(Calendar.YEAR, (int) (dateAsLong / 10000));
            c.set(Calendar.MONTH, (int) ((dateAsLong % 10000) / 100) - 1);
            c.set(Calendar.DATE, (int) ((dateAsLong % 10000) % 100));
            Date date = c.getTime();
            float low = getFloat(buffer, 4);
            float high = getFloat(buffer, 8);
            float close = getFloat(buffer, 12);
            float volume = getFloat(buffer, 16);
            float open = getFloat(buffer, 20);
            list.add(new OHLC(date, (double) open, (double) high, (double) low, (double) close, (long) volume));
        }
    }

    protected void parseIntradayStream(BufferedInputStream in, List<OHLC> list) throws Exception {
        Calendar cal = Calendar.getInstance();

        int startTime = 9 * 60;
        int endTime = 17 * 60 + 25;
        ;

        byte[] buffer = new byte[1];
        while (in.read(buffer) == 1) {
            if (buffer[0] == '<') {
                StringBuilder sb = new StringBuilder();
                while (in.read(buffer) == 1) {
                    if (buffer[0] == '>') {
                        break;
                    }
                    sb.append(new String(buffer));
                }
                String line = sb.toString();
                if (line.startsWith("GRA")) { //$NON-NLS-1$
                    int s = line.indexOf("L=") + 2; //$NON-NLS-1$
                    int e = line.indexOf(" ", s); //$NON-NLS-1$
                    int uncompressLen = Integer.parseInt(line.substring(s, e));

                    byte[] output = new byte[uncompressLen];
                    boolean compressed = line.indexOf("LC=") != -1; //$NON-NLS-1$

                    if (compressed) {
                        s = line.indexOf("LC=") + 3; //$NON-NLS-1$
                        e = line.indexOf(" ", s); //$NON-NLS-1$
                        int compressLen = Integer.parseInt(line.substring(s, e));

                        while (in.read(buffer) == 1) {
                            if (buffer[0] == 0x78) {
                                break;
                            }
                        }
                        if (buffer[0] != 0x78) {
                            break;
                        }

                        int readed = 1, len;
                        byte[] input = new byte[compressLen];
                        input[0] = buffer[0];
                        do {
                            len = in.read(input, readed, input.length - readed);
                            readed += len;
                        } while (len > 0 && readed < input.length);

                        Inflater infl = new Inflater();
                        infl.setInput(input, 0, readed);
                        infl.inflate(output);
                        infl.end();
                    }
                    else {
                        in.read(buffer);

                        int readed = 0, len;
                        do {
                            len = in.read(output, readed, output.length - readed);
                            readed += len;
                        } while (len > 0 && readed < output.length);
                    }

                    for (int i = 0; i < output.length; i += 28) {
                        Date date = getDate(output, i);
                        cal.setTime(date);
                        int time = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                        if (time >= startTime && time <= endTime) {
                            float low = getFloat(output, i + 8);
                            float high = getFloat(output, i + 12);
                            float close = getFloat(output, i + 16);
                            float volume = getFloat(output, i + 20);
                            float open = getFloat(output, i + 24);
                            list.add(new OHLC(date, (double) open, (double) high, (double) low, (double) close, (long) volume));
                        }
                    }
                }
            }
        }
    }

    private Date getDate(byte arr[], int start) {
        long dt = getLong64(arr, start);
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 0, 1, 0, 0, 0);
        dt /= 10000L;
        long data = dt >> 20;
        cal.add(5, (int) data);
        long tick = dt & 0xffffeL;
        tick >>= 1;
        double secs = tick / 6D;
        cal.add(13, (int) secs);
        return cal.getTime();
    }

    private long getLong64(byte arr[], int start) {
        int i = 0;
        int len = 8;
        int cnt = 0;
        int tmp[] = new int[len];
        for (i = start; i < start + len; i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }

        long accum = 0L;
        i = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
            accum |= (long) (tmp[i] & 0xff) << shiftBy;
            i++;
        }

        return accum;
    }

    private float getFloat(byte arr[], int start) {
        int i = 0;
        int len = 4;
        int cnt = 0;
        int tmp[] = new int[len];
        for (i = start; i < start + len; i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }

        int accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum = (int) (accum | (long) (tmp[i] & 0xff) << shiftBy);
            i++;
        }

        return Float.intBitsToFloat(accum);
    }

    private long getLong(byte arr[], int start) {
        int i = 0;
        int len = 4;
        int cnt = 0;
        int tmp[] = new int[len];
        for (i = start; i < start + len; i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }

        long accum = 0L;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= (long) (tmp[i] & 0xff) << shiftBy;
            i++;
        }

        return accum;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillDividends(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
     */
    @Override
    public IDividend[] backfillDividends(IFeedIdentifier identifier, Date from, Date to) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillSplits(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date)
     */
    @Override
    public ISplit[] backfillSplits(IFeedIdentifier identifier, Date from, Date to) {
        return null;
    }
}
