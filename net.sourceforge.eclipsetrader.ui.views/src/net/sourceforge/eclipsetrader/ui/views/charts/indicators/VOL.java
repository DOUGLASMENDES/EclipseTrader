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

import org.eclipse.swt.graphics.Color;

import net.sourceforge.eclipsetrader.ui.views.charts.BarData;
import net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.ui.views.charts.PlotLine;

/**
 */
public class VOL extends IndicatorPlugin
{
  private Color upColor = new Color(null, 0, 255, 0);
  private Color downColor = new Color(null, 255, 0, 0);
  private int period = 0;
  private int maType = MA.SMA;
  private Color maColor = new Color(null, 255, 255, 0);

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#getName()
   */
  public String getPluginName()
  {
    return "Volumne (VOL)";
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IndicatorPlugin#calculate()
   */
  public void calculate()
  {
    PlotLine pl = getData().getInput(BarData.VOLUME);
    pl.setLabel("VOL");
    pl.setType(PlotLine.HISTOGRAM_BAR);

    pl.appendColorBar(upColor);

    int loop;
    for (loop = 1; loop < (int) getData().size(); loop++)
    {
      if (getData().getClose(loop) > getData().getClose(loop - 1))
        pl.appendColorBar(upColor);
      else
        pl.appendColorBar(downColor);
    }
    
    getOutput().add(pl);
    
    if (period > 1)
    {
      PlotLine ma = MA.getMA(pl, maType, period);
      ma.setColor(maColor);
      getOutput().add(ma);
    }
  }
}
