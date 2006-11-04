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

package net.sourceforge.eclipsetrader.charts;

import java.util.Date;

import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Security;


/**
 * Base abstract class for all indicator plugins
 */
public abstract class IndicatorPlugin implements IIndicatorPlugin
{
    public static final int SMA = 0;
    public static final int EMA = 1;
    public static final int WMA = 2;
    public static final int Wilder = 3;
    private BarData barData;
    private Indicator output = new Indicator();

    public IndicatorPlugin()
    {
    }
    
    public void dispose()
    {
    }
    
    public void setInput(BarData barData)
    {
        this.barData = barData;
    }
    
    public BarData getBarData()
    {
        return barData;
    }
    
    public BarData getBarData(int securityId)
    {
        Security security = (Security)CorePlugin.getRepository().load(Security.class, new Integer(securityId));
        if (security != null)
        {
            int compression = barData.getCompression();
            Date begin = barData.getBegin();
            Date end = barData.getEnd();
            if (compression < BarData.INTERVAL_DAILY)
                barData = new BarData(security.getIntradayHistory(), compression, begin, end);
            else
                barData = new BarData(security.getHistory(), compression, begin, end);
        }
        return barData;
    }
    
    public Indicator getOutput()
    {
        return output;
    }
    
    public void clearOutput()
    {
        output.clear();
    }
    
    public abstract void calculate();
    
    public void setParameters(Settings settings)
    {
    }
    
    public Settings getParameters()
    {
        return new Settings();
    }

    public static PlotLine getMA(PlotLine in, int type, int period)
    {
        PlotLine ma = null;

        switch (type)
        {
            case SMA:
                ma = getSMA(in, period);
                break;
            case EMA:
                ma = getEMA(in, period);
                break;
            case WMA:
                ma = getWMA(in, period);
                break;
            case Wilder:
                ma = getWilderMA(in, period);
                break;
            default:
                ma = new PlotLine();
                break;
        }

        return ma;
    }

    public static PlotLine getEMA(PlotLine d, int period)
    {
        PlotLine ema = new PlotLine();
        ema.setLabel(Messages.IndicatorPlugin_EMA);

        if (period >= d.getSize())
            return ema;

        if (period < 1)
            return ema;

        double smoother = 2.0 / (period + 1);

        double t = 0;
        int loop;
        for (loop = 0; loop < period; loop++)
            t = t + d.getData(loop);

        double yesterday = t / period;
        ema.append(yesterday);

        for (; loop < d.getSize(); loop++)
        {
            double t1 = (smoother * (d.getData(loop) - yesterday)) + yesterday;
            yesterday = t1;
            ema.append(t1);
        }

        return ema;
    }

    public static PlotLine getSMA(PlotLine d, int period)
    {
        PlotLine sma = new PlotLine();
        sma.setLabel(Messages.IndicatorPlugin_SMA);

        int size = d.getSize();

        // weed out degenerate cases

        if (period < 1 || period >= size) // STEVE: should be period > size
            return sma; // left this way to keep behaviour

        // create the circular buffer and its running total

        double[] values = new double[period];
        double total = 0.0;

        // fill buffer first time around, keeping its running total

        int loop = -1;
        while (++loop < period)
        {
            double val = d.getData(loop);
            total += val;
            values[loop] = val;
        }

        // buffer filled with first period values, output first sma value

        sma.append(total / period);

        // loop over the rest, each time replacing oldest value in buffer

        --loop;
        while (++loop < size)
        {
            int index = loop % period;
            double newval = d.getData(loop);

            total += newval;
            total -= values[index];
            values[index] = newval;

            sma.append(total / period);
        }

        return sma;
    }

    public static PlotLine getWMA(PlotLine d, int period)
    {
        PlotLine wma = new PlotLine();
        wma.setLabel(Messages.IndicatorPlugin_WMA);

        if (period >= d.getSize())
            return wma;

        if (period < 1)
            return wma;

        int loop;
        for (loop = period - 1; loop < d.getSize(); loop++)
        {
            int loop2;
            int weight;
            int divider;
            double total;
            for (loop2 = period - 1, weight = 1, divider = 0, total = 0; loop2 >= 0; loop2--, weight++)
            {
                total = total + (d.getData(loop - loop2) * weight);
                divider = divider + weight;
            }

            wma.append(total / divider);
        }

        return wma;
    }

    public static PlotLine getWilderMA(PlotLine d, int period)
    {
        PlotLine wilderma = new PlotLine();
        wilderma.setLabel(Messages.IndicatorPlugin_WilderMA);

        if (period >= d.getSize())
            return wilderma;

        if (period < 1)
            return wilderma;

        double t = 0;
        int loop;
        for (loop = 0; loop < period; loop++)
            t = t + d.getData(loop);

        double yesterday = t / period;

        wilderma.append(yesterday);

        for (; loop < (int) d.getSize(); loop++)
        {
            double t1 = (yesterday * (period - 1) + d.getData(loop)) / period;
            yesterday = t1;
            wilderma.append(t1);
        }

        return wilderma;
    }
}
