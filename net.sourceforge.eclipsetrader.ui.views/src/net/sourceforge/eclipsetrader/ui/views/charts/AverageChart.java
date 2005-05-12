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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Moving Average chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class AverageChart extends ChartPlotter implements IChartConfigurer
{
  public static final int SIMPLE = 0;
  public static final int EXPONENTIAL = 1;
  public static final int WEIGHTED = 2;
  public static final int WILDER = 3;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.average"; //$NON-NLS-1$
  private int period = 7;
  private int type = SIMPLE;
  
  public AverageChart()
  {
    setName(Messages.getString("AverageChart.label")); //$NON-NLS-1$
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

      // Tipo di line e colore
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(getColor());
      
      List list = getMA(chartData, type, period);
      if (list != null)
        drawLine(list, gc, height, chartData.length - list.size());
    }
  }
  
  public static List getSMA(IChartData[] data, int period)
  {
    List list = new ArrayList();
    
    if (period >= 1 && period < data.length)
    {
      // create the circular buffer and its running total
      double[] values = new double[period];
      double total = 0.0;
      
      // fill buffer first time around, keeping its running total
      int loop = -1;
      while (++loop < period) 
      {
        double val = data[loop].getClosePrice();
        total += val;
        values[loop] = val;
      }

      // buffer filled with first period values, output first sma value
      list.add(new Double(total / period));

      // loop over the rest, each time replacing oldest value in buffer
      --loop;
      while (++loop < data.length) 
      {
        int index = loop % period;
        double newval = data[loop].getClosePrice();
        
        total += newval;
        total -= values[index];
        values[index] = newval;

        list.add(new Double(total / period));
      }
    }
    
    return list;
  }
  
  public static List getEMA(IChartData[] data, int period)
  {
    List list = new ArrayList();
    
    if (period >= 1 && period < data.length)
    {
      double smoother = 2.0 / (period + 1);

      double t = 0;
      int loop;
      for (loop = 0; loop < period; loop++)
        t = t + data[loop].getClosePrice();

      double yesterday = t / period;
      list.add(new Double(yesterday));

      for (; loop < data.length; loop++)
      {
        t = (smoother * (data[loop].getClosePrice() - yesterday)) + yesterday;
        yesterday = t;
        list.add(new Double(t));
      }

    }
    
    return list;
  }
  
  public static List getWMA(IChartData[] data, int period)
  {
    List list = new ArrayList();
    
    if (period >= 1 && period < data.length)
    {
      int loop;
      for (loop = period - 1; loop < (int) data.length; loop++)
      {
        int loop2;
        int weight;
        int divider;
        double total;
        for (loop2 = period - 1, weight = 1, divider = 0, total = 0; loop2 >= 0; loop2--, weight++)
        {
          total = total + (data[loop - loop2].getClosePrice() * weight);
          divider = divider + weight;
        }

        list.add(new Double(total / divider));
      }
    }
    
    return list;
  }
  
  public static List getWilderMA(IChartData[] data, int period)
  {
    List list = new ArrayList();
    
    if (period >= 1 && period < data.length)
    {
      double t = 0;
      int loop;
      for (loop = 0; loop < period; loop++)
        t = t + data[loop].getClosePrice();

      double yesterday = t / period;

      list.add(new Double(yesterday));

      for (; loop < data.length; loop++)
      {
        t  = (yesterday * (period - 1) + data[loop].getClosePrice()) / period;
        yesterday = t;
        list.add(new Double(t));
      }
    }
    
    return list;
  }
  
  public static List getMA(IChartData[] data, int type, int period)
  {
    switch(type)
    {
      case SIMPLE:
        return getSMA(data, period);
      case EXPONENTIAL:
        return getEMA(data, period);
      case WEIGHTED:
        return getWMA(data, period);
      case WILDER:
        return getWilderMA(data, period);
    }
    return null;
  }

  public static List getMA(List list, int type, int period)
  {
    IChartData data[] = new IChartData[list.size()];
    for (int i = 0; i < data.length; i++)
    {
      data[i] = new ChartData();
      data[i].setClosePrice(((Double)list.get(i)).doubleValue());
    }

    switch(type)
    {
      case SIMPLE:
        return getSMA(data, period);
      case EXPONENTIAL:
        return getEMA(data, period);
      case WEIGHTED:
        return getWMA(data, period);
      case WILDER:
        return getWilderMA(data, period);
    }
    return null;
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
    label.setText(Messages.getString("AverageChart.periods")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));
    
    addParameters(parent, Messages.getString("AverageChart.type"), "type", type);

    return parent;
  }
  
  public static void addParameters(Composite parent, String text, String param, int type)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(text);
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData(param);
    combo.add(Messages.getString("AverageChart.simple")); //$NON-NLS-1$
    combo.add(Messages.getString("AverageChart.exponential")); //$NON-NLS-1$
    combo.add(Messages.getString("AverageChart.weighted")); //$NON-NLS-1$
    combo.add(Messages.getString("AverageChart.william")); //$NON-NLS-1$
    combo.setText(combo.getItem(type));
  }
}
