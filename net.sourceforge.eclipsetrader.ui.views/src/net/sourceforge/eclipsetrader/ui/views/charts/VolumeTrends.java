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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Volume Trends - VT
 */
public class VolumeTrends extends ChartPlotter implements IChartConfigurer
{
  public static final int NVI = 0;
  public static final int OBV = 1;
  public static final int PVI = 2;
  public static final int PVT = 3;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.vt"; //$NON-NLS-1$
  private int method = OBV;
  private List list = new ArrayList();
  
  public VolumeTrends()
  {
    setName(Messages.getString("VolumeTrends.label")); //$NON-NLS-1$
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
    switch(method)
    {
      case NVI:
        calculateNVI(data);
        break;
      case OBV:
        calculateOBV(data);
        break;
      case PVI:
        calculatePVI(data);
        break;
      case PVT:
        calculatePVT(data);
        break;
    }
    setMinMax(list);
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
    if (name.equalsIgnoreCase("method") == true) //$NON-NLS-1$
      method = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("VolumeTrends.method")); //$NON-NLS-1$
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData("method"); //$NON-NLS-1$
    combo.add(Messages.getString("VolumeTrends.nvi")); //$NON-NLS-1$
    combo.add(Messages.getString("VolumeTrends.obv")); //$NON-NLS-1$
    combo.add(Messages.getString("VolumeTrends.pvi")); //$NON-NLS-1$
    combo.add(Messages.getString("VolumeTrends.pvt")); //$NON-NLS-1$
    combo.setText(combo.getItem(method));

    return parent;
  }

  private void calculateOBV(IChartData[] data)
  {
    list = new ArrayList();

    int loop;
    double t = 0;
    for (loop = 1; loop < data.length; loop++)
    {
      double close = data[loop].getClosePrice();
      double volume = data[loop].getVolume();
      double yclose = data[loop - 1].getClosePrice();

      if (close > yclose)
        t = t + volume;
      else
      {
        if (close < yclose)
          t = t - volume;
      }

      list.add(new Double(t));
    }
  }

  public void calculateNVI(IChartData[] data)
  {
    list = new ArrayList();

    int loop;
    double nv = 1000;
    for (loop = 1; loop < data.length; loop++)
    {
      double volume = data[loop].getVolume();
      double close = data[loop].getClosePrice();
      double yvolume = data[loop - 1].getVolume();
      double yclose = data[loop - 1].getClosePrice();

      if (volume < yvolume)
        nv = nv + ((close - yclose) / yclose) * nv;

      list.add(new Double(nv));
    }
  }

  public void calculatePVI (IChartData[] data)
  {
    list = new ArrayList();

    int loop = 0;
    double pv = 1000;
    for (loop = 1; loop < data.length; loop++)
    {
      double volume = data[loop].getVolume();
      double close = data[loop].getClosePrice();
      double yvolume = data[loop - 1].getVolume();
      double yclose = data[loop - 1].getClosePrice();

      if (volume > yvolume)
        pv = pv + ((close - yclose) / yclose) * pv;

      list.add(new Double(pv));
    }
  }

  public void calculatePVT (IChartData[] data)
  {
    list = new ArrayList();

    int loop = 0;
    double pv = 0;
    for (loop = 1; loop < data.length; loop++)
    {
      double close = data[loop].getClosePrice();
      double volume = data[loop].getVolume();
      double yclose = data[loop - 1].getClosePrice();

      pv = pv + (((close - yclose) / yclose) * volume);
      list.add(new Double(pv));
    }
  }
}
