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

import net.sourceforge.eclipsetrader.ExtendedData;
import net.sourceforge.eclipsetrader.IDataStore;
import net.sourceforge.eclipsetrader.IExtendedData;

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
            data.add(decodeData(index++, (NodeList)n));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    dataArray = new ExtendedData[data.size()];
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
        ExtendedData item = (ExtendedData)data.elementAt(i);

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

  private ExtendedData decodeData(int index, NodeList parent) throws ParseException
  {
    ExtendedData pd = new ExtendedData();
    
    pd.symbol = ((Node)parent).getAttributes().getNamedItem("symbol").getNodeValue();

    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("ticker") == true)
          pd.ticker = value.getNodeValue();
        else if (n.getNodeName().equalsIgnoreCase("description") == true)
          pd.description = value.getNodeValue();
        else if (n.getNodeName().equalsIgnoreCase("ticker") == true)
          pd.ticker = value.getNodeValue();
        else if (n.getNodeName().equalsIgnoreCase("last_price") == true)
          pd.setLastPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("bid_price") == true)
          pd.setBidPrice(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("bid_size") == true)
          pd.bidSize = nf.parse(value.getNodeValue()).intValue();
        else if (n.getNodeName().equalsIgnoreCase("ask_price") == true)
          pd.askPrice = pf.parse(value.getNodeValue()).doubleValue();
        else if (n.getNodeName().equalsIgnoreCase("ask_size") == true)
          pd.askSize = nf.parse(value.getNodeValue()).intValue();
        else if (n.getNodeName().equalsIgnoreCase("volume") == true)
          pd.volume = nf.parse(value.getNodeValue()).intValue();
        else if (n.getNodeName().equalsIgnoreCase("minimum_quantity") == true)
          pd.minimumQuantity = nf.parse(value.getNodeValue()).intValue();
        else if (n.getNodeName().equalsIgnoreCase("quantity") == true)
          pd.quantity = Integer.parseInt(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("paid") == true)
          pd.setPaid(pf.parse(value.getNodeValue()).doubleValue());
        else if (n.getNodeName().equalsIgnoreCase("time") == true)
        {
          pd.time = value.getNodeValue();
          pd.setDate(df.parse(value.getNodeValue()));
        }
        else if (n.getNodeName().equalsIgnoreCase("open_price") == true)
          pd.openPrice = pf.parse(value.getNodeValue()).doubleValue();
        else if (n.getNodeName().equalsIgnoreCase("high_price") == true)
          pd.highPrice = pf.parse(value.getNodeValue()).doubleValue();
        else if (n.getNodeName().equalsIgnoreCase("low_price") == true)
          pd.lowPrice = pf.parse(value.getNodeValue()).doubleValue();
        else if (n.getNodeName().equalsIgnoreCase("close_price") == true)
          pd.closePrice = pf.parse(value.getNodeValue()).doubleValue();
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
}
