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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ChartPainter
{
  protected String name = "";
  protected Color gridColor = new Color(null, 192, 192, 192);
  protected Color lineColor = new Color(null, 0, 0, 255);
  protected Color textColor = new Color(null, 0, 0, 0);
  protected Color separatorColor = new Color(null, 255, 0, 0);
  protected int columnWidth = 5;
  protected int chartMargin = 2;
  protected int scaleWidth = 60;
  protected IChartData[] data;
  protected double min = 0;
  protected double max = 0;
  protected double pixelRatio = 0;
  protected boolean visible = true;
  
  public ChartPainter()
  {
  }
  
  public void setData(IChartData[] data)
  {
    this.data = data;
    
    if (data != null)
    {
      // Determina massimo e minimo
      min = max = 0;
      for (int i = 0; i < data.length; i++)
      {
        if (data[i].getMaxPrice() > max)
          max = data[i].getMaxPrice();
        if (min == 0 || data[i].getMinPrice() < min)
          min = data[i].getMinPrice();
      }
    }
  }
  
  public void setVisible(boolean state)
  {
    this.visible = state;
  }
  
  public void setLineColor(Color c)
  {
    lineColor = c;
  }
  
  public void setTextColor(Color c)
  {
    textColor = c;
  }
  
  public void setSeparatorColor(Color c)
  {
    separatorColor = c;
  }
  
  public abstract void paintChart(GC gc, int width, int height);
  
  public abstract void paintScale(GC gc, int width, int height);
  
  public void setScaleWidth(int scaleWidth)
  {
    this.scaleWidth = scaleWidth;
  }
  public int getScaleWidth()
  {
    return scaleWidth;
  }
  
  public void setColumnWidth(int width)
  {
    this.columnWidth = width;
  }
  public int getColumnWidth()
  {
    return columnWidth;
  }
  
  public void setChartMargin(int margin)
  {
    this.chartMargin = margin;
  }
  public int getChartMargin()
  {
    return chartMargin;
  }

  /**
   * Method to return the name field.<br>
   *
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }
  /**
   * Method to set the name field.<br>
   * 
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  public void drawLine(double[] value, GC gc, int height)
  {
    int[] pointArray = new int[value.length * 2];
    int x = columnWidth / 2;
    for (int i = 0, pa = 0; i < value.length; i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      int y = (int)((value[i] - min) * pixelRatio);
      pointArray[pa++] = height - y;
    }
    // Traccia la linea
    gc.drawPolyline(pointArray);
  }
  
  public void drawLine(double[] value, GC gc, int height, int ofs)
  {
    int[] pointArray = new int[value.length * 2];
    int x = chartMargin + columnWidth / 2 + ofs * columnWidth;
    for (int i = 0, pa = 0; i < value.length; i++, x += columnWidth)
    {
      pointArray[pa++] = x;
      int y = (int)((value[i] - min) * pixelRatio);
      pointArray[pa++] = height - y;
    }
    // Traccia la linea
    gc.drawPolyline(pointArray);
  }

  /**
   * Rounds the given price to the nearest tick.<br>
   * 
   * @param price The price value
   * @return The rounded price
   */
  public double roundToTick(double price)
  {
    double tick = getPriceTick(price);
    return ((int)(price / tick)) * tick;
  }
  
  /**
   * Get the price tick related to the passed as argument.<br>
   * 
   * @param price The price value
   * @return The price tick 
   */
  public double getPriceTick(double price) 
  {
    if (price <= 0.3)
      return 0.0005;
    else if (price <= 1.5)
      return 0.001;
    else if (price <= 3)
      return 0.005;
    else if (price <= 30)
      return 0.01;
    else
      return 0.05;
  }
}
