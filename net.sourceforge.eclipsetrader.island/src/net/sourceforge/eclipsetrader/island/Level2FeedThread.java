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

package net.sourceforge.eclipsetrader.island;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Level2Ask;
import net.sourceforge.eclipsetrader.core.db.Level2Bid;
import net.sourceforge.eclipsetrader.core.db.Security;

public class Level2FeedThread implements Runnable
{
    private Security security;
    private Thread thread;
    private boolean stopping = false;
    private InputStream inputStream;

    public Level2FeedThread(Security security)
    {
        this.security = security;
    }
    
    public void start()
    {
        if (thread == null)
        {
            stopping = false;
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public void stop() throws InterruptedException
    {
        stopping = true;
        if (thread != null)
        {
            thread.join();
            thread = null;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run()
    {
        String symbol = security.getLevel2Feed().getSymbol();
        if (symbol == null || symbol.length() == 0)
            symbol = security.getCode();

        try
        {
            URL url = new URL("http://bvserver.island.com/SERVICE/SQUOTE?STOCK=" + symbol);
            inputStream = url.openStream();

            while (!stopping)
            {
                if (inputStream.available() == 0)
                {
                    try {
                        Thread.sleep(100);
                    } catch(Exception e) {}
                    continue;
                }
                
                int i = inputStream.read();
                if (i <= 0)
                    break;

                switch (i)
                {
                    case 72: // 'H'
                        readNum(inputStream, 3);
                        break;

                    case 83: // 'S'
                        readNum(inputStream, 3);
                        readStockData();
                        break;

                    case 78: // 'N'
                        break;
                }
            }

            inputStream.close();
        }
        catch (Exception e) {
            CorePlugin.logException(e);
        }
        
        thread = null;
    }

    private void readStockData() throws IOException
    {
        readNum(inputStream, 2); // buyCount = 
        readNum(inputStream, 2); // sellCount = 
        readNum(inputStream, 3); // totalOrderCount = 
        readNum(inputStream, 4); // volume = 
        readNum(inputStream, 3); // lastTradePrice = 
        readNum(inputStream, 3); // lastTradeTime = 

        int i = inputStream.read();
        int j = i >> 4;
        int k = i & 0xf;
        int r = inputStream.read();
        int sharesSize = r >> 4;
        int priceSize = r & 0xf;
        long priceBase = readNum(inputStream, 4);

        Level2Bid bid = new Level2Bid();
        for (int i1 = 0; i1 < j; i1++)
        {
            int quantity = (int) readNum(inputStream, sharesSize);
            double price = Math.abs(readNum(inputStream, priceSize) - priceBase) / 10000.0;
            bid.add(price, quantity);
        }

        Level2Ask ask = new Level2Ask();
        for (int i1 = 0; i1 < k; i1++)
        {
            int quantity = (int) readNum(inputStream, sharesSize);
            double price = Math.abs(readNum(inputStream, priceSize) + priceBase) / 10000.0;
            ask.add(price, quantity);
        }

        security.setLevel2(bid, ask);
    }

    private long readNum(InputStream inputStream, int size) throws IOException
    {
        long num = 0L;
        for (int i = 0; i < size; i++)
            num += readByte(inputStream) << i * 8;

        return num;
    }

    private int readByte(InputStream inputStream) throws IOException
    {
        int i = inputStream.read();
        if (i < 0)
            throw new IOException("Self generated IOException on negative read() return");
        else
            return i;
    }
}
