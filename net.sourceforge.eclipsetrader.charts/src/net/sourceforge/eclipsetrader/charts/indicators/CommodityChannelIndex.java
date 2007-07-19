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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class CommodityChannelIndex extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "CCI"; //$NON-NLS-1$
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_PERIOD = 20;
    public static final int DEFAULT_SMOOTHING = 3;
    public static final int DEFAULT_MATYPE = EMA;
    private String label = DEFAULT_LABEL;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int lineType = DEFAULT_LINETYPE;
    private int period = DEFAULT_PERIOD;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maType = DEFAULT_MATYPE;

    public CommodityChannelIndex()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine cci = new PlotLine();

        PlotLine tp = new PlotLine();
        for (int loop = 0; loop < getBarData().size(); loop++)
            tp.append((getBarData().getHigh(loop) + getBarData().getLow(loop) + getBarData().getClose(loop)) / 3);
        int tpLoop = tp.getSize() - 1;

        PlotLine sma = getMA(tp, maType, period);
        int smaLoop = sma.getSize() - 1;

        while (tpLoop >= period && smaLoop >= period)
        {
            double md = 0;
            for (int loop = 0; loop < period; loop++)
                md = md + Math.abs(tp.getData(tpLoop - loop) - sma.getData(smaLoop - loop));
            md = md / period;

            double t = (tp.getData(tpLoop) - sma.getData(smaLoop)) / (0.015 * md);
            cci.prepend(t);

            tpLoop--;
            smaLoop--;
        }

        if (smoothing > 1)
            cci = getMA(cci, maType, smoothing);

        cci.setColor(color);
        cci.setType(lineType);
        cci.setLabel(label);
        getOutput().add(cci);
        
        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color); //$NON-NLS-1$
        label = settings.getString("label", label); //$NON-NLS-1$
        lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
        period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
        smoothing = settings.getInteger("smoothing", smoothing).intValue(); //$NON-NLS-1$
        maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
    }
}
