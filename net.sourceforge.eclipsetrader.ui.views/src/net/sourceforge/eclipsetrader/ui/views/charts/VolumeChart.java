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

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

/**
 * Volume chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class VolumeChart extends ChartPlotter
{
  private static int VERTICAL_BORDER = 3;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return "Volume";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    chartData = data;
    min = max = 0;
    
    if (data != null)
    {
      // Determina massimo e minimo
      for (int i = 0; i < data.length; i++)
      {
        if (data[i].getVolume() > max)
          max = data[i].getVolume();
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);

    // Line type and color
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
    
    if (chartData != null && max > min)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (height - VERTICAL_BORDER * 2) / (max - min);

      int x = chartMargin + columnWidth / 2;
      for (int i = 0; i < chartData.length; i++, x += columnWidth)
      {
        int y1 = height - (int)((chartData[i].getVolume() - min) * pixelRatio);
        int y2 = height;
        gc.drawLine(x, y1 - VERTICAL_BORDER, x, y2 - VERTICAL_BORDER);
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }
}
