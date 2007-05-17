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

package net.sourceforge.eclipsetrader.borsaitalia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import org.eclipse.jface.preference.IPreferenceStore;

public class HistoryFeed implements IHistoryFeed
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.borsaitalia"; //$NON-NLS-1$
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$
    private Log log = LogFactory.getLog(getClass());

    public HistoryFeed()
    {
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IHistoryFeed#updateHistory(net.sourceforge.eclipsetrader.core.db.Security, int)
     */
    public void updateHistory(Security security, int interval)
    {
        History history = null;
        Calendar from = Calendar.getInstance();
        from.set(Calendar.MILLISECOND, 0);

        if (interval < IHistoryFeed.INTERVAL_DAILY)
        {
            history = security.getIntradayHistory();
            from.set(Calendar.HOUR_OF_DAY, 0);
            from.set(Calendar.MINUTE, 0);
            from.set(Calendar.SECOND, 0);
            log.info("Updating intraday data for " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            history = security.getHistory();
            if (history.size() == 0)
                from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
            else
            {
                Bar cd = history.getLast();
                from.setTime(cd.getDate());
                from.add(Calendar.DATE, 1);
            }
            log.info("Updating historical data for " + security.getCode() + " - " + security.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String symbol = null;
        if (security.getHistoryFeed() != null)
            symbol = security.getHistoryFeed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();

        try {
            StringBuffer url = new StringBuffer("http://grafici.borsaitalia.it/scripts/cligipsw.dll?app=tic_d&action=dwnld4push&cod=&codneb=" + symbol + "&req_type=GRAF_DS&ascii=1&form_id="); //$NON-NLS-1$ //$NON-NLS-2$
            if (interval < IHistoryFeed.INTERVAL_DAILY)
                url.append("&period=1MIN"); //$NON-NLS-1$
            else
            {
                url.append("&period=1DAY"); //$NON-NLS-1$
                url.append("&From=" + df.format(from.getTime())); //$NON-NLS-1$
            }
            log.debug(url);

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            
            IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
            if (store.getBoolean(CorePlugin.PREFS_ENABLE_HTTP_PROXY))
            {
                client.getHostConfiguration().setProxy(store.getString(CorePlugin.PREFS_PROXY_HOST_ADDRESS), store.getInt(CorePlugin.PREFS_PROXY_PORT_ADDRESS));
                if (store.getBoolean(CorePlugin.PREFS_ENABLE_PROXY_AUTHENTICATION))
                    client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(store.getString(CorePlugin.PREFS_PROXY_USER), store.getString(CorePlugin.PREFS_PROXY_PASSWORD)));
            }

            HttpMethod method = new GetMethod(url.toString());
            method.setFollowRedirects(true);
            client.executeMethod(method);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

            // The first line is the header, ignoring
            String inputLine = in.readLine();
            log.trace(inputLine);

            while ((inputLine = in.readLine()) != null)
            {
                log.trace(inputLine);
                if (inputLine.startsWith("@") == true || inputLine.length() == 0) //$NON-NLS-1$
                    continue;
                String[] item = inputLine.split("\\|"); //$NON-NLS-1$

                Bar bar = new Bar();
                bar.setDate(df.parse(item[0]));
                bar.setOpen(Double.parseDouble(item[1]));
                bar.setHigh(Double.parseDouble(item[2]));
                bar.setLow(Double.parseDouble(item[3]));
                bar.setClose(Double.parseDouble(item[4]));
                bar.setVolume((long)Double.parseDouble(item[5]));

                // Remove the old bar, if exists
                int index = history.indexOf(bar.getDate());
                if (index != -1)
                    history.remove(index);
                
                history.add(bar);
            }

            in.close();

        } catch (Exception e) {
            CorePlugin.logException(e);
        }
        
        CorePlugin.getRepository().save(history);
    }
}
