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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class StockList
{
  private static String FILE_NAME = "stocklist"; //$NON-NLS-1$
  private static String FILE_EXT = ".xml"; //$NON-NLS-1$
  private IBasicData[] data;
  
  public StockList()
  {
    load();
  }

  private void load()
  {
    Vector v = new Vector();
    InputStream is = null;
    
    // Attempt to read a locale-specific file from the workspace
    File f = new File(Platform.getLocation().toFile(), FILE_NAME + "_" + Locale.getDefault().getCountry().toLowerCase() + FILE_EXT);
    if (f.exists() == true)
    {
      try {
        is = new FileInputStream(f);
      } catch (FileNotFoundException e) {}
    }
    // Attempt to read a locale-specific file from the plugin's install location
    if (is == null)
    {
      try {
        is = ViewsPlugin.getDefault().openStream(new Path(FILE_NAME + "_" + Locale.getDefault().getCountry().toLowerCase() + FILE_EXT));
      } catch (IOException e) {}
    }
    
    // Attempt to read the file from the workspace location
    f = new File(Platform.getLocation().toFile(), FILE_NAME + FILE_EXT);
    if (f.exists() == true)
    {
      try {
        is = new FileInputStream(f);
      } catch (FileNotFoundException e) {}
    }
    // Attempt to read the file from the plugin's install location
    if (is == null)
    {
      try {
        is = ViewsPlugin.getDefault().openStream(new Path(FILE_NAME + FILE_EXT));
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
  
  public IBasicData[] getData()
  {
    return data;
  }
  
  public IBasicData getData(String symbol)
  {
    if (data == null)
      load();
    
    for (int i = 0; i < data.length; i++)
    {
      if (symbol.equals(data[i].getSymbol()) == true)
        return data[i];
    }
    
    return null;
  }
}
