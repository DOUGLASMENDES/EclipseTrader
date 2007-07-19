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

public class SAR extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = Messages.SAR_0;
    public static final int DEFAULT_LINETYPE = PlotLine.DOT;
    public static final double DEFAULT_INITIAL = 0.02;
    public static final double DEFAULT_ADD = 0.02;
    public static final double DEFAULT_LIMIT = 0.2;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private double initial = DEFAULT_INITIAL;
    private double add = DEFAULT_ADD;
    private double limit = DEFAULT_LIMIT;

    public SAR()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine d = new PlotLine();
        d.setColor(color);
        d.setType(lineType);
        d.setLabel(label);

        double high = getBarData().getHigh(1);
        double low = getBarData().getLow(1);
        double yhigh = getBarData().getHigh(0);
        double ylow = getBarData().getLow(0);

        boolean flag = false;
        double ep = 0;
        double sar = 0;
        double psar = 0;
        double a = initial;
        double t = yhigh;
        double t2 = high;
        if (t2 > t)
        {
            // we are long
            flag = false;
            t = ylow;
            t2 = low;
            if (t < t2)
                ep = t;
            else
                ep = t2;

            sar = ep;
            psar = ep;
        }
        else
        {
            // we are short
            flag = true;
            t = yhigh;
            t2 = high;
            if (t > t2)
                ep = t;
            else
                ep = t2;

            sar = ep;
            psar = ep;
        }

        d.append(sar);

        int loop;
        for (loop = 2; loop < getBarData().size(); loop++)
        {
            high = getBarData().getHigh(loop);
            low = getBarData().getLow(loop);
            yhigh = getBarData().getHigh(loop - 1);
            ylow = getBarData().getLow(loop - 1);

            // are we short?
            if (flag)
            {
                // check for a switch
                t = high;
                if (t >= sar)
                {
                    sar = ep;
                    psar = sar;
                    ep = t;
                    flag = false;
                    a = initial;
                }
                else
                {
                    t = low;
                    if (t < ep)
                    {
                        ep = t;
                        a = a + add;
                        if (a > limit)
                            a = limit;
                    }

                    t = psar + (a * (ep - psar));
                    t2 = high;
                    if (t < t2)
                    {
                        double t3 = yhigh;
                        if (t3 > t2)
                            t = t3;
                        else
                            t = t2;
                    }
                    psar = sar;
                    sar = t;
                }
            }
            else
            {
                // we are long
                // check for a switch
                t = low;
                if (t <= sar)
                {
                    sar = ep;
                    psar = sar;
                    ep = t;
                    flag = true;
                    a = initial;
                }
                else
                {
                    t = high;
                    if (t > ep)
                    {
                        ep = t;
                        a = a + add;
                        if (a > limit)
                            a = limit;
                    }

                    t = psar + (a * (ep - psar));
                    t2 = low;
                    if (t > t2)
                    {
                        double t3 = ylow;
                        if (t3 < t2)
                            t = t3;
                        else
                            t = t2;
                    }
                    psar = sar;
                    sar = t;
                }
            }

            d.append(sar);
        }

        getOutput().add(d);
        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor(Messages.SAR_1, color);
        label = settings.getString("label", label); //$NON-NLS-1$
        lineType = settings.getInteger(Messages.SAR_3, lineType).intValue();
        initial = settings.getDouble(Messages.SAR_4, initial).doubleValue();
        add = settings.getDouble(Messages.SAR_5, add).doubleValue();
        limit = settings.getDouble(Messages.SAR_6, limit).doubleValue();
    }
}
