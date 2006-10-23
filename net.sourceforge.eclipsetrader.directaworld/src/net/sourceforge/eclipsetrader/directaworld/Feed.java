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

package net.sourceforge.eclipsetrader.directaworld;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private String userName = ""; //$NON-NLS-1$
    private String password = ""; //$NON-NLS-1$
    private NumberFormat nf = NumberFormat.getInstance();
    private NumberFormat pf = NumberFormat.getInstance();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

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
            userName = DirectaWorldPlugin.getDefault().getPreferenceStore().getString(DirectaWorldPlugin.USERNAME_PREFS);
            password = DirectaWorldPlugin.getDefault().getPreferenceStore().getString(DirectaWorldPlugin.PASSWORD_PREFS);

            if (userName.length() == 0 || password.length() == 0)
            {
                LoginDialog dlg = new LoginDialog(userName, password);
                if (dlg.open() != LoginDialog.OK)
                    return;
                
                userName = dlg.getUserName();
                password = dlg.getPassword();
            }
            
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
        userName = DirectaWorldPlugin.getDefault().getPreferenceStore().getString(DirectaWorldPlugin.USERNAME_PREFS);
        password = DirectaWorldPlugin.getDefault().getPreferenceStore().getString(DirectaWorldPlugin.PASSWORD_PREFS);

        if (userName.length() == 0 || password.length() == 0)
        {
            LoginDialog dlg = new LoginDialog(userName, password);
            if (dlg.open() != LoginDialog.OK)
                return;
            
            userName = dlg.getUserName();
            password = dlg.getPassword();
        }

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
                int requiredDelay = update();
                if (requiredDelay > 0)
                    nextRun = System.currentTimeMillis() + requiredDelay * 1000;
                else
                    nextRun = System.currentTimeMillis() + 16 * 1000;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        
        thread = null;
    }

    private int update()
    {
        int i, requiredDelay = -1;
        String inputLine;

        nf.setGroupingUsed(true);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        pf.setGroupingUsed(true);
        pf.setMinimumFractionDigits(4);
        pf.setMaximumFractionDigits(4);

        try
        {
            // Legge la pagina contenente gli ultimi prezzi
            StringBuffer url = new StringBuffer("http://registrazioni.directaworld.it/cgi-bin/qta?idx=alfa&modo=t&appear=n"); //$NON-NLS-1$
            i = 0;
            for (Iterator iter = map.values().iterator(); iter.hasNext(); )
                url.append("&id" + (++i) + "=" + (String)iter.next()); //$NON-NLS-1$ //$NON-NLS-2$
            for (; i < 30; i++)
                url.append("&id" + (i + 1) + "="); //$NON-NLS-1$ //$NON-NLS-2$
            url.append("&u=" + userName + "&p=" + password); //$NON-NLS-1$ //$NON-NLS-2$

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
            while ((inputLine = in.readLine()) != null)
            {
                if (inputLine.indexOf("<!--QT START HERE-->") != -1) //$NON-NLS-1$
                {
                    while ((inputLine = in.readLine()) != null)
                    {
                        if (inputLine.indexOf("<!--QT STOP HERE-->") != -1) //$NON-NLS-1$
                            break;
                        parseLine(inputLine);
                    }
                }
                else if (inputLine.indexOf("Sara' possibile ricaricare la pagina tra") != -1) //$NON-NLS-1$
                {
                    int beginIndex = inputLine.indexOf("tra ") + 4; //$NON-NLS-1$
                    int endIndex = inputLine.indexOf("sec") - 1; //$NON-NLS-1$
                    try {
                        requiredDelay = Integer.parseInt(inputLine.substring(beginIndex, endIndex)) + 1;
                    } catch (Exception e) {
                        CorePlugin.logException(e);
                    }
                }
            }
            in.close();
        }
        catch (Exception e) {
            CorePlugin.logException(e);
        }
        
        return requiredDelay;
    }

    public void parseLine(String line) throws ParseException
    {
        String[] item = line.split(";"); //$NON-NLS-1$

        for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )
        {
            Security security = (Security)iter.next();
            if (item[0].equalsIgnoreCase((String)map.get(security)))
            {
                Double open = null, high = null, low = null, close = null;
                Quote quote = new Quote();
                
                // item[1] - Nome
                quote.setLast(pf.parse(item[2]).doubleValue());
                // item[3] - Variazione
                quote.setVolume(nf.parse(item[4]).intValue());
                try {
                    if (item[5].length() == 7)
                        item[5] = item[5].charAt(0) + ":" + item[5].charAt(1) + item[5].charAt(3) + ":" + item[5].charAt(4) + item[5].charAt(6); //$NON-NLS-1$ //$NON-NLS-2$
                    quote.setDate(df.parse(item[6] + " " + item[5])); //$NON-NLS-1$
                }
                catch (Exception e) {
                }

                quote.setBid(pf.parse(item[7]).doubleValue());
                quote.setBidSize(nf.parse(item[8]).intValue());
                quote.setAsk(pf.parse(item[9]).doubleValue());
                quote.setAskSize(nf.parse(item[10]).intValue());
                // item[11] - ???
                open = new Double(pf.parse(item[12]).doubleValue());
                close = new Double(pf.parse(item[13]).doubleValue());
                low = new Double(pf.parse(item[14]).doubleValue());
                high = new Double(pf.parse(item[15]).doubleValue());

                security.setQuote(quote, open, high, low, close);
                break;
            }
        }
    }
}
