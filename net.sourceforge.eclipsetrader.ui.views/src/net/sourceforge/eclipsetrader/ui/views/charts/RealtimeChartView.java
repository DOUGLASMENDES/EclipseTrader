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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IRealtimeChartListener;
import net.sourceforge.eclipsetrader.IRealtimeChartProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RealtimeChartView extends ChartView implements IRealtimeChartListener, DropTargetListener
{
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    df = new SimpleDateFormat("HH:mm");
    super.createPartControl(parent);
    
    // Drag and drop support
    DropTarget target = new DropTarget(parent, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    target.setTransfer(types);
    target.addDropListener(this);

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("rtchart." + id);
    if (!symbol.equals(""))
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      setData(bd);
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(basicData, this);
    }
    super.dispose();
  }

  public void reloadPreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");
    reloadPreferences(folder);
    setMargin(1);
    setWidth(3);
  }

  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");
    savePreferences(folder);
  }

  public void setData(final IBasicData d)
  {
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.removeRealtimeChartListener(basicData, this);
    }
    setData(d, d.getTicker() + " - Intraday", "rtchart.");
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.addRealtimeChartListener(basicData, this);
    }
  }
  
  public IChartData[] getChartData(IBasicData data)
  {
    IChartData[] c = null;
    if (TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      c = rtp.getHistoryData(basicData);
    }
    if (c == null)
      c = load();
    return c;
  }
  
  public void refreshChart()
  {
    if (basicData != null && TraderPlugin.getDataProvider() instanceof IRealtimeChartProvider)
    {
      IRealtimeChartProvider rtp = (IRealtimeChartProvider)TraderPlugin.getDataProvider();
      rtp.backfill(basicData);
      realtimeChartUpdated(rtp);
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    GC gc = e.gc;
    
    if (e.getSource() == bottombar)
    {
      gc.setForeground(textColor);
      gc.drawLine(0, 0, bottombar.getClientArea().width, 0); 

      if (basicData != null)
      {
        int lastValue = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        
        gc.setClipping(0, 0, bottombar.getClientArea().width - scaleWidth, bottombar.getClientArea().height);

        int x = margin + width / 2;
        if (container.getHorizontalBar().isVisible() == true)
          x -= container.getHorizontalBar().getSelection();
        for (int i = 0; i < data.length; i++, x += width)
        {
          c.setTime(data[i].getDate());
          if (c.get(Calendar.HOUR_OF_DAY) != lastValue || c.get(Calendar.MINUTE) == 30)
          {
            String s = df.format(data[i].getDate());
            int x1 = x - gc.stringExtent(s).x / 2;
            gc.drawLine(x, 0, x, 5);
            gc.drawString(s, x1, 5);
            lastValue = c.get(Calendar.HOUR_OF_DAY);
          }
        }
      }
    }
    else
    {
      gc.setForeground(textColor);
      Composite c = (Composite)e.getSource();
      gc.drawLine(0, c.getClientArea().height - 1, c.getClientArea().width, c.getClientArea().height - 1); 
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IRealtimeChartListener#realtimeChartUpdated(net.sourceforge.eclipsetrader.IRealtimeChartProvider)
   */
  public void realtimeChartUpdated(final IRealtimeChartProvider provider)
  {
    new Thread(new Runnable() {
      public void run() 
      {
        data = provider.getHistoryData(basicData);
        store();
        container.getDisplay().asyncExec(new Runnable() {
          public void run() {
            controlResized(null);
            bottombar.redraw();
            updateLabels();
          }
        });
        updateView();
      }
    }).start();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragEnter(DropTargetEvent event)
  {
    event.detail = DND.DROP_COPY;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragLeave(DropTargetEvent event)
  {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragOperationChanged(DropTargetEvent event)
  {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragOver(DropTargetEvent event)
  {
    event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void drop(DropTargetEvent event)
  {
    String[] item = ((String)event.data).split(";");
    String id = getViewSite().getSecondaryId();
    String symbol = item[1];
    ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + id, item[1]);
    if (!symbol.equals(""))
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      setData(bd);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dropAccept(DropTargetEvent event)
  {
  }

  /**
   * Load the chart data from the local storage.
   * <p></p>
   */
  private IChartData[] load()
  {
    Vector _data = new Vector();
    File folder = new File(Platform.getLocation().toFile(), "rtcharts");
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    NumberFormat nf = NumberFormat.getInstance();
    
    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    nf.setMinimumIntegerDigits(1);
    
    File file = new File(folder, basicData.getSymbol().toLowerCase() + ".xml");
    if (file.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int c = 0; c < firstChild.getLength(); c++)
        {
          if (firstChild.item(c).getNodeName().equalsIgnoreCase("data"))
          {
            NodeList parent = firstChild.item(c).getChildNodes();
            IChartData cd = new ChartData();
            for (int i = 0; i < parent.getLength(); i++)
            {
              Node n = parent.item(i);
              Node value = n.getFirstChild();
              if (value != null)
              {
                if (n.getNodeName().equalsIgnoreCase("open") == true)
                  cd.setOpenPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("max") == true)
                  cd.setMaxPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("min") == true)
                  cd.setMinPrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("close") == true)
                  cd.setClosePrice(nf.parse(value.getNodeValue()).doubleValue());
                else if (n.getNodeName().equalsIgnoreCase("volume") == true)
                  cd.setVolume(Integer.parseInt(value.getNodeValue()));
                else if (n.getNodeName().equalsIgnoreCase("date") == true)
                {
                  try {
                    cd.setDate(df.parse(value.getNodeValue()));
                  } catch(Exception e) {};
                }
              }
            }
            _data.addElement(cd);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    data = new IChartData[_data.size()];
    _data.toArray(data);
    
    return data;
  }

  /**
   * Save the chart data to the local storage.
   * <p></p>
   */
  private void store()
  {
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    NumberFormat nf = NumberFormat.getInstance();
    
    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    nf.setMinimumIntegerDigits(1);

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "chart", null);

      for (int i = 0; i < data.length; i++)
      {
        Element element = document.createElement("data");
        document.getDocumentElement().appendChild(element);

        Node node = document.createElement("date");
        element.appendChild(node);
        node.appendChild(document.createTextNode(df.format(data[i].getDate())));
        node = document.createElement("open");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getOpenPrice())));
        node = document.createElement("close");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getClosePrice())));
        node = document.createElement("max");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getMaxPrice())));
        node = document.createElement("min");
        element.appendChild(node);
        node.appendChild(document.createTextNode(nf.format(data[i].getMinPrice())));
        node = document.createElement("volume");
        element.appendChild(node);
        node.appendChild(document.createTextNode("" + data[i].getVolume()));
      }

      File folder = new File(Platform.getLocation().toFile(), "rtcharts");
      folder.mkdirs();

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(folder, basicData.getSymbol().toLowerCase() + ".xml")));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) {};
  }
}
