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
package net.sourceforge.eclipsetrader.ui.views.news;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.IDataUpdateListener;
import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.INewsProvider;
import net.sourceforge.eclipsetrader.NewsData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NewsView extends ViewPart implements IDataUpdateListener, ControlListener, IPropertyChangeListener 
{
  private Table table;
  private Color background = new Color(null, 255, 255, 224);
  private Color foreground = new Color(null, 0, 0, 0);
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm"); //$NON-NLS-1$
  private Timer timer;
  private INewsData[] data;
  private int columnWidth[] = { 105, 435, 145 };

  public NewsView()
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.addPropertyChangeListener(this);

    foreground = new Color(null, PreferenceConverter.getColor(pref, "news.color")); //$NON-NLS-1$
    background = new Color(null, PreferenceConverter.getColor(pref, "news.background")); //$NON-NLS-1$

    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    table.setLayoutData(gd);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setBackground(background);

    // Columns width
    String[] w = pref.getString("news.columnWidth").split(",");
    for (int i = 0; i < w.length && i < columnWidth.length; i++)
      columnWidth[i] = Integer.parseInt(w[i]);

    TableColumn column = new TableColumn(table, SWT.RIGHT, 0);
    column.setText(Messages.getString("NewsView.1")); //$NON-NLS-1$
    column.setWidth(columnWidth[0]);
    column.addControlListener(this);
    column = new TableColumn(table, SWT.LEFT, 1);
    column.setText(Messages.getString("NewsView.2")); //$NON-NLS-1$
    column.setWidth(columnWidth[1]);
    column.addControlListener(this);
    column = new TableColumn(table, SWT.LEFT, 2);
    column.setText(Messages.getString("NewsView.3")); //$NON-NLS-1$
    column.setWidth(columnWidth[2]);
    column.addControlListener(this);

    table.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        INewsData item = data[table.getSelectionIndex()];
        try {
          IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser"); //$NON-NLS-1$
          if (browser != null)
          {
            browser.setFocus();
            ((NewsBrowser)browser).setUrl(item.getUrl());
          }
        } catch(PartInitException x) {};
      }
      public void mouseDown(MouseEvent e) {
      }
      public void mouseUp(MouseEvent e) {
      }
    });

    load();
    update();
  }
  
  public void startUpdate()
  {
    Thread t = new Thread(new Runnable() {
      public void run() {
        updateList();
      }
    });
    t.start();
  }
  
  public void next()
  {
    int index = table.getSelectionIndex() + 1;
    if (index >= table.getItemCount())
      index = 0;
    table.setSelection(index);

    INewsData item = data[table.getSelectionIndex()];
    try {
      IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser"); //$NON-NLS-1$
      if (browser != null)
        ((NewsBrowser)browser).setUrl(item.getUrl());
    } catch(PartInitException x) {};
  }
  
  public void previous()
  {
    int index = table.getSelectionIndex();
    if (index == -1 || index == 0)
      index = table.getItemCount() - 1;
    else
      index--;
    table.setSelection(index);

    INewsData item = data[table.getSelectionIndex()];
    try {
      IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser"); //$NON-NLS-1$
      if (browser != null)
        ((NewsBrowser)browser).setUrl(item.getUrl());
    } catch(PartInitException x) {};
  }
  
  private void updateList()
  {
    Job job = new Job(Messages.getString("NewsView.4")) { //$NON-NLS-1$
      public IStatus run(IProgressMonitor monitor)
      {
        INewsProvider provider = (INewsProvider)TraderPlugin.getNewsProvider();
        if (provider != null)
        {
          provider.update(monitor);
          data = provider.getData();
          store();
          table.getDisplay().asyncExec(new Runnable() {
            public void run() {
              update();
            }
          });
        }
        return new Status(0, "plugin.id", 0, "OK", null);  //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    job.setUser(true);
    job.schedule();
  }
  
  /**
   * Updates the table contents
   */
  public void update()
  {
    if (data != null)
    {
      table.setRedraw(false);
      table.setItemCount(data.length);
  
      for (int row = 0; row < data.length; row++)
      {
        TableItem item = table.getItem(row);
        item.setForeground(foreground);
        item.setText(0, df.format(data[row].getDate()));
        item.setText(1, data[row].getTitle());
        item.setText(2, data[row].getSource());
      }
  
      table.setRedraw(true);
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    // Save the columns width
    String value = "";
    for (int i = 0; i < columnWidth.length; i++)
      value += String.valueOf(columnWidth[i]) + ",";
    
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.removePropertyChangeListener(this);
    pref.setValue("news.columnWidth", value);

    super.dispose();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IDataStreamer)
   */
  public void dataUpdated(IBasicDataProvider ds)
  {
    table.getDisplay().asyncExec(new Runnable() {
      public void run() {
        table.setRedraw(false);
        update();
        table.setRedraw(true);
      }
    });
  }
  public void dataUpdated(IBasicDataProvider dataProvider, IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    table.setFocus();
  }

  public void load()
  {
    Vector _data = new Vector();
    
    File file = new File(Platform.getLocation().toFile(), "news.xml"); //$NON-NLS-1$
    if (file.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
            _data.add(decodeData(n.getChildNodes()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    data = new INewsData[_data.size()];
    _data.toArray(data);
  }
  
  private INewsData decodeData(NodeList parent)
  {
    INewsData item = new NewsData();
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("title") == true) //$NON-NLS-1$
          item.setTitle(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("source") == true) //$NON-NLS-1$
          item.setSource(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("url") == true) //$NON-NLS-1$
          item.setUrl(value.getNodeValue());
        else if (n.getNodeName().equalsIgnoreCase("date") == true) //$NON-NLS-1$
        {
          try {
            item.setDate(df.parse(value.getNodeValue()));
          } catch(Exception e) {};
        }
      }
    }
    return item;
  }

  /**
   * Method to store the chart data.<br>
   */
  public void store()
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "news", null); //$NON-NLS-1$ //$NON-NLS-2$

      for (int i = 0; i < data.length; i++)
      {
        Element element = document.createElement("data"); //$NON-NLS-1$
        document.getDocumentElement().appendChild(element);

        INewsData item = data[i];
        Node node = document.createElement("date"); //$NON-NLS-1$
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(item.getDate())));
        node = document.createElement("title"); //$NON-NLS-1$
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getTitle()));
        node = document.createElement("source"); //$NON-NLS-1$
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getSource()));
        node = document.createElement("url"); //$NON-NLS-1$
        element.appendChild(node);
        node.appendChild(document.createTextNode(item.getUrl()));
      }

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
      transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(Platform.getLocation().toFile(), "news.xml"))); //$NON-NLS-1$
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
   */
  public void controlMoved(ControlEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  public void controlResized(ControlEvent e)
  {
    if (e.getSource() instanceof TableColumn)
    {
      for (int i = 0; i < table.getColumnCount(); i++)
      {
        TableColumn column = table.getColumn(i);
        if (column == e.getSource())
        {
          columnWidth[i] = column.getWidth();
          break;
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    if (property.equalsIgnoreCase("news.color") == true)
    {
      foreground = new Color(null, PreferenceConverter.getColor(pref, "news.color")); //$NON-NLS-1$
      for (int row = 0; row < data.length; row++)
      {
        TableItem item = table.getItem(row);
        item.setForeground(foreground);
      }
    }
    if (property.equalsIgnoreCase("news.background") == true)
    {
      background = new Color(null, PreferenceConverter.getColor(pref, "news.background")); //$NON-NLS-1$
      table.setBackground(background);
    }
  }
}
