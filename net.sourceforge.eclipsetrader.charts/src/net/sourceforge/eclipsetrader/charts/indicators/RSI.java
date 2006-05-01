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

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 */
public class RSI extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "RSI";
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_SMOOTHING = 10;
    public static final int DEFAULT_SMOOTHING_TYPE = EMA;
    public static final int DEFAULT_INPUT = BarData.CLOSE;
    public static final int DEFAULT_BUYLINE = 30;
    public static final RGB DEFAULT_BUYLINE_COLOR = new RGB(192, 192, 192);
    public static final int DEFAULT_SELLLINE = 70;
    public static final RGB DEFAULT_SELLLINE_COLOR = new RGB(192, 192, 192);
    private Color color = new Color(null, DEFAULT_COLOR);
    private Color buyLineColor = new Color(null, DEFAULT_BUYLINE_COLOR);
    private Color sellLineColor = new Color(null, DEFAULT_SELLLINE_COLOR);
    private int lineType = DEFAULT_LINETYPE;
    private String label = DEFAULT_LABEL;
    private int period = DEFAULT_PERIOD;
    private int smoothing = DEFAULT_SMOOTHING;
    private int smoothingType = DEFAULT_SMOOTHING_TYPE;
    private int input = DEFAULT_INPUT;
    private int buyLine = DEFAULT_BUYLINE;
    private int sellLine = DEFAULT_SELLLINE;

    public RSI()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), input);

        PlotLine rsi = new PlotLine();

        int loop;
        for (loop = period; loop < in.getSize(); loop++)
        {
            double loss = 0;
            double gain = 0;
            int loop2;
            for (loop2 = 0; loop2 < period; loop2++)
            {
                double t = in.getData(loop - loop2) - in.getData(loop - loop2 - 1);
                if (t > 0)
                    gain = gain + t;
                if (t < 0)
                    loss = loss + Math.abs(t);
            }

            double again = gain / period;
            double aloss = loss / period;
            double rs = again / aloss;
            double t = 100 - (100 / (1 + rs));
            if (t > 100)
                t = 100;
            if (t < 0)
                t = 0;

            rsi.append(t);
        }
        rsi.setHigh(100);
        rsi.setLow(0);

        if (smoothing > 1)
        {
            PlotLine ma = getMA(rsi, smoothingType, smoothing);
            ma.setColor(color);
            ma.setType(lineType);
            ma.setLabel(label);
            ma.setHigh(100);
            ma.setLow(0);
            getOutput().add(ma);
        }
        else
        {
            rsi.setColor(color);
            rsi.setType(lineType);
            rsi.setLabel(label);
            getOutput().add(rsi);
        }

        if (buyLine != 0)
        {
            PlotLine bline = new PlotLine();
            bline.setColor(buyLineColor);
            bline.setType(PlotLine.HORIZONTAL);
            bline.append(buyLine);
            getOutput().add(bline);
        }

        if (sellLine != 0)
        {
            PlotLine sline = new PlotLine();
            sline.setColor(sellLineColor);
            sline.setType(PlotLine.HORIZONTAL);
            sline.append(sellLine);
            getOutput().add(sline);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        label = settings.getString("label", label);
        color = settings.getColor("color", color);
        lineType = settings.getInteger("lineType", lineType).intValue();
        period = settings.getInteger("period", period).intValue();
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        smoothingType = settings.getInteger("smoothingType", smoothingType).intValue();
        input = settings.getInteger("input", input).intValue();
        buyLine = settings.getInteger("buyLine", buyLine).intValue();
        buyLineColor = settings.getColor("buyLineColor", buyLineColor);
        sellLine = settings.getInteger("sellLine", sellLine).intValue();
        sellLineColor = settings.getColor("sellLineColor", sellLineColor);
    }
}
