/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan S. Stratigakos - Original qtstalker code
 *     Marco Maccaferri      - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts.indicators;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
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
public class ATR extends IndicatorPlugin
{
  private Color color = new Color(null, 0, 0, 255);
  private String label = "ATR";
  private int lineType = PlotLine.LINE;
  private int smoothing = 14;
  private int maType = MA.SMA;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getPluginName()
   */
  public String getPluginName()
  {
    return "Average True Range (ATR)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine tr = new PlotLine();
    int loop;
    for (loop = 0; loop < (int) getData().size(); loop++)
    {
      double high = getData().getHigh(loop);
      double low = getData().getLow(loop);
      double close;
      if (loop > 0)
        close = getData().getClose(loop - 1);
      else
        close = high;

      double t = high - low;

      double t2 = Math.abs(high - close);
      if (t2 > t)
        t = t2;

      t2 = Math.abs(low - close);
      if (t2 > t)
        t = t2;

      tr.append(t);
    }
    
    if (smoothing > 1)
    {
      PlotLine ma = MA.getMA(tr, maType, smoothing);
      ma.setColor(color);
      ma.setType(lineType);
      ma.setLabel(label);
      getOutput().add(ma);
    }
    else
    {
      tr.setColor(color);
      tr.setType(lineType);
      tr.setLabel(label);
      getOutput().add(tr);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartObject#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("label"))
      label = value;
    else if (name.equalsIgnoreCase("color") == true)
    {
      String[] values = value.split(",");
      color = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equals("lineType"))
      lineType = Integer.parseInt(value);
    else if (name.equals("maType"))
      maType = Integer.parseInt(value);
    else if (name.equals("smoothing"))
      smoothing = Integer.parseInt(value);

    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getParametersPage()
   */
  public IndicatorParametersPage getParametersPage()
  {
    return new IndicatorParametersPage() {
      private Composite composite;
      private Text label;
      private ColorSelector colorSelector;
      private Combo lineType;
      private Text smoothing;
      private Combo maType;

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      public Control createControl(Composite parent)
      {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));

        Label _label = new Label(composite, SWT.NONE);
        _label.setText("Label");
        _label.setLayoutData(new GridData(125, SWT.DEFAULT));
        label = new Text(composite, SWT.BORDER);
        label.setText(String.valueOf(ATR.this.label));
        label.setLayoutData(new GridData(150, SWT.DEFAULT));
        
        _label = new Label(composite, SWT.NONE);
        _label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
        colorSelector = new ColorSelector(composite);
        colorSelector.getButton().setData("color"); //$NON-NLS-1$
        colorSelector.setColorValue(ATR.this.color.getRGB()); //$NON-NLS-1$

        lineType = IndicatorPlugin.getLineTypeControl(composite);
        lineType.setText(lineType.getItem(ATR.this.lineType));

        _label = new Label(composite, SWT.NONE);
        _label.setText("Smoothing Period");
        smoothing = new Text(composite, SWT.BORDER);
        smoothing.setText(String.valueOf(ATR.this.smoothing));
        smoothing.setLayoutData(new GridData(25, SWT.DEFAULT));

        maType = MA.getAverageTypeControl(composite);
        maType.setText(maType.getItem(ATR.this.maType));
        
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
          ATR.this.color.dispose();
          ATR.this.color = new Color(null, colorSelector.getColorValue());
          ATR.this.lineType = lineType.getSelectionIndex();
          ATR.this.maType = maType.getSelectionIndex();
          ATR.this.smoothing = Integer.parseInt(smoothing.getText());
          
          Map map = new HashMap();
          map.put("label", ATR.this.label);
          RGB rgb = ATR.this.color.getRGB(); 
          map.put("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("lineType", String.valueOf(ATR.this.lineType));
          map.put("maType", String.valueOf(ATR.this.maType));
          map.put("smoothing", String.valueOf(ATR.this.smoothing));
          setParameters(map);
        }
      }
    };
  }
}
