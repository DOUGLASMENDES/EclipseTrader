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

package net.sourceforge.eclipsetrader.charts;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Scaler
{
    private int height = 0;
    private boolean logScale = false;
    private double scaleHigh = -99999999;
    private double scaleLow = 99999999;
    private double logScaleHigh = 0;
    private double logRange = 0;
    private double range = 0;
    private double scaler = 0;
    private List scaleList = new ArrayList();

    public Scaler()
    {
        scaleList.add(new Double(.00001));
        scaleList.add(new Double(.00002));
        scaleList.add(new Double(.00005));
        scaleList.add(new Double(.0001));
        scaleList.add(new Double(.0002));
        scaleList.add(new Double(.0005));
        scaleList.add(new Double(.001));
        scaleList.add(new Double(.002));
        scaleList.add(new Double(.005));
        scaleList.add(new Double(.01));
        scaleList.add(new Double(.02));
        scaleList.add(new Double(.05));
        scaleList.add(new Double(.1));
        scaleList.add(new Double(.2));
        scaleList.add(new Double(.5));
        scaleList.add(new Double(1));
        scaleList.add(new Double(2));
        scaleList.add(new Double(5));
        scaleList.add(new Double(10));
        scaleList.add(new Double(25));
        scaleList.add(new Double(50));
        scaleList.add(new Double(100));
        scaleList.add(new Double(250));
        scaleList.add(new Double(500));
        scaleList.add(new Double(1000));
        scaleList.add(new Double(2500));
        scaleList.add(new Double(5000));
        scaleList.add(new Double(10000));
        scaleList.add(new Double(25000));
        scaleList.add(new Double(50000));
        scaleList.add(new Double(100000));
        scaleList.add(new Double(250000));
        scaleList.add(new Double(500000));
        scaleList.add(new Double(1000000));
        scaleList.add(new Double(2500000));
        scaleList.add(new Double(5000000));
        scaleList.add(new Double(10000000));
        scaleList.add(new Double(25000000));
        scaleList.add(new Double(50000000));
        scaleList.add(new Double(100000000));
        scaleList.add(new Double(250000000));
        scaleList.add(new Double(500000000));
        scaleList.add(new Double(1000000000));
        scaleList.add(new Double(2500000000.0));
        scaleList.add(new Double(5000000000.0));
        scaleList.add(new Double(10000000000.0));
        scaleList.add(new Double(25000000000.0));
        scaleList.add(new Double(50000000000.0));
        scaleList.add(new Double(100000000000.0));
        scaleList.add(new Double(250000000000.0));
        scaleList.add(new Double(500000000000.0));
    }

    public void set(int height, double scaleHigh, double scaleLow, double logScaleHigh, double logRange, boolean logScale)
    {
        this.height = 0;
        this.scaleHigh = 0;
        this.scaleLow = 0;
        this.logScaleHigh = 0;
        this.logRange = 0;
        this.logScale = false;
        range = 0;
        scaler = 0;

        if ((scaleHigh - scaleLow) != 0)
        {
            this.height = height;
            this.scaleHigh = scaleHigh;
            this.scaleLow = scaleLow;
            this.logScaleHigh = logScaleHigh;
            this.logRange = logRange;
            this.logScale = logScale;

            range = this.scaleHigh - this.scaleLow;
            scaler = this.height / this.range;
        }
    }
    
    public void set(int height)
    {
        if ((scaleHigh - scaleLow) != 0)
        {
            this.height = height;

            range = this.scaleHigh - this.scaleLow;
            scaler = this.height / this.range;
        }
    }
    
    public void set(double scaleHigh, double scaleLow)
    {
        if ((scaleHigh - scaleLow) != 0)
        {
            this.scaleHigh = scaleHigh;
            this.scaleLow = scaleLow;

            range = this.scaleHigh - this.scaleLow;
            scaler = this.height / this.range;
        }
    }

    public int convertToY(double val)
    {
        if (logScale)
        {
            if (val <= 0.0)
                return height;
            else
                return (int) (height * (logScaleHigh - Math.log(val)) / logRange);
        }

        double t = val - scaleLow;
        int y = (int) (t * scaler);
        y = height - y;
        if (y > height)
            y = height;
        return y;
    }

    public double convertToValue(int y)
    {
        if (logScale)
        {
            if (y >= height)
                return scaleLow;
            else
                return Math.exp(logScaleHigh - ((y * logRange) / height));
        }

        if (height == 0)
            return 0;

        int p = height - y;
        double val = scaleLow + (p / scaler);
        return val;
    }

    public double convertToRoundedValue(int y)
    {
        return roundToTick(convertToValue(y));
    }

    public double getLogScaleHigh()
    {
        return logScaleHigh;
    }

    public double getLogRange()
    {
        return logRange;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean getLogFlag()
    {
        return logScale;
    }

    public double getHigh()
    {
        return scaleHigh;
    }

    public double getLow()
    {
        return scaleLow;
    }

    public List getScaleArray()
    {
        int ticks;
        for (ticks = 2; (ticks * 15) < height; ticks++)
            ;
        ticks--;
        if (ticks > 10)
            ticks = 10;

        double interval = 0;
        int loop;
        for (loop = 0; loop < (int) scaleList.size(); loop++)
        {
            interval = ((Double) scaleList.get(loop)).doubleValue();
            if ((range / interval) < ticks)
                break;
        }

        loop = 0;
        double t = 0 - (ticks * interval);
        List scaleArray = new ArrayList();

        if (interval > 0)
        {
            while (t <= scaleHigh)
            {
                t = t + interval;

                if (t >= scaleLow)
                {
                    scaleArray.add(new Double(t));
                    loop++;
                }
            }
        }

        return scaleArray;
    }

    /**
     * Rounds the given price to the nearest tick.<br>
     * 
     * @param price The price value
     * @return The rounded price
     */
    public static double roundToTick(Double price)
    {
        double tick = getPriceTick(price.doubleValue());
        return ((int) ((price.doubleValue() + tick / 2) / tick)) * tick;
    }

    /**
     * Rounds the given price to the nearest tick.<br>
     * 
     * @param value The price value
     * @return The rounded price
     */
    public static double roundToTick(double value)
    {
        if (value < 1000)
        {
            double tick = getPriceTick(value);
            return ((int) ((value + tick / 2) / tick)) * tick;
        }
        
        return value;
    }

    /**
     * Get the price tick related to the passed as argument.<br>
     * 
     * @param value The price value
     * @return The price tick 
     */
    public static double getPriceTick(double value)
    {
        if (value <= 0.3)
            return 0.0005;
        else if (value <= 1.5)
            return 0.001;
        else if (value <= 3)
            return 0.005;
        else if (value <= 30)
            return 0.01;
        else
            return 0.05;
    }
}
