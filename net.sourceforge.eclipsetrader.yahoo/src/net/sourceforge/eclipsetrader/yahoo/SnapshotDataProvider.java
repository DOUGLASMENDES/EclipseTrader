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
package net.sourceforge.eclipsetrader.yahoo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.RealtimeChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.yahoo.internal.SymbolMapper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import sun.misc.BASE64Encoder;

/**
 * @author Marco Maccaferri
 */
public class SnapshotDataProvider extends RealtimeChartDataProvider
{
  private Timer timer;
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private SimpleDateFormat df_us = new SimpleDateFormat("MM/dd/yyyy h:mma");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  
  public SnapshotDataProvider()
  {
  }
  
  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
   */
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
  {
  }

  private IExtendedData findData(String ticker)
  {
    ticker = stripQuotes(ticker);
    for (int i = 0; i < data.length; i++) {
      if (ticker.equalsIgnoreCase(SymbolMapper.getYahooSymbol(data[i].getTicker())))
      {
        if (data[i] instanceof IExtendedData)
          return (IExtendedData)data[i];
      }
    }
    return null;
  }
  
  private String stripQuotes(String s)
  {
    if (s.startsWith("\""))
      s = s.substring(1);
    if (s.endsWith("\""))
      s = s.substring(0, s.length() - 1);
    return s;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataStreamer#startStreaming()
   */
  public void startStreaming()
  {
    // Inizializza il timer con un delay iniziale di 2 secondi.
    if (timer == null)
    {
      timer = new Timer();
      timer.schedule(new TimerTask() {
        public void run() {
          update();
        }
      }, 2 * 1000);
    }
    
    super.startStreaming();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataStreamer#stopStreaming()
   */
  public void stopStreaming()
  {
    if (timer != null)
    {
      timer.cancel();
      timer = null;
    }
    
    super.stopStreaming();
  }

  private void update()
  {
    // Compone l'url per la lettura degli ultimi prezzi rimappando i codici usati da Yahoo
    String url = YahooPlugin.getDefault().getPreferenceStore().getString("yahoo.url") + "?s=";
    data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++) {
      if (i > 0)
        url = url + "+";
      url = url + SymbolMapper.getYahooSymbol(data[i].getTicker());
    }
    url = url + "&f=sl1d1t1c1ohgvbap&e=.csv";
    
    System.out.println(Calendar.getInstance().getTimeInMillis() + ": " + url);

    // Read the last price changes
    try {
      String line;
      HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
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
        String[] item = line.split(",");

        // 0 = Code
        String ticker = stripQuotes(item[0]);
        for (int i = 0; i < data.length; i++) 
        {
          if (ticker.equalsIgnoreCase(SymbolMapper.getYahooSymbol(data[i].getTicker())))
          {
            if (!(data[i] instanceof IExtendedData))
              continue;
            IExtendedData pd = data[i];
            try {
              // 1 = Last price or N/A
              if (item[1].equalsIgnoreCase("N/A") == false)
                pd.setLastPrice(Double.parseDouble(item[1]));
              // 2 = Date
              // 3 = Time
              if (item[3].indexOf("am") != -1 || item[3].indexOf("pm") != -1)
                pd.setDate(df_us.parse(stripQuotes(item[2]) + " " + stripQuotes(item[3])));
              else
                pd.setDate(df.parse(stripQuotes(item[2]) + " " + stripQuotes(item[3]) + ":00"));
              pd.setTime(tf.format(pd.getDate()));
              // 4 = Change
//              pd.change = stripQuotes(item[4]);
              // 5 = Open
              if (item[5].equalsIgnoreCase("N/A") == false)
                pd.setOpenPrice(Double.parseDouble(item[5]));
              // 6 = Maximum
              if (item[6].equalsIgnoreCase("N/A") == false)
                pd.setHighPrice(Double.parseDouble(item[6]));
              // 7 = Minimum
              if (item[7].equalsIgnoreCase("N/A") == false)
                pd.setLowPrice(Double.parseDouble(item[7]));
              // 8 = Volume
              if (item[8].equalsIgnoreCase("N/A") == false)
                pd.setVolume(Integer.parseInt(item[8]));
              // 9 = Bid Price
              if (item[9].equalsIgnoreCase("N/A") == false)
                pd.setBidPrice(Double.parseDouble(item[9]));
              // 10 = Ask Price
              if (item[10].equalsIgnoreCase("N/A") == false)
                pd.setAskPrice(Double.parseDouble(item[10]));
              // 11 = Close Price
              if (item[11].equalsIgnoreCase("N/A") == false)
                pd.setClosePrice(Double.parseDouble(item[11]));
              
              // Data not available from Yahoo
              pd.setBidSize(0);
              pd.setAskSize(0);
            } catch(Exception x) {
              System.out.println(x.getMessage());
              System.out.println(line);
            };
          }
        }
      }
      in.close();
    } catch(IOException x) {};
    
    // Signal the update to all listeners.
    fireDataUpdated();

    // Schedule the next update.
    int refresh = YahooPlugin.getDefault().getPreferenceStore().getInt("yahoo.refresh");
    try {
      if (timer != null)
      {
        timer.schedule(new TimerTask() {
          public void run() {
            update();
          }
        }, refresh * 1000);
      }
    } catch(IllegalStateException e) {};
  }
}
