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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IFeed;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.jface.preference.IPreferenceStore;

public class Feed implements IFeed, Runnable
{
    private Map map = new HashMap();
    private Thread thread;
    private boolean stopping = false;
    private SimpleDateFormat usDateTimeParser = new SimpleDateFormat("MM/dd/yyyy h:mma");
    private SimpleDateFormat usDateParser = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat usTimeParser = new SimpleDateFormat("h:mma");
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    public Feed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IFeed#subscribe(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribe(Security security)
    {
        String symbol = security.getQuoteFeed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();
        map.put(security, symbol);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IFeed#unSubscribe(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribe(Security security)
    {
        map.remove(security);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IFeed#start()
     */
    public void start()
    {
        if (thread == null)
        {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IFeed#stop()
     */
    public void stop()
    {
        stopping = true;
        if (thread != null)
        {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IFeed#snapshot()
     */
    public void snapshot()
    {
        update();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        long nextRun = System.currentTimeMillis() + 2 * 1000;

        while(!stopping)
        {
            if (System.currentTimeMillis() >= nextRun)
            {
                update();
                nextRun = System.currentTimeMillis() + 10 * 1000;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void update()
    {
        // Builds the url for quotes download
        StringBuffer url = new StringBuffer("http://quote.yahoo.com/download/javasoft.beans?symbols=");
        for (Iterator iter = map.values().iterator(); iter.hasNext(); )
            url = url.append((String)iter.next() + "+");
        if (url.charAt(url.length() - 1) == '+')
            url.deleteCharAt(url.length() - 1);
        url.append("&format=sl1d1t1c1ohgvbap");

        // Read the last prices
        String line = "";
        try
        {
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
            while ((line = in.readLine()) != null)
            {
                String[] item = line.split(",");
                if (line.indexOf(";") != -1)
                    item = line.split(";");
                
                Double open = null, high = null, low = null, close = null;
                Quote quote = new Quote();

                // 2 = Date
                // 3 = Time
                try
                {
                    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("EST"), Locale.US);
                    usDateTimeParser.setTimeZone(c.getTimeZone());
                    usDateParser.setTimeZone(c.getTimeZone());
                    usTimeParser.setTimeZone(c.getTimeZone());

                    String date = stripQuotes(item[2]);
                    if (date.indexOf("N/A") != -1)
                        date = usDateParser.format(Calendar.getInstance().getTime());
                    String time = stripQuotes(item[3]);
                    if (time.indexOf("N/A") != -1)
                        time = usTimeParser.format(Calendar.getInstance().getTime());
                    c.setTime(usDateTimeParser.parse(date + " " + time));
                    c.setTimeZone(TimeZone.getDefault());
                    quote.setDate(c.getTime());
                }
                catch (Exception e) {
                    System.out.println(e.getMessage() + ": " + line);
                }
                // 1 = Last price or N/A
                if (item[1].equalsIgnoreCase("N/A") == false)
                    quote.setLast(numberFormat.parse(item[1]).doubleValue());
                // 4 = Change
                // 5 = Open
                if (item[5].equalsIgnoreCase("N/A") == false)
                    open = new Double(numberFormat.parse(item[5]).doubleValue());
                // 6 = Maximum
                if (item[6].equalsIgnoreCase("N/A") == false)
                    high = new Double(numberFormat.parse(item[6]).doubleValue());
                // 7 = Minimum
                if (item[7].equalsIgnoreCase("N/A") == false)
                    low = new Double(numberFormat.parse(item[7]).doubleValue());
                // 8 = Volume
                if (item[8].equalsIgnoreCase("N/A") == false)
                    quote.setVolume(numberFormat.parse(item[8]).intValue());
                // 9 = Bid Price
                if (item[9].equalsIgnoreCase("N/A") == false)
                    quote.setBid(numberFormat.parse(item[9]).doubleValue());
                // 10 = Ask Price
                if (item[10].equalsIgnoreCase("N/A") == false)
                    quote.setAsk(numberFormat.parse(item[10]).doubleValue());
                // 11 = Close Price
                if (item[11].equalsIgnoreCase("N/A") == false)
                    close = new Double(numberFormat.parse(item[11]).doubleValue());
                
                // 0 = Code
                String symbol = stripQuotes(item[0]);
                for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )
                {
                    Security security = (Security)iter.next();
                    if (symbol.equalsIgnoreCase((String)map.get(security)))
                        security.setQuote(quote, open, high, low, close);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + ": " + line);
            e.printStackTrace();
        }
    }

    private String stripQuotes(String s)
    {
        if (s.startsWith("\""))
            s = s.substring(1);
        if (s.endsWith("\""))
            s = s.substring(0, s.length() - 1);
        return s;
    }
}
