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
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.internal.ChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HistoryChartView extends ChartView
{
  private File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
  private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$
  private NumberFormat nf = NumberFormat.getInstance(Locale.US);
  private NumberFormat pf = NumberFormat.getInstance(Locale.US);
  private IChartData[] chartData;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("chart." + id); //$NON-NLS-1$
    if (!symbol.equals("")) //$NON-NLS-1$
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
  
  public void reloadPreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
    reloadPreferences(folder);
  }

  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
    savePreferences(folder);
  }

  public void setData(final IBasicData d)
  {
    setData(d, d.getTicker() + " - " + Messages.getString("HistoryChartView.title"), "chart."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  public IChartData[] getChartData(IBasicData data)
  {
    if (chartData == null)
      load(basicData);
    
    // Check if the user has selected a subperiod to display
    if (chartData != null && chartData.length > 0 && limitPeriod != 0)
    {
      // Set the limit date
      Calendar limit = Calendar.getInstance();
      limit.setTime(chartData[chartData.length - 1].getDate());
      limit.add(Calendar.MONTH, -limitPeriod);

      // Find the first element that is after the limit date
      int srcPos = 0;
      Calendar chart = Calendar.getInstance();
      for (; srcPos < chartData.length; srcPos++)
      {
        chart.setTime(chartData[srcPos].getDate());
        if (chart.after(limit) == true || chart.equals(limit) == true)
          break;
      }

      // Create an array with a subset of the original chart data
      if (srcPos != 0)
      {
        int length = chartData.length - srcPos;
        IChartData[] newChartData = new IChartData[length];
        System.arraycopy(chartData, srcPos, newChartData, 0, length);
        return newChartData;
      }
    }
    
    return chartData;
  }
  
  public void updateChart()
  {
    Job job = new Job(Messages.getString("HistoryChartView.updateChart")) { //$NON-NLS-1$
      public IStatus run(IProgressMonitor monitor)
      {
        dataProvider = TraderPlugin.getChartDataProvider();
        if (dataProvider != null)
        {
          try {
            chartData = dataProvider.update(basicData, chartData);
            store(basicData);
          } catch(Exception e) {
            return new Status(0, "plugin.id", 0, "Exception occurred", e.getCause());  //$NON-NLS-1$ //$NON-NLS-2$
          };
          container.getDisplay().asyncExec(new Runnable() {
            public void run() {
              setData(basicData);
            }
          });
        }
        return new Status(0, "plugin.id", 0, "OK", null);  //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    job.setUser(true);
    job.schedule();
  }
  
  public void showNext()
  {
    chartData = null;
    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i < _tpData.length - 1)
          setData(_tpData[i + 1]);
        else
          setData(_tpData[0]);
        break;
      }
    }
  }
  
  public void showPrevious()
  {
    chartData = null;
    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i > 0)
          setData(_tpData[i - 1]);
        else
          setData(_tpData[_tpData.length - 1]);
        break;
      }
    }
  }

  /**
   * Method to load the chart data.<br>
   *
   * @param symbol The symbol of the data to load.
   */
  private void load(IBasicData data)
  {
    Vector v = new Vector();
    
    File f = new File(folder, data.getSymbol().toLowerCase() + ".xml"); //$NON-NLS-1$
    if (f.exists() == true)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data")) //$NON-NLS-1$
            v.add(decodeData(n.getChildNodes()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    sort(v);

    chartData = new IChartData[v.size()];
    v.toArray(chartData);
  }

  /**
   * Order by date.
   */
  private void sort(Vector v)
  {
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        IChartData d1 = (IChartData)o1;
        IChartData d2 = (IChartData)o2;
        if (d1.getDate().after(d2.getDate()) == true)
          return 1;
        else if (d1.getDate().before(d2.getDate()) == true)
          return -1;
        return 0;
      }
    });
  }
  
  private IChartData decodeData(NodeList parent)
  {
    IChartData cd = new ChartData();
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node n = parent.item(i);
      Node value = n.getFirstChild();
      if (value != null)
      {
        if (n.getNodeName().equalsIgnoreCase("open_price") == true) //$NON-NLS-1$
          cd.setOpenPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("max_price") == true) //$NON-NLS-1$
          cd.setMaxPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("min_price") == true) //$NON-NLS-1$
          cd.setMinPrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("close_price") == true) //$NON-NLS-1$
          cd.setClosePrice(Double.parseDouble(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("volume") == true) //$NON-NLS-1$
          cd.setVolume(Integer.parseInt(value.getNodeValue()));
        else if (n.getNodeName().equalsIgnoreCase("date") == true) //$NON-NLS-1$
        {
          try {
            cd.setDate(df.parse(value.getNodeValue()));
          } catch(Exception e) {};
        }
      }
    }
    return cd;
  }

  /**
   * Method to store the chart data.<br>
   */
  private void store(IBasicData data)
  {
    
    if (chartData != null)
      try {
        folder.mkdirs();
        BufferedWriter xmlout = new BufferedWriter(new FileWriter(new File(folder, data.getSymbol().toLowerCase() + ".xml"))); //$NON-NLS-1$

        xmlout.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"); //$NON-NLS-1$
        xmlout.write("<chart>\r\n"); //$NON-NLS-1$
        xmlout.write("    <symbol>" + data.getTicker() + "</symbol>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 0; i < chartData.length; i++)
        {
          IChartData cd = (IChartData)chartData[i];
          xmlout.write("    <data>\r\n"); //$NON-NLS-1$
          xmlout.write("        <date>" + df.format(cd.getDate()) + "</date>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("        <open_price>" + pf.format(cd.getOpenPrice()) + "</open_price>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("        <max_price>" + pf.format(cd.getMaxPrice()) + "</max_price>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("        <min_price>" + pf.format(cd.getMinPrice()) + "</min_price>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("        <close_price>" + pf.format(cd.getClosePrice()) + "</close_price>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("        <volume>" + cd.getVolume() + "</volume>\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
          xmlout.write("    </data>\r\n"); //$NON-NLS-1$
        }
        xmlout.write("</chart>\r\n"); //$NON-NLS-1$
        xmlout.flush();
        xmlout.close();
      } catch (Exception ex) {};
  }
}
