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

package net.sourceforge.eclipsetrader.yahoo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.jface.preference.IPreferenceStore;

public class HistoryFeed implements IHistoryFeed
{
    protected SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    protected NumberFormat nf = NumberFormat.getInstance(Locale.US);
    protected NumberFormat pf = NumberFormat.getInstance(Locale.US);
    String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    public HistoryFeed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IHistoryFeed#updateHistory(net.sourceforge.eclipsetrader.core.db.Security, int)
     */
    public void updateHistory(Security security, int interval)
    {
        Calendar today = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();

        List history = security.getHistory();
        if (history.size() == 0)
        {
            from.add(Calendar.YEAR, - CorePlugin.getDefault().getPreferenceStore().getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
            to.setTime(from.getTime());
            to.add(Calendar.DATE, 200);
        }

        do {
            if (history.size() != 0)
            {
                Bar cd = (Bar)history.get(history.size() - 1);
                from.setTime(cd.getDate());
                from.add(Calendar.DATE, 1);
                to.setTime(from.getTime());
                to.add(Calendar.DATE, 200);
            }

            StringBuffer url = new StringBuffer("http://table.finance.yahoo.com/table.csv" + "?s=");
            String symbol = null;
            if (security.getHistoryFeed() != null)
                symbol = security.getHistoryFeed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            url.append(symbol);
            url.append("&a=" + from.get(Calendar.MONTH) + "&b=" + from.get(Calendar.DAY_OF_MONTH) + "&c=" + from.get(Calendar.YEAR));
            url.append("&d=" + to.get(Calendar.MONTH) + "&e=" + to.get(Calendar.DAY_OF_MONTH) + "&f=" + to.get(Calendar.YEAR));
            url.append("&g=d&q=q&y=0&z=&x=.csv");

            try {
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
                    if (inputLine.startsWith("<"))
                        continue;
                    String[] item = inputLine.split(",");
                    if (item.length < 6)
                        continue;
                    String[] dateItem = item[0].split("-");
                    if (dateItem.length != 3)
                        continue;

                    int yr = Integer.parseInt(dateItem[2]);
                    if (yr < 30)
                        yr += 2000;
                    else
                        yr += 1900;
                    int mm = 0;
                    for (mm = 0; mm < months.length; mm++)
                    {
                        if (dateItem[1].equalsIgnoreCase(months[mm]) == true)
                            break;
                    }
                    Calendar day = new GregorianCalendar(yr, mm, Integer.parseInt(dateItem[0]));

                    Bar bar = new Bar();
                    bar.setDate(day.getTime());
                    bar.setOpen(Double.parseDouble(item[1].replace(',', '.')));
                    bar.setHigh(Double.parseDouble(item[2].replace(',', '.')));
                    bar.setLow(Double.parseDouble(item[3].replace(',', '.')));
                    bar.setClose(Double.parseDouble(item[4].replace(',', '.')));
                    bar.setVolume(Long.parseLong(item[5]));
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
                e.printStackTrace();
                break;
            }

            if (history.size() == 0)
            {
                from.add(Calendar.DATE, 200);
                to.setTime(from.getTime());
                to.add(Calendar.DATE, 200);
                if (to.after(today) == true)
                {
                    to.setTime(today.getTime());
                    to.add(Calendar.DATE, -1);
                }
                if (from.after(to) == true)
                    break;
            }
        } while (to.before(today));
        
        CorePlugin.getRepository().saveHistory(security.getId(), security.getHistory());
        CorePlugin.getRepository().save(security);
    }
}
