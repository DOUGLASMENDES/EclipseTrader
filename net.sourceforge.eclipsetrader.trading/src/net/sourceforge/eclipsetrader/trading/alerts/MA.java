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

package net.sourceforge.eclipsetrader.trading.alerts;

import net.sourceforge.eclipsetrader.core.db.BarData;

/**
 * Moving Average
 */
public class MA
{
    public static final int SMA = 0;
    public static final int EMA = 1;
    public static final int WMA = 2;
    public static final int Wilder = 3;

    public static double getMA(BarData in, int field, int type, int period)
    {
        double ma = 0;

        if (period < 1 || period >= in.size())
            return ma;
        
        double data[] = new double[period + 1];

        int index = in.size() - period - 1;
        for (int i = 0; i < data.length; i++, index++)
        {
            switch(field)
            {
                case BarData.OPEN:
                    data[i] = in.getOpen(index);
                    break;
                case BarData.HIGH:
                    data[i] = in.getHigh(index);
                    break;
                case BarData.LOW:
                    data[i] = in.getLow(index);
                    break;
                case BarData.CLOSE:
                    data[i] = in.getClose(index);
                    break;
            }
        }

        switch (type)
        {
            case SMA:
                ma = getSMA(data, period);
                break;
            case EMA:
                ma = getEMA(data, period);
                break;
            case WMA:
                ma = getWMA(data, period);
                break;
            case Wilder:
                ma = getWilderMA(data, period);
                break;
        }

        return ma;
    }

    public static double getEMA(double[] data, int period)
    {
        double result = 0;

        if (period < 1 || period >= data.length)
            return result;

        double smoother = 2.0 / (period + 1);

        double t = 0;
        int loop;
        for (loop = 0; loop < period; loop++)
            t = t + data[loop];

        double yesterday = t / period;
        result = yesterday;

        for (; loop < data.length; loop++)
        {
            double t1 = (smoother * (data[loop] - yesterday)) + yesterday;
            yesterday = t1;
            result = t1;
        }
        
        return result;
    }

    public static double getSMA(double[] data, int period)
    {
        double result = 0;

        if (period < 1 || period >= data.length)
            return result;

        // create the circular buffer and its running total

        double[] values = new double[period];
        double total = 0.0;

        // fill buffer first time around, keeping its running total

        int loop = -1;
        while (++loop < period)
        {
            double val = data[loop];
            total += val;
            values[loop] = val;
        }

        // buffer filled with first period values, output first sma value

        result = total / period;

        // loop over the rest, each time replacing oldest value in buffer

        --loop;
        while (++loop < data.length)
        {
            int index = loop % period;
            double newval = data[loop];

            total += newval;
            total -= values[index];
            values[index] = newval;

            result = total / period;
        }

        return result;
    }

    public static double getWMA(double[] data, int period)
    {
        double result = 0;

        if (period < 1 || period >= data.length)
            return result;

        int loop;
        for (loop = period - 1; loop < data.length; loop++)
        {
            int loop2;
            int weight;
            int divider;
            double total;
            for (loop2 = period - 1, weight = 1, divider = 0, total = 0; loop2 >= 0; loop2--, weight++)
            {
                total = total + (data[loop - loop2] * weight);
                divider = divider + weight;
            }

            result = total / divider;
        }

        return result;
    }

    public static double getWilderMA(double[] data, int period)
    {
        double result = 0;

        if (period < 1 || period >= data.length)
            return result;

        double t = 0;
        int loop;
        for (loop = 0; loop < period; loop++)
            t = t + data[loop];

        double yesterday = t / period;

        result = yesterday;

        for (; loop < data.length; loop++)
        {
            double t1 = (yesterday * (period - 1) + data[loop]) / period;
            yesterday = t1;
            result = t1;
        }

        return result;
    }
}
