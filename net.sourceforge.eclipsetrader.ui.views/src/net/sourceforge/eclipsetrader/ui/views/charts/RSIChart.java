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
 * Relative Strength Index
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class RSIChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.rsi"; //$NON-NLS-1$
  private int period = 14;
  private Color gridColor = new Color(null, 192, 192, 192);
  
  public RSIChart()
  {
    setName(Messages.getString("RSIChart.label")); //$NON-NLS-1$
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
    return getName() + " (" + period + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
      setMinMax(-5, 105);
      double pixelRatio = (height) / (getMax() - getMin());

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = (int)((70 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = (int)((30 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);

      // Computa i punti
      if (chartData.length > period)
      {
        // RSI
        double[] value = new double[chartData.length - period];
        for (int i = 0; i < value.length; i++)
        {
          double gains = 0;
          double losses = 0;
          for (int m = 1; m <= period; m++)
          {
            double close1 = chartData[i + m - 1].getClosePrice();
            double close2 = chartData[i + m].getClosePrice();
            if (close2 > close1)
              gains += close2 - close1;
            else
              losses += close1 - close2;
          }
          double averageGain = gains / period;
          double averageLoss = losses / period;
          value[i] = 100 - (100 / (1 + (averageGain / averageLoss)));
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(getColor());
        drawLine(value, gc, height, period);
      }
    }

    // Tipo di linea e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(getColor());
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
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("RSIChart.periods")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    return parent;
  }
}
