/*
 * Created on 29-ago-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PriceChart extends ChartPainter
{
  private final static int CLOSE = 1;
  private final static int OPEN = 2;
  private final static int MAX = 3;
  private final static int MIN = 4;
  private int type = CLOSE;
  private NumberFormat nf = NumberFormat.getInstance();
  
  public PriceChart()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
  }
  
  public void paintChart(GC gc, int width, int height)
  {
    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      pixelRatio = (height) / (max - min);

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

      // Tipo di line e colore
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(lineColor);

      // Computa i punti
      double[] value = new double[data.length];
      switch(type)
      {
        case OPEN:
          for (int i = 0; i < value.length; i++)
            value[i] = data[i].getOpenPrice();
          break;
        case CLOSE:
          for (int i = 0; i < value.length; i++)
            value[i] = data[i].getClosePrice();
          break;
        case MAX:
          for (int i = 0; i < value.length; i++)
            value[i] = data[i].getMaxPrice();
          break;
        case MIN:
          for (int i = 0; i < value.length; i++)
            value[i] = data[i].getMinPrice();
          break;
      }
      
      // Disegna il grafico
      drawLine(value, gc, height);
    }
  }
  
  public void paintScale(GC gc, int width, int height)
  {
    gc.setForeground(separatorColor);
    gc.drawLine(0, 0, 0, height);

    if (data != null && max > min)
    {
      if (data[0].getMaxPrice() >= 10)
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
      pixelRatio = height / (max - min);

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
}
