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
 */
public class BollingerBandsChart extends ChartPainter implements IChartPlotter
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.bollinger";
  private int period = 15;
  private int deviations = 2;
  
  public BollingerBandsChart()
  {
    name = "Bande di Bollinger";
  }
  
  public void paintChart(GC gc, int width, int height)
  {
    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      pixelRatio = (height) / (max - min);

      // Colore della linea
      gc.setForeground(lineColor);

      // Computa i punti
      if (data.length >= period)
      {
        // Calcola la media mobile
        double[] average = new double[data.length - period];
        for (int i = 0; i < average.length; i++)
        {
          for (int m = 0; m < period; m++)
            average[i] += data[i + m].getClosePrice();
          average[i] /= period;
        }
        
        // Calcola la deviazione standard
        double[] deviation = new double[data.length - period];
        for (int i = 0; i < deviation.length; i++)
        {
          for (int m = 0; m < period; m++)
            deviation[i] += Math.pow(data[i + m].getClosePrice() - average[i], 2);
          deviation[i] /= period;
          deviation[i] = Math.sqrt(deviation[i]);
        }
        
        // Calcola la banda superiore
        double[] value = new double[data.length - period];
        for (int i = 0; i < value.length; i++)
          value[i] = average[i] + deviation[2] * deviations;
        gc.setLineStyle(SWT.LINE_SOLID);
        this.drawLine(value, gc, height, period);

        // Banda intermedia (media mobile)
        gc.setLineStyle(SWT.LINE_DOT);
        this.drawLine(average, gc, height, period);
        
        // Calcola la banda inferiore
        for (int i = 0; i < value.length; i++)
          value[i] = average[i] - deviation[2] * deviations;
        gc.setLineStyle(SWT.LINE_SOLID);
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
    return PLUGIN_ID;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return name + " (" + period + "," + deviations + ")";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameters()
   */
  public ChartParametersDialog showParametersDialog()
  {
    BollingerBandsChartDialog dlg = new BollingerBandsChartDialog();
    dlg.setName(name);
    dlg.setPeriod(period);
    dlg.setDeviations(deviations);
    dlg.setColor(lineColor.getRGB());
    if (dlg.open() == AverageChartDialog.OK)
    {
      name = dlg.getName();
      period = dlg.getPeriod();
      params.put("period", String.valueOf(period));
      deviations = dlg.getDeviations();
      params.put("deviations", String.valueOf(deviations));
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
    if (name.equalsIgnoreCase("deviations") == true)
      deviations = Integer.parseInt(value);
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
