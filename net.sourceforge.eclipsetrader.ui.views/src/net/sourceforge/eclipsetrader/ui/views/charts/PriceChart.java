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
    if (chartData != null && max > min)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (height) / (max - min);

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      
      double midPrice = roundToTick((max - min) / 2 + min);
      int y1 = height - (int)((midPrice - min) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      
      double step = roundToTick(midPrice + (max - min) / 5) - midPrice;
      for (int i = 1; i <= 2; i++)
      {
        y1 = height - (int)((midPrice + step * i - min) * pixelRatio);
        gc.drawLine(0, y1, width, y1);
        y1 = height - (int)((midPrice - step * i - min) * pixelRatio);
        gc.drawLine(0, y1, width, y1);
      }

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
        gc.setForeground(lineColor);
        drawLine(value, gc, height);
      }
      else if (type == CANDLE)
      {
        gc.setForeground(textColor);

        int x = columnWidth / 2;
        for (int i = 0, pa = 0; i < chartData.length; i++, x += columnWidth)
        {
          y1 = height - (int)((chartData[i].getMaxPrice() - min) * pixelRatio);
          int y2 = height - (int)((chartData[i].getMinPrice() - min) * pixelRatio);
          gc.drawLine(x, y1, x, y2);
          
          y1 = height - (int)((chartData[i].getOpenPrice() - min) * pixelRatio);
          y2 = height - (int)((chartData[i].getClosePrice() - min) * pixelRatio);
          if (y1 > y2)
          {
            gc.setBackground(chartBackground);
            gc.fillRectangle(x - 1, y2, 4, y1 - y2);
            if (y1 == y2)
              gc.drawRectangle(x - 1, y2, 3, y1 - y2);
            else
              gc.drawRectangle(x - 1, y2, 3, y1 - y2 - 1);
          }
          else
          {
            gc.setBackground(textColor);
            gc.fillRectangle(x - 1, y1, 4, y2 - y1 + 1);
          }
        }
      }
      else if (type == BAR)
      {
        int x = columnWidth / 2;
        for (int i = 0, pa = 0; i < chartData.length; i++, x += columnWidth)
        {
          y1 = height - (int)((chartData[i].getMaxPrice() - min) * pixelRatio);
          int y2 = height - (int)((chartData[i].getMinPrice() - min) * pixelRatio);
          if (chartData[i].getClosePrice() >= chartData[i].getOpenPrice())
            gc.setForeground(positiveColor);
          else
            gc.setForeground(negativeColor);
          gc.drawLine(x, y1, x, y2);
          y1 = height - (int)((chartData[i].getOpenPrice() - min) * pixelRatio);
          gc.drawLine(x - 2, y1, x, y1);
          y1 = height - (int)((chartData[i].getClosePrice() - min) * pixelRatio);
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
    gc.setForeground(separatorColor);
    gc.drawLine(0, 0, 0, height);

    if (chartData != null && max > min)
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
      double pixelRatio = height / (max - min);

      gc.setForeground(textColor);
      gc.setLineStyle(SWT.LINE_SOLID);
      
      double midPrice = roundToTick((max - min) / 2 + min);
      int y1 = height - (int)((midPrice - min) * pixelRatio);
      gc.drawLine(1, y1, 5, y1);
      String s = nf.format(midPrice);
      gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
      
      double step = roundToTick(midPrice + (max - min) / 5) - midPrice;
      for (int i = 1; i <= 2; i++)
      {
        y1 = height - (int)((midPrice + step * i - min) * pixelRatio);
        gc.drawLine(1, y1, 5, y1);
        s = nf.format(midPrice + step * i);
        gc.drawString(s, 10, y1 - gc.stringExtent(s).y / 2 - 1);
        y1 = height - (int)((midPrice - step * i - min) * pixelRatio);
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
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("type") == true)
      type = Integer.parseInt(value);
    super.setParameter(name, value);
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
