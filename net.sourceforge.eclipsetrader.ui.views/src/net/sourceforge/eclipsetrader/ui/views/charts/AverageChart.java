/*
 * Created on 29-ago-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.eclipsetrader.ui.views.charts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AverageChart extends ChartPainter
{
  private int period = 7;
  
  public AverageChart(int period)
  {
    this.period = period;
  }
  
  public AverageChart(int period, Color color)
  {
    this.period = period;
    this.lineColor = color;
  }
  
  public void setPeriod(int period)
  {
    this.period = period;
  }
  
  public void paintChart(GC gc, int width, int height)
  {
    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      pixelRatio = (height) / (max - min);

      // Tipo di line e colore
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(lineColor);

      // Computa i punti
      if (data.length >= period)
      {
        double[] value = new double[data.length - period];
        for (int i = 0; i < value.length; i++)
        {
          for (int m = 0; m < period; m++)
            value[i] += data[i + m].getClosePrice();
          value[i] /= period;
        }
        this.drawLine(value, gc, height, period);
      }
    }
  }

  public void paintScale(GC gc, int width, int height)
  {
  }
}
