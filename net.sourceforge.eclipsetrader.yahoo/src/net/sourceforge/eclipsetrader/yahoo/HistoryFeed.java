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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import sun.misc.BASE64Encoder;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

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
            from.add(Calendar.YEAR, -2);
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

            String s = "http://table.finance.yahoo.com/table.csv" + "?s=";
            String symbol = null;
            if (security.getHistoryFeed() != null)
                symbol = security.getHistoryFeed().getSymbol();
            if (symbol == null || symbol.length() == 0)
                symbol = security.getCode();
            s += symbol + "&a=" + from.get(Calendar.MONTH) + "&b=" + from.get(Calendar.DAY_OF_MONTH) + "&c=" + from.get(Calendar.YEAR) + "&d=" + to.get(Calendar.MONTH) + "&e=" + to.get(Calendar.DAY_OF_MONTH) + "&f=" + to.get(Calendar.YEAR) + "&g=d&q=q&y=0&z=&x=.csv";

            try {
                URL url = new URL(s);
                System.out.println(getClass() + " " + df.format(from.getTime()) + "->" + df.format(to.getTime()) + " " + url);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
                    if (inputLine.startsWith("<") == true)
                        continue;
                    String[] item = inputLine.split(",");
                    String[] dateItem = item[0].split("-");

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
                    try {
                        bar.setVolume(Integer.parseInt(item[5]));
                    } catch(Exception e) {
                        CorePlugin.logException(e);
                    }
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
            } catch (FileNotFoundException e) {
                // Still no data, maybe the symbol was not present yet
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
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (to.before(today));
        
        CorePlugin.getRepository().saveHistory(security.getId(), security.getHistory());
        CorePlugin.getRepository().save(security);
    }
}
