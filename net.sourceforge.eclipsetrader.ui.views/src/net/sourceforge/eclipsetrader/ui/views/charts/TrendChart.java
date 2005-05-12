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
 * @author Marco
 */
public class TrendChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.trend"; //$NON-NLS-1$
  private int period = 15;
  private int deviations = 2;
  
  public TrendChart()
  {
    setName(Messages.getString("TrendChart.label")); //$NON-NLS-1$
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
    return getName() + " (" + period + "," + deviations + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

      // Colore della linea
      gc.setForeground(getColor());

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
        
        int e1x = (getColumnWidth() / 2) + (getColumnWidth() * (chartData.length - period));  
        int e1y = height - (int)((a - getMin()) * pixelRatio);
        int e2x = getColumnWidth() * period;
        int e2y = height - (int)(((a + period * b) - getMin()) * pixelRatio);
        
        gc.setLineStyle(SWT.LINE_DOT);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);
        if (isSelected() == true)
          drawSelectionMarkers(e1x, e1y, e1x + e2x, e2y, gc);

        e1y = height - (int)(((a + average * deviations) - getMin()) * pixelRatio);
        e2y = height - (int)((((a + average * deviations) + period * b) - getMin()) * pixelRatio);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);
        if (isSelected() == true)
          drawSelectionMarkers(e1x, e1y, e1x + e2x, e2y, gc);

        e1y = height - (int)(((a - average * deviations) - getMin()) * pixelRatio);
        e2y = height - (int)((((a - average * deviations) + period * b) - getMin()) * pixelRatio);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawLine(e1x, e1y, e1x + e2x, e2y);
        if (isSelected() == true)
          drawSelectionMarkers(e1x, e1y, e1x + e2x, e2y, gc);
      }
    }
  }
  
  /**
   * Draws the selection markers over the chart line.
   * 
   * @param e1x x coordinate of line begin
   * @param e1y y coordinate of line begin
   * @param e2x x coordinate of line end
   * @param e2y y coordinate of line end
   * @param gc the current graphics context 
   */
  private void drawSelectionMarkers(int e1x, int e1y, int e2x, int e2y, GC gc)
  {
    if (e1x > e2x)
    {
      int s = e1x;
      e1x = e2x;
      e2x = s;
    }
    double ratio = ((double)e2y - (double)e1y) / ((double)e2x - (double)e1x);

    Color oldBackground = gc.getBackground();
    gc.setBackground(getColor());
    
    if (period <= 20)
      gc.fillRectangle(e1x - 1, e1y - 1, 5, 5);
    else
    {
      for (int x = e1x, i = 0; i < period - 5; x += getColumnWidth() * 10, i++)
      {
        int y = (int)(ratio * (x - e1x)) + e1y;
        gc.fillRectangle(x - 1, y - 2, 5, 5);
      }
    }
    gc.fillRectangle(e2x - 1, e2y - 1, 5, 5);
    
    gc.setBackground(oldBackground);
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
    else if (name.equalsIgnoreCase("deviations") == true) //$NON-NLS-1$
      deviations = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("TrendChart.periods")); //$NON-NLS-1$
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));
    
    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("TrendChart.deviations")); //$NON-NLS-1$
    text = new Text(parent, SWT.BORDER);
    text.setData("deviations"); //$NON-NLS-1$
    text.setText(String.valueOf(deviations));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    return parent;
  }
}
