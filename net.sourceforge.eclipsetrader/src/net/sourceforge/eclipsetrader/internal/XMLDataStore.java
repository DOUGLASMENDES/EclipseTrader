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
package net.sourceforge.eclipsetrader.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IDataStore;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Default implementation of the IPortfolioDataStore interface.<br>
 * Provides an XML storage for the portfolio data.<br>
 * 
 * @author Marco Maccaferri - 11/08/2004
 */
public class XMLDataStore implements IDataStore
{
  private static String PORTFOLIO_FILE_NAME = "portfolio.xml";
  private static String STOCKWATCH_FILE_NAME = "stockwatch.xml";
  private static String INDICES_FILE_NAME = "indices.xml";
  private static File HISTORY_CHART_FOLDER = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
  private Document document = null;
  private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
  private Vector data = new Vector();
  private IExtendedData[] dataArray;

  public XMLDataStore()
  {
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(0);
    nf.setMinimumFractionDigits(0);
    nf.setMinimumIntegerDigits(1);
    
    pf.setGroupingUsed(false);
    pf.setMaximumFractionDigits(4);
    pf.setMinimumFractionDigits(4);
    pf.setMinimumIntegerDigits(1);
  }

  /**
   * Returns the portfolio data.<br>
   * 
   * @return The data array.
   */
  public IExtendedData[] getData()
  {
    return dataArray;
  }
  
  /**
   * Read the portfolio data from an XML resource.<br>
   */
  public void load()
  {
    File file = new File(Platform.getLocation().toFile(), STOCKWATCH_FILE_NAME);
    if (file.exists() == true)
    {
      try {
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(file);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("stock"))
            data.add(decodeData(index++, n.getChildNodes()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    dataArray = new IExtendedData[data.size()];
    data.toArray(dataArray);
  }
  
  /**
   * Writes the portfolio data to an XML resource.<br>
   */
  public void store()
  {
    data = new Vector(Arrays.asList(dataArray));
    
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "stockwatch", null);

      for (int i = 0; i < data.size(); i++)
      {
        IExtendedData item = (IExtendedData)data.elementAt(i);

        Element element = document.createElement("stock");
        element.setAttribute("symbol", item.getSymbol());
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("ticker");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getTicker()));
        node = document.createElement("description");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getDescription()));
        node = document.createElement("last_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getLastPrice())));
        node = document.createElement("bid_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getBidPrice())));
        node = document.createElement("bid_size");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(item.getBidSize())));
        node = document.createElement("ask_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getAskPrice())));
        node = document.createElement("ask_size");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(item.getAskSize())));
        node = document.createElement("volume");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(item.getVolume())));
        node = document.createElement("minimum_quantity");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(item.getMinimumQuantity())));
        node = document.createElement("open_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getOpenPrice())));
        node = document.createElement("high_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getHighPrice())));
        node = document.createElement("low_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getLowPrice())));
        node = document.createElement("close_price");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getClosePrice())));
        node = document.createElement("time");
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(item.getDate())));
        node = document.createElement("quantity");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(item.getQuantity())));
        node = document.createElement("paid");
        element.appendChild(node);
        node.appendChild(document.createTextNode(pf.format(item.getPaid())));
      }

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), STOCKWATCH_FILE_NAME)));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) { ex.printStackTrace(); };
  }

  private IExtendedData decodeData(int index, NodeList parent) throws ParseException
  {
    IExtendedData pd = TraderPlugin.createExtendedData();
    
    pd.setSymbol(((Node)parent).getAttributes().getNamedItem("symbol").getNodeValue());

    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("ticker") == true)
          pd.setTicker(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("description") == true)
          pd.setDescription(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("ticker") == true)
          pd.setTicker(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("last_price") == true)
          pd.setLastPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("bid_price") == true)
          pd.setBidPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("bid_size") == true)
          pd.setBidSize(nf.parse(value.getNodeValue()).intValue());
        else if (n.getNodeName().equalsIgnoreCase("ask_price") == true)
          pd.setAskPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("ask_size") == true)
          pd.setAskSize(nf.parse(value.getNodeValue()).intValue());
        else if (n.getNodeName().equalsIgnoreCase("volume") == true)
          pd.setVolume(nf.parse(value.getNodeValue()).intValue());
        else if (n.getNodeName().equalsIgnoreCase("minimum_quantity") == true)
          pd.setMinimumQuantity(nf.parse(value.getNodeValue()).intValue());
        else if (n.getNodeName().equalsIgnoreCase("quantity") == true)
          pd.setQuantity(Integer.parseInt(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("paid") == true)
          pd.setPaid(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("time") == true)
          pd.setDate(df.parse(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("open_price") == true)
          pd.setOpenPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("high_price") == true)
          pd.setHighPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("low_price") == true)
          pd.setLowPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("close_price") == true)
          pd.setClosePrice(pf.parse(value.getNodeValue()).doubleValue());
        else
          System.out.println(n.getNodeName() + "=" + value.getNodeValue());
      }
    }
    
    return pd;
  }

  /**
   * Updates the portfolio data with the given data array, adding, removing
   * or updating data as needed.<br>
   * 
   * @param newData The new data array.
   */
  public void update(IExtendedData[] newData)
  {
    dataArray = newData;

/*    int m;

    // Remove items from the data vector not in the new array
    for (int i = data.size() - 1; i >= 0; i--)
    {
      ExtendedData pd = (ExtendedData)data.elementAt(i);
      for (m = 0; m < newData.length; m++)
      {
        if (pd.symbol.equalsIgnoreCase(newData[m].symbol) == true)
        {
          pd.ticker = newData[m].ticker;
          pd.description = newData[m].description;
          pd.quantity = newData[m].quantity;
          pd.paid = newData[m].paid;
          pd.minimumQuantity = newData[m].minimumQuantity;
          break;
        }
      }
      if (m >= newData.length)
        data.removeElementAt(i);
    }
    
    // Add new items
    for (int i = 0; i < newData.length; i++)
    {
      for (m = 0; m < data.size(); m++)
      {
        ExtendedData pd = (ExtendedData)data.elementAt(m);
        if (pd.symbol.equalsIgnoreCase(newData[i].symbol) == true)
          break;
      }
      if (m >= data.size())
        data.add(newData[i]);
    }

    dataArray = new ExtendedData[data.size()];
    data.toArray(dataArray);*/
    
/*    // Sort the items
    java.util.Collections.sort(data, new Comparator() {
      public int compare(Object o1, Object o2) {
        ExtendedData d1 = (ExtendedData)o1;
        ExtendedData d2 = (ExtendedData)o2;
        return d1.description.compareTo(d2.description);
      }
    });*/
  }

  /**
   * Load the historycal chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @return An array of IChartData items.
   */
  public IChartData[] loadHistoryChart(IBasicData data)
  {
    Vector v = new Vector();
    
    File f = new File(HISTORY_CHART_FOLDER, data.getSymbol().toLowerCase() + ".xml"); //$NON-NLS-1$
    if (f.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node node = firstChild.item(i);
          if (node.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
          {
            IChartData cd = new ChartData();
            NodeList parent = node.getChildNodes();
            for (int ii = 0; ii < parent.getLength(); ii++)
            {
              Node item = parent.item(ii);
              Node value = item.getFirstChild();
              if (value != null)
              {
                if (item.getNodeName().equalsIgnoreCase("open_price") == true) //$NON-NLS-1$
                  cd.setOpenPrice(Double.parseDouble(value.getNodeValue()));
                else if (item.getNodeName().equalsIgnoreCase("max_price") == true) //$NON-NLS-1$
                  cd.setMaxPrice(Double.parseDouble(value.getNodeValue()));
                else if (item.getNodeName().equalsIgnoreCase("min_price") == true) //$NON-NLS-1$
                  cd.setMinPrice(Double.parseDouble(value.getNodeValue()));
                else if (item.getNodeName().equalsIgnoreCase("close_price") == true) //$NON-NLS-1$
                  cd.setClosePrice(Double.parseDouble(value.getNodeValue()));
                else if (item.getNodeName().equalsIgnoreCase("volume") == true) //$NON-NLS-1$
                  cd.setVolume(Integer.parseInt(value.getNodeValue()));
                else if (item.getNodeName().equalsIgnoreCase("date") == true) //$NON-NLS-1$
                {
                  try {
                    cd.setDate(df.parse(value.getNodeValue()));
                  } catch(Exception e) {};
                }
              }
            }
            v.add(cd);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    IChartData[] chartData = new IChartData[v.size()];
    v.toArray(chartData);
    return chartData;
  }

  /**
   * Stores the historical chart data for the given stock item.
   * 
   * @param data - The stock item.
   * @param chartData - The array of IChartData items to store. 
   */
  public void storeHistoryData(IBasicData data, IChartData[] chartData)
  {
    if (chartData != null)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.getDOMImplementation().createDocument("", "chart", null);

        Element element = document.createElement("data");
        document.getDocumentElement().appendChild(element);
        Node node = document.createElement("symbol");
        node.appendChild(document.createTextNode(data.getTicker()));
        element.appendChild(node);

        for (int i = 0; i < chartData.length; i++)
        {
          element = document.createElement("data");
          document.getDocumentElement().appendChild(element);

          node = document.createElement("date");
          node.appendChild(document.createTextNode(df.format(chartData[i].getDate())));
          element.appendChild(node);
          node = document.createElement("open_price");
          node.appendChild(document.createTextNode(pf.format(chartData[i].getOpenPrice())));
          element.appendChild(node);
          node = document.createElement("max_price");
          node.appendChild(document.createTextNode(pf.format(chartData[i].getMaxPrice())));
          element.appendChild(node);
          node = document.createElement("min_price");
          node.appendChild(document.createTextNode(pf.format(chartData[i].getMinPrice())));
          element.appendChild(node);
          node = document.createElement("close_price");
          node.appendChild(document.createTextNode(pf.format(chartData[i].getClosePrice())));
          element.appendChild(node);
          node = document.createElement("volume");
          node.appendChild(document.createTextNode(nf.format(chartData[i].getVolume())));
          element.appendChild(node);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(document);
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(HISTORY_CHART_FOLDER, data.getSymbol().toLowerCase() + ".xml")));
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
        out.flush();
        out.close();
      } catch (Exception ex) { ex.printStackTrace(); };
  }

  public IExtendedData[] loadIndexData()
  {
    Vector v = new Vector();
    
    File f = new File(Platform.getLocation().toFile(), INDICES_FILE_NAME);
    if (f.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node node = firstChild.item(i);
          if (node.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
          {
            IExtendedData ed = TraderPlugin.createExtendedData();
            ed.setSymbol(node.getAttributes().getNamedItem("symbol").getNodeValue());
            ed.setTicker(node.getAttributes().getNamedItem("ticker").getNodeValue());
            ed.setDescription(node.getAttributes().getNamedItem("description").getNodeValue());
            
            NodeList parent = (NodeList)node;
            for (int ii = 0; ii < parent.getLength(); ii++)
            {
              Node item = parent.item(ii);
              Node value = item.getFirstChild();
              if (value != null)
              {
                if (item.getNodeName().equalsIgnoreCase("price") == true) //$NON-NLS-1$
                  ed.setLastPrice(pf.parse(value.getNodeValue()).doubleValue());
                else if (item.getNodeName().equalsIgnoreCase("open_price") == true) //$NON-NLS-1$
                  ed.setOpenPrice(pf.parse(value.getNodeValue()).doubleValue());
                else if (item.getNodeName().equalsIgnoreCase("max_price") == true) //$NON-NLS-1$
                  ed.setHighPrice(pf.parse(value.getNodeValue()).doubleValue());
                else if (item.getNodeName().equalsIgnoreCase("min_price") == true) //$NON-NLS-1$
                  ed.setLowPrice(pf.parse(value.getNodeValue()).doubleValue());
                else if (item.getNodeName().equalsIgnoreCase("close_price") == true) //$NON-NLS-1$
                  ed.setClosePrice(pf.parse(value.getNodeValue()).doubleValue());
                else if (item.getNodeName().equalsIgnoreCase("volume") == true) //$NON-NLS-1$
                  ed.setVolume(nf.parse(value.getNodeValue()).intValue());
                else if (item.getNodeName().equalsIgnoreCase("date") == true) //$NON-NLS-1$
                {
                  try {
                    ed.setDate(df.parse(value.getNodeValue()));
                  } catch(Exception e) {};
                }
              }
            }
            v.add(ed);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    IExtendedData[] data = new IExtendedData[v.size()];
    v.toArray(data);
    return data;
  }

  public void storeIndexData(IExtendedData[] data)
  {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.getDOMImplementation().createDocument("", "index", null);

        for (int i = 0; i < data.length; i++)
        {
          Element element = document.createElement("data");
          element.setAttribute("symbol", data[i].getSymbol());
          element.setAttribute("ticker", data[i].getTicker());
          element.setAttribute("description", data[i].getDescription());
          document.getDocumentElement().appendChild(element);

          Node node = document.createElement("date");
          node.appendChild(document.createTextNode(df.format(data[i].getDate())));
          element.appendChild(node);
          node = document.createElement("price");
          node.appendChild(document.createTextNode(pf.format(data[i].getLastPrice())));
          element.appendChild(node);
          node = document.createElement("open_price");
          node.appendChild(document.createTextNode(pf.format(data[i].getOpenPrice())));
          element.appendChild(node);
          node = document.createElement("max_price");
          node.appendChild(document.createTextNode(pf.format(data[i].getHighPrice())));
          element.appendChild(node);
          node = document.createElement("min_price");
          node.appendChild(document.createTextNode(pf.format(data[i].getLowPrice())));
          element.appendChild(node);
          node = document.createElement("close_price");
          node.appendChild(document.createTextNode(pf.format(data[i].getClosePrice())));
          element.appendChild(node);
          node = document.createElement("volume");
          node.appendChild(document.createTextNode(nf.format(data[i].getVolume())));
          element.appendChild(node);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(document);
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), INDICES_FILE_NAME)));
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
        out.flush();
        out.close();
      } catch (Exception ex) { ex.printStackTrace(); };
  }
}
