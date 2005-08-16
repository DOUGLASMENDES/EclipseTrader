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

import java.io.File;
import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBackfillDataProvider;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.monitors.IMonitorListener;
import net.sourceforge.eclipsetrader.monitors.IntradayChartMonitor;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.StockList;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

public class RealtimeChartView extends ChartView implements IMonitorListener
{
  public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ui.views.RealtimeChart";
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    df = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$

    super.createPartControl(parent);
    dateLabel.setText(Messages.getString("ChartView.Time")); //$NON-NLS-1$
    
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
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("rtchart." + id); //$NON-NLS-1$
    if (!symbol.equals("")) //$NON-NLS-1$
    {
      IBasicData basicData = TraderPlugin.getData(symbol);
      if (basicData == null)
      {
        basicData = new BasicData();
        basicData.setSymbol(symbol);
        basicData.setTicker(symbol);
        basicData.setDescription(symbol);
      }
      setData(basicData);
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    if (getBasicData() != null)
      IntradayChartMonitor.getInstance().removeMonitor(getBasicData(), this);
    super.dispose();
  }

  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts"); //$NON-NLS-1$
    savePreferences(folder);
  }

  private void setData(IBasicData basicData)
  {
    if (getBasicData() != null)
      IntradayChartMonitor.getInstance().removeMonitor(getBasicData(), this);
    
    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + id, basicData.getSymbol());
    setPartName(basicData.getTicker() + " - " + Messages.getString("HistoryChartView.title"));

    File folder = new File(Platform.getLocation().toFile(), "charts"); //$NON-NLS-1$
    File file = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs"); //$NON-NLS-1$

    loadPreferences(file);
    setBasicData(basicData);
    setBarData(getChartData(basicData));

    IntradayChartMonitor.getInstance().addMonitor(getBasicData(), this);
  }

  public void reloadPreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "rtcharts"); //$NON-NLS-1$
    loadPreferences(folder);
    setMargin(1);
    setWidth(3);
  }

  private BarData getChartData(IBasicData data)
  {
    BarData barData = new BarData();

    barData.addAll(TraderPlugin.getDataStore().getIntradayData(data));
    barData.setCompression(BarData.INTERVAL_MINUTE2);

    return barData;
  }
  
  public void updateChart()
  {
    if (basicData != null)
    {
      IBackfillDataProvider backfill = TraderPlugin.getBackfillDataProvider();
      if (isIndex(basicData) == true)
      {
        backfill = null;
        IIndexDataProvider indexDataProvider = getIndexProvider(basicData);
        if (indexDataProvider instanceof IBackfillDataProvider)
          backfill = (IBackfillDataProvider)indexDataProvider;
      }
      
      if (backfill != null)
      {
        BarData barData = new BarData();
        IChartData[] newChartData = backfill.getIntradayData(basicData);
        barData.addAll(newChartData);
        TraderPlugin.getDataStore().storeIntradayData(basicData, newChartData);
        setBarData(barData);
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
