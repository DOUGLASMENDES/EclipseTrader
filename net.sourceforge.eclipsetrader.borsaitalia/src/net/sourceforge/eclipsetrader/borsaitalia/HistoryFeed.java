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
import java.util.Date;

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
import org.eclipse.jface.preference.IPreferenceStore;

public class HistoryFeed implements IHistoryFeed
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.borsaitalia";
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

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
        }
        else
        {
            history = security.getHistory();
            if (history.size() == 0)
                from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
            else
            {
                Bar cd = (Bar)history.get(history.size() - 1);
                from.setTime(cd.getDate());
                from.add(Calendar.DATE, 1);
            }
        }

        String symbol = null;
        if (security.getHistoryFeed() != null)
            symbol = security.getHistoryFeed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();

        try {
            StringBuffer url = new StringBuffer("http://grafici.borsaitalia.it/scripts/cligipsw.dll?app=tic_d&action=dwnld4push&cod=&codneb=" + symbol + "&req_type=GRAF_DS&ascii=1&form_id=");
            if (interval < IHistoryFeed.INTERVAL_DAILY)
                url.append("&period=1MIN");
            else
            {
                url.append("&period=1DAY");
                url.append("&From=" + df.format(from.getTime()));
            }

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

            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null)
            {
                if (inputLine.startsWith("@") == true || inputLine.length() == 0)
                    continue;
                String[] item = inputLine.split("\\|");

                Date date = df.parse(item[0]);
                Bar bar = history.get(date);
                if (bar == null)
                {
                    bar = new Bar();
                    history.add(bar);
                }
                bar.setDate(date);
                bar.setOpen(Double.parseDouble(item[1]));
                bar.setHigh(Double.parseDouble(item[2]));
                bar.setLow(Double.parseDouble(item[3]));
                bar.setClose(Double.parseDouble(item[4]));
                bar.setVolume((long)Double.parseDouble(item[5]));
            }

            in.close();

        } catch (Exception e) {
            CorePlugin.logException(e);
        }
        
        history.sort();
        CorePlugin.getRepository().save(history);
    }
}
