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

public class VT extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "VT";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final String DEFAULT_METHOD = "OBV";
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private String method = DEFAULT_METHOD;

    public VT()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        if (method.equals("OBV"))
            calculateOBV();
        else if (method.equals("NVI"))
            calculateNVI();
        else if (method.equals("PVI"))
            calculatePVI();
        else
            calculatePVT();
        
        getOutput().setScaleFlag(true);
    }

    private void calculateOBV()
    {
        PlotLine obv = new PlotLine();
        obv.setColor(color);
        obv.setType(lineType);
        obv.setLabel(label);

        int loop;
        double t = 0;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double close = getBarData().getClose(loop);
            double volume = getBarData().getVolume(loop);
            double yclose = getBarData().getClose(loop - 1);

            if (close > yclose)
                t = t + volume;
            else
            {
                if (close < yclose)
                    t = t - volume;
            }

            obv.append(t);
        }

        getOutput().add(obv);
    }

    private void calculateNVI()
    {
        PlotLine nvi = new PlotLine();
        nvi.setColor(color);
        nvi.setType(lineType);
        nvi.setLabel(label);

        int loop;
        double nv = 1000;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double volume = getBarData().getVolume(loop);
            double close = getBarData().getClose(loop);
            double yvolume = getBarData().getVolume(loop - 1);
            double yclose = getBarData().getClose(loop - 1);

            if (volume < yvolume)
                nv = nv + ((close - yclose) / yclose) * nv;

            nvi.append(nv);
        }

        getOutput().add(nvi);
    }

    private void calculatePVI()
    {
        PlotLine pvi = new PlotLine();
        pvi.setColor(color);
        pvi.setType(lineType);
        pvi.setLabel(label);

        int loop = 0;
        double pv = 1000;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double volume = getBarData().getVolume(loop);
            double close = getBarData().getClose(loop);
            double yvolume = getBarData().getVolume(loop - 1);
            double yclose = getBarData().getClose(loop - 1);

            if (volume > yvolume)
                pv = pv + ((close - yclose) / yclose) * pv;

            pvi.append(pv);
        }

        getOutput().add(pvi);
    }

    private void calculatePVT()
    {
        PlotLine pvt = new PlotLine();
        pvt.setColor(color);
        pvt.setType(lineType);
        pvt.setLabel(label);

        int loop = 0;
        double pv = 0;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double close = getBarData().getClose(loop);
            double volume = getBarData().getVolume(loop);
            double yclose = getBarData().getClose(loop - 1);

            pv = pv + (((close - yclose) / yclose) * volume);
            pvt.append(pv);
        }

        getOutput().add(pvt);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color);
        label = settings.getString("label", label);
        lineType = settings.getInteger("lineType", lineType).intValue();
        method = settings.getString("method", method);
    }
}
