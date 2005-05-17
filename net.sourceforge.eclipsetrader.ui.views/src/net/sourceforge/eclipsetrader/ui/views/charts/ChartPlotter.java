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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Default implementation of the IChartPlotter interface.
 */
public class ChartPlotter implements IChartPlotter
{
  public final static int CLOSE = 0;
  public final static int OPEN = 1;
  public final static int MAX = 2;
  public final static int MIN = 3;
  private String name;
  private ChartCanvas chartCanvas;
  private int columnWidth = 5;
  protected int chartMargin = 2;
  protected int scaleWidth = 60;
  private Color lineColor = new Color(null, 0, 0, 255);
  protected IChartData[] chartData;
  private double min;
  private double max;
  protected HashMap params = new HashMap();
  private boolean selected = false;
  private NumberFormat pf = NumberFormat.getInstance();
  
  public ChartPlotter()
  {
    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }

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

  /**
   * Method to return the selected field.<br>
   *
   * @return Returns the selected.
   */
  public boolean isSelected()
  {
    return selected;
  }

  /**
   * Method to set the selected field.<br>
   * 
   * @param selected The selected to set.
   */
  public void setSelected(boolean selected)
  {
    this.selected = selected;
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
  
  public List getValues(IChartData[] data, int type)
  {
    List list = new ArrayList();
    
    switch(type)
    {
      case CLOSE:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(data[i].getClosePrice()));
        break;
      case OPEN:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(data[i].getOpenPrice()));
        break;
      case MAX:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(data[i].getMaxPrice()));
        break;
      case MIN:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(data[i].getMinPrice()));
        break;
    }
    
    return list;
  }
  
  public void setMinMax(double min, double max)
  {
    this.min = min;
    this.max = max;
  }
  
  public double getMin()
  {
    return min;
  }
  
  public double getMax()
  {
    return max;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getColor()
   */
  public Color getColor()
  {
    return lineColor;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintChart(GC gc, int width, int height)
  {
    if (chartCanvas != null)
    {
      columnWidth = chartCanvas.getColumnWidth();
      chartMargin = chartCanvas.getMargin();
    }
    // Default line type and color
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintGrid(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintGrid(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("name") == true)
    {
      this.name = value;
      params.put(name, value);
    }
    else if (value != null)
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
   * @param value - the array of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   */
  public void drawLine(double[] value, GC gc, int height)
  {
    drawLine(value, gc, height, chartData.length - value.length);
  }
  
  /**
   * Draw a line based on the given value array and starting at the give offset
   * on the chart.
   *
   * @param value - the array of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   * @param ofs - the offset to start drawing at
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
    
    if (isSelected() == true)
      drawSelectionMarkers(pointArray, gc);
  }

  /**
   * Draw a line based on the given list of double values.
   *
   * @param value - the list of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   */
  public void drawLine(List value, GC gc, int height)
  {
    drawLine(value, gc, height, chartData.length - value.size());
  }
  
  /**
   * Draw a line based on the given value list and starting at the give offset
   * on the chart.
   *
   * @param value - the list of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   * @param ofs - the offset to start drawing at
   */
  public void drawLine(List value, GC gc, int height, int ofs)
  {
    double pixelRatio = height / (max - min);
    int[] pointArray = new int[value.size() * 2];
    int x = chartMargin + columnWidth / 2 + ofs * columnWidth;
    for (int i = 0, pa = 0; i < value.size(); i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      int y = (int)((((Double)value.get(i)).doubleValue() - min) * pixelRatio);
      pointArray[pa++] = height - y;
    }
    gc.drawPolyline(pointArray);
    
    if (isSelected() == true)
      drawSelectionMarkers(pointArray, gc);
  }
  
  /**
   * Draw an istogram chart based on the given value list and placed at the 
   * rightmost position on the chart.
   *
   * @param value - the list of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   */
  public void drawIstogram(List value, GC gc, int height)
  {
    drawIstogram(value, gc, height, chartData.length - value.size());
  }
  
  /**
   * Draw an istogram chart based on the given value list and starting at 
   * the give offset on the chart.
   *
   * @param value - the list of values to draw
   * @param gc - the graphics context
   * @param height - the chart's height
   * @param ofs - the offset to start drawing at
   */
  public void drawIstogram(List value, GC gc, int height, int ofs)
  {
    // Draw the istogram chart
    double pixelRatio = height / (getMax() - getMin());
    int x = chartMargin + columnWidth / 2 + ofs * columnWidth;
    int y2 = height - (int)((0 - min) * pixelRatio);
    for (int i = 0; i < value.size(); i++, x += columnWidth)
    {
      int y1 = height - (int)((((Double)value.get(i)).doubleValue() - min) * pixelRatio);
      gc.drawLine(x, y1, x, y2);
    }
  }

  /**
   * Draws the selection markers over the chart line.
   * 
   * @param pointArray array of x,y coordinates that represents the chart line
   * @param gc the current graphics context 
   */
  public void drawSelectionMarkers(int[] pointArray, GC gc)
  {
    int length = pointArray.length / 2;
    
    Color oldBackground = gc.getBackground();
    gc.setBackground(getColor());
    if (length <= 20)
    {
      gc.fillRectangle(pointArray[0] - 1, pointArray[1] - 1, 5, 5);
      gc.fillRectangle(pointArray[(length / 2) * 2] - 1, pointArray[(length / 2) * 2 + 1] - 1, 5, 5);
    }
    else
    {
      for (int i = 0; i < length - 5; i += 10)
        gc.fillRectangle(pointArray[i * 2] - 1, pointArray[i * 2 + 1] - 1, 5, 5);
    }
    gc.fillRectangle(pointArray[pointArray.length - 2] - 1, pointArray[pointArray.length - 1] - 1, 5, 5);
    gc.setBackground(oldBackground);
  }

  /**
   * Scales the chart based on the lower and upper range of values of the
   * given values array.
   *
   * @param value - the array of values
   */
  public void setMinMax(double[] value)
  {
    min = 0; max = 0;
    for (int i = 0; i < value.length; i++)
    {
      if (i == 0 || value[i] < min)
        min = value[i];
      if (value[i] > max)
        max = value[i];
    }
    double margin = (max - min) / 100 * 2; 
    max += margin;
    min -= margin;

    // Adjust the upper and lower values so that the value 0 is centered
    // on the chart (only for charts that have negative minimum value)
    if (min < 0 && max > 0)
    {
      max = Math.abs(max);
      min = Math.abs(min);
      if (min > max)
        max = min;
      min = -max;
    }
  }
  
  /**
   * Updates the chart scale based on the lower and upper range of values of the
   * given values array.
   *
   * @param value - the array of values
   */
  public void updateMinMax(double[] value)
  {
    for (int i = 0; i < value.length; i++)
    {
      if (i == 0 || value[i] < min)
        min = value[i];
      if (value[i] > max)
        max = value[i];
    }
    double margin = (max - min) / 100 * 2; 
    max += margin;
    min -= margin;

    // Adjust the upper and lower values so that the value 0 is centered
    // on the chart (only for charts that have negative minimum value)
    if (min < 0 && max > 0)
    {
      max = Math.abs(max);
      min = Math.abs(min);
      if (min > max)
        max = min;
      min = -max;
    }
  }
  
  /**
   * Scales the chart based on the lower and upper range of values of the
   * given values list.
   *
   * @param value - the list of values
   */
  public void setMinMax(List value)
  {
    min = 0; max = 0;
    for (int i = 0; i < value.size(); i++)
    {
      double t = ((Double)value.get(i)).doubleValue();
      if (i == 0 || t < min)
        min = t;
      if (t > max)
        max = t;
    }
    double margin = (max - min) / 100 * 2; 
    max += margin;
    min -= margin;

    // Adjust the upper and lower values so that the value 0 is centered
    // on the chart (only for charts that have negative minimum value)
    if (min < 0 && max > 0)
    {
      max = Math.abs(max);
      min = Math.abs(min);
      if (min > max)
        max = min;
      min = -max;
    }
  }
  
  /**
   * Updates the chart scale based on the lower and upper range of values of the
   * given values list.
   *
   * @param value - the list of values
   */
  public void updateMinMax(List value)
  {
    for (int i = 0; i < value.size(); i++)
    {
      double t = ((Double)value.get(i)).doubleValue();
      if (i == 0 || t < min)
        min = t;
      if (t > max)
        max = t;
    }
    double margin = (max - min) / 100 * 2; 
    max += margin;
    min -= margin;

    // Adjust the upper and lower values so that the value 0 is centered
    // on the chart (only for charts that have negative minimum value)
    if (min < 0 && max > 0)
    {
      max = Math.abs(max);
      min = Math.abs(min);
      if (min > max)
        max = min;
      min = -max;
    }
  }

  /**
   * Method to return the columnWidth field.<br>
   *
   * @return Returns the columnWidth.
   */
  public int getColumnWidth()
  {
    columnWidth = chartCanvas.getColumnWidth();
    return columnWidth;
  }
  
  public double getValue(int y, int height)
  {
    double pixelRatio = height / (max - min);
    return (height - y) / pixelRatio + min;
  }
  
  public String getFormattedValue(int y, int height)
  {
    if (max >= 1000)
    {
      pf.setMinimumFractionDigits(0);
      pf.setMaximumFractionDigits(0);
    }
    else if (max >= 10)
    {
      pf.setMinimumFractionDigits(2);
      pf.setMaximumFractionDigits(2);
    }
    else
    {
      pf.setMinimumFractionDigits(4);
      pf.setMaximumFractionDigits(4);
    }
    return pf.format(getValue(y, height));
  }
}
