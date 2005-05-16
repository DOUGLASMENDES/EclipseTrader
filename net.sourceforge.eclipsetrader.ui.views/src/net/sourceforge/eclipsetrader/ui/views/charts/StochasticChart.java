/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.IChartData;
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
 */
public class StochasticChart extends ChartPlotter implements IChartConfigurer
{
  private static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.stochastic"; //$NON-NLS-1$
  private int period = 14;
  private int dperiod = 3;
  private int kperiod = 3;
  private int type = AverageChart.EXPONENTIAL;
  private int buyLine = 20;
  private int sellLine = 80;
  private Color gridColor = new Color(null, 192, 192, 192);
  private List k = new ArrayList();
  private List d = new ArrayList();
  
  public StochasticChart()
  {
    setName(Messages.getString("StochasticChart.label")); //$NON-NLS-1$
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
    return getName();
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    super.setData(data);

    k = new ArrayList();
    d = new ArrayList();

    if (data != null && data.length != 0)
    {
      setMinMax(-2, 102);

      k = new ArrayList();
      for (int loop = period; loop < chartData.length; loop++)
      {
        int loop2;
        double l;
        double h;
        for (loop2 = 0, l = 9999999, h = 0; loop2 < period; loop2++)
        {
          double high = chartData[loop - loop2].getMaxPrice();
          double low = chartData[loop - loop2].getMinPrice();

          double t = high;
          if (t > h)
            h = t;

          t = low;
          if (t < l)
            l = t;
        }

        double close = chartData[loop].getClosePrice();
        double t = ((close - l) / (h - l)) * 100;
        if (t > 100)
          t = 100;
        if (t < 0)
          t = 0;

        k.add(new Double(t));
      }

      if (kperiod > 1)
        k = AverageChart.getMA(k, type, kperiod);

      if (dperiod > 1)
        d = AverageChart.getMA(k, type, dperiod);;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    // Grafico
    super.paintChart(gc, width, height);
    if (chartData != null && getMax() > getMin())
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      setMinMax(-2, 102);
      double pixelRatio = (height) / (getMax() - getMin());
      
      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = height - (int)((buyLine - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = height - (int)((sellLine - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);

      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(getColor());
      drawLine(k, gc, height);

      gc.setLineStyle(SWT.LINE_DOT);
      gc.setForeground(getColor());
      drawLine(d, gc, height);
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
    else if (name.equalsIgnoreCase("kperiod") == true) //$NON-NLS-1$
      kperiod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("dperiod") == true) //$NON-NLS-1$
      dperiod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
      type = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("buyLine") == true) //$NON-NLS-1$
      buyLine = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("sellLine") == true) //$NON-NLS-1$
      sellLine = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.periods")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.kSmoothingPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("kperiod"); //$NON-NLS-1$
    text.setText(String.valueOf(kperiod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.dSmoothingPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("dperiod"); //$NON-NLS-1$
    text.setText(String.valueOf(dperiod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("StochasticChart.smoothingType"), "type", type); //$NON-NLS-1$ //$NON-NLS-2$

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.buySignal")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("buyLine"); //$NON-NLS-1$
    text.setText(String.valueOf(buyLine));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("StochasticChart.sellSignal")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("sellLine"); //$NON-NLS-1$
    text.setText(String.valueOf(sellLine));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    return parent;
  }
}
