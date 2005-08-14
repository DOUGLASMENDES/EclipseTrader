/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan S. Stratigakos - Original qtstalker code
 *     Marco Maccaferri      - Java porting and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts.indicators;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.BarData;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.PlotLine;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class MA extends IndicatorPlugin
{
  public static final int SMA = 0;
  public static final int EMA = 1;
  public static final int WMA = 2;
  public static final int Wilder = 3;
  private int lineType = PlotLine.LINE;
  private int type = SMA;
  private int period = 7;
  private Color color = new Color(null, 0, 0, 0);

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getName()
   */
  public String getPluginName()
  {
    return "Moving Average (MA)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine ma = getMA(getData().getInput(BarData.CLOSE), type, period);
    ma.setType(lineType);
    ma.setColor(color);
    getOutput().add(ma);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("lineType"))
      lineType = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("color") == true)
    {
      String[] values = value.split(",");
      color = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equals("period"))
      period = Integer.parseInt(value);
    else if (name.equals("type"))
      type = Integer.parseInt(value);

    super.setParameter(name, value);
  }

  public static PlotLine getMA(PlotLine in, int type, int period)
  {
    PlotLine ma = null;
    
    switch (type)
    {
      case SMA:
        ma = getSMA(in, period);
        break;
      case EMA:
        ma = getEMA(in, period);
        break;
      case WMA:
        ma = getWMA(in, period);
        break;
      case Wilder:
        ma = getWilderMA(in, period);
        break;
      default:
        break;    
    }
    
    return ma;  
  }

  public static PlotLine getEMA(PlotLine d, int period)
  {
    PlotLine ema = new PlotLine();
    ema.setLabel("EMA");

    if (period >= d.getSize())
      return ema;

    if (period < 1)
      return ema;

    double smoother = 2.0 / (period + 1);

    double t = 0;
    int loop;
    for (loop = 0; loop < period; loop++)
      t = t + d.getData(loop);

    double yesterday = t / period;
    ema.append(yesterday);

    for (; loop < d.getSize(); loop++)
    {
      double t1  = (smoother * (d.getData(loop) - yesterday)) + yesterday;
      yesterday = t1;
      ema.append(t1);
    }

    return ema;
  }

  public static PlotLine getSMA(PlotLine d, int period)
  {
    PlotLine sma = new PlotLine();
    sma.setLabel("SMA");

    int size = d.getSize();

    // weed out degenerate cases
    
    if (period < 1 || period >= size) // STEVE: should be period > size
      return sma;       // left this way to keep behaviour

    // create the circular buffer and its running total
    
    double[] values = new double[period];
    double total = 0.0;
    
    // fill buffer first time around, keeping its running total

    int loop = -1;
    while (++loop < period)
    {
      double val = d.getData(loop);
      total += val;
      values[loop] = val;
    }

    // buffer filled with first period values, output first sma value
    
    sma.append(total / period);

    // loop over the rest, each time replacing oldest value in buffer
   
    --loop;
    while (++loop < size) 
    {
      int index = loop % period;
      double newval = d.getData(loop);
      
      total += newval;
      total -= values[index];
      values[index] = newval;

      sma.append(total / period);
    }
   
    return sma;
  }

  public static PlotLine getWMA(PlotLine d, int period)
  {
    PlotLine wma = new PlotLine();
    wma.setLabel("WMA");

    if (period >= d.getSize())
      return wma;

    if (period < 1)
      return wma;

    int loop;
    for (loop = period - 1; loop < d.getSize(); loop++)
    {
      int loop2;
      int weight;
      int divider;
      double total;
      for (loop2 = period - 1, weight = 1, divider = 0, total = 0; loop2 >= 0; loop2--, weight++)
      {
        total = total + (d.getData(loop - loop2) * weight);
        divider = divider + weight;
      }

      wma.append(total / divider);
    }

    return wma;
  }

  public static PlotLine getWilderMA(PlotLine d, int period)
  {
    PlotLine wilderma = new PlotLine();
    wilderma.setLabel("WilderMA");

    if (period >= d.getSize())
      return wilderma;

    if (period < 1)
      return wilderma;

    double t = 0;
    int loop;
    for (loop = 0; loop < period; loop++)
      t = t + d.getData(loop);

    double yesterday = t / period;

    wilderma.append(yesterday);

    for (; loop < (int) d.getSize(); loop++)
    {
      double t1  = (yesterday * (period - 1) + d.getData(loop))/period;
      yesterday = t1;
      wilderma.append(t1);
    }

    return wilderma;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getParametersPage()
   */
  public IndicatorParametersPage getParametersPage()
  {
    return new IndicatorParametersPage() {
      private Composite composite;
      private Combo lineType;
      private ColorSelector colorSelector;
      private Text period;
      private Combo type;

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      public Control createControl(Composite parent)
      {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));
        
        lineType = IndicatorPlugin.getLineTypeControl(composite);
        lineType.setText(lineType.getItem(MA.this.lineType));
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        colorSelector = new ColorSelector(composite);
        colorSelector.setColorValue(MA.this.color.getRGB()); //$NON-NLS-1$

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("AverageChart.periods")); //$NON-NLS-1$
        period = new Text(composite, SWT.BORDER);
        period.setText(String.valueOf(MA.this.period));
        period.setLayoutData(new GridData(25, SWT.DEFAULT));

        type = getAverageTypeControl(composite);
        type.setText(type.getItem(MA.this.type));
        
        return composite;
      }
      
      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#dispose()
       */
      public void dispose()
      {
        if (composite != null)
          composite.dispose();
      }

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#performFinish()
       */
      public void performFinish()
      {
        if (composite != null)
        {
          MA.this.lineType = lineType.getSelectionIndex();
          MA.this.color.dispose();
          MA.this.color = new Color(null, colorSelector.getColorValue());
          MA.this.type = type.getSelectionIndex();
          MA.this.period = Integer.parseInt(period.getText());
          
          Map map = new HashMap();
          map.put("lineType", String.valueOf(MA.this.lineType));
          map.put("type", String.valueOf(MA.this.type));
          map.put("period", String.valueOf(MA.this.period));
          RGB rgb = MA.this.color.getRGB(); 
          map.put("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          setParameters(map);
        }
      }
    };
  }

  public static Combo getAverageTypeControl(Composite parent)
  {
    return getAverageTypeControl(parent, Messages.getString("AverageChart.type")); //$NON-NLS-1$
  }

  public static Combo getAverageTypeControl(Composite parent, String text)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(text);
    Combo type = new Combo(parent, SWT.READ_ONLY);
    type.add(Messages.getString("AverageChart.simple")); //$NON-NLS-1$
    type.add(Messages.getString("AverageChart.exponential")); //$NON-NLS-1$
    type.add(Messages.getString("AverageChart.weighted")); //$NON-NLS-1$
    type.add(Messages.getString("AverageChart.william")); //$NON-NLS-1$
    return type;
  }
}
