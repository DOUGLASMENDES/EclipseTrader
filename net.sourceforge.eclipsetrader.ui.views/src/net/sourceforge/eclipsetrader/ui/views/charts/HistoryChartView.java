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
import java.util.Calendar;

import net.sourceforge.eclipsetrader.BasicData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;

public class HistoryChartView extends ChartView
{
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);

    // Restore del grafico precedente
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("chart." + id);
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
  
  public void reloadPreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "charts");
    reloadPreferences(folder);
  }

  public void savePreferences()
  {
    File folder = new File(Platform.getLocation().toFile(), "charts");
    savePreferences(folder);
  }

  public void setData(final IBasicData d)
  {
    setData(d, d.getTicker() + " - Chart", "chart.");
  }
  
  public IChartData[] getChartData(IBasicData data)
  {
    dataProvider = TraderPlugin.getChartDataProvider();
    if (dataProvider != null)
    {
      IChartData[] chartData = dataProvider.getData(basicData);

      // Check if the user has selected a subperiod to display
      if (limitPeriod != 0)
      {
        // Set the limit date
        Calendar limit = Calendar.getInstance();
        limit.set(Calendar.HOUR, 0);
        limit.set(Calendar.MINUTE, 0);
        limit.set(Calendar.SECOND, 0);
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
          chartData = newChartData;
        }
      }

      return chartData;
    }
    
    return null;
  }
  
  public void updateChart()
  {
    Job job = new Job("Update Chart") {
      public IStatus run(IProgressMonitor monitor)
      {
        dataProvider = TraderPlugin.getChartDataProvider();
        if (dataProvider != null)
        {
          try {
            dataProvider.update(basicData);
          } catch(Exception e) {
            return new Status(0, "plugin.id", 0, "Exception occurred", e.getCause()); 
          };
          data = dataProvider.getData(basicData);
          container.getDisplay().asyncExec(new Runnable() {
            public void run() {
              controlResized(null);
              bottombar.redraw();
              updateLabels();
            }
          });
          for (int i = 0; i < chart.size(); i++)
            ((ChartCanvas)chart.elementAt(i)).setData(data);
        }
        return new Status(0, "plugin.id", 0, "OK", null); 
      }
    };
    job.setUser(true);
    job.schedule();
    new Thread(new Runnable() {
      public void run()
      {
      }
    }).start();
  }
  
  public void showNext()
  {
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
}
