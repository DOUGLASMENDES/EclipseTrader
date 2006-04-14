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

import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.Bar;

public class AccumulationDistribution extends IndicatorPlugin
{
    public static final int AD = 0;
    public static final int WAD = 1;
    public static final String DEFAULT_LABEL = "AD";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_METHOD = AD;
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int method = AD;

    public AccumulationDistribution()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        if (method == AD)
            calculateAD();
        else if (method == WAD)
            calculateWAD();
    }

    private void calculateAD()
    {
        PlotLine line = new PlotLine();
        line.setColor(color);
        line.setType(lineType);
        line.setLabel(label);

        double accum = 0;
        for (Iterator iter = getBarData().iterator(); iter.hasNext();)
        {
            Bar bar = (Bar) iter.next();
            double volume = bar.getVolume();
            if (volume > 0)
            {
                double high = bar.getHigh();
                double low = bar.getLow();

                double t = high - low;

                if (t != 0)
                {
                    double close = bar.getClose();
                    double t2 = (close - low) - (high - close);
                    accum = accum + ((t2 / t) * volume);
                }
            }

            line.append(accum);
        }

        getOutput().add(line);
    }

    private void calculateWAD()
    {
        PlotLine wad = new PlotLine();
        wad.setColor(color);
        wad.setType(lineType);
        wad.setLabel(label);

        int loop;
        double accum = 0;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double high = getBarData().getHigh(loop);
            double low = getBarData().getLow(loop);
            double close = getBarData().getClose(loop);
            double yclose = getBarData().getClose(loop - 1);

            double h = high;
            if (yclose > h)
                h = yclose;

            double l = low;
            if (yclose < l)
                l = yclose;

            if (close > yclose)
                accum = accum + (close - l);
            else
            {
                if (yclose == close)
                    ;
                else
                    accum = accum - (h - close);
            }

            wad.append(accum);
        }

        getOutput().add(wad);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color);
        label = settings.getString("label", label);
        lineType = settings.getInteger("lineType", lineType).intValue();
        method = settings.getInteger("method", method).intValue();
    }
}
