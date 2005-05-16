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

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Moving Average Convergence Divergence
 */
public class MACDChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.macd"; //$NON-NLS-1$
  private int fastPeriod = 12;
  private int slowPeriod = 26;
  private int type = AverageChart.EXPONENTIAL;
  private int trigPeriod = 9;
  private Color trigColor = new Color(null, 192, 192, 0);
  private Color oscColor = new Color(null, 192, 0, 0);
  private ColorSelector trigColorSelector;
  private ColorSelector oscColorSelector;
  private List macd = new ArrayList();
  private List osc = new ArrayList();
  private List signal = new ArrayList();
  
  public MACDChart()
  {
    setName(Messages.getString("MACDChart.label")); //$NON-NLS-1$
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
    osc = new ArrayList();
    signal = new ArrayList();

    if (data != null && data.length != 0)
    {
      List fma = AverageChart.getMA(chartData, type, fastPeriod);
      int fmaLoop = fma.size() - 1;
      
      List sma = AverageChart.getMA(chartData, type, slowPeriod);
      int smaLoop = sma.size() - 1;
      
      double min = 0, max = 0;
      while (fmaLoop > -1 && smaLoop > -1)
      {
        double t = ((Double)fma.get(fmaLoop)).doubleValue() - ((Double)sma.get(smaLoop)).doubleValue();
        macd.add(0, new Double(t));
        fmaLoop--;
        smaLoop--;
        if (min == 0 || t < min)
          min = t;
        if (t > max)
          max = t;
      }
      setMinMax(macd);

      signal = AverageChart.getMA(macd, type, trigPeriod);
      updateMinMax(signal);

      double omin = 0, omax = 0;
      int floop = macd.size() - 1;
      int sloop = signal.size() - 1;
      while (floop > -1 && sloop > -1)
      {
        double t = ((Double)macd.get(floop)).doubleValue() - ((Double)signal.get(sloop)).doubleValue();
        osc.add(0, new Double(t));
        floop--;
        sloop--;
        if (omin == 0 || t < omin)
          omin = t;
        if (t > omax)
          omax = t;
      }
      updateMinMax(osc);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);

    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(oscColor);
    gc.setBackground(oscColor);
    drawOscillatorLine(osc, gc, height, chartData.length - osc.size());
    
    gc.setForeground(getColor());
    drawLine(macd, gc, height, chartData.length - macd.size());
    gc.setForeground(trigColor);
    drawLine(signal, gc, height, chartData.length - signal.size());
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
    else if (name.equalsIgnoreCase("slowPeriod") == true) //$NON-NLS-1$
      slowPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("trigPeriod") == true) //$NON-NLS-1$
      trigPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
    {
      type = Integer.parseInt(value);
      if (trigColorSelector != null)
        trigColor = new Color(null, trigColorSelector.getColorValue());
      if (oscColorSelector != null)
        oscColor = new Color(null, oscColorSelector.getColorValue());
    }
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MACDChart.fastPeriod")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("fastPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(fastPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MACDChart.slowPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("slowPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(slowPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MACDChart.signalPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("trigPeriod"); //$NON-NLS-1$
    text.setText(String.valueOf(trigPeriod));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("MACDChart.averageType"), "type", type); //$NON-NLS-1$ //$NON-NLS-2$
    
    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MACDChart.signalColor")); //$NON-NLS-1$
    trigColorSelector = new ColorSelector(parent);
    trigColorSelector.setColorValue(trigColor.getRGB());
    trigColorSelector.getButton().setData("trigColor"); //$NON-NLS-1$
    
    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("MACDChart.oscillatorColor")); //$NON-NLS-1$
    oscColorSelector = new ColorSelector(parent);
    oscColorSelector.setColorValue(oscColor.getRGB());
    oscColorSelector.getButton().setData("oscColor"); //$NON-NLS-1$

    return parent;
  }
}
