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
public class BB extends IndicatorPlugin
{
  private Color color = new Color(null, 0, 0, 0);
  private String upperLabel = "BBU";
  private String lowerLabel = "BBL";
  private int lineType = PlotLine.LINE;
  private int deviation = 2;
  private int period = 20;
  private int maType = MA.EMA;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getPluginName()
   */
  public String getPluginName()
  {
    return "Bollinger Bands (BB)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine in = new PlotLine();
    int loop;
    for (loop = 0; loop < getData().size(); loop++)
      in.append((getData().getHigh(loop) + getData().getLow(loop) + getData().getClose(loop)) / 3);
      
    PlotLine sma = MA.getMA(in, maType, period);
    sma.setColor(color);
    sma.setType(lineType);
    sma.setLabel("BBM");
    int smaLoop = sma.getSize() - 1;

    if (sma.getSize() < period * 2)
      return;
    
    PlotLine bbu = new PlotLine();
    bbu.setColor(color);
    bbu.setType(lineType);
    bbu.setLabel(upperLabel);
    
    PlotLine bbl = new PlotLine();
    bbl.setColor(color);
    bbl.setType(lineType);
    bbl.setLabel(lowerLabel);

    int inputLoop = in.getSize() - 1;
    while (inputLoop >= period && smaLoop >= period)
    {
      int count;
      double t2 = 0;
      for (count = 0, t2 = 0; count < period; count++)
      {
        double t = in.getData(inputLoop - count) - sma.getData(smaLoop);
        t2 = t2 + (t * t);
      }

      double t = Math.sqrt(t2 / period);

      bbu.prepend(sma.getData(smaLoop) + (deviation * t)); // upper band
      bbl.prepend(sma.getData(smaLoop) - (deviation * t)); // lower band

      inputLoop--;
      smaLoop--;
    }

    getOutput().add(bbu);
    getOutput().add(bbl);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartObject#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("upperLabel"))
      upperLabel = value;
    else if (name.equals("lowerLabel"))
      lowerLabel = value;
    else if (name.equalsIgnoreCase("color") == true)
    {
      String[] values = value.split(",");
      color = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equals("lineType"))
      lineType = Integer.parseInt(value);
    else if (name.equals("maType"))
      maType = Integer.parseInt(value);
    else if (name.equals("deviation"))
      deviation = Integer.parseInt(value);
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
      private Text upperLabel;
      private Text lowerLabel;
      private ColorSelector color;
      private Combo lineType;
      private Text deviation;
      private Text period;
      private Combo maType;

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      public Control createControl(Composite parent)
      {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Upper Band Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        upperLabel = new Text(composite, SWT.BORDER);
        upperLabel.setText(String.valueOf(BB.this.upperLabel));
        upperLabel.setLayoutData(new GridData(150, SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText("Lower Band Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lowerLabel = new Text(composite, SWT.BORDER);
        lowerLabel.setText(String.valueOf(BB.this.lowerLabel));
        lowerLabel.setLayoutData(new GridData(150, SWT.DEFAULT));
        
        label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ChartParametersDialog.color")); //$NON-NLS-1$
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        color = new ColorSelector(composite);
        color.setColorValue(BB.this.color.getRGB()); //$NON-NLS-1$
        
        lineType = IndicatorPlugin.getLineTypeControl(composite);
        lineType.setText(lineType.getItem(BB.this.lineType));

        label = new Label(composite, SWT.NONE);
        label.setText("Period");
        period = new Text(composite, SWT.BORDER);
        period.setText(String.valueOf(BB.this.period));
        period.setLayoutData(new GridData(25, SWT.DEFAULT));

        maType = MA.getAverageTypeControl(composite);
        maType.setText(maType.getItem(BB.this.maType));

        label = new Label(composite, SWT.NONE);
        label.setText("Deviation");
        deviation = new Text(composite, SWT.BORDER);
        deviation.setText(String.valueOf(BB.this.deviation));
        deviation.setLayoutData(new GridData(25, SWT.DEFAULT));
        
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
          BB.this.color.dispose();
          BB.this.upperLabel = upperLabel.getText();
          BB.this.lowerLabel = lowerLabel.getText();
          BB.this.color = new Color(null, color.getColorValue());
          BB.this.lineType = lineType.getSelectionIndex();
          BB.this.period = Integer.parseInt(period.getText());
          BB.this.maType = maType.getSelectionIndex();
          BB.this.deviation = Integer.parseInt(deviation.getText());
          
          Map map = new HashMap();
          map.put("upperLabel", BB.this.upperLabel);
          map.put("lowerLabel", BB.this.lowerLabel);
          RGB rgb = BB.this.color.getRGB(); 
          map.put("color", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("lineType", String.valueOf(BB.this.lineType));
          map.put("maType", String.valueOf(BB.this.maType));
          map.put("deviation", String.valueOf(BB.this.deviation));
          map.put("period", String.valueOf(BB.this.period));
          setParameters(map);
        }
      }
    };
  }
}
