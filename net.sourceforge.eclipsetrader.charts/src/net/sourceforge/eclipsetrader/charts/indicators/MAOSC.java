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

public class MAOSC extends IndicatorPlugin
{
    public static final boolean DEFAULT_SCALE_FLAG = false;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "MAOSC";
    public static final int DEFAULT_LINETYPE = PlotLine.HISTOGRAM;
    public static final int DEFAULT_FAST_PERIOD = 9;
    public static final int DEFAULT_SLOW_PERIOD = 18;
    public static final int DEFAULT_FAST_MA_TYPE = EMA;
    public static final int DEFAULT_SLOW_MA_TYPE = EMA;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int fastPeriod = DEFAULT_FAST_PERIOD;
    private int slowPeriod = DEFAULT_SLOW_PERIOD;
    private int fastMaType = DEFAULT_FAST_MA_TYPE;
    private int slowMaType = DEFAULT_SLOW_MA_TYPE;
    private boolean scaleFlag = DEFAULT_SCALE_FLAG;

    public MAOSC()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), BarData.CLOSE);

        PlotLine fma = getMA(in, fastMaType, fastPeriod);
        int fmaLoop = fma.getSize() - 1;

        PlotLine sma = getMA(in, slowMaType, slowPeriod);
        int smaLoop = sma.getSize() - 1;

        PlotLine osc = new PlotLine();
        osc.setColor(color);
        osc.setType(lineType);
        osc.setLabel(label);

        while (fmaLoop > -1 && smaLoop > -1)
        {
            osc.prepend(fma.getData(fmaLoop) - sma.getData(smaLoop));
            fmaLoop--;
            smaLoop--;
        }

        getOutput().add(osc);
        getOutput().setScaleFlag(scaleFlag);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        scaleFlag = settings.getBoolean("scaleFlag", scaleFlag);
        color = settings.getColor("color", color);
        label = settings.getString("label", label);
        lineType = settings.getInteger("lineType", lineType).intValue();
        fastPeriod = settings.getInteger("fastPeriod", fastPeriod).intValue();
        slowPeriod = settings.getInteger("slowPeriod", slowPeriod).intValue();
        fastMaType = settings.getInteger("fastMaType", fastMaType).intValue();
        slowMaType = settings.getInteger("slowMaType", slowMaType).intValue();
    }
}
