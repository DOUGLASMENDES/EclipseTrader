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
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.PortfolioData;

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
public class XMLPortfolioDataStore
{
  private static String FILE_NAME = "portfolio.xml";
  private Document document = null;
  private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
  private Vector data = new Vector();
  private PortfolioData[] dataArray;

  public XMLPortfolioDataStore()
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
  public PortfolioData[] getData()
  {
    return dataArray;
  }
  
  /**
   * Read the portfolio data from an XML resource.<br>
   */
  public void load()
  {
    File file = new File(Platform.getLocation().toFile(), FILE_NAME);
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

    dataArray = new PortfolioData[data.size()];
    data.toArray(dataArray);
  }
  
  /**
   * Writes the portfolio data to an XML resource.<br>
   */
  public void store()
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "stockwatch", null);

      for (int i = 0; i < data.size(); i++)
      {
        PortfolioData item = (PortfolioData)data.elementAt(i);

        Element element = document.createElement("stock");
        element.setAttribute("symbol", item.getSymbol());
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("ticker");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getTicker()));
        node = document.createElement("description");
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getDescription()));
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
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), FILE_NAME)));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
  }

  private PortfolioData decodeData(int index, NodeList parent) throws ParseException
  {
    PortfolioData pd = new PortfolioData();
    
    pd.setSymbol( ((Node)parent).getAttributes().getNamedItem("symbol").getNodeValue() );

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
        else if (n.getNodeName().equalsIgnoreCase("quantity") == true)
          pd.setQuantity(nf.parse(value.getNodeValue()).intValue());
        else if (n.getNodeName().equalsIgnoreCase("last_price") == true)
          pd.setPaid(pf.parse(value.getNodeValue()).doubleValue());
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
  public void update(PortfolioData[] newData)
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
    
    // Sort the items
    java.util.Collections.sort(data, new Comparator() {
      public int compare(Object o1, Object o2) {
        ExtendedData d1 = (ExtendedData)o1;
        ExtendedData d2 = (ExtendedData)o2;
        return d1.description.compareTo(d2.description);
      }
    });*/
  }
}
