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

import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.Bar;

/**
 */
public class Volume extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "VOL";
    public static final RGB DEFAULT_NEUTRAL = new RGB(192, 192, 192);
    public static final RGB DEFAULT_POSITIVE = new RGB(0, 192, 0);
    public static final RGB DEFAULT_NEGATIVE = new RGB(192, 0, 0);
    private String label = DEFAULT_LABEL;
    private Color neutral = new Color(null, DEFAULT_NEUTRAL);
    private Color positive = new Color(null, DEFAULT_POSITIVE);
    private Color negative = new Color(null, DEFAULT_NEGATIVE);

    public Volume()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine line = new PlotLine(label);
        line.setScaleFlag(true);
        line.setType(PlotLine.HISTOGRAM_BAR);

        Bar previous = null;
        for (Iterator iter = getBarData().iterator(); iter.hasNext(); )
        {
            Bar bar = (Bar)iter.next();

            Color color = neutral;
            if (previous != null)
            {
                if (bar.getClose() > previous.getClose())
                    color = positive;
                else if (bar.getClose() < previous.getClose())
                    color = negative;
            }

            line.append(bar.getVolume(), color);
            
            previous = bar;
        }
        
        getOutput().add(line);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        label = settings.getString("label", label);
        neutral = settings.getColor("neutral", neutral);
        positive = settings.getColor("positive", positive);
        negative = settings.getColor("negative", negative);
    }
}
