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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Elder's Safezone Stop - SZ
 * <p></p>
 */
public class SafezoneStop extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.sz"; //$NON-NLS-1$
  private static final int LONG = 0;
  private static final int SHORT = 1;
  private double coefficient = 2.5;
  private int period = 10;
  private int no_decline_period = 2;
  private int method = LONG;
  private List sz_uptrend = new ArrayList();
  private List sz_dntrend = new ArrayList();
  
  public SafezoneStop()
  {
    setName(Messages.getString("SafezoneStop.label")); //$NON-NLS-1$
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
    sz_uptrend = new ArrayList();
    sz_dntrend = new ArrayList();
    
    if (data != null)
    {
      if (period < 1)
        period = 1;
      
      double uptrend_stop = 0;
      double dntrend_stop = 0;

      if (no_decline_period < 0)
        no_decline_period = 0;
      if (no_decline_period > 365)
        no_decline_period = 365;

      double old_uptrend_stops[] = new double[no_decline_period];
      double old_dntrend_stops[] = new double[no_decline_period];

      int loop;
      for (loop = 0; loop < no_decline_period; loop++)
      {
        old_uptrend_stops[loop] = 0;
        old_dntrend_stops[loop] = 0;
      }

      int start = period + 1;
      for (loop = start; loop < chartData.length; loop++)
      {
        // calculate downside/upside penetration for lookback period
        int lbloop;
        int lbstart = loop - period;
        if (lbstart < 2)
          lbstart = 2;
        double uptrend_noise_avg = 0;
        double uptrend_noise_cnt = 0;
        double dntrend_noise_avg = 0;
        double dntrend_noise_cnt = 0;
        for (lbloop = lbstart; lbloop < loop; lbloop++)
        {
          double lo_curr = chartData[lbloop].getMinPrice();
          double lo_last = chartData[lbloop - 1].getMinPrice();
          double hi_curr = chartData[lbloop].getMaxPrice();
          double hi_last = chartData[lbloop - 1].getMaxPrice();
          if (lo_last > lo_curr)
          {
            uptrend_noise_avg += lo_last - lo_curr;
            uptrend_noise_cnt++;
          }
          if (hi_last < hi_curr)
          {
            dntrend_noise_avg += hi_curr - hi_last;
            dntrend_noise_cnt++;
          }
        }
        // make *_avg into actual averages
        if (uptrend_noise_cnt > 0)
          uptrend_noise_avg /= uptrend_noise_cnt;
        if (dntrend_noise_cnt > 0)
          dntrend_noise_avg /= dntrend_noise_cnt;

        double lo_last = chartData[loop - 1].getMinPrice();
        double hi_last = chartData[loop - 1].getMaxPrice();
        uptrend_stop = lo_last - coefficient * uptrend_noise_avg;
        dntrend_stop = hi_last + coefficient * dntrend_noise_avg;

        double adjusted_uptrend_stop = uptrend_stop;
        double adjusted_dntrend_stop = dntrend_stop;

        int backloop;
        for (backloop = no_decline_period - 1; backloop >= 0; backloop--)
        {
          if (loop - backloop > start)
          {
            if (old_uptrend_stops[backloop] > adjusted_uptrend_stop)
              adjusted_uptrend_stop = old_uptrend_stops[backloop];
            if (old_dntrend_stops[backloop] < adjusted_dntrend_stop)
              adjusted_dntrend_stop = old_dntrend_stops[backloop];
          }
          if (backloop > 0)
          {
            old_uptrend_stops[backloop] = old_uptrend_stops[backloop-1];
            old_dntrend_stops[backloop] = old_dntrend_stops[backloop-1];
          }
        }

        old_uptrend_stops[0] = uptrend_stop;
        old_dntrend_stops[0] = dntrend_stop;

        sz_uptrend.add(new Double(adjusted_uptrend_stop));
        sz_dntrend.add(new Double(adjusted_dntrend_stop));
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    if (method == LONG)
      drawLine(sz_uptrend, gc, height);
    else if (method == SHORT)
      drawLine(sz_dntrend, gc, height);
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
    if (name.equalsIgnoreCase("coefficient") == true) //$NON-NLS-1$
      coefficient = Double.parseDouble(value.replace(',', '.'));
    else if (name.equalsIgnoreCase("period") == true) //$NON-NLS-1$
      period = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("no_decline_period") == true) //$NON-NLS-1$
      no_decline_period = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("method") == true) //$NON-NLS-1$
      method = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("SafezoneStop.position")); //$NON-NLS-1$
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData("method"); //$NON-NLS-1$
    combo.add(Messages.getString("SafezoneStop.long")); //$NON-NLS-1$
    combo.add(Messages.getString("SafezoneStop.short")); //$NON-NLS-1$
    combo.setText(combo.getItem(method));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("SafezoneStop.noDeclinePeriod")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("no_decline_period"); //$NON-NLS-1$
    text.setText(String.valueOf(no_decline_period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("SafezoneStop.loopbackPeriod")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("SafezoneStop.coefficient")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("coefficient"); //$NON-NLS-1$
    text.setText(String.valueOf(coefficient));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    return parent;
  }
}
