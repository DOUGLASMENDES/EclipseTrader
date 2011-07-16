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

package org.eclipsetrader.borsaitalia.internal.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.borsaitalia.internal.Activator;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.TimeSpan.Units;

public class BackfillConnector implements IBackfillConnector, IExecutableExtension {

    private String id;
    private String name;

    private String host = "grafici.borsaitalia.it"; //$NON-NLS-1$
    private NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

    public BackfillConnector() {
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
        String code = identifier.getSymbol();
        String isin = null;

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            if (properties.getProperty(Activator.PROP_ISIN) != null) {
                isin = properties.getProperty(Activator.PROP_ISIN);
            }
            if (properties.getProperty(Activator.PROP_CODE) != null) {
                code = properties.getProperty(Activator.PROP_CODE);
            }
        }

        if (code == null || isin == null) {
            return false;
        }

        if (timeSpan.getUnits() == Units.Days && timeSpan.getLength() != 1) {
            return false;
        }
        if (timeSpan.getUnits() != Units.Days && timeSpan.getUnits() != Units.Minutes) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IBackfillConnector#backfillHistory(org.eclipsetrader.core.feed.IFeedIdentifier, java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public IOHLC[] backfillHistory(IFeedIdentifier identifier, Date from, Date to, TimeSpan timeSpan) {
        String code = identifier.getSymbol();
        String isin = null;

        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
        if (properties != null) {
            if (properties.getProperty(Activator.PROP_ISIN) != null) {
                isin = properties.getProperty(Activator.PROP_ISIN);
            }
            if (properties.getProperty(Activator.PROP_CODE) != null) {
                code = properties.getProperty(Activator.PROP_CODE);
            }
        }

        if (code == null || isin == null) {
            return null;
        }

        String period = String.valueOf(timeSpan.getLength()) + (timeSpan.getUnits() == Units.Minutes ? "MIN" : "DAY"); //$NON-NLS-1$ //$NON-NLS-2$

        List<OHLC> list = new ArrayList<OHLC>();

        try {
            HttpMethod method = new GetMethod("http://" + host + "/scripts/cligipsw.dll"); //$NON-NLS-1$ //$NON-NLS-2$
            method.setQueryString(new NameValuePair[] {
                    new NameValuePair("app", "tic_d"), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("action", "dwnld4push"), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("cod", code), //$NON-NLS-1$
                    new NameValuePair("codneb", isin), //$NON-NLS-1$
                    new NameValuePair("req_type", "GRAF_DS"), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("ascii", "1"), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("form_id", ""), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("period", period), //$NON-NLS-1$
                    new NameValuePair("From", new SimpleDateFormat("yyyyMMdd000000").format(from)), //$NON-NLS-1$ //$NON-NLS-2$
                    new NameValuePair("To", new SimpleDateFormat("yyyyMMdd000000").format(to)), //$NON-NLS-1$ //$NON-NLS-2$
            });
            method.setFollowRedirects(true);

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            client.executeMethod(method);

            BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

            String inputLine = in.readLine();
            if (inputLine.startsWith("@")) { //$NON-NLS-1$
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("@") || inputLine.length() == 0) { //$NON-NLS-1$
                        continue;
                    }

                    try {
                        String[] item = inputLine.split("\\|"); //$NON-NLS-1$
                        OHLC bar = new OHLC(df.parse(item[0]), nf.parse(item[1]).doubleValue(), nf.parse(item[2]).doubleValue(), nf.parse(item[3]).doubleValue(), nf.parse(item[4]).doubleValue(), nf.parse(item[5]).longValue());
                        list.add(bar);
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error parsing data: " + inputLine, e); //$NON-NLS-1$
                        Activator.getDefault().getLog().log(status);
                    }
                }
            }
            else {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, NLS.bind("Unexpected response from {0}: {1}", new Object[] { //$NON-NLS-1$
                        method.getURI().toString(), inputLine
                }), null);
                Activator.getDefault().getLog().log(status);
            }

            in.close();

        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error reading data", e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }

        return list.toArray(new IOHLC[list.size()]);
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
