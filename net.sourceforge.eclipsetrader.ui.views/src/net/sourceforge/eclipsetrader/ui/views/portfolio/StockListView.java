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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.monitors.HistoricalChartMonitor;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.StockList;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class StockListView extends ViewPart
{
  private Table table;
  private StockList stockList = new StockList();

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION);
    table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    table.setHeaderVisible(false);
    table.setLinesVisible(false);
    
    DragSource dragSource = new DragSource(table, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    dragSource.setTransfer(types);
    dragSource.addDragListener(new DragSourceListener() {
      public void dragStart(DragSourceEvent event)
      {
        if (table.getSelectionIndex() == -1) 
          event.doit = false;
      }
      public void dragSetData(DragSourceEvent event) 
      {
        IBasicData data = stockList.getData()[table.getSelectionIndex()];
        event.data = "B;" + data.getSymbol() + ";" + data.getTicker() + ";" + data.getMinimumQuantity() + ";0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
      public void dragFinished(DragSourceEvent event) 
      {
      }
    });    

    table.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e) 
      {
        String CHART_ID = "net.sourceforge.eclipsetrader.ui.views.ChartView"; //$NON-NLS-1$

        IBasicData data = stockList.getData()[table.getSelectionIndex()];
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 1;; i++)
        {
          IViewReference ref = page.findViewReference(CHART_ID, "B" + String.valueOf(i));
          if (ref == null)
          {
            ViewsPlugin.getDefault().getPreferenceStore().setValue("chart.B" + String.valueOf(i), data.getSymbol()); //$NON-NLS-1$
            try {
              page.showView(CHART_ID, "B" + String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException ex) {}
            break;
          }
        }
      }
    });
    
    updateView();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    if (table.getSelectionIndex() != -1)
      table.setFocus();
    else
      table.getParent().setFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    super.dispose();
  }
  
  public void updateChartData()
  {
    if (TraderPlugin.getChartDataProvider() == null)
      return;
    if (stockList.getData() == null)
      return;
    
    Job job = new Job(Messages.getString("HistoryChartView.updateChart")) { //$NON-NLS-1$
      public IStatus run(IProgressMonitor monitor)
      {
        IChartDataProvider dataProvider = TraderPlugin.getChartDataProvider();
        IBasicData[] data = stockList.getData();
        monitor.beginTask(Messages.getString("HistoryChartView.updateChart"), data.length);
        for (int i = 0; i < data.length; i++)
        {
          IBasicData basicData = data[i];
          IChartData[] chartData = TraderPlugin.getDataStore().getHistoricalData(basicData);
          try {
            IChartData[] newChartData = dataProvider.update(basicData, chartData);
            TraderPlugin.getDataStore().storeHistoryData(basicData, newChartData);
            if (newChartData.length != chartData.length)
              HistoricalChartMonitor.getInstance().notifyUpdate(basicData);
            monitor.worked(1);
            if (monitor.isCanceled() == true)
              break;
          } catch(Exception e) {
            return new Status(0, "plugin.id", 0, "Exception occurred", e.getCause());  //$NON-NLS-1$ //$NON-NLS-2$
          };
        }
        monitor.done();
        return new Status(0, "plugin.id", 0, "OK", null);  //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    job.setUser(true);
    job.schedule();
  }
  
  public void updateView()
  {
    TableItem item;
    
    table.setRedraw(false);

    IBasicData[] data = stockList.getData();
    if (data != null)
    {
      for (int i = 0; i < data.length; i++)
      {
        if (i < table.getItemCount())
          item = table.getItem(i);
        else
          item = new TableItem(table, SWT.NONE);
        item.setText(0, data[i].getDescription());
      }
      table.setItemCount(data.length);
    }
    else
      table.setItemCount(0);
    
    table.setRedraw(true);
  }
}
