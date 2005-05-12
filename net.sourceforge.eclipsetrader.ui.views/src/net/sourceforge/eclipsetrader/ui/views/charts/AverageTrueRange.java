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

import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.internal.ChartData;
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
 * Average True Range.
 * <p></p>
 */
public class AverageTrueRange extends ChartPlotter implements IChartConfigurer
{
  public static final int SIMPLE = 0;
  public static final int EXPONENTIAL = 1;
  public static final int WEIGHTED = 2;
  public static final int WILDER = 3;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.atr"; //$NON-NLS-1$
  private int period = 7;
  private int type = SIMPLE;
  private Color gridColor = new Color(null, 192, 192, 192);
  
  public AverageTrueRange()
  {
    setName(Messages.getString("AverageTrueRange.label")); //$NON-NLS-1$
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
      double pixelRatio = height / (getMax() - getMin());

      // Grid line
      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      gc.drawLine(0, height / 2, width, height / 2);

      // Line type and color
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(getColor());
      
      double min = 0, max = 0;
      List list = new ArrayList();

      int loop;
      for (loop = 0; loop < chartData.length; loop++)
      {
        double high = chartData[loop].getMaxPrice();
        double low = chartData[loop].getMinPrice();
        double close;
        if (loop > 0)
          close = chartData[loop - 1].getClosePrice();
        else
          close = high;

        double t = high - low;

        double t2 = Math.abs(high - close);
        if (t2 > t)
          t = t2;

        t2 = Math.abs(low - close);
        if (t2 > t)
          t = t2;

        list.add(new Double(t));

        if (min == 0 || t < min)
          min = t;
        if (t > max)
          max = t;
      }
      
      if (period > 1)
      {
        IChartData values[] = new IChartData[list.size()];
        for (int i = 0; i < values.length; i++)
        {
          values[i] = new ChartData();
          values[i].setClosePrice(((Double)list.get(i)).doubleValue());
        }
        
        list = AverageChart.getMA(values, type, period);
      }

      double margin = (max - min) / 100 * 2; 
      max += margin;
      min -= margin;
      setMinMax(min, max);

      drawLine(list, gc, height, chartData.length - list.size());
    }
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
    label.setText("Smoothing Period");
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, "Smoothing Average Type", "type", type);

    return parent;
  }
}
