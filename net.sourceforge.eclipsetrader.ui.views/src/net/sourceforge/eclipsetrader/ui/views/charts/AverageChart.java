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
public class AverageChart extends ChartPainter implements IChartPlotter
{
  private int period = 7;
  private HashMap params = new HashMap();
  
  public AverageChart()
  {
    name = "Media Mobile";
  }
  
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

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return "net.sourceforge.eclipsetrader.charts.average";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return name + " (" + period + ")";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#showParametersDialog()
   */
  public ChartParametersDialog showParametersDialog()
  {
    AverageChartDialog dlg = new AverageChartDialog();
    dlg.setPeriod(period);
    dlg.setName(name);
    dlg.setColor(lineColor.getRGB());
    if (dlg.open() == AverageChartDialog.OK)
    {
      name = dlg.getName();
      period = dlg.getPeriod();
      params.put("period", String.valueOf(period));
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
