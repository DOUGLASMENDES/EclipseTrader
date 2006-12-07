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

public class FI extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final String DEFAULT_LABEL = "FI";
    public static final int DEFAULT_SMOOTHING = 2;
    public static final int DEFAULT_MA_TYPE = SMA;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int lineType = DEFAULT_LINETYPE;
    private String label = DEFAULT_LABEL;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maType = DEFAULT_MA_TYPE;

    public FI()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine fi = new PlotLine();
        
        int loop;
        double force = 0;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double cdiff = getBarData().getClose(loop) - getBarData().getClose(loop - 1);
            force = getBarData().getVolume(loop) * cdiff;
            fi.append(force);
        }

        if (smoothing > 1)
            fi = getMA(fi, maType, smoothing);

        fi.setColor(color);
        fi.setType(lineType);
        fi.setLabel(label);
        getOutput().add(fi);

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
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        maType = settings.getInteger("maType", maType).intValue();
    }
}
