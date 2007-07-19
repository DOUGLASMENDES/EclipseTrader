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
import net.sourceforge.eclipsetrader.core.db.BarData;

public class AdaptiveStoch extends IndicatorPlugin
{
    public static final RGB DEFAULT_DCOLOR = new RGB(0, 0, 192);
    public static final RGB DEFAULT_KCOLOR = new RGB(224, 0, 0);
    public static final RGB DEFAULT_BUYCOLOR = new RGB(192, 192, 192);
    public static final RGB DEFAULT_SELLCOLOR = new RGB(192, 192, 192);
    public static final int DEFAULT_DLINETYPE = PlotLine.DOT;
    public static final String DEFAULT_DLABEL = "%D"; //$NON-NLS-1$
    public static final int DEFAULT_DPERIOD = 3;
    public static final int DEFAULT_KLINETYPE = PlotLine.LINE;
    public static final String DEFAULT_KLABEL = "%K"; //$NON-NLS-1$
    public static final int DEFAULT_KPERIOD = 3;
    public static final int DEFAULT_MIN_LOOKBACK = 5;
    public static final int DEFAULT_MAX_LOOKBACK = 20;
    public static final int DEFAULT_K_MATYPE = SMA;
    public static final int DEFAULT_D_MATYPE = SMA;
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_BUYLINE = 20;
    public static final int DEFAULT_SELLLINE = 80;
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
    private int minLookback = DEFAULT_MIN_LOOKBACK;
    private int maxLookback = DEFAULT_MAX_LOOKBACK;
    private int kMaType = DEFAULT_K_MATYPE;
    private int dMaType = DEFAULT_D_MATYPE;
    private int period = DEFAULT_PERIOD;
    private int buyLine = DEFAULT_BUYLINE;
    private int sellLine = DEFAULT_SELLLINE;

    public AdaptiveStoch()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), BarData.CLOSE);

        if (in.getSize() < (period + maxLookback + 5))
            return;

        //  Calculate 20-day std. Dev. And its 20-day range 
        PlotLine v1 = getStdDev(in, period);
        PlotLine v2 = getHighest(v1, period);
        PlotLine v3 = getLowest(v1, period);

        // Create v4: stochastic oscillator for 20-day std. dev.
        // if v1=v2 (highest level) => v4 = 1; if v1=v3 (lowest level) => v4=0 

        PlotLine v4 = new PlotLine();

        int i = 0;

        for (i = 0; i < v2.getSize(); i++)
        {
            if ((v2.getData(i) - v3.getData(i)) > 0)
                v4.append((v1.getData(i) - v3.getData(i)) / (v2.getData(i) - v3.getData(i)));
            else
                v4.append(0);
        }

        // Calculate current effective length; if v4 = 1, then length = mininum 

        PlotLine currentLength = new PlotLine();

        for (i = 0; i < v4.getSize(); i++)
            currentLength.append((int) (minLookback + (maxLookback - minLookback) * (1 - v4.getData(i))));

        // now build indicator
        double stoch = 0;

        PlotLine aStoch = new PlotLine();

        // work backwards to insure alignment
        int index = in.getSize() - 1;
        for (i = currentLength.getSize() - 1; i >= 0; i--)
        {
            double hh = -999999;
            double ll = 999999;
            int loop2;

            for (loop2 = 0; loop2 < currentLength.getData(i); loop2++) // hihest high
            {
                if (getBarData().getHigh(index - loop2) > hh)
                    hh = getBarData().getHigh(index - loop2);

                if (getBarData().getLow(index - loop2) < ll)
                    ll = getBarData().getLow(index - loop2);
            }

            if ((hh - ll) > 0)
                stoch = (((getBarData().getClose(index) - ll) / (hh - ll)) * 100);
            else
            {
                stoch = 0;
            }

            aStoch.prepend(stoch);
            index--;
        }

        if (kperiod > 1)
        {
            PlotLine aStoch2 = getMA(aStoch, kMaType, kperiod);
            aStoch = aStoch2;
        }

        aStoch.setColor(kcolor);
        aStoch.setType(klineType);
        aStoch.setLabel(klabel);

        getOutput().add(aStoch);

        if (dperiod > 1)
        {
            PlotLine d = getMA(aStoch, dMaType, dperiod);
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

    private PlotLine getHighest(PlotLine line, int period)
    {
        int loop;

        PlotLine lineHigh = new PlotLine();

        for (loop = period - 1; loop < line.getSize(); loop++)
        {

            double highest = -999999;
            int loop2;

            for (loop2 = 0; loop2 < period; loop2++)
            {
                if (line.getData(loop - loop2) > highest)
                    highest = line.getData(loop - loop2);
            }

            lineHigh.append(highest);
        }
        return lineHigh;
    }

    private PlotLine getLowest(PlotLine line, int period)
    {
        int loop;

        PlotLine lineLow = new PlotLine();

        for (loop = period - 1; loop < line.getSize(); loop++)
        {
            double lowest = 999999;
            int loop2;

            for (loop2 = 0; loop2 < period; loop2++)
            {
                if (line.getData(loop - loop2) < lowest)
                    lowest = line.getData(loop - loop2);
            }

            lineLow.append(lowest);
        }
        return lineLow;
    }

    private PlotLine getStdDev(PlotLine line, int period)
    {
        PlotLine std = new PlotLine();

        int loop;

        for (loop = period - 1; loop < line.getSize(); loop++)
        {
            double mean = 0;
            int loop2;
            for (loop2 = 0; loop2 < period; loop2++)
                mean += line.getData(loop - loop2);

            mean /= (double) period;

            double ds = 0;
            for (loop2 = 0; loop2 < period; loop2++)
            {
                double t = line.getData(loop - loop2) - mean;
                ds += (t * t);
            }

            ds = Math.sqrt(ds / (double) period);
            std.append(ds);
        }
        return std;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        dcolor = settings.getColor("dcolor", dcolor); //$NON-NLS-1$
        kcolor = settings.getColor("kcolor", kcolor); //$NON-NLS-1$
        buyColor = settings.getColor("buyColor", buyColor); //$NON-NLS-1$
        sellColor = settings.getColor("sellColor", sellColor); //$NON-NLS-1$
        dlineType = settings.getInteger("dlineType", dlineType).intValue(); //$NON-NLS-1$
        dlabel = settings.getString("dlabel", dlabel); //$NON-NLS-1$
        dperiod = settings.getInteger("dperiod", dperiod).intValue(); //$NON-NLS-1$
        klineType = settings.getInteger("klineType", klineType).intValue(); //$NON-NLS-1$
        klabel = settings.getString("klabel", klabel); //$NON-NLS-1$
        kperiod = settings.getInteger("kperiod", kperiod).intValue(); //$NON-NLS-1$
        minLookback = settings.getInteger("minLookback", minLookback).intValue(); //$NON-NLS-1$
        maxLookback = settings.getInteger("maxLookback", maxLookback).intValue(); //$NON-NLS-1$
        kMaType = settings.getInteger("kMaType", kMaType).intValue(); //$NON-NLS-1$
        dMaType = settings.getInteger("dMaType", dMaType).intValue(); //$NON-NLS-1$
        period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
        buyLine = settings.getInteger("buyLine", buyLine).intValue(); //$NON-NLS-1$
        sellLine = settings.getInteger("sellLine", sellLine).intValue(); //$NON-NLS-1$
    }
}
