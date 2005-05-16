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

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Volume chart.
 */
public class VolumeChart extends ChartPlotter
{
  private static int VERTICAL_BORDER = 3;
  private Color gridColor = new Color(null, 192, 192, 192);
  private Color textColor = new Color(null, 0, 0, 0);
  private NumberFormat nf = NumberFormat.getInstance();
  
  public VolumeChart()
  {
    nf.setGroupingUsed(false);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return Messages.getString("VolumeChart.label"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    double min = 0, max = 0;
    
    chartData = data;
    if (data != null)
    {
      // Determina massimo e minimo
      for (int i = 0; i < data.length; i++)
      {
        if (data[i].getVolume() > max)
          max = data[i].getVolume();
      }
    }
    
    setMinMax(min, max);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);

    // Line type and color
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(getColor());
    
    if (chartData != null && getMax() > getMin())
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (height - VERTICAL_BORDER * 2) / (getMax() - getMin());

      int x = chartMargin + getColumnWidth() / 2;
      for (int i = 0; i < chartData.length; i++, x += getColumnWidth())
      {
        int y1 = height - (int)((chartData[i].getVolume() - getMin()) * pixelRatio);
        int y2 = height;
        gc.drawLine(x, y1 - VERTICAL_BORDER, x, y2 - VERTICAL_BORDER);
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintGrid(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintGrid(GC gc, int width, int height)
  {
    int value = (int)getMax();
    if (value >= 10000000)
      value = value / 1000000 * 1000000;
    else if (value >= 1000000)
      value = value / 100000 * 100000;
    else if (value >= 100000)
      value = value / 1000 * 1000;
    else
      value = value / 100 * 100;

    double pixelRatio = (height - VERTICAL_BORDER * 2) / (getMax() - getMin());
    gc.setForeground(gridColor);
    gc.setLineStyle(SWT.LINE_DOT);

    int y1 = height - (int)((value - getMin()) * pixelRatio) - VERTICAL_BORDER;
    gc.drawLine(0, y1, width, y1);
    y1 = height - (int)(((value / 2) - getMin()) * pixelRatio) - VERTICAL_BORDER;
    gc.drawLine(0, y1, width, y1);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
    int value = (int)getMax();
    if (value >= 10000000)
      value = value / 1000000 * 1000000;
    else if (value >= 1000000)
      value = value / 100000 * 100000;
    else if (value >= 100000)
      value = value / 1000 * 1000;
    else
      value = value / 100 * 100;

    double pixelRatio = (height - VERTICAL_BORDER * 2) / (getMax() - getMin());
    gc.setForeground(textColor);
    gc.setLineStyle(SWT.LINE_SOLID);

    int y1 = height - (int)((value - getMin()) * pixelRatio) - VERTICAL_BORDER;
    gc.drawLine(1, y1, 5, y1);
    String s = nf.format(value);
    if (getMax() >= 1000000)
      s = nf.format(value / 1000);
    gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
    y1 = height - (int)(((value / 2) - getMin()) * pixelRatio) - VERTICAL_BORDER;
    gc.drawLine(1, y1, 5, y1);
    s = nf.format(value / 2);
    if (getMax() >= 1000000)
      s = nf.format(value / 1000 / 2);
    gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getValue(int, int)
   */
  public double getValue(int y, int height)
  {
    double pixelRatio = (height - VERTICAL_BORDER * 2) / (getMax() - getMin());
    return (height - y) / pixelRatio + getMin();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getFormattedValue(int, int)
   */
  public String getFormattedValue(int y, int height)
  {
    if (getMax() >= 1000000)
      return nf.format(getValue(y, height) / 1000);
    else
      return nf.format(getValue(y, height));
  }
}
