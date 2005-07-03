/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stefan S. Stratigakos - Original Qtstalker code
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Accumulation Distribution.
 * <p></p>
 */
public class AccumulationDistributionChart extends ChartPlotter implements IChartConfigurer
{
  public static final int STANDARD = 0;
  public static final int WILLIAM = 1;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.accumulationDistribution"; //$NON-NLS-1$
  private int type = STANDARD;
  private List list = new ArrayList();
  
  public AccumulationDistributionChart()
  {
    setName(Messages.getString("AccumulationDistributionChart.label")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return PLUGIN_ID;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return getName();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    super.setData(data);
    list = new ArrayList();
    
    if (data != null && data.length != 0)
    {
      if (type == STANDARD)
      {
        int loop;
        double accum = 0;
        for (loop = 0; loop < chartData.length; loop++)
        {
          int volume = chartData[loop].getVolume();
          if (volume > 0)
          {
            double high = chartData[loop].getMaxPrice();
            double low = chartData[loop].getMinPrice();

            double t = high - low;

            if (t != 0)
            {
              double close = chartData[loop].getClosePrice();
              double t2 = (close - low) - (high - close);
              accum = accum + ((t2 / t) * volume);
            }
          }

          list.add(new Double(accum));
        }
      }
      else
      {
        int loop;
        double accum = 0;
        for (loop = 1; loop < chartData.length; loop++)
        {
          double high = chartData[loop].getMaxPrice();
          double low = chartData[loop].getMinPrice();
          double close = chartData[loop].getClosePrice();
          double yclose = chartData[loop - 1].getClosePrice();

          double h = high;
          if (yclose > h)
            h = yclose;

          double l = low;
          if (yclose < l)
            l = yclose;

          if (close > yclose)
            accum = accum + (close - l);
          else
          {
            if (yclose == close)
              ;
            else
              accum = accum - (h - close);
          }

          list.add(new Double(accum));
        }
      }
    }
    
    setMinMax(list);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    drawLine(list, gc, height);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
      type = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("AccumulationDistributionChart.type")); //$NON-NLS-1$
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData("type"); //$NON-NLS-1$
    combo.add(Messages.getString("AccumulationDistributionChart.standard")); //$NON-NLS-1$
    combo.add(Messages.getString("AccumulationDistributionChart.williams")); //$NON-NLS-1$
    combo.setText(combo.getItem(type));

    return parent;
  }
}
