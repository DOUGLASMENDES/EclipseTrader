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
 * Elder's Market Thermometer - THERM
 * <p></p>
 */
public class MarketThermometerChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.therm"; //$NON-NLS-1$
  private int threshold = 3;
  private int smoothing = 2;
  private int maPeriod = 22;
  private int maType = AverageChart.SIMPLE;
  private int smoothType = AverageChart.SIMPLE;
  private Color downColor = new Color(null, 0, 192, 0);
  private Color upColor = new Color(null, 192, 0, 192);
  private Color threshColor = new Color(null, 192, 0, 0);
  private List therm = new ArrayList();
  private List therm_ma = new ArrayList();
  
  public MarketThermometerChart()
  {
    setName(Messages.getString("MarketThermometerChart.label")); //$NON-NLS-1$
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
    return getName() + " (" + threshold + ", " + smoothing + ", " + maPeriod + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    super.setData(data);
    if (data != null)
    {
      therm = new ArrayList();
      int loop;
      double thermometer = 0;
      for (loop = 1; loop < chartData.length; loop++)
      {
        double high = Math.abs(chartData[loop].getMaxPrice() - chartData[loop - 1].getMaxPrice());
        double lo = Math.abs(chartData[loop - 1].getMinPrice() - chartData[loop].getMinPrice());
        
        if (high > lo)
          thermometer = high;
        else
          thermometer = lo;

        therm.add(0, new Double(thermometer));
      }
      setMinMax(therm);
      if (smoothing > 1)
        therm = AverageChart.getMA(therm, smoothType, smoothing);
      
      therm_ma = AverageChart.getMA(therm, maType, maPeriod);
      updateMinMax(therm_ma);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);

    // Draw the thermometer bar chart
    int ofs = chartData.length - therm.size();
    double pixelRatio = height / (getMax() - getMin());
    int x = chartMargin + getColumnWidth() / 2 + chartData.length * getColumnWidth();

    int thermLoop = therm.size() - 1;
    int maLoop = therm_ma.size() - 1;
    while (thermLoop > -1)
    {
      double thrm = ((Double)therm.get(thermLoop)).doubleValue();
      if (maLoop > -1)
      {
        double thrmma = ((Double)therm_ma.get(maLoop)).doubleValue();

        if (thrm > (thrmma * threshold))
          gc.setForeground(threshColor);
        else
        {
          if (thrm > thrmma)
            gc.setForeground(upColor);
          else
            gc.setForeground(downColor);
        }
      }
      else
        gc.setForeground(downColor);

      int y1 = height - (int)((thrm - getMin()) * pixelRatio);
      int y2 = height - (int)((0 - getMin()) * pixelRatio);
      gc.drawLine(x, y1, x, y2);

      thermLoop--;
      maLoop--;
      x -= getColumnWidth();
    }

    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(getColor());
    drawLine(therm_ma, gc, height);
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
    if (name.equalsIgnoreCase("threshold") == true) //$NON-NLS-1$
      threshold = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("smoothing") == true) //$NON-NLS-1$
      smoothing = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("maPeriod") == true) //$NON-NLS-1$
      maPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("maType") == true) //$NON-NLS-1$
      maType = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("smoothType") == true) //$NON-NLS-1$
      smoothType = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MarketThermometerChart.threshold")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("threshold"); //$NON-NLS-1$
    text.setText(String.valueOf(threshold));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MarketThermometerChart.period")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("maPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(maPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("MarketThermometerChart.maType"), "maType", maType); //$NON-NLS-1$ //$NON-NLS-2$

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MarketThermometerChart.smoothingPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("smoothing"); //$NON-NLS-1$
    text.setText(String.valueOf(smoothing));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("MarketThermometerChart.smoothingType"), "smoothType", smoothType); //$NON-NLS-1$ //$NON-NLS-2$

    return parent;
  }
}
