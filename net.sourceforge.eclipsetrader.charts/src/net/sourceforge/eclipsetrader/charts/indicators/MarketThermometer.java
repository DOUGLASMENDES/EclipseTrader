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

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class MarketThermometer extends IndicatorPlugin
{
    public static final RGB DEFAULT_DOWN_COLOR = new RGB(0, 224, 0);
    public static final RGB DEFAULT_UP_COLOR = new RGB(0, 192, 192);
    public static final RGB DEFAULT_THRESH_COLOR = new RGB(0, 0, 192);
    public static final RGB DEFAULT_MA_COLOR = new RGB(255, 165, 0);
    public static final int DEFAULT_LINETYPE = PlotLine.HISTOGRAM_BAR;
    public static final int DEFAULT_MA_LINETYPE = PlotLine.LINE;
    public static final String DEFAULT_LABEL = "THERM";
    public static final String DEFAULT_MA_LABEL = "THERM MA";
    public static final int DEFAULT_THRESHOLD = 3;
    public static final int DEFAULT_SMOOTHING = 2;
    public static final int DEFAULT_MA_PERIOD = 22;
    public static final int DEFAULT_MA_TYPE = SMA;
    public static final int DEFAULT_SMOOTHTYPE = SMA;
    private Color downColor = new Color(null, DEFAULT_DOWN_COLOR);
    private Color upColor = new Color(null, DEFAULT_UP_COLOR);
    private Color threshColor = new Color(null, DEFAULT_THRESH_COLOR);
    private Color maColor = new Color(null, DEFAULT_MA_COLOR);
    private int lineType = DEFAULT_LINETYPE;
    private int maLineType = DEFAULT_MA_LINETYPE;
    private String label = DEFAULT_LABEL;
    private String maLabel = DEFAULT_MA_LABEL;
    private int threshold = DEFAULT_THRESHOLD;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maPeriod = DEFAULT_MA_PERIOD;
    private int maType = DEFAULT_MA_TYPE;
    private int smoothType = DEFAULT_SMOOTHTYPE;

    public MarketThermometer()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine therm = new PlotLine();
        int loop;
        double thermometer = 0;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double high = Math.abs(getBarData().getHigh(loop) - getBarData().getHigh(loop - 1));
            double lo = Math.abs(getBarData().getLow(loop - 1) - getBarData().getLow(loop));

            if (high > lo)
                thermometer = high;
            else
                thermometer = lo;

            therm.append(thermometer);
        }

        if (smoothing > 1)
        {
            PlotLine ma = getMA(therm, smoothType, smoothing);
            getOutput().add(ma);
            therm = ma;
        }
        else
            getOutput().add(therm);

        PlotLine therm_ma = getMA(therm, maType, maPeriod);
        therm_ma.setColor(maColor);
        therm_ma.setType(maLineType);
        therm_ma.setLabel(maLabel);
        getOutput().add(therm_ma);

        // assign the therm colors

        therm.setType(lineType);
        therm.setLabel(label);

        int thermLoop = therm.getSize() - 1;
        int maLoop = therm_ma.getSize() - 1;
        while (thermLoop > -1)
        {
            if (maLoop > -1)
            {
                double thrm = therm.getData(thermLoop);
                double thrmma = therm_ma.getData(maLoop);

                if (thrm > (thrmma * threshold))
                    therm.prependColorBar(threshColor);
                else
                {
                    if (thrm > thrmma)
                        therm.prependColorBar(upColor);
                    else
                        therm.prependColorBar(downColor);
                }
            }
            else
                therm.prependColorBar(downColor);

            thermLoop--;
            maLoop--;
        }

        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        downColor = settings.getColor("downColor", downColor);
        upColor = settings.getColor("upColor", upColor);
        threshColor = settings.getColor("threshColor", threshColor);
        maColor = settings.getColor("maColor", maColor);
        lineType = settings.getInteger("lineType", lineType).intValue();
        maLineType = settings.getInteger("maLineType", maLineType).intValue();
        label = settings.getString("label", label);
        maLabel = settings.getString("maLabel", maLabel);
        threshold = settings.getInteger("threshold", threshold).intValue();
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        maPeriod = settings.getInteger("maPeriod", maPeriod).intValue();
        maType = settings.getInteger("maType", maType).intValue();
        smoothType = settings.getInteger("smoothType", smoothType).intValue();
    }
}
