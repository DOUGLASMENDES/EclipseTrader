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
package net.sourceforge.eclipsetrader.ui.internal.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StockList
{
  private static String FILE_NAME = "stocklist.xml"; //$NON-NLS-1$
  private IBasicData[] data;
  
  public StockList()
  {
/*    try {
      loadFromSite();
    } catch(Exception e) {};*/
    load();
  }
  
  private void loadFromSite() throws Exception
  {
    int s, e;
    String line, ticker = "", isin = "", description = "";
    Vector v = new Vector();
    
    for (char letter = 'A'; letter <= 'Z'; letter++)
    {
      URL url = new URL("http://www.borsaitalia.it/servlet/HomeController?param_iniziale=" + letter + "&target=SearchQuotes");
      System.out.println(url);
      HttpURLConnection.setFollowRedirects(true);
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setAllowUserInteraction(true);
      con.setRequestMethod("GET");
      con.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      while ((line = in.readLine()) != null) 
      {
        if (line.indexOf("target=PersonalSiteStock") != -1)
        {
          if ((s = line.indexOf("id_int=AF")) != -1)
          {
            s += 9;
            if ((e = line.indexOf("&", s)) > s)
            {
              ticker = line.substring(s, e);
              
              s = line.indexOf("isin=") + 5;
              e = line.indexOf("'", s);
              isin = line.substring(s, e);
            }
          }
        }
        else if (line.indexOf("Controller?target=Display") != -1)
        {
          if ((s = line.indexOf("isin=")) != -1)
          {
            s += 5;
            if ((e = line.indexOf("\"", s)) > s)
            {
              if (line.substring(s, e).equalsIgnoreCase(isin) == true)
              {
                s = line.indexOf(">", s) + 1;
                e = line.indexOf("<", s);
                description = line.substring(s, e);

                IBasicData data = new BasicData();
                data.setSymbol(isin);
                data.setTicker(ticker);
                data.setDescription(description);
                v.addElement(data);
                
                System.out.println(description + "," + ticker + "," + isin);
              }
            }
          }
        }
      }
    }

    // Sort the items
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((IBasicData)o1).getDescription().compareTo(((IBasicData)o2).getDescription());
      }
    });
    
    data = new IBasicData[v.size()];
    v.toArray(data);
    
    store();
  }

  private void load()
  {
    Vector v = new Vector();
    InputStream is = null;
    
    // Attempt to read the map file from the workspace location
    File f = new File(Platform.getLocation().toFile(), FILE_NAME);
    if (f.exists() == true)
    {
      try {
        is = new FileInputStream(f);
      } catch (FileNotFoundException e) {}
    }
    // Attempt to read the default map file from the plugin's install location
    if (is == null)
    {
      try {
        is = ViewsPlugin.getDefault().openStream(new Path(FILE_NAME));
      } catch (IOException e) {}
    }
    
    if (is != null)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node item = firstChild.item(i);
          if (item.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
          {
            NodeList childNodes = item.getChildNodes();
            IBasicData data = new BasicData();

            data.setSymbol(item.getAttributes().getNamedItem("symbol").getNodeValue());
            data.setTicker(item.getAttributes().getNamedItem("ticker").getNodeValue());
            
            for (int x = 0; x < childNodes.getLength(); x++)
            {
              Node node = childNodes.item(x);
              Node value = node.getFirstChild();
              if (value != null)
              {
                if (node.getNodeName().equalsIgnoreCase("description") == true) //$NON-NLS-1$
                  data.setDescription(value.getNodeValue());
                else if (node.getNodeName().equalsIgnoreCase("minimum_quantity") == true) //$NON-NLS-1$
                  data.setMinimumQuantity(Integer.parseInt(value.getNodeValue()));
              }
            }
            v.addElement(data);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    // Sort the items
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((IBasicData)o1).getDescription().compareTo(((IBasicData)o2).getDescription());
      }
    });
    
    data = new IBasicData[v.size()];
    v.toArray(data);
  }

  private void store()
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "stocklist", null);

      for (int i = 0; i < data.length; i++)
      {
        Element element = document.createElement("data");
        element.setAttribute("symbol", data[i].getSymbol());
        element.setAttribute("ticker", data[i].getTicker());
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("description");
        element.appendChild(node);
        node.appendChild(document.createTextNode(data[i].getDescription()));
        node = document.createElement("minimum_quantity");
        element.appendChild(node);
        node.appendChild(document.createTextNode("1"));
      }

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), "stocklist1.xml")));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) { ex.printStackTrace(); };
  }
  
  public IBasicData[] getData()
  {
    return data;
  }
}
