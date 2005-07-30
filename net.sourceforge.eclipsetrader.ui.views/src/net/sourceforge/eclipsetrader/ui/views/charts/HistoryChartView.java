/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.StockList;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

public class HistoryChartView extends ChartView implements DropTargetListener
{
  public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ui.views.ChartView"; //$NON-NLS-1$
  protected IChartData[] chartData;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);
    
    // Drag and drop support
    DropTarget target = new DropTarget(parent, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    target.setTransfer(types);
    target.addDropListener(this);

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("chart." + id); //$NON-NLS-1$
    if (!symbol.equals("")) //$NON-NLS-1$
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        StockList sl = new StockList();
        bd = sl.getData(symbol);
      }
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      setData(bd);
    }

    getSite().setSelectionProvider(this);
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
    if (isIndex(d) == true)
      updateIndexData(d);
    setData(d, d.getTicker() + " - " + Messages.getString("HistoryChartView.title"), "chart."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  public IChartData[] getChartData(IBasicData data)
  {
    if (chartData == null)
    {
      chartData = new IChartData[TraderPlugin.getDataStore().getHistoricalData(data).size()];
      TraderPlugin.getDataStore().getHistoricalData(data).toArray(chartData);
    }
    
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
        if (isIndex(basicData) == true)
        {
          IIndexDataProvider ip = getIndexProvider(basicData);
          if (ip instanceof IChartDataProvider)
            dataProvider = (IChartDataProvider)ip;
        }
        else
          dataProvider = TraderPlugin.getChartDataProvider();
        if (dataProvider != null)
        {
          try {
            chartData = dataProvider.update(basicData, chartData);
            TraderPlugin.getDataStore().storeHistoryData(basicData, chartData);
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
    
    List list = TraderPlugin.getDataStore().getStockwatchData();
    for (int i = 0; i < list.size(); i++)
    {
      IBasicData _tpData = (IBasicData)list.get(i);
      if (_tpData.getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i < list.size() - 1)
          setData((IBasicData)list.get(i + 1));
        else
          setData((IBasicData)list.get(0));
        break;
      }
    }
  }
  
  public void showPrevious()
  {
    chartData = null;
    
    List list = TraderPlugin.getDataStore().getStockwatchData();
    for (int i = 0; i < list.size(); i++)
    {
      IBasicData _tpData = (IBasicData)list.get(i);
      if (_tpData.getSymbol().equalsIgnoreCase(basicData.getSymbol()) == true)
      {
        if (i > 0)
          setData((IBasicData)list.get(i - 1));
        else
          setData((IBasicData)list.get(list.size() - 1));
        break;
      }
    }
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
    String[] item = ((String)event.data).split(";"); //$NON-NLS-1$
    String id = getViewSite().getSecondaryId();
    String symbol = item[1];
    ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + id, item[1]); //$NON-NLS-1$
    if (!symbol.equals("")) //$NON-NLS-1$
    {
      IBasicData bd = TraderPlugin.getData(symbol);
      if (bd == null)
      {
        StockList sl = new StockList();
        bd = sl.getData(symbol);
      }
      if (bd == null)
      {
        bd = new BasicData();
        bd.setSymbol(symbol);
        bd.setTicker(symbol);
        bd.setDescription(symbol);
      }
      chartData = null;
      setData(bd);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dropAccept(DropTargetEvent event)
  {
  }
}
