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

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.Bar;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 */
public class Bars extends IndicatorPlugin
{
    public static final int DEFAULT_BARTYPE = PlotLine.BAR;
    public static final RGB DEFAULT_NEUTRAL_BAR = new RGB(192, 192, 192);
    public static final RGB DEFAULT_POSITIVE_BAR = new RGB(0, 192, 0);
    public static final RGB DEFAULT_NEGATIVE_BAR = new RGB(192, 0, 0);
    public static final RGB DEFAULT_POSITIVE_CANDLE = new RGB(255, 255, 255);
    public static final RGB DEFAULT_NEGATIVE_CANDLE = new RGB(0, 0, 0);
    public static final RGB DEFAULT_LINE_CANDLE = new RGB(0, 0, 0);
    private int securityId = 0;
    private int barType = DEFAULT_BARTYPE;
    private Color positiveBar = new Color(null, DEFAULT_POSITIVE_BAR);
    private Color negativeBar = new Color(null, DEFAULT_NEGATIVE_BAR);
    private Color neutralBar = new Color(null, DEFAULT_NEUTRAL_BAR);
    private Color lineCandle = new Color(null, DEFAULT_LINE_CANDLE);
    private Color positiveCandle = new Color(null, DEFAULT_POSITIVE_CANDLE);
    private Color negativeCandle = new Color(null, DEFAULT_NEGATIVE_CANDLE);

    public Bars()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine line = new PlotLine();

        if (barType == PlotLine.BAR)
        {
            for (Iterator iter = getBarData(securityId).iterator(); iter.hasNext(); )
            {
                Bar bar = (Bar)iter.next();
    
                Color color = neutralBar;
                if (bar.getClose() > bar.getOpen())
                    color = positiveBar;
                else if (bar.getClose() < bar.getOpen())
                    color = negativeBar;
    
                line.append(color, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
            }
        }
        else if (barType == PlotLine.CANDLE)
        {
            line.setColor(lineCandle);

            for (Iterator iter = getBarData(securityId).iterator(); iter.hasNext(); )
            {
                Bar bar = (Bar)iter.next();
    
                Color color = negativeCandle;
                if (bar.getClose() > bar.getOpen())
                    color = positiveCandle;
    
                line.append(color, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
            }
        }

        line.setType(barType);

        getOutput().setScaleFlag(securityId != 0);
        getOutput().add(line);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        securityId = settings.getInteger("securityId", securityId).intValue(); //$NON-NLS-1$
        barType = settings.getInteger("barType", barType).intValue(); //$NON-NLS-1$
        positiveBar = settings.getColor("positiveBar", positiveBar); //$NON-NLS-1$
        negativeBar = settings.getColor("negativeBar", negativeBar); //$NON-NLS-1$
        neutralBar = settings.getColor("neutralBar", neutralBar); //$NON-NLS-1$
        lineCandle = settings.getColor("lineCandle", lineCandle); //$NON-NLS-1$
        positiveCandle = settings.getColor("positiveCandle", positiveCandle); //$NON-NLS-1$
        negativeCandle = settings.getColor("negativeCandle", negativeCandle); //$NON-NLS-1$
    }
}
