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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Moving Average chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class AccumulationDistributionChart extends ChartPlotter implements IChartConfigurer
{
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.accumulationDistribution";
  private Color gridColor = new Color(null, 192, 192, 192);
  
  public AccumulationDistributionChart()
  {
    name = "Accumulation/Distribution";
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
    return name;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    if (chartData != null && max > min)
    {
      max = min = 0;

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      gc.drawLine(0, height / 2, width, height / 2);

      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(lineColor);

      // Calculate the values
      if (chartData.length >= 1)
      {
        double previous = 0;
        double[] value = new double[chartData.length];
        for (int i = 0; i < value.length; i++)
        {
          double v1 = chartData[i].getClosePrice() - chartData[i].getMinPrice();
          double v2 = chartData[i].getMaxPrice() - chartData[i].getClosePrice();
          double v3 = chartData[i].getMaxPrice() - chartData[i].getMinPrice();
          if (v3 != 0)
            value[i] = (((v1 - v2) / v3) * chartData[i].getVolume()) + previous;
          if (min == 0 || value[i] < min)
            min = value[i];
          if (value[i] > max)
            max = value[i];
          previous = value[i];
        }

        double margin = (max - min) / 100 * 2; 
        max += margin;
        min -= margin;

        this.drawLine(value, gc, height);
      }
    }
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
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    return parent;
  }
}
