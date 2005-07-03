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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 */
public class StockListDialog extends TitleAreaDialog
{
  private static String FILE_NAME = "stocklist.xml";
  
  public StockListDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    
    Table table = new Table(parent, SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = 300;
    data.widthHint = 200;
    table.setLayoutData(data);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableColumn column = new TableColumn(table, SWT.LEFT, 0);
    column.setText("Codice");
    column.setWidth(80);
    column = new TableColumn(table, SWT.LEFT, 1);
    column.setText("Ticker");
    column.setWidth(48);
    column = new TableColumn(table, SWT.LEFT, 2);
    column.setText("Descrizione");
    column.setWidth(188);
    
    loadStocklist(table);
    
    return super.createDialogArea(parent);
  }

  public int open()
  {
    create();
    
    setTitle("Realtime Server Login");
    setMessage("Please enter your user id and password");
    
    return super.open();
  }
  
  private void loadStocklist(Table table)
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

        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
          {
            NodeList parent = n.getChildNodes();
            IBasicData data = new BasicData();
            for (int x = 0; x < parent.getLength(); x++)
            {
              Node node = parent.item(x);
              Node value = node.getFirstChild();
              if (value != null)
              {
                if (node.getNodeName().equalsIgnoreCase("symbol") == true)
                  data.setSymbol(value.getNodeValue());
                else if (node.getNodeName().equalsIgnoreCase("ticker") == true)
                  data.setTicker(value.getNodeValue());
                else if (node.getNodeName().equalsIgnoreCase("description") == true)
                  data.setDescription(value.getNodeValue());
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

    table.setItemCount(v.size());
    for (int i = 0; i < v.size(); i++)
    {
      TableItem item = table.getItem(i);
      item.setData(v.elementAt(i));
      item.setText(0, ((IBasicData)v.elementAt(i)).getSymbol());
      item.setText(1, ((IBasicData)v.elementAt(i)).getTicker());
      item.setText(2, ((IBasicData)v.elementAt(i)).getDescription());
    }
  }
}
