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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Moving Average Oscillator
 */
public class MAOSCChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.maosc"; //$NON-NLS-1$
  private int fastPeriod = 9;
  private int slowPeriod = 18;
  private int fastType = AverageChart.EXPONENTIAL;
  private int slowType = AverageChart.EXPONENTIAL;
  private List macd = new ArrayList();
  
  public MAOSCChart()
  {
    setName(Messages.getString("MAOSCChart.label")); //$NON-NLS-1$
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
    
    macd = new ArrayList();

    if (data != null && data.length != 0)
    {
      List fma = AverageChart.getMA(chartData, fastType, fastPeriod);
      int fmaLoop = fma.size() - 1;
      
      List sma = AverageChart.getMA(chartData, slowType, slowPeriod);
      int smaLoop = sma.size() - 1;
      
      while (fmaLoop > -1 && smaLoop > -1)
      {
        double t = ((Double)fma.get(fmaLoop)).doubleValue() - ((Double)sma.get(smaLoop)).doubleValue();
        macd.add(0, new Double(t));
        fmaLoop--;
        smaLoop--;
      }

      setMinMax(macd);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);

    gc.setForeground(getColor());
    gc.setBackground(getColor());
    drawOscillatorLine(macd, gc, height, chartData.length - macd.size());

    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(getColor());
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  public void drawOscillatorLine(List value, GC gc, int height, int ofs)
  {
    double pixelRatio = height / (getMax() - getMin());
    int[] pointArray = new int[value.size() * 2 + 4];
    int x = chartMargin + getColumnWidth() / 2 + ofs * getColumnWidth();
    int pa = 0;
    for (int i = 0; i < value.size(); i++, x += getColumnWidth())
    {
      pointArray[pa++] = x;
      int y = (int)((((Double)value.get(i)).doubleValue() - getMin()) * pixelRatio);
      pointArray[pa++] = height - y;
    }

    int y1 = height - (int)((0 - getMin()) * pixelRatio);
    pointArray[pa++] = x - getColumnWidth();
    pointArray[pa++] = y1;
    pointArray[pa++] = chartMargin + getColumnWidth() / 2 + ofs * getColumnWidth();
    pointArray[pa++] = y1;

    gc.drawLine(chartMargin + getColumnWidth() / 2 + ofs * getColumnWidth(), y1, x - getColumnWidth(), y1);
    gc.fillPolygon(pointArray);
    
    if (isSelected() == true)
      drawSelectionMarkers(pointArray, gc);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("fastPeriod") == true) //$NON-NLS-1$
      fastPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("fastType") == true) //$NON-NLS-1$
      fastType = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("slowPeriod") == true) //$NON-NLS-1$
      slowPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("slowType") == true) //$NON-NLS-1$
      slowType = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MAOSCChart.fastPeriod")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("fastPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(fastPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("MAOSCChart.fastAverageType"), "fastType", fastType); //$NON-NLS-1$ //$NON-NLS-2$

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MAOSCChart.slowPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("slowPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(slowPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("MAOSCChart.slowAverageType"), "slowType", slowType); //$NON-NLS-1$ //$NON-NLS-2$

    return parent;
  }
}
