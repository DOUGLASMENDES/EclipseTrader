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
import net.sourceforge.eclipsetrader.core.db.BarData;

public class ROC extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "ROC";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_SMOOTHING = 10;
    public static final int DEFAULT_MA_TYPE = EMA;
    public static final int DEFAULT_INPUT = BarData.CLOSE;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int period = DEFAULT_PERIOD;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maType = DEFAULT_MA_TYPE;
    private int input = DEFAULT_INPUT;

    public ROC()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), input);

        PlotLine roc = new PlotLine();

        int loop;
        for (loop = period; loop < in.getSize(); loop++)
            roc.append(((in.getData(loop) - in.getData(loop - period)) / in.getData(loop - period)) * 100);

        if (smoothing > 1)
        {
            PlotLine ma = getMA(roc, maType, smoothing);
            ma.setColor(color);
            ma.setType(lineType);
            ma.setLabel(label);
            getOutput().add(ma);
        }
        else
        {
            roc.setColor(color);
            roc.setType(lineType);
            roc.setLabel(label);
            getOutput().add(roc);
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
        period = settings.getInteger("period", period).intValue();
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        maType = settings.getInteger("maType", maType).intValue();
        input = settings.getInteger("input", input).intValue();
    }
}
