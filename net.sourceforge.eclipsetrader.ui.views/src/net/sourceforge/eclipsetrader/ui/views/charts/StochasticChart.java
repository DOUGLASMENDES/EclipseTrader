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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StochasticChart extends ChartPainter implements IChartPlotter
{
  private int period = 14;
  private int subperiod = 3;
  private HashMap params = new HashMap();
  
  public StochasticChart()
  {
    name = "Stochastic";
  }
  
  public void setPeriod(int period)
  {
    this.period = period;
  }
  
  public void paintChart(GC gc, int width, int height)
  {
    // Grafico
    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      max = 105;
      min = -5;
      pixelRatio = (height) / (max - min);

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = (int)((70 - min) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = (int)((30 - min) * pixelRatio);
      gc.drawLine(0, y1, width, y1);

      // Computa i punti
      if (data.length >= period)
      {
        // Indicatore stocastico
        double[] value = new double[data.length - period];
        for (int i = 0; i < value.length; i++)
        {
          double high = 0, low = 0, recent = 0;
          for (int m = 0; m < period; m++)
          {
            recent = data[i + m].getClosePrice();
            if (recent > high)
              high = recent;
            if (recent < low || low == 0)
              low = recent;
          }
          value[i] = 100 * ((recent - low) / (high - low));
        }
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(lineColor);
        drawLine(value, gc, height, period);
        
        // Media mobile dell'indicatore
        if (subperiod != 0)
        {
          double[] average = new double[value.length - subperiod];
          for (int i = 0; i < average.length; i++)
          {
            for (int m = 0; m < subperiod; m++)
              average[i] += value[i + m];
            average[i] /= subperiod;
          }
          gc.setLineStyle(SWT.LINE_DOT);
          gc.setForeground(lineColor);
          drawLine(average, gc, height, period);
        }
      }
    }

    // Tipo di linea e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
  }

  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return "net.sourceforge.eclipsetrader.charts.stochastic";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return name + " (" + period + ", " + subperiod + ")";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getParametersDialog()
   */
  public ChartParametersDialog showParametersDialog()
  {
    StochasticChartDialog dlg = new StochasticChartDialog();
    dlg.setName(name);
    dlg.setPeriod(period);
    dlg.setSubPeriod(subperiod);
    dlg.setColor(lineColor.getRGB());
    if (dlg.open() == AverageChartDialog.OK)
    {
      name = dlg.getName();
      period = dlg.getPeriod();
      params.put("period", String.valueOf(period));
      subperiod = dlg.getSubPeriod();
      params.put("subperiod", String.valueOf(subperiod));
      lineColor = new Color(null, dlg.getColor());
      params.put("color", String.valueOf(lineColor.getRed()) + "," + String.valueOf(lineColor.getGreen()) + "," + String.valueOf(lineColor.getBlue()));
    }
    return dlg;
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameters(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    params.put(name, value);
    if (name.equalsIgnoreCase("period") == true)
      period = Integer.parseInt(value);
    if (name.equalsIgnoreCase("subperiod") == true)
      subperiod = Integer.parseInt(value);
    if (name.equalsIgnoreCase("color") == true)
    {
      String[] values = value.split(",");
      lineColor = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getParameters()
   */
  public HashMap getParameters()
  {
    return params;
  }
}
