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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Relative Strength Index
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class RSIChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.rsi";
  private int period = 14;
  private Color gridColor = new Color(null, 192, 192, 192);
  
  public RSIChart()
  {
    name = "RSI";
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
    return name + " (" + period + ")";
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    // Grafico
    if (chartData != null && max > min)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      max = 105;
      min = -5;
      double pixelRatio = (height) / (max - min);

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = (int)((70 - min) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = (int)((30 - min) * pixelRatio);
      gc.drawLine(0, y1, width, y1);

      // Computa i punti
      if (chartData.length > period)
      {
        // First period averages
        double gains = 0;
        double losses = 0;
        for (int m = 1; m <= period; m++)
        {
          if (chartData[m].getClosePrice() >= chartData[m - 1].getClosePrice())
            gains += chartData[m].getClosePrice() - chartData[m - 1].getClosePrice();
          else
            losses += chartData[m - 1].getClosePrice() - chartData[m].getClosePrice();;
        }
        double averageGain = gains / period;
        double averageLoss = losses / period;

        // RSI
        double[] value = new double[chartData.length - period];
        if (averageLoss == 0) value[0] = 100;
        else value[0] = 100 - (100 / (1 + (averageGain / averageLoss)));
        for (int i = 1; i < value.length; i++)
        {
          // Current gain/loss
          double currentGain = 0;
          double currentLoss = 0;
          if (chartData[i + period].getClosePrice() >= chartData[i + period - 1].getClosePrice())
            currentGain = chartData[i + period].getClosePrice() - chartData[i + period - 1].getClosePrice();
          else
            currentLoss = chartData[i + period - 1].getClosePrice() - chartData[i + period].getClosePrice();;
          // Smoothed RS
          averageGain = ((averageGain * (period - 1)) + currentGain) / period;
          averageLoss = ((averageLoss * (period - 1)) + currentLoss) / period; 
          if (averageLoss == 0) value[i] = 100;
          else value[i] = 100 - (100 / (1 + (averageGain / averageLoss)));
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(lineColor);
        drawLine(value, gc, height, period);
      }
    }

    // Tipo di linea e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("period") == true)
      period = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Selected Periods");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period");
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    return parent;
  }
}
