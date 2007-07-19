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
 * Moving Average
 */
public class MA extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "MA"; //$NON-NLS-1$
    public static final boolean DEFAULT_SCALE_FLAG = false;
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_INPUT = BarData.CLOSE;
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_TYPE = SMA;
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int input = DEFAULT_INPUT;
    private int type = DEFAULT_TYPE;
    private int period = DEFAULT_PERIOD;
    private Color color = new Color(null, DEFAULT_COLOR);
    private boolean scaleFlag = DEFAULT_SCALE_FLAG;

    public MA()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine ma = getMA(new PlotLine(getBarData(), input), type, period);
        if (getBarData().getMax() > ma.getHigh())
            ma.setHigh(getBarData().getMax());
        if (getBarData().getMin() < ma.getLow())
            ma.setLow(getBarData().getMin());
        ma.setLabel(label);
        ma.setType(lineType);
        ma.setColor(color);
        getOutput().add(ma);
        getOutput().setScaleFlag(scaleFlag);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        scaleFlag = settings.getBoolean("scaleFlag", scaleFlag); //$NON-NLS-1$
        label = settings.getString("label", label); //$NON-NLS-1$
        lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
        input = settings.getInteger("input", input).intValue(); //$NON-NLS-1$
        period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
        type = settings.getInteger("type", type).intValue(); //$NON-NLS-1$
        color = settings.getColor("color", color); //$NON-NLS-1$
    }
}
