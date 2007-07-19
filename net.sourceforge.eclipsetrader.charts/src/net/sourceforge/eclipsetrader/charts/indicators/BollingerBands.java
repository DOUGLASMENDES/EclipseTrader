/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan S. Stratigakos - original qtstalker code
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts.indicators;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;

public class BollingerBands extends IndicatorPlugin
{
    public static final boolean DEFAULT_SCALE_FLAG = false;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_DEVIATION = 2;
    public static final int DEFAULT_PERIOD = 20;
    public static final int DEFAULT_MATYPE = EMA;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int lineType = DEFAULT_LINETYPE;
    private int deviation = DEFAULT_DEVIATION;
    private int period = DEFAULT_PERIOD;
    private int maType = DEFAULT_MATYPE;
    private boolean scaleFlag = DEFAULT_SCALE_FLAG;

    public BollingerBands()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine();
        for (int loop = 0; loop < getBarData().size(); loop++)
          in.append((getBarData().getHigh(loop) + getBarData().getLow(loop) + getBarData().getClose(loop)) / 3);
          
        PlotLine sma = getMA(in, maType, period);
        sma.setColor(color);
        sma.setType(lineType);
        sma.setLabel("BBM"); //$NON-NLS-1$
        int smaLoop = sma.getSize() - 1;

        if (sma.getSize() < period * 2)
          return;
        
        PlotLine bbu = new PlotLine();
        bbu.setColor(color);
        bbu.setType(lineType);
        bbu.setLabel("BBU"); //$NON-NLS-1$
        
        PlotLine bbl = new PlotLine();
        bbl.setColor(color);
        bbl.setType(lineType);
        bbl.setLabel("BBL"); //$NON-NLS-1$

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

        getOutput().setScaleFlag(scaleFlag);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        scaleFlag = settings.getBoolean("scaleFlag", scaleFlag); //$NON-NLS-1$
        color = settings.getColor("color", color); //$NON-NLS-1$
        lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
        period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
        deviation = settings.getInteger("deviation", deviation).intValue(); //$NON-NLS-1$
        maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
    }
}
