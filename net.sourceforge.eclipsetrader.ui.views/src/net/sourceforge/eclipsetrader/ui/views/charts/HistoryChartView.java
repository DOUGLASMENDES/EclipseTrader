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
import net.sourceforge.eclipsetrader.monitors.HistoricalChartMonitor;
import net.sourceforge.eclipsetrader.monitors.IMonitorListener;
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
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

public class HistoryChartView extends ChartView implements IMonitorListener
{
  public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ui.views.ChartView"; //$NON-NLS-1$
  
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
    target.addDropListener(new DropTargetAdapter() {
      public void dragEnter(DropTargetEvent event)
      {
        event.detail = DND.DROP_COPY;
      }

      public void dragOver(DropTargetEvent event)
      {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }

      public void drop(DropTargetEvent event)
      {
        String[] item = ((String)event.data).split(";"); //$NON-NLS-1$
        String id = getViewSite().getSecondaryId();
        String symbol = item[1];
        ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + id, item[1]); //$NON-NLS-1$
        if (!symbol.equals("")) //$NON-NLS-1$
        {
          IBasicData basicData = TraderPlugin.getData(symbol);
          if (basicData == null)
          {
            StockList sl = new StockList();
            basicData = sl.getData(symbol);
          }
          if (basicData == null)
            basicData = new BasicData(symbol);
          setData(basicData);
        }
      }
      
    });

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("chart." + id); //$NON-NLS-1$
    if (!symbol.equals("")) //$NON-NLS-1$
    {
      IBasicData basicData = TraderPlugin.getData(symbol);
      if (basicData == null)
      {
        StockList stockList = new StockList();
        basicData = stockList.getData(symbol);
      }
      if (basicData == null)
        basicData = new BasicData(symbol);
      setData(basicData);
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartView#dispose()
   */
  public void dispose()
  {
    if (getBasicData() != null)
      HistoricalChartMonitor.getInstance().removeMonitor(getBasicData(), this);
    super.dispose();
  }

  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
    savePreferences(folder);
  }

  private void setData(IBasicData basicData)
  {
    if (getBasicData() != null)
      HistoricalChartMonitor.getInstance().removeMonitor(getBasicData(), this);
    
    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + id, basicData.getSymbol());
    setPartName(basicData.getTicker() + " - " + Messages.getString("HistoryChartView.title"));

    File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
    File file = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs"); //$NON-NLS-1$

    loadPreferences(file);
    setBasicData(basicData);
    setBarData(getChartData(basicData));

    HistoricalChartMonitor.getInstance().addMonitor(getBasicData(), this);
  }
  
  private BarData getChartData(IBasicData data)
  {
    BarData barData = new BarData();
    IChartData[] chartData = TraderPlugin.getDataStore().getHistoricalData(data);

    if (chartData.length > 0)
    {
      Calendar limit = Calendar.getInstance();
      limit.setTime(chartData[chartData.length - 1].getDate());
      limit.add(Calendar.MONTH, - getLimitPeriod());
      
      Calendar chart = Calendar.getInstance();
      for (int i = 0; i < chartData.length; i++)
      {
        chart.setTime(chartData[i].getDate());
        if (getLimitPeriod() == 0 || chart.after(limit) == true)
          barData.add(chartData[i]);
      }
    }
    
    return barData;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartView#setLimitPeriod(int)
   */
  public void setLimitPeriod(int limitPeriod)
  {
    super.setLimitPeriod(limitPeriod);
    setBarData(getChartData(basicData));
  }

  public void updateChart()
  {
    Job job = new Job(Messages.getString("HistoryChartView.updateChart")) { //$NON-NLS-1$
      public IStatus run(IProgressMonitor monitor)
      {
        IChartDataProvider dataProvider = null;
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
          IChartData[] chartData = TraderPlugin.getDataStore().getHistoricalData(basicData);
          try {
            IChartData[] newChartData = dataProvider.update(basicData, chartData);
            TraderPlugin.getDataStore().storeHistoryData(basicData, newChartData);
            if (newChartData.length != chartData.length)
              HistoricalChartMonitor.getInstance().notifyUpdate(basicData);
          } catch(Exception e) {
            return new Status(0, "plugin.id", 0, "Exception occurred", e.getCause());  //$NON-NLS-1$ //$NON-NLS-2$
          };
        }
        return new Status(0, "plugin.id", 0, "OK", null);  //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    job.setUser(true);
    job.schedule();
  }
  
  public void showNext()
  {
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
   * @see net.sourceforge.eclipsetrader.monitors.IMonitorListener#monitoredDataUpdated(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void monitoredDataUpdated(IBasicData obj)
  {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        setBarData(getChartData(basicData));
      }
    });
  }
}
