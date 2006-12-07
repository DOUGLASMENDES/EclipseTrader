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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;

public class Stochastic extends IndicatorPlugin
{
    public static final RGB DEFAULT_DCOLOR = new RGB(224, 0, 0);
    public static final String DEFAULT_DLABEL = "%D";
    public static final int DEFAULT_DLINETYPE = PlotLine.DOT;
    public static final int DEFAULT_DPERIOD = 3;
    public static final int DEFAULT_KLINETYPE = PlotLine.LINE;
    public static final String DEFAULT_KLABEL = "%K";
    public static final int DEFAULT_KPERIOD = 3;
    public static final RGB DEFAULT_KCOLOR = new RGB(0, 0, 192);
    public static final RGB DEFAULT_BUYCOLOR = new RGB(192, 192, 192);
    public static final RGB DEFAULT_SELLCOLOR = new RGB(192, 192, 192);
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_BUYLINE = 20;
    public static final int DEFAULT_SELLLINE = 80;
    public static final int DEFAULT_MATYPE = EMA;
    private Color dcolor = new Color(null, DEFAULT_DCOLOR);
    private Color kcolor = new Color(null, DEFAULT_KCOLOR);
    private Color buyColor = new Color(null, DEFAULT_BUYCOLOR);
    private Color sellColor = new Color(null, DEFAULT_SELLCOLOR);
    private int dlineType = DEFAULT_DLINETYPE;
    private String dlabel = DEFAULT_DLABEL;
    private int dperiod = DEFAULT_DPERIOD;
    private int klineType = DEFAULT_KLINETYPE;
    private String klabel = DEFAULT_KLABEL;
    private int kperiod = DEFAULT_KPERIOD;
    private int period = DEFAULT_PERIOD;
    private int buyLine = DEFAULT_BUYLINE;
    private int sellLine = DEFAULT_SELLLINE;
    private int maType = DEFAULT_MATYPE;

    public Stochastic()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine k = new PlotLine();

        int loop;
        for (loop = period; loop < getBarData().size(); loop++)
        {
            int loop2;
            double l;
            double h;
            for (loop2 = 0, l = 9999999, h = 0; loop2 < period; loop2++)
            {
                double high = getBarData().getHigh(loop - loop2);
                double low = getBarData().getLow(loop - loop2);

                double t = high;
                if (t > h)
                    h = t;

                t = low;
                if (t < l)
                    l = t;
            }

            double close = getBarData().getClose(loop);
            double t = ((close - l) / (h - l)) * 100;
            if (t > 100)
                t = 100;
            if (t < 0)
                t = 0;

            k.append(t);
        }

        if (kperiod > 1)
            k = getMA(k, maType, kperiod);

        k.setColor(kcolor);
        k.setType(klineType);
        k.setLabel(klabel);
        getOutput().add(k);

        if (dperiod > 1)
        {
            PlotLine d = getMA(k, maType, dperiod);
            d.setColor(dcolor);
            d.setType(dlineType);
            d.setLabel(dlabel);
            getOutput().add(d);
        }

        if (buyLine != 0)
        {
            PlotLine bline = new PlotLine();
            bline.setColor(buyColor);
            bline.setType(PlotLine.HORIZONTAL);
            bline.append(buyLine);
            getOutput().add(bline);
        }

        if (sellLine != 0)
        {
            PlotLine sline = new PlotLine();
            sline.setColor(sellColor);
            sline.setType(PlotLine.HORIZONTAL);
            sline.append(sellLine);
            getOutput().add(sline);
        }

        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        dcolor = settings.getColor("dcolor", dcolor);
        kcolor = settings.getColor("kcolor", kcolor);
        buyColor = settings.getColor("buyColor", buyColor);
        sellColor = settings.getColor("sellColor", sellColor);
        dlineType = settings.getInteger("dlineType", dlineType).intValue();
        dlabel = settings.getString("dlabel", dlabel);
        dperiod = settings.getInteger("dperiod", dperiod).intValue();
        klineType = settings.getInteger("klineType", klineType).intValue();
        klabel = settings.getString("klabel", klabel);
        kperiod = settings.getInteger("kperiod", kperiod).intValue();
        period = settings.getInteger("period", period).intValue();
        buyLine = settings.getInteger("buyLine", buyLine).intValue();
        sellLine = settings.getInteger("sellLine", sellLine).intValue();
        maType = settings.getInteger("maType", maType).intValue();
    }
}
