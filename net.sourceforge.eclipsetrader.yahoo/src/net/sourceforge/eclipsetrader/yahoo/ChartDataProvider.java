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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.yahoo.internal.SymbolMapper;
import sun.misc.BASE64Encoder;

/**
 * Yahoo chart data provider.
 * <p></p>
 */
public class ChartDataProvider implements IChartDataProvider
{
  protected SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
  protected NumberFormat nf = NumberFormat.getInstance(Locale.US);
  protected NumberFormat pf = NumberFormat.getInstance(Locale.US);
  protected int symbolField = 1;
  protected boolean useMapping = false;
  protected String defaultExtension = "";

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IChartDataProvider#getData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getData(IBasicData data)
  {
    return update(data, null);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IChartDataProvider#update(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IChartData[])
   */
  public IChartData[] update(IBasicData data, IChartData[] chartData)
  {
    String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    
    IPreferenceStore ps = YahooPlugin.getDefault().getPreferenceStore();
    symbolField = ps.getInt("yahoo.charts.field");
    useMapping = ps.getBoolean("yahoo.charts.mapping");
    defaultExtension = ps.getString("yahoo.suffix");

    Vector v = new Vector();
    if (chartData != null)
    {
      for (int i = 0; i < chartData.length; i++)
        v.add(chartData[i]);
    }
    
    Calendar today = Calendar.getInstance();
    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();

    // If no data is avalable, start from one year back.
    if (v.size() == 0)
    {
      String value = ps.getString("NEW_CHART_YEARS");
      from.add(Calendar.YEAR, -Integer.parseInt(value));
      to.setTime(from.getTime());
      to.add(Calendar.DATE, 200);
    }

    do {
      // Start reading data from the most recent data available +1
      if (v.size() != 0)
      {
        IChartData cd = (IChartData)v.elementAt(v.size() - 1);
        from.setTime(cd.getDate());
        from.add(Calendar.DATE, 1);
        to.setTime(from.getTime());
        to.add(Calendar.DATE, 200);
      }

      String s = ps.getString("yahoo.charts.url") + "?s=";
      String symbol = (symbolField == 0) ? data.getSymbol() : data.getTicker();
      if (useMapping == true)
      {
        symbol = SymbolMapper.getYahooSymbol(symbol);
        if (symbol.indexOf(".") == -1)
          symbol += defaultExtension;
      }
      s += symbol + "&a=" + from.get(GregorianCalendar.MONTH) + "&b=" + from.get(GregorianCalendar.DAY_OF_MONTH) + "&c=" + from.get(GregorianCalendar.YEAR) + "&d=" + to.get(GregorianCalendar.MONTH) + "&e=" + to.get(GregorianCalendar.DAY_OF_MONTH) + "&f=" + to.get(GregorianCalendar.YEAR) + "&g=d&q=q&y=0&z=&x=.csv"; 
      
      try {
        URL url = new URL(s);
        System.out.println(getClass() + " " + df.format(from.getTime()) + "->" + df.format(to.getTime()) + " " + url);
  
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setInstanceFollowRedirects(true);
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
          if (inputLine.startsWith("<") == true)
            continue;
          String[] item = inputLine.split(",");
          String[] dateItem = item[0].split("-");
  
          int yr = Integer.parseInt(dateItem[2]);
          if (yr < 30) yr += 2000;
          else yr += 1900;
          int mm = 0;
          for (mm = 0; mm < months.length; mm++) {
            if (dateItem[1].equalsIgnoreCase(months[mm]) == true)
              break;
          }
          Calendar day = new GregorianCalendar(yr, mm, Integer.parseInt(dateItem[0]));
          double adjustRatio = Double.parseDouble(item[6]) / Double.parseDouble(item[4]);
          
          IChartData cd = new ChartData();
          cd.setDate(day.getTime());
          cd.setOpenPrice(Double.parseDouble(item[1].replace(',', '.')));
          cd.setMaxPrice(Double.parseDouble(item[2].replace(',', '.')));
          cd.setMinPrice(Double.parseDouble(item[3].replace(',', '.')));
          cd.setClosePrice(Double.parseDouble(item[4].replace(',', '.')));
          cd.setVolume((int)(Integer.parseInt(item[5])));
          v.add(cd);
        }
        in.close();
        sort(v);
      } catch(FileNotFoundException e) {
        // Still no data, maybe the symbol was not present yet
        if (v.size() == 0)
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
      } catch(Exception e) {
        e.printStackTrace(); 
      };
    } while(to.before(today));

    sort(v);

    chartData = new IChartData[v.size()];
    v.toArray(chartData);
    
    return chartData;
  }

  /**
   * Order by date.
   */
  private void sort(Vector v)
  {
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        IChartData d1 = (IChartData)o1;
        IChartData d2 = (IChartData)o2;
        if (d1.getDate().after(d2.getDate()) == true)
          return 1;
        else if (d1.getDate().before(d2.getDate()) == true)
          return -1;
        return 0;
      }
    });
  }
}
