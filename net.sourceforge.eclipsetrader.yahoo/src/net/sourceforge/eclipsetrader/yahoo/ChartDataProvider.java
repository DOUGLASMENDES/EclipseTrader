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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.yahoo.internal.SymbolMapper;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.misc.BASE64Encoder;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChartDataProvider implements IChartDataProvider
{
  private HashMap charts = new HashMap();
  private IBasicData data;
  private File folder = new File(Platform.getLocation().toFile(), "charts");
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
  private NumberFormat nf = NumberFormat.getInstance(Locale.US);
  private NumberFormat pf = NumberFormat.getInstance(Locale.US);
  
  public ChartDataProvider()
  {
    nf.setGroupingUsed(false);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(false);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }

  /**
   * Method to load the chart data.<br>
   *
   * @param symbol The symbol of the data to load.
   */
  public void load(IBasicData data)
  {
    Vector chartData = new Vector();
    
    this.data = data;
    chartData.removeAllElements();
    
    File f = new File(folder, data.getSymbol().toLowerCase() + ".xml");
    if (f.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
            chartData.add(decodeData(n.getChildNodes()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    sort(chartData);
    charts.put(data.getSymbol(), chartData);
  }

  /**
   * Order by date.
   */
  private void sort(Vector chartData)
  {
    java.util.Collections.sort(chartData, new Comparator() {
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
  
  private IChartData decodeData(NodeList parent)
  {
    IChartData cd = new ChartData();
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("open_price") == true)
          cd.setOpenPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("max_price") == true)
          cd.setMaxPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("min_price") == true)
          cd.setMinPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("close_price") == true)
          cd.setClosePrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("volume") == true)
          cd.setVolume(Integer.parseInt(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("date") == true)
        {
          try {
            cd.setDate(df.parse(value.getNodeValue()));
          } catch(Exception e) {};
        }
      }
    }
    return cd;
  }

  /**
   * Method to store the chart data.<br>
   */
  public void store(IBasicData data)
  {
    Vector chartData = (Vector)charts.get(data.getSymbol());
    
    if (chartData != null)
      try {
        folder.mkdirs();
        BufferedWriter xmlout = new BufferedWriter(new FileWriter(new File(folder, data.getSymbol().toLowerCase() + ".xml")));

        xmlout.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
        xmlout.write("<chart>\r\n");
        xmlout.write("    <symbol>" + SymbolMapper.getYahooSymbol(data.getTicker()) + "</symbol>\r\n");
        for (int i = 0; i < chartData.size(); i++)
        {
          IChartData cd = (IChartData)chartData.elementAt(i);
          xmlout.write("    <data>\r\n");
          xmlout.write("        <date>" + df.format(cd.getDate()) + "</date>\r\n");
          xmlout.write("        <open_price>" + pf.format(cd.getOpenPrice()) + "</open_price>\r\n");
          xmlout.write("        <max_price>" + pf.format(cd.getMaxPrice()) + "</max_price>\r\n");
          xmlout.write("        <min_price>" + pf.format(cd.getMinPrice()) + "</min_price>\r\n");
          xmlout.write("        <close_price>" + pf.format(cd.getClosePrice()) + "</close_price>\r\n");
          xmlout.write("        <volume>" + cd.getVolume() + "</volume>\r\n");
          xmlout.write("    </data>\r\n");
        }
        xmlout.write("</chart>\r\n");
        xmlout.flush();
        xmlout.close();
      } catch (Exception ex) {};
  }

  /**
   * Method to update the chart data.<br>
   */
  public void update(IBasicData data)
  {
    String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    Vector chartData = (Vector)charts.get(data.getSymbol());
    if (chartData == null)
      chartData = new Vector();
    
    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();
    do {
    // Start reading data from the most recent data available +1
    // If no data is avalable, start from one year back.
    if (chartData.size() != 0)
    {
      IChartData cd = (IChartData)chartData.elementAt(chartData.size() - 1);
      from.setTime(cd.getDate());
      from.add(Calendar.DATE, 1);
      to.setTime(from.getTime());
      to.add(Calendar.DATE, 200);
    }
    else
    {
      from.add(Calendar.YEAR, -1);
      to.setTime(from.getTime());
      to.add(Calendar.DATE, 200);
    }

    try {
      URL url = new URL(YahooPlugin.getDefault().getPreferenceStore().getString("yahoo.charts.url") + "?s=" + SymbolMapper.getYahooSymbol(data.getTicker()) + "&a=" + from.get(GregorianCalendar.MONTH) + "&b=" + from.get(GregorianCalendar.DAY_OF_MONTH) + "&c=" + from.get(GregorianCalendar.YEAR) + "&d=" + to.get(GregorianCalendar.MONTH) + "&e=" + to.get(GregorianCalendar.DAY_OF_MONTH) + "&f=" + to.get(GregorianCalendar.YEAR) + "&g=d&q=q&y=0&z=&x=.csv");
      System.out.println(getClass() + " " + df.format(from.getTime()) + "->" + df.format(to.getTime()) + " " + url);

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
        double adjustRatio = Double.parseDouble(item[4]) / Double.parseDouble(item[6]);
        
        IChartData cd = new ChartData();
        cd.setDate(day.getTime());
        cd.setOpenPrice(Double.parseDouble(item[1]) / adjustRatio);
        cd.setMaxPrice(Double.parseDouble(item[2]) / adjustRatio);
        cd.setMinPrice(Double.parseDouble(item[3]) / adjustRatio);
        cd.setClosePrice(Double.parseDouble(item[6]));
        cd.setVolume((int)(Integer.parseInt(item[5]) * adjustRatio));
        chartData.add(cd);
      }
      in.close();
      sort(chartData);
    } catch(Exception e) { e.printStackTrace(); };
    } while(to.before(Calendar.getInstance()));

    sort(chartData);
    charts.put(data.getSymbol(), chartData);
    store(data);
  }
  
  /**
   * Method to return the chart data array.<br>
   *
   * @return Returns the IChartData array.
   */
  public IChartData[] getData(IBasicData data)
  {
    Vector chartData = (Vector)charts.get(data.getSymbol());
    if (chartData == null)
    {
      load(data);
      chartData = (Vector)charts.get(data.getSymbol());
      if (chartData == null)
        return null;
    }
    
    IChartData[] d = new IChartData[chartData.size()];
    chartData.toArray(d);
    
    return d;
  }
}
