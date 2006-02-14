/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;
import sun.misc.BASE64Encoder;

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
        List history = new ArrayList();
        Calendar from = Calendar.getInstance();

        if (interval < IHistoryFeed.INTERVAL_DAILY)
        {
            history = security.getIntradayHistory();
            history.clear();
            from.set(Calendar.HOUR_OF_DAY, 0);
            from.set(Calendar.MINUTE, 0);
            from.set(Calendar.SECOND, 0);
        }
        else
        {
            history = security.getHistory();
            if (history.size() == 0)
                from.add(Calendar.YEAR, -2);
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
            System.out.println(getClass() + " " + df.format(from.getTime()) + " " + url);

            HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();
            con.setInstanceFollowRedirects(true);
            String proxyHost = (String) System.getProperties().get("http.proxyHost");
            String proxyUser = (String) System.getProperties().get("http.proxyUser");
            String proxyPassword = (String) System.getProperties().get("http.proxyPassword");
            if (proxyHost != null && proxyHost.length() != 0 && proxyUser != null && proxyUser.length() != 0 && proxyPassword != null)
            {
                String login = proxyUser + ":" + proxyPassword;
                String encodedLogin = new BASE64Encoder().encodeBuffer(login.getBytes());
                con.setRequestProperty("Proxy-Authorization", "Basic " + encodedLogin.trim());
            }
            con.setAllowUserInteraction(true);
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null)
            {
                if (inputLine.startsWith("@") == true || inputLine.length() == 0)
                    continue;
                String[] item = inputLine.split("\\|");

                Bar bar = new Bar();
                bar.setDate(df.parse(item[0]));
                bar.setOpen(Double.parseDouble(item[1]));
                bar.setHigh(Double.parseDouble(item[2]));
                bar.setLow(Double.parseDouble(item[3]));
                bar.setClose(Double.parseDouble(item[4]));
                bar.setVolume((int)Double.parseDouble(item[5]));
                history.add(bar);
            }

            in.close();

            java.util.Collections.sort(history, new Comparator() {
                public int compare(Object o1, Object o2)
                {
                    Bar d1 = (Bar) o1;
                    Bar d2 = (Bar) o2;
                    if (d1.getDate().after(d2.getDate()) == true)
                        return 1;
                    else if (d1.getDate().before(d2.getDate()) == true)
                        return -1;
                    return 0;
                }
            });
        } catch (Exception e) {
            CorePlugin.logException(e);
        }
        
        if (interval < IHistoryFeed.INTERVAL_DAILY)
            CorePlugin.getRepository().saveIntradayHistory(security.getId(), security.getIntradayHistory());
        else
            CorePlugin.getRepository().saveHistory(security.getId(), security.getHistory());
        CorePlugin.getRepository().save(security);
    }
}
