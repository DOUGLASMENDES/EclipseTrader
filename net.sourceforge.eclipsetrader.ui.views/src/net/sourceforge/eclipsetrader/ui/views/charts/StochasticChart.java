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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Stochastic indicator
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class StochasticChart extends ChartPlotter implements IChartConfigurer
{
  private static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.stochastic"; //$NON-NLS-1$
  private int period = 14;
  private int subperiod = 3;
  private Color gridColor = new Color(null, 192, 192, 192);
  
  public StochasticChart()
  {
    name = Messages.getString("StochasticChart.label"); //$NON-NLS-1$
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
    return name + " (" + period + ", " + subperiod + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    // Grafico
    super.paintChart(gc, width, height);
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
      if (chartData.length >= period)
      {
        // Indicatore stocastico
        double[] value = new double[chartData.length - period];
        for (int i = 0; i < value.length; i++)
        {
          double high = 0, low = 0, recent = 0;
          for (int m = 0; m < period; m++)
          {
            recent = chartData[i + m].getClosePrice();
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
    if (name.equalsIgnoreCase("period") == true) //$NON-NLS-1$
      period = Integer.parseInt(value);
    if (name.equalsIgnoreCase("subperiod") == true) //$NON-NLS-1$
      subperiod = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.periods")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.averagePeriods")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text = new Text(parent, SWT.BORDER);
    text.setData("subperiod"); //$NON-NLS-1$
    text.setText(String.valueOf(subperiod));
    gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    return parent;
  }
}
