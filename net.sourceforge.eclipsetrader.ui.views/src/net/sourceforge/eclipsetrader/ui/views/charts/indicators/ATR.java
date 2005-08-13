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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.PlotLine;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
      ma.setLabel("ATR");
      getOutput().add(ma);
    }
    else
    {
      tr.setColor(color);
      tr.setType(lineType);
      tr.setLabel("ATR");
      getOutput().add(tr);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getParametersPage()
   */
  public IndicatorParametersPage getParametersPage()
  {
    return new IndicatorParametersPage() {
      private Composite composite;
      private ColorSelector colorSelector;
      private Combo lineType;
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
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ChartParametersDialog.name")); //$NON-NLS-1$
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Text text = new Text(composite, SWT.BORDER);
        text.setText(getPluginName());
        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
        
        label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
        colorSelector = new ColorSelector(composite);
        colorSelector.getButton().setData("color"); //$NON-NLS-1$
        colorSelector.setColorValue(ATR.this.color.getRGB()); //$NON-NLS-1$

        lineType = IndicatorPlugin.getLineTypeControl(composite);
        lineType.setText(lineType.getItem(ATR.this.lineType));

        label = new Label(composite, SWT.NONE);
        label.setText("Smoothing Period");
        period = new Text(composite, SWT.BORDER);
        period.setText(String.valueOf(ATR.this.smoothing));
        period.setLayoutData(new GridData(25, SWT.DEFAULT));

        type = MA.getAverageTypeControl(composite);
        type.setText(type.getItem(ATR.this.maType));
        
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
        ATR.this.color.dispose();
        ATR.this.color = new Color(null, colorSelector.getColorValue());
        ATR.this.lineType = lineType.getSelectionIndex();
        ATR.this.maType = type.getSelectionIndex();
        ATR.this.smoothing = Integer.parseInt(period.getText());
      }
    };
  }
}
