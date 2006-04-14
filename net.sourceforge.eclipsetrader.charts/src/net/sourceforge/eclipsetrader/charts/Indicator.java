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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 */
public class Indicator
{
    private String name = "";
    private boolean enabled = true;
    private List lines = new ArrayList();
    private double high = -99999999;
    private double low = 99999999;

    public Indicator()
    {
    }
    
    public void dispose()
    {
    }

    public Indicator(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void add(PlotLine plotLine)
    {
        lines.add(plotLine);

        if (plotLine.getHigh() > high)
            high = plotLine.getHigh();
        if (plotLine.getLow() < low)
            low = plotLine.getLow();
    }
    
    public void clear()
    {
        lines.clear();
        high = -99999999;
        low = 99999999;
    }
    
    public PlotLine get(int index)
    {
        return (PlotLine)lines.get(index);
    }
    
    public int size()
    {
        return lines.size();
    }
    
    public Iterator iterator()
    {
        return lines.iterator();
    }

    public List getLines()
    {
        return Collections.unmodifiableList(lines);
    }

    public double getHigh()
    {
        return this.high;
    }

    public void setHigh(double high)
    {
        this.high = high;
    }

    public double getLow()
    {
        return this.low;
    }

    public void setLow(double low)
    {
        this.low = low;
    }
}
