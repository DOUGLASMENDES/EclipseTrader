/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Marco
 */
public class TrendChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.trend";
  private int period = 15;
  private int deviations = 2;
  
  public TrendChart()
  {
    name = "Trend";
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
    return name + " (" + period + "," + deviations + ")";
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    if (chartData != null && max > min)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (height) / (max - min);

      // Colore della linea
      gc.setForeground(lineColor);

      // Computa i punti
      if (chartData.length >= period)
      {
        int numberPlotPoints = 0;
        double sumxx = 0;
        double sumxy = 0;
        double sumx = 0;
        double sumy = 0;
        for (int i = chartData.length - period; i < chartData.length; i++, numberPlotPoints++)
        {
          double x = numberPlotPoints;
          double y = chartData[i].getClosePrice();
          sumx += x;
          sumy += y;
          sumxx += x * x;
          sumxy += x * y;
        }
        double n = (double)numberPlotPoints;
        double Sxx = sumxx - sumx * sumx / n;
        double Sxy = sumxy - sumx * sumy / n;
        double b = Sxy / Sxx;
        double a = (sumy - b * sumx) / n;

        double average = 0;
        numberPlotPoints = 0;
        for (int i = chartData.length - period; i < chartData.length; i++, numberPlotPoints++)
          average += Math.pow(chartData[i].getClosePrice() - ((a + numberPlotPoints * b)), 2);
        average /= numberPlotPoints;
        average /= Math.sqrt(average);
        
        int e1x = (columnWidth / 2) + (columnWidth * (chartData.length - period));  
        int e1y = height - (int)((a - min) * pixelRatio);
        int e2x = columnWidth * period;
        int e2y = height - (int)(((a + period * b) - min) * pixelRatio);
        
        gc.setLineStyle(SWT.LINE_DOT);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);

        e1y = height - (int)(((a + average) - min) * pixelRatio);
        e2y = height - (int)((((a + average) + period * b) - min) * pixelRatio);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);

        e1y = height - (int)(((a - average) - min) * pixelRatio);
        e2y = height - (int)((((a - average) + period * b) - min) * pixelRatio);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);
      }
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
    if (name.equalsIgnoreCase("period") == true)
      period = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("deviations") == true)
      deviations = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Selected Periods");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period");
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);
    
    label = new Label(parent, SWT.NONE);
    label.setText("Standard deviations");
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text = new Text(parent, SWT.BORDER);
    text.setData("deviations");
    text.setText(String.valueOf(deviations));
    gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    return parent;
  }
}
