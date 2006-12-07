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

public class UO extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "UO";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_SHORT_PERIOD = 7;
    public static final int DEFAULT_MEDIUM_PERIOD = 14;
    public static final int DEFAULT_LONG_PERIOD = 28;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int shortPeriod = DEFAULT_SHORT_PERIOD;
    private int mediumPeriod = DEFAULT_MEDIUM_PERIOD;
    private int longPeriod = DEFAULT_LONG_PERIOD;

    public UO()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine trg = getTR();

        PlotLine atr = MA.getMA(trg, 1, shortPeriod);
        int atrLoop = atr.getSize() - 1;

        PlotLine atr2 = MA.getMA(trg, 1, mediumPeriod);
        int atr2Loop = atr2.getSize() - 1;

        PlotLine atr3 = MA.getMA(trg, 1, longPeriod);
        int atr3Loop = atr3.getSize() - 1;

        PlotLine f = new PlotLine();

        int loop;
        for (loop = 0; loop < getBarData().size(); loop++)
            f.append(getBarData().getClose(loop) - getBarData().getLow(loop));

        PlotLine sma = MA.getMA(f, 1, shortPeriod);
        int smaLoop = sma.getSize() - 1;

        PlotLine sma2 = MA.getMA(f, 1, mediumPeriod);
        int sma2Loop = sma2.getSize() - 1;

        PlotLine sma3 = MA.getMA(f, 1, longPeriod);
        int sma3Loop = sma3.getSize() - 1;

        PlotLine uo = new PlotLine();

        while (smaLoop > -1 && sma2Loop > -1 && sma3Loop > -1 && atrLoop > -1 && atr2Loop > -1 && atr3Loop > -1)
        {
            double t = (sma.getData(smaLoop) / atr.getData(atrLoop)) * 4;
            t = t + ((sma2.getData(sma2Loop) / atr2.getData(atr2Loop)) * 2);
            t = t + (sma3.getData(sma3Loop) / atr3.getData(atr3Loop));
            t = (t / 7) * 100;

            uo.prepend(t);

            smaLoop--;
            sma2Loop--;
            sma3Loop--;
            atrLoop--;
            atr2Loop--;
            atr3Loop--;
        }

        uo.setColor(color);
        uo.setType(lineType);
        uo.setLabel(label);
        getOutput().add(uo);

        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color);
        label = settings.getString("label", label);
        lineType = settings.getInteger("lineType", lineType).intValue();
        shortPeriod = settings.getInteger("shortPeriod", shortPeriod).intValue();
        mediumPeriod = settings.getInteger("mediumPeriod", mediumPeriod).intValue();
        longPeriod = settings.getInteger("longPeriod", longPeriod).intValue();
    }

    PlotLine getTR()
    {
        PlotLine tr = new PlotLine();
        int loop;
        for (loop = 0; loop < getBarData().size(); loop++)
        {
            double high = getBarData().getHigh(loop);
            double low = getBarData().getLow(loop);
            double close;
            if (loop > 0)
                close = getBarData().getClose(loop - 1);
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

        return tr;
    }
}
