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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.internal.ChartData;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for collecting realtime data used to build charts.
 * <p></p>
 */
public class DataCollector implements IDataUpdateListener
{
  private int period = 120;
  private Calendar startTime1 = Calendar.getInstance();
  private Calendar startTime2 = Calendar.getInstance();
  private Calendar stopTime1 = Calendar.getInstance();
  private Calendar stopTime2 = Calendar.getInstance();
  private HashMap map = new HashMap();
  private HashMap chartMap = new HashMap();
  private long lastUpdate = 0;
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private HashMap _rtListener = new HashMap();
  private IRealtimeChartProvider realtimeChartProvider;
  
  public DataCollector(IRealtimeChartProvider realtimeChartProvider)
  {
    this.realtimeChartProvider = realtimeChartProvider;

    // Regular time
    startTime1.set(Calendar.HOUR, 9);
    startTime1.set(Calendar.MINUTE, 5);
    startTime1.set(Calendar.SECOND, 0);
    stopTime1.set(Calendar.HOUR, 17);
    stopTime1.set(Calendar.MINUTE, 25);
    stopTime1.set(Calendar.SECOND, 0);

    // After hours
    startTime2.set(Calendar.HOUR, 18);
    startTime2.set(Calendar.MINUTE, 0);
    startTime2.set(Calendar.SECOND, 0);
    stopTime2.set(Calendar.HOUR, 20);
    stopTime2.set(Calendar.MINUTE, 30);
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
  
  public void clear()
  {
    map.clear();
    chartMap.clear();
    fireRealtimeChartUpdate();
  }
  
  public IChartData[] getData(String symbol)
  {
    Vector values = (Vector)chartMap.get(symbol);
    if (values == null)
      values = new Vector();
    IChartData[] data = new IChartData[values.size()];
    values.toArray(data);
    return data;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#addRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void addRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    Vector _v = (Vector)_rtListener.get(data.getSymbol());
    if (_v == null)
    {
      _v = new Vector();
      _rtListener.put(data.getSymbol(), _v);
    }
    if (_v.indexOf(listener) < 0)
      _v.addElement(listener);
    System.out.println("Add RT listener for " + data.getSymbol());
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartProvider#removeRealtimeChartListener(net.sourceforge.eclipsetrader.IBasicData, net.sourceforge.eclipsetrader.IRealtimeChartListener)
   */
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener)
  {
    Vector _v = (Vector)_rtListener.get(data.getSymbol());
    if (_v != null)
    {
      _v.removeElement(listener);
    }
    System.out.println("Remove RT listener for " + data.getSymbol());
  }
  
  private void fireRealtimeChartUpdate()
  {
    Iterator e = _rtListener.values().iterator();
    while(e.hasNext() == true)
    {
      Vector _v = (Vector)e.next();
      for (int i = 0; i < _v.size(); i++)
      {
        IRealtimeChartListener listener = (IRealtimeChartListener)_v.elementAt(i);
        listener.realtimeChartUpdated(realtimeChartProvider);
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IBasicDataProvider, net.sourceforge.eclipsetrader.IBasicData)
   */
  public void dataUpdated(IBasicDataProvider dataProvider, IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IBasicDataProvider)
   */
  public void dataUpdated(IBasicDataProvider dataProvider)
  {
    Calendar current = Calendar.getInstance();
    IExtendedData[] data = TraderPlugin.getData();
    
    if (lastUpdate == 0)
      lastUpdate = System.currentTimeMillis();
    
    for (int i = 0; i < data.length; i++)
    {
      current.setTime(data[i].getDate());
      if (!((current.after(startTime1) && current.before(stopTime1)) || (current.after(startTime2) && current.before(stopTime2))))
        continue;
      
      ChartData chartData = (ChartData)map.get(data[i].getSymbol());
      if (chartData == null)
      {
        chartData = new ChartData();
        chartData.setOpenPrice(data[i].getLastPrice());
        chartData.setMaxPrice(data[i].getLastPrice());
        chartData.setMinPrice(data[i].getLastPrice());
        chartData.setVolume(data[i].getVolume());
        chartData.setDate(data[i].getDate());
        map.put(data[i].getSymbol(), chartData);
      }

      // Updates the collected values
      chartData.setClosePrice(data[i].getLastPrice());
      if (data[i].getLastPrice() < chartData.getMinPrice())
        chartData.setMinPrice(data[i].getLastPrice());
      if (data[i].getLastPrice() > chartData.getMaxPrice())
        chartData.setMaxPrice(data[i].getLastPrice());
    }

    if ((System.currentTimeMillis() - lastUpdate) / 1000 >= period)
    {
      // Set the time of update
      lastUpdate = System.currentTimeMillis();

      for (int i = 0; i < data.length; i++)
      {
        ChartData chartData = (ChartData)map.get(data[i].getSymbol());
        // Adjust the volume so that it represents the exchanged volume in the period
        chartData.setVolume(data[i].getVolume() - chartData.getVolume());
        if (chartData.getVolume() == 0)
          continue;
//        chartData.setDate(Calendar.getInstance().getTime());
        
        // Add the collected data to the values array
        Vector values = (Vector)chartMap.get(data[i].getSymbol());
        if (values == null)
        {
          values = new Vector();
          chartMap.put(data[i].getSymbol(), values);
        }
        // If the day has changed from the last period the old data must be discarded
        if (values.size() != 0)
        {
          Calendar last = Calendar.getInstance();
          last.setTime(((IChartData)values.elementAt(values.size() - 1)).getDate());
          if (current.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR))
            values.clear();
        }
        values.addElement(chartData);
        
        // Remove this period's data from the hashmap
        map.remove(data[i].getSymbol());
      }

      // Notify all listeners of the chart update
      fireRealtimeChartUpdate();
      store();
    }
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
            Vector values = new Vector();
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
              chartData.setDate(df.parse(item.getAttributes().getNamedItem("date").getNodeValue()));
              values.addElement(chartData);
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
  }

  public void store()
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "collector", null); //$NON-NLS-1$ //$NON-NLS-2$

      Iterator key = chartMap.keySet().iterator();
      while(key.hasNext() == true)
      {
        String symbol = (String)key.next();

        Element element = document.createElement("data"); //$NON-NLS-1$
        element.setAttribute("symbol", symbol);
        document.getDocumentElement().appendChild(element);
        
        Iterator values = ((Vector)chartMap.get(symbol)).iterator();
        while(values.hasNext() == true)
        {
          IChartData chartData = (IChartData)values.next();
          Element item = document.createElement("item"); //$NON-NLS-1$
          item.setAttribute("date", df.format(chartData.getDate()));
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
    } catch (Exception ex) {};
  }

}
