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
package net.sourceforge.eclipsetrader.yahoo.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides a mapping table from the official stock exchange symbols
 * (also knowns as tickers) to the Yahoo symbols.
 * <p>The mapping file is first readed from the platform workspace then, if not found,
 * from the plugin's install location. The default file name is yahoo.xml.</p>
 * <p><b>Note:</b><br>
 * The class is specifically designed for the italian stock exchange market.</p>
 * 
 * @author Marco Maccaferri - 14/08/2004
 */
public class SymbolMapper
{
  private static String FILE_NAME = "mapping.xml";
  private static Hashtable map = new Hashtable();

  static {
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
        is = YahooPlugin.getDefault().openStream(new Path(FILE_NAME));
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
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
            map.put(n.getAttributes().getNamedItem("ticker").getNodeValue(), n.getAttributes().getNamedItem("symbol").getNodeValue());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * Return the Yahoo symbol corresponding to the official stock exchange symbol.<br>
   * If there isn't a mapping then returns the input symbol unmodified.<br>
   * 
   * @param s The official stock exchange symbol.
   * @return The Yahoo symbol
   */
  public static String getYahooSymbol(String s)
  {
    String r = (String)map.get(s);
    if (r != null)
      return r;

    return s;
  }
}
