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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * Price chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class PriceChart extends ChartPlotter
{
  private final static int CLOSE = 1;
  private final static int OPEN = 2;
  private final static int MAX = 3;
  private final static int MIN = 4;
  public final static int LINE = 1;
  public final static int CANDLE = 2;
  public final static int BAR = 3;
  private int dataSource = CLOSE;
  private int type = LINE;
  protected Color gridColor = new Color(null, 192, 192, 192);
  protected Color separatorColor = new Color(null, 255, 0, 0);
  protected Color textColor = new Color(null, 0, 0, 0);
  protected Color positiveColor = new Color(null, 0, 192, 0);
  protected Color negativeColor = new Color(null, 192, 0, 0);
  protected Color neutralColor = new Color(null, 128, 128, 128);
  private Color chartBackground = new Color(Display.getCurrent(), 255, 255, 240);
  private NumberFormat nf = NumberFormat.getInstance();
  
  public PriceChart()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return "price";
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    if (chartData != null && getMax() > getMin())
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = height / (getMax() - getMin());

      // Tipo di linea
      gc.setLineStyle(SWT.LINE_SOLID);

      if (type == LINE)
      {
        // Computa i punti
        double[] value = new double[chartData.length];
        switch(dataSource)
        {
          case OPEN:
            for (int i = 0; i < value.length; i++)
              value[i] = chartData[i].getOpenPrice();
            break;
          case CLOSE:
            for (int i = 0; i < value.length; i++)
              value[i] = chartData[i].getClosePrice();
            break;
          case MAX:
            for (int i = 0; i < value.length; i++)
              value[i] = chartData[i].getMaxPrice();
            break;
          case MIN:
            for (int i = 0; i < value.length; i++)
              value[i] = chartData[i].getMinPrice();
            break;
        }
        
        // Disegna il grafico
        gc.setForeground(getColor());
        drawLine(value, gc, height);
      }
      else if (type == CANDLE)
      {
        gc.setForeground(textColor);

        int x = getColumnWidth() / 2;
        for (int i = 0; i < chartData.length; i++, x += getColumnWidth())
        {
          int y1 = height - (int)((chartData[i].getMaxPrice() - getMin()) * pixelRatio);
          int y2 = height - (int)((chartData[i].getMinPrice() - getMin()) * pixelRatio);
          gc.drawLine(x, y1, x, y2);
          
          y1 = height - (int)((chartData[i].getOpenPrice() - getMin()) * pixelRatio);
          y2 = height - (int)((chartData[i].getClosePrice() - getMin()) * pixelRatio);
          if (y1 > y2)
          {
            gc.setBackground(chartBackground);
            gc.fillRectangle(x - 2, y2, 5, y1 - y2);
            if (y1 == y2)
              gc.drawRectangle(x - 2, y2, 4, y1 - y2);
            else
              gc.drawRectangle(x - 2, y2, 4, y1 - y2 - 1);
          }
          else
          {
            gc.setBackground(textColor);
            gc.fillRectangle(x - 2, y1, 5, y2 - y1 + 1);
          }
        }
      }
      else if (type == BAR)
      {
        int x = getColumnWidth() / 2;
        for (int i = 0; i < chartData.length; i++, x += getColumnWidth())
        {
          int y1 = height - (int)((chartData[i].getMaxPrice() - getMin()) * pixelRatio);
          int y2 = height - (int)((chartData[i].getMinPrice() - getMin()) * pixelRatio);
          if (i > 0 && chartData[i].getOpenPrice() < chartData[i - 1].getClosePrice())
            gc.setForeground(neutralColor);
          else if (chartData[i].getClosePrice() >= chartData[i].getOpenPrice())
            gc.setForeground(positiveColor);
          else
            gc.setForeground(negativeColor);
          gc.drawLine(x, y1, x, y2);
          y1 = height - (int)((chartData[i].getOpenPrice() - getMin()) * pixelRatio);
          gc.drawLine(x - 2, y1, x, y1);
          y1 = height - (int)((chartData[i].getClosePrice() - getMin()) * pixelRatio);
          gc.drawLine(x, y1, x + 2, y1);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
    if (chartData != null && getMax() > getMin())
    {
      if (chartData[0].getMaxPrice() >= 10)
      {
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
      }
      else
      {
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
      }

      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = height / (getMax() - getMin());

      gc.setForeground(textColor);
      gc.setLineStyle(SWT.LINE_SOLID);
      
      double midPrice = roundToTick((getMax() - getMin()) / 2 + getMin());
      int y1 = height - (int)((midPrice - getMin()) * pixelRatio);
      gc.drawLine(1, y1, 5, y1);
      String s = nf.format(midPrice);
      gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
  
      double step = getPriceTick(midPrice) * 2;
      while((getMax() - getMin()) / step > 5)
        step += getPriceTick(midPrice);
      for (int i = 1; i <= 2; i++)
      {
        y1 = height - (int)((midPrice + step * i - getMin()) * pixelRatio);
        gc.drawLine(1, y1, 5, y1);
        s = nf.format(midPrice + step * i);
        gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
        y1 = height - (int)((midPrice - step * i - getMin()) * pixelRatio);
        if (y1 < height)
        {
          gc.drawLine(1, y1, 5, y1);
          s = nf.format(midPrice - step * i);
          gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintGrid(org.eclipse.swt.graphics.GC, int, int)
   */
  public void paintGrid(GC gc, int width, int height)
  {
    double pixelRatio = height / (getMax() - getMin());

    gc.setForeground(gridColor);
    gc.setLineStyle(SWT.LINE_DOT);
    
    double midPrice = roundToTick((getMax() - getMin()) / 2 + getMin());
    int y1 = height - (int)((midPrice - getMin()) * pixelRatio);
    gc.drawLine(0, y1, width, y1);
    
    double step = getPriceTick(midPrice) * 2;
    while((getMax() - getMin()) / step > 5)
      step += getPriceTick(midPrice);
    for (int i = 1; i <= 2; i++)
    {
      y1 = height - (int)((midPrice + step * i - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = height - (int)((midPrice - step * i - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("type") == true)
      type = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getValue(int, int)
   */
  public double getValue(int y, int height)
  {
    return roundToTick(super.getValue(y, height));
  }

  /**
   * Rounds the given price to the nearest tick.<br>
   * 
   * @param price The price value
   * @return The rounded price
   */
  public static double roundToTick(double price)
  {
    double tick = getPriceTick(price);
    return ((int)((price + tick / 2) / tick)) * tick;
  }
  
  /**
   * Get the price tick related to the passed as argument.<br>
   * 
   * @param price The price value
   * @return The price tick 
   */
  public static double getPriceTick(double price) 
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
