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
package net.sourceforge.eclipsetrader.archipelago;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import net.sourceforge.eclipsetrader.BookData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookDataProvider;
import net.sourceforge.eclipsetrader.IBookUpdateListener;
import net.sourceforge.eclipsetrader.TraderPlugin;

/**
 */
public class BookDataProvider implements Runnable, IBookDataProvider, IPropertyChangeListener
{
  private boolean runThread = false;
  private HashMap bookData = new HashMap();
  private Socket socket = null;
  private DataOutputStream os = null;
  private DataInputStream is = null;
  
  public BookDataProvider()
  {
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#dispose()
   */
  public void dispose()
  {
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    runThread = false;
    bookData.clear();
  }
  
  public void run()
  {

/*    try {
      String line;
      HttpURLConnection con = (HttpURLConnection)new URL("http://www.tradearca.com//tools/book/book_info.asp").openConnection();
      String proxyHost = (String)System.getProperties().get("http.proxyHost");
      String proxyUser = (String)System.getProperties().get("http.proxyUser");
      String proxyPassword = (String)System.getProperties().get("http.proxyPassword");
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
      while ((line = in.readLine()) != null) 
      {
        System.out.println(line);
      }
      in.close();
    } catch(Exception e) {
      e.printStackTrace();
    };

    try {
      String line;
      HttpURLConnection con = (HttpURLConnection)new URL("http://datasvr.tradearca.com/arcadataserver/JArcaBook.php?").openConnection();
      String proxyHost = (String)System.getProperties().get("http.proxyHost");
      String proxyUser = (String)System.getProperties().get("http.proxyUser");
      String proxyPassword = (String)System.getProperties().get("http.proxyPassword");
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
      while ((line = in.readLine()) != null) 
      {
        System.out.println(line);
      }
      in.close();
    } catch(Exception e) {
      e.printStackTrace();
    };*/
    
    // Open the socket connection to the server
    for (int i = 0; i < 5; i++)
    {
      try {
        socket = new Socket("datasvr.tradearca.com", 8092);
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        Iterator iterator = bookData.values().iterator();
        while(iterator.hasNext() == true)
        {
          PriceBook pb = (PriceBook)iterator.next();
          os.writeBytes("MsgType=RegisterBook&Symbol=" + pb.data.getSymbol() + "\n");
          os.writeBytes("MsgType=RegisterSymbol&Symbol=" + pb.data.getSymbol() + "\n");
        }
        os.flush();
        break;
      } catch(Exception e) {
        e.printStackTrace();
      };
    }
    if (socket == null || os == null || is == null)
      return;
    
    runThread = true;
    while (runThread == true) 
    {
      try {
        String inputLine = is.readLine();
        if (inputLine.startsWith("MsgType=Book") == true)
        {
          PriceBook pb = null;
          String[] sections = inputLine.split("&");
          for (int i = 0; i < sections.length; i++)
          {
            if (sections[i].startsWith("Symbol=") == true)
            {
              String symbol = sections[i].substring(7);
              pb = (PriceBook)bookData.get(symbol);
//              System.out.println(sections[i]);
            }
            else if (sections[i].startsWith("BidBook=") == true)
            {
              if (pb != null)
              {
                pb.bidBook = decodeBookData(sections[i].substring(8), pb.bidBook);
                if (pb.bid == null || pb.bid.length != pb.bidBook.size())
                {
                  pb.bid = new IBookData[pb.bidBook.size()];
                  pb.bidBook.toArray(pb.bid);
                }
              }
            }
            else if (sections[i].startsWith("AskBook=") == true)
            {
              if (pb != null)
              {
                pb.askBook = decodeBookData(sections[i].substring(8), pb.askBook);
                if (pb.ask == null || pb.ask.length != pb.askBook.size())
                {
                  pb.ask = new IBookData[pb.askBook.size()];
                  pb.askBook.toArray(pb.ask);
                }
              }
            }
          }
          
          // Notify all listeners that the book was updated
          if (pb != null)
          {
            for (int i = 0; i < pb.listeners.size(); i++)
              ((IBookUpdateListener)pb.listeners.elementAt(i)).bookUpdated(pb.data, pb.bid, pb.ask);
          }
        }
      } catch(SocketException e) {
        for (int i = 0; i < 5; i++)
        {
          try {
            socket = new Socket("datasvr.tradearca.com", 8092);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Iterator iterator = bookData.values().iterator();
            while(iterator.hasNext() == true)
            {
              PriceBook pb = (PriceBook)iterator.next();
              os.writeBytes("MsgType=RegisterBook&Symbol=" + pb.data.getSymbol() + "\n");
            }
            os.flush();
            break;
          } catch(Exception ex) {
            ex.printStackTrace();
          };
        }
        if (socket == null || os == null || is == null)
          return;
      } catch(Exception e) {
        e.printStackTrace();
        break;
      }
    }

    try {
      if (socket != null)
        socket.close();
      socket = null;
      os = null;
      is = null;
    } catch(Exception e) {};
  }
  
  private Vector decodeBookData(String line, Vector data)
  {
    int index = 0, item = 0;
    IBookData bookData;

    String[] elements = line.split("#");
    
    while(index < elements.length)
    {
      if (data.size() == item)
      {
        bookData = new BookData();
        data.addElement(bookData);
      }
      else
        bookData = (IBookData)data.elementAt(item);
      bookData.setPrice(Double.parseDouble(elements[index++]));
      bookData.setQuantity(Integer.parseInt(elements[index++]));
      bookData.setNumber(1);
      index++; // Time
      bookData.setMarketMaker(elements[index++]);
      item++;
    }
    
    return data;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#addBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void addBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
    {
      pb = new PriceBook();
      pb.data = data;
      bookData.put(data.getSymbol(), pb);
    }
    pb.listeners.addElement(listener);
System.out.println(getClass() + ": Add book listener for " + data.getSymbol());
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getAskData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getAskData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
      return pb.ask;
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#getBidData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IBookData[] getBidData(IBasicData data)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb == null)
      return pb.bid;
    return null;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#removeBookListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IBookUpdateListener)
   */
  public void removeBookListener(IBasicData data, IBookUpdateListener listener)
  {
    PriceBook pb = (PriceBook)bookData.get(data.getSymbol());
    if (pb != null)
    {
System.out.println(getClass() + ": Remove book listener for " + data.getSymbol());
      pb.listeners.removeElement(listener);
      if (pb.listeners.size() == 0)
      {
        stopBook(data);
        bookData.remove(data.getSymbol());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#startBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void startBook(IBasicData data)
  {
    if (os != null && TraderPlugin.getDefault().getPreferenceStore().getBoolean("net.sourceforge.eclipsetrader.streaming") == true)
    {
System.out.println(getClass() + ": Start book for " + data.getSymbol());
      try {
        os.writeBytes("MsgType=RegisterBook&Symbol=" + data.getSymbol() + "\n");
        os.flush();
      } catch(Exception e) {};
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBookDataProvider#stopBook(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void stopBook(IBasicData data)
  {
    if (os != null)
    {
System.out.println(getClass() + ": Stop book for " + data.getSymbol());
      try {
        os.writeBytes("MsgType=UnregisterBook&Symbol=" + data.getSymbol() + "\n");
        os.flush();
      } catch(Exception e) {};
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.streaming") == true)
    {
      System.out.println(property + "=" + TraderPlugin.getDefault().getPreferenceStore().getBoolean(property));
      if (TraderPlugin.getDefault().getPreferenceStore().getBoolean(property) == true)
        new Thread(this).start();
      else
        runThread = false;
    }
  }
}
