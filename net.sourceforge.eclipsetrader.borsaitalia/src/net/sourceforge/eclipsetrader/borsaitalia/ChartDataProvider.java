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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import net.sourceforge.eclipsetrader.ChartData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import sun.misc.BASE64Encoder;

/**
 * Yahoo chart data provider.
 * <p></p>
 */
public class ChartDataProvider implements IChartDataProvider
{
  protected SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
  protected NumberFormat nf = NumberFormat.getInstance(Locale.US);
  protected NumberFormat pf = NumberFormat.getInstance(Locale.US);

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
    Vector v = new Vector();
    if (chartData != null)
    {
      for (int i = 0; i < chartData.length; i++)
        v.add(chartData[i]);
    }
    
    Calendar from = Calendar.getInstance();

    // If no data is avalable, start from one year back.
    if (v.size() == 0)
    {
      int value = BorsaitaliaPlugin.getDefault().getPreferenceStore().getInt("NEW_CHART_YEARS");
      from.add(Calendar.YEAR, -value);
    }

      // Start reading data from the most recent data available +1
      if (v.size() != 0)
      {
        IChartData cd = (IChartData)v.elementAt(v.size() - 1);
        from.setTime(cd.getDate());
        from.add(Calendar.DATE, 1);
      }
  
      try {
        URL url = new URL("http://grafici.borsaitalia.it/scripts/cligipsw.dll?app=tic_d&action=dwnld4push&cod=&codneb=" + data.getSymbol() + "&period=1DAY&req_type=GRAF_DS&ascii=1&From=" + df.format(from.getTime()) + "000000&form_id=");
        System.out.println(getClass() + " " + df.format(from.getTime()) + " " + url);
  
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
          cd.setDate(df.parse(item[0].substring(0, 8)));
          cd.setOpenPrice(Double.parseDouble(item[1]));
          cd.setMaxPrice(Double.parseDouble(item[2]));
          cd.setMinPrice(Double.parseDouble(item[3]));
          cd.setClosePrice(Double.parseDouble(item[4]));
          cd.setVolume((int)Double.parseDouble(item[5]));
          v.add(cd);
        }
        in.close();
        sort(v);
      } catch(Exception e) {
        e.printStackTrace(); 
      };

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
