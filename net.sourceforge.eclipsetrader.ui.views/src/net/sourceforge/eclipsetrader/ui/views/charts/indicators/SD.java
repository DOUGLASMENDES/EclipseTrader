/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts.indicators;

import java.util.HashMap;
import java.util.Map;

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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.views.charts.BarData;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.PlotLine;

/**
 */
public class SD extends IndicatorPlugin
{
  private Color color = new Color(null, 0, 0, 0);
  private String label = "SD";
  private int lineType = PlotLine.LINE;
  private int period = 21;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getPluginName()
   */
  public String getPluginName()
  {
    return "Standard Deviation (SD)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine in = getData().getInput(BarData.CLOSE);

    PlotLine sd = new PlotLine();
    sd.setColor(color);
    sd.setType(lineType);
    sd.setLabel(label);

    int loop;
    for (loop = period; loop < in.getSize(); loop++)
    {
      double mean = 0;
      int loop2;
      for (loop2 = 0; loop2 < period; loop2++)
        mean = mean + in.getData(loop - loop2);
      mean = mean / period;

      double ds = 0;
      for (loop2 = 0; loop2 < period; loop2++)
      {
        double t = in.getData(loop - loop2) - mean;
        ds = ds + (t * t);
      }
      ds = Math.sqrt(ds / period);

      sd.append(ds);
    }

    getOutput().add(sd);
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
    else if (name.equals("period"))
      period = Integer.parseInt(value);

    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getParametersPage()
   */
  public IndicatorParametersPage getParametersPage()
  {
    return new IndicatorParametersPage() {
      private Composite composite;
      private ColorSelector color;
      private Combo lineType;
      private Text period;

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      public Control createControl(Composite parent)
      {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        color = new ColorSelector(composite);
        color.setColorValue(SD.this.color.getRGB()); //$NON-NLS-1$
        
        lineType = IndicatorPlugin.getLineTypeControl(composite);
        lineType.setText(lineType.getItem(SD.this.lineType));

        label = new Label(composite, SWT.NONE);
        label.setText("Period");
        period = new Text(composite, SWT.BORDER);
        period.setText(String.valueOf(SD.this.period));
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        
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
          SD.this.color.dispose();
          SD.this.color = new Color(null, color.getColorValue());
          SD.this.lineType = lineType.getSelectionIndex();
          SD.this.period = Integer.parseInt(period.getText());

          Map map = new HashMap();
          map.put("label", SD.this.label);
          RGB rgb = SD.this.color.getRGB(); 
          map.put("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("lineType", String.valueOf(SD.this.lineType));
          map.put("period", String.valueOf(SD.this.period));
          setParameters(map);
        }
      }
    };
  }
}
