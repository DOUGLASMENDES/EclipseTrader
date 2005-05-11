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
package net.sourceforge.eclipsetrader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.internal.ChartData;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for collecting realtime data used to build charts.
 * <p></p>
 */
public class DataCollector implements IRealtimeChartProvider, IPropertyChangeListener
{
  private static DataCollector instance;
  private int period = 120;
  private boolean session1 = true;
  private Calendar startTime1 = Calendar.getInstance();
  private Calendar startTime2 = Calendar.getInstance();
  private boolean session2 = true;
  private Calendar stopTime1 = Calendar.getInstance();
  private Calendar stopTime2 = Calendar.getInstance();
  private HashMap map = new HashMap();
  private HashMap chartMap = new HashMap();
  private long lastUpdate = 0;
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  private SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private HashMap _rtListener = new HashMap();
  private boolean needSaving = false;
  private Calendar current = Calendar.getInstance();
  private Calendar last = Calendar.getInstance();
  
  private DataCollector()
  {
    load();

    IPreferenceStore pref = TraderPlugin.getDefault().getPreferenceStore();
    period = pref.getInt("net.sourceforge.eclipsetrader.rtchart.period") * 60;

    // Regular time
    session1 = pref.getBoolean("net.sourceforge.eclipsetrader.timing.session1");
    startTime1.set(Calendar.HOUR_OF_DAY, 0);
    startTime1.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.startTime1"));
    startTime1.set(Calendar.SECOND, 0);
    stopTime1.set(Calendar.HOUR_OF_DAY, 0);
    stopTime1.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.stopTime1"));
    stopTime1.set(Calendar.SECOND, 0);

    // After hours
    session2 = pref.getBoolean("net.sourceforge.eclipsetrader.timing.session2");
    startTime2.set(Calendar.HOUR_OF_DAY, 0);
    startTime2.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.startTime2"));
    startTime2.set(Calendar.SECOND, 0);
    stopTime2.set(Calendar.HOUR_OF_DAY, 0);
    stopTime2.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.stopTime2"));
    stopTime2.set(Calendar.SECOND, 0);
    
    nf.setGroupingUsed(false);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(false);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }
  
  public static DataCollector getInstance()
  {
    if (instance == null)
      instance = new DataCollector();
    return instance;
  }
  
  public void clear()
  {
    map.clear();
    chartMap.clear();
    fireRealtimeChartUpdate();
  }
  
  public void setData(String symbol, List values)
  {
    chartMap.put(symbol, values);
    store();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#addRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void addRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    List _v = (ArrayList)_rtListener.get(data.getSymbol());
    if (_v == null)
    {
      _v = new ArrayList();
      _rtListener.put(data.getSymbol(), _v);
    }
    if (_v.indexOf(listener) < 0)
      _v.add(listener);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    List _v = (ArrayList)_rtListener.get(data.getSymbol());
    if (_v != null)
    {
      _v.remove(listener);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#backfill(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void backfill(IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#getHistoryData(net.sourceforge.eclipsetrader.IBasicData)
   */
  public IChartData[] getHistoryData(IBasicData data)
  {
    List values = (ArrayList)chartMap.get(data.getSymbol());
    if (values == null)
      values = new ArrayList();
    IChartData[] chartData = new IChartData[values.size()];
    values.toArray(chartData);
    return chartData;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#setHistoryData(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setHistoryData(IBasicData data, IChartData[] chartData)
  {
    List values = new ArrayList();
    for (int i = 0; i < chartData.length; i++)
      values.add(chartData[i]);
    chartMap.put(data.getSymbol(), values);
    needSaving = true;
    store();
  }

  private void fireRealtimeChartUpdate()
  {
    Iterator e = _rtListener.values().iterator();
    while(e.hasNext() == true)
    {
      List _v = (ArrayList)e.next();
      for (int i = 0; i < _v.size(); i++)
      {
        IRealtimeChartListener listener = (IRealtimeChartListener)_v.get(i);
        listener.realtimeChartUpdated(this);
      }
    }
  }
  
  private void fireRealtimeChartUpdate(IBasicData data)
  {
    List _v = (ArrayList)_rtListener.get(data.getSymbol());
    if (_v != null)
    {
      for (int i = 0; i < _v.size(); i++)
      {
        IRealtimeChartListener listener = (IRealtimeChartListener)_v.get(i);
        listener.realtimeChartUpdated(this);
      }
    }
  }

  public void dataUpdated(IExtendedData data)
  {
    
    if (lastUpdate == 0)
    {
      lastUpdate = System.currentTimeMillis();
      lastUpdate -= (lastUpdate % (period * 1000));
    }

    current.setTime(data.getDate());
    if ( !( session1 == true && current.after(startTime1) && current.before(stopTime1) ) && !( session2 == true && current.after(startTime2) && current.before(stopTime2) ) )
      return;
    
    ChartData chartData = (ChartData)map.get(data.getSymbol());
    if (chartData != null)
    {
      // Check if the time period is completed
      last.setTime(chartData.getDate());
      if ((current.getTimeInMillis() - last.getTimeInMillis()) / 1000 >= period)
      {
        List values = (ArrayList)chartMap.get(data.getSymbol());
        if (values == null)
        {
          values = new ArrayList();
          chartMap.put(data.getSymbol(), values);
        }

        // Adjust the volume so that it represents the exchanged volume in the period
        chartData.setVolume(data.getVolume() - chartData.getVolume());
        if (chartData.getVolume() != 0)
        {
          // Check if the existing data is obsolete
          if (values.size() != 0)
          {
            last.setTime(((ChartData)values.get(values.size() - 1)).getDate());
            if (current.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR))
              values.clear();
          }

          // Adjust the time to a period's boundary
          current.setTimeInMillis(current.getTimeInMillis() - (current.getTimeInMillis() % (period * 1000)));
          chartData.setDate(current.getTime());

          // Add the collected data to the values array
          values.add(chartData);
          needSaving = true;

          // Notify all listeners of the chart update
          fireRealtimeChartUpdate((IBasicData)data);
        }

        // Remove this period's data from the hashmap
        map.remove(data.getSymbol());
        chartData = null;
      }
    }
    if (chartData == null)
    {
      chartData = new ChartData();
      chartData.setOpenPrice(data.getLastPrice());
      chartData.setMaxPrice(data.getLastPrice());
      chartData.setMinPrice(data.getLastPrice());
      chartData.setVolume(data.getVolume());
      current.setTimeInMillis(current.getTimeInMillis() - (current.getTimeInMillis() % (period * 1000)));
      chartData.setDate(current.getTime());
      map.put(data.getSymbol(), chartData);
    }

    // Updates the collected values
    chartData.setClosePrice(data.getLastPrice());
    if (data.getLastPrice() < chartData.getMinPrice())
      chartData.setMinPrice(data.getLastPrice());
    if (data.getLastPrice() > chartData.getMaxPrice())
      chartData.setMaxPrice(data.getLastPrice());
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IBasicDataProvider)
   */
  public void dataUpdated(IBasicDataProvider dataProvider)
  {
    IExtendedData[] data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++)
      dataUpdated(data[i]);
    store();
  }

  public void load()
  {
    // Clear the existing data 
    map.clear();
    chartMap.clear();
    
    File file = new File(Platform.getLocation().toFile(), "chartData.xml"); //$NON-NLS-1$
    if (file.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node node = firstChild.item(i);
          if (node.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
          {
            NodeList element = node.getChildNodes();
            String symbol = node.getAttributes().getNamedItem("symbol").getNodeValue();
            String date = node.getAttributes().getNamedItem("date").getNodeValue();
            List values = new ArrayList();
            chartMap.put(symbol, values);

            for (int ii = 0; ii < element.getLength(); ii++)
            {
              Node item = element.item(ii);
              if (item.getNodeName().equalsIgnoreCase("item") == false) //$NON-NLS-1$
                continue;
              ChartData chartData = new ChartData();
              chartData.setOpenPrice(pf.parse(item.getAttributes().getNamedItem("open").getNodeValue()).doubleValue());
              chartData.setMinPrice(pf.parse(item.getAttributes().getNamedItem("min").getNodeValue()).doubleValue());
              chartData.setMaxPrice(pf.parse(item.getAttributes().getNamedItem("max").getNodeValue()).doubleValue());
              chartData.setClosePrice(pf.parse(item.getAttributes().getNamedItem("close").getNodeValue()).doubleValue());
              chartData.setVolume(nf.parse(item.getAttributes().getNamedItem("volume").getNodeValue()).intValue());
              chartData.setDate(dtf.parse(date + " " + item.getAttributes().getNamedItem("time").getNodeValue()));
              values.add(chartData);
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    needSaving = false;
  }

  public void store()
  {
    if (needSaving == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.getDOMImplementation().createDocument("", "collector", null); //$NON-NLS-1$ //$NON-NLS-2$
  
        Iterator key = chartMap.keySet().iterator();
        while(key.hasNext() == true)
        {
          String symbol = (String)key.next();
          List v = (ArrayList)chartMap.get(symbol);
          if (v == null || v.size() == 0)
            continue;
  
          Element element = document.createElement("data"); //$NON-NLS-1$
          element.setAttribute("symbol", symbol);
          element.setAttribute("date", df.format(((IChartData)v.get(0)).getDate()));
          document.getDocumentElement().appendChild(element);
          
          Iterator values = v.iterator();
          while(values.hasNext() == true)
          {
            IChartData chartData = (IChartData)values.next();

            Element item = document.createElement("item"); //$NON-NLS-1$
            item.setAttribute("time", tf.format(chartData.getDate()));
            item.setAttribute("open", pf.format(chartData.getOpenPrice()));
            item.setAttribute("max", pf.format(chartData.getMaxPrice()));
            item.setAttribute("min", pf.format(chartData.getMinPrice()));
            item.setAttribute("close", pf.format(chartData.getClosePrice()));
            item.setAttribute("volume", nf.format(chartData.getVolume()));
            element.appendChild(item);
          }
        }
  
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(document);
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), "chartData.xml"))); //$NON-NLS-1$
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
        out.flush();
        out.close();
        needSaving = false;
      } catch (Exception ex) {};
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    
    if (property.startsWith("net.sourceforge.eclipsetrader.rtchart.period") == true)
    {
      IPreferenceStore pref = TraderPlugin.getDefault().getPreferenceStore();
      period = pref.getInt("net.sourceforge.eclipsetrader.rtchart.period") * 60;
    }
    else if (property.startsWith("net.sourceforge.eclipsetrader.timing") == true)
    {
      IPreferenceStore pref = TraderPlugin.getDefault().getPreferenceStore();

      session1 = pref.getBoolean("net.sourceforge.eclipsetrader.timing.session1");
      startTime1.set(Calendar.HOUR_OF_DAY, 0);
      startTime1.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.startTime1"));
      startTime1.set(Calendar.SECOND, 0);
      stopTime1.set(Calendar.HOUR_OF_DAY, 0);
      stopTime1.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.stopTime1"));
      stopTime1.set(Calendar.SECOND, 0);

      session2 = pref.getBoolean("net.sourceforge.eclipsetrader.timing.session2");
      startTime2.set(Calendar.HOUR_OF_DAY, 0);
      startTime2.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.startTime2"));
      startTime2.set(Calendar.SECOND, 0);
      stopTime2.set(Calendar.HOUR_OF_DAY, 0);
      stopTime2.set(Calendar.MINUTE, pref.getInt("net.sourceforge.eclipsetrader.timing.stopTime2"));
      stopTime2.set(Calendar.SECOND, 0);
    }
  }
}
