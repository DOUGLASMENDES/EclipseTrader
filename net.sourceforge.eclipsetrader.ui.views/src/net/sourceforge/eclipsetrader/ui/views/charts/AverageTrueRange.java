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
  private List list = new ArrayList();
  
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
    return getName();
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    super.setData(data);
    
    list = new ArrayList();
    if (data != null && data.length != 0)
    {
      int loop;
      for (loop = 0; loop < data.length; loop++)
      {
        double high = data[loop].getMaxPrice();
        double low = data[loop].getMinPrice();
        double close;
        if (loop > 0)
          close = data[loop - 1].getClosePrice();
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
      }
      
      if (period > 1)
        list = AverageChart.getMA(list, type, period);

      setMinMax(list);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    drawLine(list, gc, height);
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
