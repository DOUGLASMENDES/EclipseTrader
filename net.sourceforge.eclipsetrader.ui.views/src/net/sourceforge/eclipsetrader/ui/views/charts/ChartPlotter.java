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

import java.util.HashMap;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Default implementation of the IChartPlotter interface.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class ChartPlotter implements IChartPlotter
{
  protected String name;
  protected ChartCanvas chartCanvas;
  protected int columnWidth = 5;
  protected int chartMargin = 2;
  protected int scaleWidth = 60;
  protected Color lineColor = new Color(null, 0, 0, 255);
  protected IChartData[] chartData;
  protected double min;
  protected double max;
  protected HashMap params = new HashMap();

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setCanvas(net.sourceforge.eclipsetrader.ui.views.charts.ChartCanvas)
   */
  public void setCanvas(ChartCanvas canvas)
  {
    chartCanvas = canvas;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getCanvas()
   */
  public ChartCanvas getCanvas()
  {
    return chartCanvas;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    chartData = data;
    min = max = 0;

    if (chartData != null)
    {
      // Determina massimo e minimo
      for (int i = 0; i < data.length; i++)
      {
        if (data[i].getMaxPrice() > max)
          max = data[i].getMaxPrice();
        if (min == 0 || data[i].getMinPrice() < min)
          min = data[i].getMinPrice();
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintChart(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("name") == true)
      name = value;
    else
    {
      if (name.equalsIgnoreCase("color") == true)
      {
        String[] values = value.split(",");
        lineColor = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
      }
      params.put(name, value);
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartConfigurer#getParameter(java.lang.String)
   */
  public String getParameter(String name)
  {
    return (String)params.get(name);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartConfigurer#getColorParameter(java.lang.String)
   */
  public RGB getColorParameter(String name)
  {
    if (name.equalsIgnoreCase("color") == true)
      return lineColor.getRGB();
    String[] values = ((String)params.get(name)).split(",");
    return new RGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getParameters()
   */
  public HashMap getParameters()
  {
    return params;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    return null;
  }

  /**
   * Draw a line based on the given value array.
   * <p></p>
   */
  public void drawLine(double[] value, GC gc, int height)
  {
    double pixelRatio = height / (max - min);
    int[] pointArray = new int[value.length * 2];
    int x = columnWidth / 2;
    for (int i = 0, pa = 0; i < value.length; i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      int y = (int)((value[i] - min) * pixelRatio);
      pointArray[pa++] = height - y;
    }
    gc.drawPolyline(pointArray);
  }
  
  /**
   * Draw a line based on the given value array and starting at the give offset
   * on the chart.
   * <p></p>
   */
  public void drawLine(double[] value, GC gc, int height, int ofs)
  {
    double pixelRatio = height / (max - min);
    int[] pointArray = new int[value.length * 2];
    int x = chartMargin + columnWidth / 2 + ofs * columnWidth;
    for (int i = 0, pa = 0; i < value.length; i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      int y = (int)((value[i] - min) * pixelRatio);
      pointArray[pa++] = height - y;
    }
    gc.drawPolyline(pointArray);
  }

}
