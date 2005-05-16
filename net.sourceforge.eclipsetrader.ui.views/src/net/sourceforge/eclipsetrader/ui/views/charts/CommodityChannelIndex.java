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
public class CommodityChannelIndex extends ChartPlotter implements IChartConfigurer
{
  public static final int SIMPLE = 0;
  public static final int EXPONENTIAL = 1;
  public static final int WEIGHTED = 2;
  public static final int WILDER = 3;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.cci"; //$NON-NLS-1$
  private int period = 20;
  private int smoothing = 3;
  private int type = EXPONENTIAL;
  private Color gridColor = new Color(null, 192, 192, 192);
  private List cci = new ArrayList();
  
  public CommodityChannelIndex()
  {
    setName(Messages.getString("CommodityChannelIndex.label")); //$NON-NLS-1$
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

    cci = new ArrayList();
    if (data != null && data.length != 0)
    {
      List tp = new ArrayList();
      int loop;
      for (loop = 0; loop < chartData.length; loop++)
        tp.add(new Double((chartData[loop].getMaxPrice() + chartData[loop].getMinPrice() + chartData[loop].getClosePrice()) / 3));
      int tpLoop = tp.size() - 1;
      
      List sma = AverageChart.getMA(tp, type, period);
      int smaLoop = sma.size() - 1;
      
      while (tpLoop >= period && smaLoop >= period)
      {
        double md = 0;
        for (loop = 0; loop < period; loop++)
          md = md + Math.abs(((Double)tp.get(tpLoop - loop)).doubleValue() - ((Double)sma.get(smaLoop - loop)).doubleValue());
        md = md / period;

        double t = (((Double)tp.get(tpLoop)).doubleValue() - ((Double)sma.get(smaLoop)).doubleValue()) / (0.015 * md);
        cci.add(0, new Double(t));

        tpLoop--;
        smaLoop--;
      }
      
      if (smoothing > 1)
        cci = AverageChart.getMA(cci, type, smoothing);

      setMinMax(cci);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    drawLine(cci, gc, height);
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
    else if (name.equalsIgnoreCase("smoothing") == true) //$NON-NLS-1$
      smoothing = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("CommodityChannelIndex.period")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("CommodityChannelIndex.smoothingPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("smoothing"); //$NON-NLS-1$
    text.setText(String.valueOf(smoothing));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, Messages.getString("CommodityChannelIndex.smoothingType"), "type", type); //$NON-NLS-1$ //$NON-NLS-2$

    return parent;
  }
}
