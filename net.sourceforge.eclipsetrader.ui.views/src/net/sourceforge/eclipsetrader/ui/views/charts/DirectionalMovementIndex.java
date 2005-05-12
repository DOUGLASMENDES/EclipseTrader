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
 * Directional Movement Index
 * <p></p>
 */
public class DirectionalMovementIndex extends ChartPlotter implements IChartConfigurer
{
  private static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.directionalMovement"; //$NON-NLS-1$
  private int period = 14;
  private int smoothing = 3;
  private int type = AverageChart.EXPONENTIAL;
  private Color gridColor = new Color(null, 192, 192, 192);
  private Color positiveColor = new Color(null, 192, 0, 0);
  private Color negativeColor = new Color(null, 0, 192, 0);
  private ColorSelector positiveColorSelector;
  private ColorSelector negativeColorSelector;
  
  public DirectionalMovementIndex()
  {
    setName(Messages.getString("DirectionalMovementIndex.label")); //$NON-NLS-1$
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
    return getName() + " (" + period + ", " + smoothing + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    // Grafico
    super.paintChart(gc, width, height);
    if (chartData != null && getMax() > getMin())
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      setMinMax(-5, 105);
      double pixelRatio = (height) / (getMax() - getMin());

      gc.setForeground(gridColor);
      gc.setLineStyle(SWT.LINE_DOT);
      int y1 = (int)((70 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);
      y1 = (int)((30 - getMin()) * pixelRatio);
      gc.drawLine(0, y1, width, y1);

      // Computa i punti
      if (chartData != null)
      {
        // Indicator
        double[] mdm = new double[chartData.length - 1];
        double[] pdm = new double[chartData.length - 1];
        
        for (int i = 1; i < chartData.length; i++)
        {
          double hdiff = chartData[i].getMaxPrice() - chartData[i - 1].getMaxPrice();
          double ldiff = chartData[i - 1].getMinPrice() - chartData[i].getMinPrice();
          double p = 0;
          double m = 0;
          if ((hdiff < 0 && ldiff < 0) || (hdiff == ldiff))
          {
            p = 0;
            m = 0;
          }
          else
          {
            if (hdiff > ldiff)
            {
              p = hdiff;
              m = 0;
            }
            else
            {
              if (hdiff < ldiff)
              {
                p = 0;
                m = ldiff;
              }
            }
          }
          mdm[i - 1] = m;
          pdm[i - 1] = p;
        }

        double[] tr = new double[chartData.length];
        for (int i = 0; i < chartData.length; i++)
        {
          double high = chartData[i].getMaxPrice();
          double low = chartData[i].getMinPrice();
          double close;
          if (i > 0)
            close = chartData[i - 1].getClosePrice();
          else
            close = high;

          double t = high - low;

          double t2 = Math.abs(high - close);
          if (t2 > t)
            t = t2;

          t2 = Math.abs(low - close);
          if (t2 > t)
            t = t2;

          tr[i] = t;
        }
        
        double[] smamdm = getAverage(mdm, period);
        int mdmLoop = smamdm.length - 1;
        double[] smapdm = getAverage(pdm, period);
        int pdmLoop = smapdm.length - 1;
        double[] smatr = getAverage(tr, period);
        int trLoop = smatr.length - 1;

        List mdi = new ArrayList();
        List pdi = new ArrayList();
        
        while (mdmLoop > -1 && trLoop > -1)
        {
          int m = (int) ((smamdm[mdmLoop] / smatr[trLoop]) * 100);
          int p = (int) ((smapdm[pdmLoop] / smatr[trLoop]) * 100);
          
          if (m > 100)
            m = 100;
          if (m < 0)
            m = 0;

          if (p > 100)
            p = 100;
          if (p < 0)
            p = 0;
            
          mdi.add(0, new Double(m));
          pdi.add(0, new Double(p));

          mdmLoop--;
          pdmLoop--;
          trLoop--;
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(positiveColor);
        drawLine(mdi, gc, height, chartData.length - mdi.size());

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(negativeColor);
        drawLine(pdi, gc, height, chartData.length - pdi.size());

        int mdiLoop = mdi.size() - 1;
        int pdiLoop = pdi.size() - 1;
        List dx = new ArrayList();
        while (pdiLoop > -1 && mdiLoop > -1)
        {
          double m = Math.abs(((Double)pdi.get(pdiLoop)).doubleValue() - ((Double)mdi.get(mdiLoop)).doubleValue());
          double p = ((Double)pdi.get(pdiLoop)).doubleValue() + ((Double)mdi.get(mdiLoop)).doubleValue();
          int t = (int) ((m / p) * 100);
          if (t > 100)
            t = 100;
          if (t < 0)
            t = 0;

          dx.add(0, new Double(t));
               
          pdiLoop--;
          mdiLoop--;
        }
        
        dx = AverageChart.getMA(dx, type, smoothing);

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(getColor());
        drawLine(dx, gc, height, chartData.length - dx.size());
      }
    }

    // Tipo di linea e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(getColor());
  }
  
  private double[] getAverage(double[] data, int period)
  {
    double[] value = new double[data.length - period];
    
    for (int i = 0; i < value.length; i++)
    {
      for (int m = 0; m < period; m++)
        value[i] += data[i + m];
      value[i] /= period;
    }
    
    return value;
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
    else if (name.equalsIgnoreCase("smoothing") == true) //$NON-NLS-1$
      smoothing = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
      type = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("positiveColor") == true)
    {
      if (positiveColorSelector != null)
        positiveColor = new Color(null, positiveColorSelector.getColorValue());
    }
    else if (name.equalsIgnoreCase("negativeColor") == true)
    {
      if (negativeColorSelector != null)
        negativeColor = new Color(null, negativeColorSelector.getColorValue());
    }
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Period");
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    label = new Label(parent, SWT.NONE);
    label.setText("Smoothing Period");
    text = new Text(parent, SWT.BORDER);
    text.setData("smoothing"); //$NON-NLS-1$
    text.setText(String.valueOf(smoothing));
    text.setLayoutData(new GridData(25, SWT.DEFAULT));

    AverageChart.addParameters(parent, "Smoothing Average Type", "type", type);
    
    label = new Label(parent, SWT.NONE);
    label.setText("Positive Line Color");
    positiveColorSelector = new ColorSelector(parent);
    positiveColorSelector.setColorValue(positiveColor.getRGB());
    positiveColorSelector.getButton().setData("positiveColor"); //$NON-NLS-1$
    
    label = new Label(parent, SWT.NONE);
    label.setText("Negative Line Color");
    negativeColorSelector = new ColorSelector(parent);
    negativeColorSelector.setColorValue(negativeColor.getRGB());
    negativeColorSelector.getButton().setData("negativeColor"); //$NON-NLS-1$

    return parent;
  }
}
