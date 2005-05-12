/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stefan S. Stratigakos - Original Qtstalker code
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.List;

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
  private int smoothing = 10;
  private int type = AverageChart.EXPONENTIAL;
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
      setMinMax(-2, 102);
      double pixelRatio = (height) / (getMax() - getMin());

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = (int)((70 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = (int)((30 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      
      List rsi = new ArrayList();

      int loop;
      for (loop = period; loop < chartData.length; loop++)
      {
        double loss = 0;
        double gain = 0;
        int loop2;
        for (loop2 = 0; loop2 < period; loop2++)
        {
          double t = chartData[loop - loop2].getClosePrice() - chartData[loop - loop2 - 1].getClosePrice();
          if (t > 0)
            gain = gain + t;
          if (t < 0)
            loss = loss + Math.abs(t);
        }

        double again = gain / period;
        double aloss = loss / period;
        double rs = again / aloss;
        double t = 100 - (100 / (1 + rs));
        if (t > 100)
          t = 100;
        if (t < 0)
          t = 0;

        rsi.add(new Double(t));
      }
      
      if (smoothing > 1)
        rsi = AverageChart.getMA(rsi, type, smoothing);

      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(getColor());
      drawLine(rsi, gc, height, chartData.length - rsi.size());
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
    else if (name.equalsIgnoreCase("smoothing") == true) //$NON-NLS-1$
      smoothing = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
      type = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("RSIChart.periods")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("RSIChart.smoothingPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("smoothing"); //$NON-NLS-1$
    text.setText(String.valueOf(smoothing));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("RSIChart.smoothingType"), "type", type); //$NON-NLS-1$ //$NON-NLS-2$

    return parent;
  }
}
