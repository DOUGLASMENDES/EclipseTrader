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

package net.sourceforge.eclipsetrader.archipelago;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ILevel2Feed;
import net.sourceforge.eclipsetrader.core.db.Level2Ask;
import net.sourceforge.eclipsetrader.core.db.Level2Bid;
import net.sourceforge.eclipsetrader.core.db.Security;

public class Level2Feed implements ILevel2Feed, Runnable
{
    private Map map = new HashMap();
    private Thread thread;
    private boolean stopping = false;

    public Level2Feed()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#subscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void subscribeLevel2(Security security)
    {
        String symbol = security.getLevel2Feed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();
        map.put(security, symbol);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#unSubscribeLevel2(net.sourceforge.eclipsetrader.core.db.Security)
     */
    public void unSubscribeLevel2(Security security)
    {
        map.remove(security);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#startLevel2()
     */
    public void startLevel2()
    {
        if (thread == null)
        {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ILevel2Feed#stopLevel2()
     */
    public void stopLevel2()
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
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        Socket socket = null;
        DataOutputStream os = null;
        BufferedReader is = null;

        for (int i = 0; i < 5 && !stopping; i++)
        {
            try
            {
                socket = new Socket("datasvr.tradearca.com", 8092);
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                os = new DataOutputStream(socket.getOutputStream());
                for (Iterator iter = map.values().iterator(); iter.hasNext(); )
                {
                    String symbol = (String)iter.next();
                    os.writeBytes("MsgType=RegisterBook&Symbol=" + symbol + "\n");
                    os.writeBytes("MsgType=RegisterSymbol&Symbol=" + symbol + "\n");
                }
                os.flush();
                break;
            }
            catch (Exception e) {
                CorePlugin.logException(e);
            }
        }
        if (socket == null || os == null || is == null)
            return;

        while (!stopping)
        {
            try
            {
                if (!is.ready())
                {
                    try {
                        Thread.sleep(100);
                    } catch(Exception e) {}
                    continue;
                }
                
                String symbol = "";
                String inputLine = is.readLine();
                System.out.println("Archipelago: " + inputLine);
                if (inputLine.startsWith("MsgType=Book") == true)
                {
                    String[] sections = inputLine.split("&");
                    for (int i = 0; i < sections.length; i++)
                    {
                        if (sections[i].startsWith("Symbol=") == true)
                            symbol = sections[i].substring(7);
                        else if (sections[i].startsWith("BidBook=") == true)
                        {
                            int index = 0, item = 0;
                            String[] elements = sections[i].substring(8).split("#");
                            Level2Bid list = new Level2Bid();
                            while (index < elements.length)
                            {
                                double price = Double.parseDouble(elements[index++]);
                                int quantity = Integer.parseInt(elements[index++]);
                                index++; // Time
                                String id = elements[index++];
                                list.add(price, quantity, id);
                                item++;
                            }
                            for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )
                            {
                                Security security = (Security)iter.next();
                                if (symbol.equalsIgnoreCase((String)map.get(security)))
                                {
                                    security.setLevel2(list);
                                    break;
                                }
                            }
                        }
                        else if (sections[i].startsWith("AskBook=") == true)
                        {
                            int index = 0, item = 0;
                            String[] elements = sections[i].substring(8).split("#");
                            Level2Ask list = new Level2Ask();
                            while (index < elements.length)
                            {
                                double price = Double.parseDouble(elements[index++]);
                                int quantity = Integer.parseInt(elements[index++]);
                                index++; // Time
                                String id = elements[index++];
                                list.add(price, quantity, id);
                                item++;
                            }
                            for (Iterator iter = map.keySet().iterator(); iter.hasNext(); )
                            {
                                Security security = (Security)iter.next();
                                if (symbol.equalsIgnoreCase((String)map.get(security)))
                                {
                                    security.setLevel2(list);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch (SocketException e)
            {
                for (int i = 0; i < 5 && !stopping; i++)
                {
                    try
                    {
                        socket = new Socket("datasvr.tradearca.com", 8092);
                        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        os = new DataOutputStream(socket.getOutputStream());
                        for (Iterator iter = map.values().iterator(); iter.hasNext(); )
                        {
                            String symbol = (String)iter.next();
                            os.writeBytes("MsgType=RegisterBook&Symbol=" + symbol + "\n");
                            os.writeBytes("MsgType=RegisterSymbol&Symbol=" + symbol + "\n");
                        }
                        os.flush();
                        break;
                    }
                    catch (Exception e1) {
                        CorePlugin.logException(e1);
                    }
                }
                if (socket == null || os == null || is == null)
                    return;
            }
            catch (Exception e) {
                CorePlugin.logException(e);
                break;
            }
        }

        try
        {
            if (socket != null)
                socket.close();
            socket = null;
            os = null;
            is = null;
        }
        catch (Exception e) {
            CorePlugin.logException(e);
        }
    }
}
