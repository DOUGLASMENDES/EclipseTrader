/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.island;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import net.sourceforge.eclipsetrader.BookData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookUpdateListener;

/**
 */
public class PriceBook implements Runnable
{
  public IBasicData data;
  public IBookData[] buy = new BookData[0];
  public IBookData[] sell = new BookData[0];
  public Vector listeners = new Vector();
  public boolean runThread = false;
  private InputStream inputStream;
  
  public PriceBook()
  {
  }
  
  public void run()
  {
    buy = null;
    sell = null;

    // Open the socket connection to the server
    try {
      URL url = new URL("http://bvserver.island.com/SERVICE/SQUOTE?STOCK=" + data.getSymbol());
      System.out.println(url);
      inputStream = url.openStream();
    
      runThread = true;
      while (runThread == true) 
      {
        int i = inputStream.read();
        if (i == 0)
        {
          System.out.println("Connection closed");
          break;
        }
        if (i < 0)
        {
          System.out.println("Connection failed");
          break;
        }
        
        switch(i)
        {
          case 72: // 'H'
            System.out.println("H: " + readNum(inputStream, 3));
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
    } catch(Exception e) {
      e.printStackTrace();
    };
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

    if (buy == null || buy.length != j)
      buy = new IBookData[j];
    for(int i1 = 0; i1 < j; i1++)
    {
      int quantity = (int)readNum(inputStream, sharesSize);
      double price = Math.abs(readNum(inputStream, priceSize) - priceBase) / 1000.0;
      if (buy[i1] == null)
        buy[i1] = new BookData();
      buy[i1].setQuantity(quantity);
      buy[i1].setNumber(1);
      buy[i1].setPrice(price);
    }

    if (sell == null || sell.length != j)
      sell = new IBookData[k];
    for(int i1 = 0; i1 < k; i1++)
    {
      int quantity = (int)readNum(inputStream, sharesSize);
      double price = Math.abs(readNum(inputStream, priceSize) + priceBase) / 1000.0;
      if (sell[i1] == null)
        sell[i1] = new BookData();
      sell[i1].setQuantity(quantity);
      sell[i1].setNumber(1);
      sell[i1].setPrice(price);
    }

    // Notify all listeners that the book was updated
    for (int i2 = 0; i2 < listeners.size(); i2++)
      ((IBookUpdateListener)listeners.elementAt(i2)).bookUpdated(data, buy, sell);
  }
  
  private long readNum(InputStream inputStream, int size) throws IOException
  {
    long num = 0L;
    for(int i = 0; i < size; i++)
      num += readByte(inputStream) << i * 8;

    return num;
  }

  private int readByte(InputStream inputStream) throws IOException
  {
    int i = inputStream.read();
    if(i < 0)
      throw new IOException("Self generated IOException on negative read() return");
    else
      return i;
  }
}
