/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts.indicators;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;

public class VFI extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "VFI";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_SMOOTHING = 3;
    public static final int DEFAULT_MA_TYPE = EMA;
    public static final int DEFAULT_PERIOD = 100;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maType = DEFAULT_MA_TYPE;
    private int period = DEFAULT_PERIOD;

    public VFI()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine vfi = new PlotLine();
        int loop;
        for (loop = period; loop < getBarData().size(); loop++)
        {
            double inter = 0.0;
            double sma_vol = 0.0;
            int i;
            double close = getBarData().getClose(loop - period);
            double high = getBarData().getHigh(loop - period);
            double low = getBarData().getLow(loop - period);
            double typical = (high + low + close) / 3.0;
            for (i = loop - period + 1; i <= loop; i++)
            {
                double ytypical = typical;
                close = getBarData().getClose(i);
                high = getBarData().getHigh(i);
                low = getBarData().getLow(i);
                typical = (high + low + close) / 3.0;
                double delta = (Math.log(typical) - Math.log(ytypical));
                inter += delta * delta;
                sma_vol += getBarData().getVolume(i);
            }
            inter = 0.2 * Math.sqrt(inter / (double) period) * typical;
            sma_vol /= (double) period;

            close = getBarData().getClose(loop - period);
            high = getBarData().getHigh(loop - period);
            low = getBarData().getLow(loop - period);
            typical = (high + low + close) / 3.0;
            double t = 0;
            for (i = loop - period + 1; i <= loop; i++)
            {
                double ytypical = typical;
                double volume = getBarData().getVolume(i);
                close = getBarData().getClose(i);
                high = getBarData().getHigh(i);
                low = getBarData().getLow(i);
                typical = (high + low + close) / 3.0;

                if (typical > ytypical + inter)
                    t = t + Math.log(1.0 + volume / sma_vol);
                else
                {
                    if (typical < ytypical - inter)
                        t = t - Math.log(1.0 + volume / sma_vol);
                }
            }

            vfi.append(t);
        }

        if (smoothing > 1)
        {
            PlotLine ma = getMA(vfi, maType, smoothing);
            ma.setColor(color);
            ma.setType(lineType);
            ma.setLabel(label);
            getOutput().add(ma);
        }
        else
        {
            vfi.setColor(color);
            vfi.setType(lineType);
            vfi.setLabel(label);
            getOutput().add(vfi);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color);
        label = settings.getString("label", label);
        lineType = settings.getInteger("lineType", lineType).intValue();
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        maType = settings.getInteger("maType", maType).intValue();
        period = settings.getInteger("period", period).intValue();
    }
}
