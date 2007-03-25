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

package net.sourceforge.eclipsetrader.ta_lib.internal;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;

import com.tictactec.ta.lib.MAType;

public abstract class TALibIndicatorPlugin extends IndicatorPlugin
{

    public TALibIndicatorPlugin()
    {
    }

    protected double[] getInput(BarData barData, int field)
    {
        double[] values = new double[barData.size()];
        
        switch (field)
        {
            case BarData.OPEN:
                for (int i = 0; i < barData.size(); i++)
                    values[i] = barData.get(i).getOpen();
                break;
            case BarData.HIGH:
                for (int i = 0; i < barData.size(); i++)
                    values[i] = barData.get(i).getHigh();
                break;
            case BarData.LOW:
                for (int i = 0; i < barData.size(); i++)
                    values[i] = barData.get(i).getLow();
                break;
            case BarData.VOLUME:
                for (int i = 0; i < barData.size(); i++)
                    values[i] = barData.get(i).getVolume();
                break;
            default:
                for (int i = 0; i < barData.size(); i++)
                    values[i] = barData.get(i).getClose();
                break;
        }
        
        return values;
    }

    protected Object[] getInput(BarData barData)
    {
        double[] open = new double[barData.size()];
        double[] high = new double[barData.size()];
        double[] low = new double[barData.size()];
        double[] close = new double[barData.size()];
        double[] volume = new double[barData.size()];

        for (int i = 0; i < barData.size(); i++)
        {
            Bar bar = barData.get(i);
            open[i] = bar.getOpen();
            high[i] = bar.getHigh();
            low[i] = bar.getLow();
            close[i] = bar.getClose();
            volume[i] = (int)bar.getVolume();
        }
        
        Object[] values = new Object[5];
        values[BarData.OPEN] = open;
        values[BarData.HIGH] = high;
        values[BarData.LOW] = low;
        values[BarData.CLOSE] = close;
        values[BarData.VOLUME] = volume;
        return values;
    }
    
    protected double[] getOutputArray(BarData barData, int lookback)
    {
        int startIdx = 0;
        int endIdx = barData.size() - 1;

        int temp = Math.max(lookback, startIdx);
        int allocationSize = (temp > endIdx) ? 0 : endIdx - temp + 1;
        
        return new double[allocationSize];
    }
    
    protected MAType getTA_MAType(int type)
    {
        MAType maType = MAType.Sma;
        switch(type)
        {
            case 0:
                maType = MAType.Sma;
                break;
            case 1:
                maType = MAType.Ema;
                break;
            case 2:
                maType = MAType.Wma;
                break;
            case 3:
                maType = MAType.Dema;
                break;
            case 4:
                maType = MAType.Tema;
                break;
            case 5:
                maType = MAType.Trima;
                break;
            case 6:
                maType = MAType.Kama;
                break;
            case 7:
                maType = MAType.Mama;
                break;
            case 8:
                maType = MAType.T3;
                break;
        }
        return maType;
    }
}
