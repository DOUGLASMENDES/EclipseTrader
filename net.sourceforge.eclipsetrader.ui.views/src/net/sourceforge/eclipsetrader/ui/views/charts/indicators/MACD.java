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
 * Moving Average Convergence Divergence (MACD)
 */
public class MACD extends IndicatorPlugin
{
  private int macdMAType = MA.SMA;
  private int fastPeriod = 12;
  private int slowPeriod = 26;
  private int trigPeriod = 9;
  private Color macdColor = new Color(null, 255, 0, 0);
  private Color trigColor = new Color(null, 0, 0, 0);
  private Color oscColor = new Color(null, 0, 0, 255);
  private String macdLabel = "MACD";
  private String trigLabel = macdLabel + " Trig";
  private String oscLabel = macdLabel + " Osc";
  private int macdLineType = PlotLine.LINE;
  private int trigLineType = PlotLine.DOT;
  private int oscLineType = PlotLine.HISTOGRAM;
  private boolean oscScaleFlag = false;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getName()
   */
  public String getPluginName()
  {
    return "Moving Average Convergence Divergence (MACD)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine d = getData().getInput(BarData.CLOSE);
    
    PlotLine fma = MA.getMA(d, macdMAType, fastPeriod);
    int fmaLoop = fma.getSize() - 1;

    PlotLine sma = MA.getMA(d, macdMAType, slowPeriod);
    int smaLoop = sma.getSize() - 1;
    
    PlotLine macd = new PlotLine();
    macd.setColor(macdColor);
    macd.setType(macdLineType);
    macd.setLabel(macdLabel);
    
    while (fmaLoop > -1 && smaLoop > -1)
    {
      macd.prepend(fma.getData(fmaLoop) - sma.getData(smaLoop));
      fmaLoop--;
      smaLoop--;
    }

    PlotLine signal = MA.getMA(macd, macdMAType, trigPeriod);
    signal.setColor(trigColor);
    signal.setType(trigLineType);
    signal.setLabel(trigLabel);

    PlotLine osc = new PlotLine();
    osc.setColor(oscColor);
    osc.setType(oscLineType);
    osc.setLabel(oscLabel);
    osc.setScaleFlag(oscScaleFlag);

    int floop = macd.getSize() - 1;
    int sloop = signal.getSize() - 1;

    while (floop > -1 && sloop > -1)
    {
      osc.prepend((macd.getData(floop) - signal.getData(sloop)));
      floop--;
      sloop--;
    }

    getOutput().add(osc);
    getOutput().add(macd);
    getOutput().add(signal);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartObject#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("macdMAType"))
      macdMAType = Integer.parseInt(value);
    else if (name.equals("fastPeriod"))
      fastPeriod = Integer.parseInt(value);
    else if (name.equals("slowPeriod"))
      slowPeriod = Integer.parseInt(value);
    else if (name.equals("trigPeriod"))
      trigPeriod = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("macdColor") == true)
    {
      String[] values = value.split(",");
      macdColor = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equalsIgnoreCase("trigColor") == true)
    {
      String[] values = value.split(",");
      trigColor = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equalsIgnoreCase("oscColor") == true)
    {
      String[] values = value.split(",");
      oscColor = new Color(null, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
    else if (name.equals("macdLabel"))
      macdLabel = value;
    else if (name.equals("trigLabel"))
      trigLabel = value;
    else if (name.equals("oscLabel"))
      oscLabel = value;
    else if (name.equals("macdLineType"))
      macdLineType = Integer.parseInt(value);
    else if (name.equals("trigLineType"))
      trigLineType = Integer.parseInt(value);
    else if (name.equals("oscLineType"))
      oscLineType = Integer.parseInt(value);

    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getParametersPage()
   */
  public IndicatorParametersPage getParametersPage()
  {
    return new IndicatorParametersPage() {
      private Composite composite;
      private Combo macdMAType;
      private Text fastPeriod;
      private Text slowPeriod;
      private Text trigPeriod;
      private ColorSelector macdColor;
      private ColorSelector trigColor;
      private ColorSelector oscColor;
      private Text macdLabel;
      private Text trigLabel;
      private Text oscLabel;
      private Combo macdLineType;
      private Combo trigLineType;
      private Combo oscLineType;

      /* (non-Javadoc)
       * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorParametersPage#createControl(org.eclipse.swt.widgets.Composite)
       */
      public Control createControl(Composite parent)
      {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("MACD Color");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        macdColor = new ColorSelector(composite);
        macdColor.setColorValue(MACD.this.macdColor.getRGB()); //$NON-NLS-1$

        label = new Label(composite, SWT.NONE);
        label.setText("Fast Period");
        fastPeriod = new Text(composite, SWT.BORDER);
        fastPeriod.setText(String.valueOf(MACD.this.fastPeriod));
        fastPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText("Slow Period");
        slowPeriod = new Text(composite, SWT.BORDER);
        slowPeriod.setText(String.valueOf(MACD.this.slowPeriod));
        slowPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText("MACD Label");
        macdLabel = new Text(composite, SWT.BORDER);
        macdLabel.setText(String.valueOf(MACD.this.macdLabel));
        macdLabel.setLayoutData(new GridData(150, SWT.DEFAULT));

        macdLineType = IndicatorPlugin.getLineTypeControl(composite, "MACD Line Type");
        macdLineType.setText(macdLineType.getItem(MACD.this.macdLineType));
        
        macdMAType = MA.getAverageTypeControl(composite, "MACD Moving Average Type");
        macdMAType.setText(macdMAType.getItem(MACD.this.macdMAType));
        
        label = new Label(composite, SWT.NONE);
        label.setText("Trigger Color");
        trigColor = new ColorSelector(composite);
        trigColor.setColorValue(MACD.this.trigColor.getRGB()); //$NON-NLS-1$

        label = new Label(composite, SWT.NONE);
        label.setText("Trigger Period");
        trigPeriod = new Text(composite, SWT.BORDER);
        trigPeriod.setText(String.valueOf(MACD.this.trigPeriod));
        trigPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText("Trigger Label");
        trigLabel = new Text(composite, SWT.BORDER);
        trigLabel.setText(String.valueOf(MACD.this.trigLabel));
        trigLabel.setLayoutData(new GridData(150, SWT.DEFAULT));

        trigLineType = IndicatorPlugin.getLineTypeControl(composite, "Trigger Line Type");
        trigLineType.setText(macdLineType.getItem(MACD.this.trigLineType));
        
        label = new Label(composite, SWT.NONE);
        label.setText("Oscillator Color");
        oscColor = new ColorSelector(composite);
        oscColor.setColorValue(MACD.this.oscColor.getRGB()); //$NON-NLS-1$

        label = new Label(composite, SWT.NONE);
        label.setText("Oscillator Label");
        oscLabel = new Text(composite, SWT.BORDER);
        oscLabel.setText(String.valueOf(MACD.this.oscLabel));
        oscLabel.setLayoutData(new GridData(150, SWT.DEFAULT));

        oscLineType = IndicatorPlugin.getLineTypeControl(composite, "Oscillator Line Type");
        oscLineType.setText(macdLineType.getItem(MACD.this.oscLineType));

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
          MACD.this.macdColor.dispose();
          MACD.this.macdColor = new Color(null, macdColor.getColorValue());
          MACD.this.fastPeriod = Integer.parseInt(fastPeriod.getText());
          MACD.this.slowPeriod = Integer.parseInt(slowPeriod.getText());
          MACD.this.macdLabel = macdLabel.getText();
          MACD.this.macdLineType = macdLineType.getSelectionIndex();
          MACD.this.macdMAType = macdMAType.getSelectionIndex();
          MACD.this.trigColor.dispose();
          MACD.this.trigColor = new Color(null, trigColor.getColorValue());
          MACD.this.trigPeriod = Integer.parseInt(trigPeriod.getText());
          MACD.this.trigLabel = trigLabel.getText();
          MACD.this.trigLineType = trigLineType.getSelectionIndex();
          MACD.this.oscColor.dispose();
          MACD.this.oscColor = new Color(null, oscColor.getColorValue());
          MACD.this.oscLabel = oscLabel.getText();
          MACD.this.oscLineType = oscLineType.getSelectionIndex();

          Map map = new HashMap();
          RGB rgb = MACD.this.macdColor.getRGB(); 
          map.put("macdColor", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("fastPeriod", String.valueOf(MACD.this.fastPeriod));
          map.put("slowPeriod", String.valueOf(MACD.this.slowPeriod));
          map.put("macdLabel", MACD.this.macdLabel);
          map.put("macdLineType", String.valueOf(MACD.this.macdLineType));
          map.put("macdMAType", String.valueOf(MACD.this.macdMAType));
          rgb = MACD.this.trigColor.getRGB(); 
          map.put("trigColor", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("trigPeriod", String.valueOf(MACD.this.trigPeriod));
          map.put("trigLabel", MACD.this.trigLabel);
          map.put("trigLineType", String.valueOf(MACD.this.trigLineType));
          rgb = MACD.this.oscColor.getRGB(); 
          map.put("oscColor", String.valueOf(rgb.red) + "," + String.valueOf(rgb.green) + "," + String.valueOf(rgb.blue));
          map.put("oscLabel", MACD.this.oscLabel);
          map.put("oscLineType", String.valueOf(MACD.this.oscLineType));
          setParameters(map);
        }
      }
    };
  }

}
