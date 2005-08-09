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
package net.sourceforge.eclipsetrader.borsaitalia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.sourceforge.eclipsetrader.ChartData;
import net.sourceforge.eclipsetrader.DataCollector;
import net.sourceforge.eclipsetrader.ExtendedData;
import net.sourceforge.eclipsetrader.IBackfillDataProvider;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.IIndexUpdateListener;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import sun.misc.BASE64Encoder;

/**
 */
public class IndexDataProvider extends ChartDataProvider implements IIndexDataProvider, IRealtimeChartProvider, IBackfillDataProvider, IPropertyChangeListener
{
  protected SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
  protected NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
  protected NumberFormat pf = NumberFormat.getInstance(Locale.ITALY);
  private DataCollector dataCollector = DataCollector.getInstance();
  private Timer timer;
  private String[] symbols = new String[0];
  private IExtendedData[] data = new IExtendedData[0];
  private Vector listeners = new Vector();
  
  public IndexDataProvider()
  {
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }
  
  public void dispose()
  {
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    listeners.clear();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#addUpdateListener(net.sourceforge.eclipsetrader.ui.views.indices.IIndexUpdateListener)
   */
  public void addUpdateListener(IIndexUpdateListener listener)
  {
    if (listeners.contains(listener) == false)
      listeners.add(listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#removeUpdateListener(net.sourceforge.eclipsetrader.ui.views.indices.IIndexUpdateListener)
   */
  public void removeUpdateListener(IIndexUpdateListener listener)
  {
    listeners.remove(listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#setSymbols(java.lang.String[])
   */
  public void setSymbols(String[] symbols)
  {
    this.symbols = symbols;
    data = new IExtendedData[symbols.length];
    for (int i = 0; i < symbols.length; i++)
    {
      data[i] = new ExtendedData();
      setDescription(symbols[i], data[i]);
    }
  }
  
  private void setDescription(String symbol, IExtendedData data)
  {
    data.setSymbol(symbol);
    data.setTicker(symbol);
    data.setDescription(symbol);
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        if(members[i].getAttribute("id").equalsIgnoreCase("borsaitalia.indexProvider") == false)
          continue;

        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true)
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              if (symbol.equalsIgnoreCase(items[iii].getAttribute("symbol")) == true)
              {
                data.setDescription(items[iii].getAttribute("label"));
                data.setTicker(items[iii].getAttribute("ticker"));
                return;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true)
          {
            if (symbol.equalsIgnoreCase(children[ii].getAttribute("symbol")) == true)
            {
              data.setDescription(children[ii].getAttribute("label"));
              data.setTicker(children[ii].getAttribute("ticker"));
              return;
            }
          }
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider#getIndexData()
   */
  public IExtendedData[] getIndexData()
  {
    return data;
  }

  public void update() 
  {
    Calendar now = Calendar.getInstance();
    now.set(Calendar.SECOND, 0);

    // Read the last prices
    try {
      String line;
      HttpURLConnection con = (HttpURLConnection)new URL("http://www.borsaitalia.it/it/mercati/homepage/").openConnection();
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
        if (line.indexOf("target=DisplayIndex2Lev") != -1)
        {
          for (int i = 0; i < symbols.length; i++)
          {
            if (line.indexOf("isin=" + symbols[i]) != -1)
            {
              data[i].setDate(now.getTime());
              if ((line = in.readLine()) == null)
                break;
              int s = line.indexOf("nowrap>") + 7;
              try {
                data[i].setLastPrice(pf.parse(line.substring(s)).doubleValue());
              } catch(Exception e1) {};
              if ((line = in.readLine()) == null)
                break;
              if ((line = in.readLine()) == null)
                break;
              if ((line = in.readLine()) == null)
                break;
              if ((line = in.readLine()) == null)
                break;
              if ((line = in.readLine()) == null)
                break;
              s = line.indexOf("\">") + 2;
              if (line.charAt(s) == '+')
                s++;
              int e = line.indexOf("<", s);
              try {
                double pc = pf.parse(line.substring(s, e)).doubleValue();
                data[i].setClosePrice((data[i].getLastPrice() / (100.0 + pc)) * 100.0);
              } catch(Exception e2) { e2.printStackTrace(); };
              dataCollector.dataUpdated(data[i]);
            }
          }
        }
      }
      in.close();
    } catch(IOException x) {};
    
    // Notify all listeners
    for (int i = 0; i < listeners.size(); i++)
      ((IIndexUpdateListener)listeners.get(i)).indexUpdate(this);

    try {
      if (timer != null)
      {
        timer.schedule(new TimerTask() {
          public void run() {
            update();
          }
        }, 20 * 1000);
      }
    } catch(IllegalStateException e) {};
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#addRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void addRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.addRealtimeChartListener(data, listener);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#backfill(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void backfill(IBasicData data)
  {
    dataCollector.backfill(data);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#getHistoryData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getHistoryData(IBasicData data)
  {
    return dataCollector.getHistoryData(data);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    dataCollector.removeRealtimeChartListener(data, listener);
  }
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#setHistoryData(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setHistoryData(IBasicData data, IChartData[] chartData)
  {
    dataCollector.setHistoryData(data, chartData);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IBackfillDataProvider#getIntradayData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getIntradayData(IBasicData data)
  {
    Vector v = new Vector();
    try {
      URL url = new URL("http://grafici.borsaitalia.it/Chart_Mins_" + data.getSymbol().substring(2) + ".html");
      System.out.println(getClass() + " " + url);

      HttpURLConnection con = (HttpURLConnection)url.openConnection();
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
      String inputLine = in.readLine();
      while ((inputLine = in.readLine()) != null) 
      {
        if (inputLine.startsWith("@") == true || inputLine.length() == 0)
          continue;
        String[] item = inputLine.split("\\|");
        
        IChartData cd = new ChartData();
        cd.setDate(df.parse(item[0]));
        cd.setOpenPrice(Double.parseDouble(item[1]));
        cd.setMaxPrice(Double.parseDouble(item[2]));
        cd.setMinPrice(Double.parseDouble(item[3]));
        cd.setClosePrice(Double.parseDouble(item[4]));
        cd.setVolume((int)Double.parseDouble(item[5]));
        if (cd.getVolume() == 1)
          cd.setVolume(0);
        v.add(cd);
      }
      in.close();
    } catch(Exception e) {
      e.printStackTrace(); 
    };

    IChartData[] chartData = new IChartData[v.size()];
    v.toArray(chartData);
    
    return chartData;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.streaming") == true)
    {
      if (TraderPlugin.isStreaming() == true)
      {
        if (timer == null)
        {
          timer = new Timer();
          timer.schedule(new TimerTask() {
            public void run() {
              update();
            }
          }, 2 * 1000);
        }
      }
      else
      {
        if (timer != null)
        {
          timer.cancel();
          timer = null;
        }
      }
    }
  }
}
