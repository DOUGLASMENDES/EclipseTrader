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

public class SD extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "SD";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_PERIOD = 21;
    public static final int DEFAULT_INPUT = BarData.CLOSE;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int period = DEFAULT_PERIOD;
    private int input = DEFAULT_INPUT;

    public SD()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), input);

        PlotLine sd = new PlotLine();
        sd.setColor(color);
        sd.setType(lineType);
        sd.setLabel(label);

        int loop;
        for (loop = period; loop < in.getSize(); loop++)
        {
            double mean = 0;
            int loop2;
            for (loop2 = 0; loop2 < period; loop2++)
                mean = mean + in.getData(loop - loop2);
            mean = mean / period;

            double ds = 0;
            for (loop2 = 0; loop2 < period; loop2++)
            {
                double t = in.getData(loop - loop2) - mean;
                ds = ds + (t * t);
            }
            ds = Math.sqrt(ds / period);

            sd.append(ds);
        }

        getOutput().add(sd);
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
        period = settings.getInteger("period", period).intValue();
        input = settings.getInteger("input", input).intValue();
    }
}
