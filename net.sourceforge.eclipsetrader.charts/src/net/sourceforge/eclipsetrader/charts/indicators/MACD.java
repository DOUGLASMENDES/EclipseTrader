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

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Moving Average Convergence Divergence - MACD
 */
public class MACD extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "MACD"; //$NON-NLS-1$
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 0);
    public static final int DEFAULT_FAST_PERIOD = 12;
    public static final int DEFAULT_SLOW_PERIOD = 26;
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_MA_TYPE = SMA;
    public static final RGB DEFAULT_TRIGGER_COLOR = new RGB(0, 0, 224);
    public static final int DEFAULT_TRIGGER_PERIOD = 9;
    public static final String DEFAULT_TRIGGER_LABEL = DEFAULT_LABEL + " Trig."; //$NON-NLS-1$
    public static final int DEFAULT_TRIGGER_LINETYPE = PlotLine.DOT;
    public static final RGB DEFAULT_OSC_COLOR = new RGB(192, 192, 192);
    public static final String DEFAULT_OSC_LABEL = DEFAULT_LABEL + " Osc."; //$NON-NLS-1$
    public static final int DEFAULT_OSC_LINETYPE = PlotLine.HISTOGRAM_BAR;
    public static final int DEFAULT_INPUT = BarData.CLOSE;
    private String label = DEFAULT_LABEL;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int fastPeriod = DEFAULT_FAST_PERIOD;
    private int slowPeriod = DEFAULT_SLOW_PERIOD;
    private int lineType = DEFAULT_LINETYPE;
    private int maType = DEFAULT_MA_TYPE;
    private int input = DEFAULT_INPUT;
    private Color triggerColor = new Color(null, DEFAULT_TRIGGER_COLOR);
    private int triggerPeriod = DEFAULT_TRIGGER_PERIOD;
    private String triggerLabel = DEFAULT_TRIGGER_LABEL;
    private int triggerLineType = DEFAULT_TRIGGER_LINETYPE;
    private Color oscColor = new Color(null, DEFAULT_OSC_COLOR);
    private String oscLabel = DEFAULT_OSC_LABEL;
    private int oscLineType = DEFAULT_OSC_LINETYPE;

    public MACD()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine d = new PlotLine(getBarData(), input);

        PlotLine fma = getMA(d, maType, fastPeriod);
        int fmaLoop = fma.getSize() - 1;

        PlotLine sma = getMA(d, maType, slowPeriod);
        int smaLoop = sma.getSize() - 1;

        PlotLine macd = new PlotLine();
        macd.setColor(color);
        macd.setType(lineType);
        macd.setLabel(label);

        while (fmaLoop > -1 && smaLoop > -1)
        {
            macd.prepend(fma.getData(fmaLoop) - sma.getData(smaLoop));
            fmaLoop--;
            smaLoop--;
        }
        
        double v = Math.max(Math.abs(macd.getHigh()), Math.abs(macd.getLow()));
        macd.setHigh(v);
        macd.setLow(-v);

        PlotLine signal = getMA(macd, maType, triggerPeriod);
        signal.setColor(triggerColor);
        signal.setType(triggerLineType);
        signal.setLabel(triggerLabel);
        
        v = Math.max(Math.abs(signal.getHigh()), Math.abs(signal.getLow()));
        signal.setHigh(v);
        signal.setLow(-v);

        PlotLine osc = new PlotLine();
        osc.setColor(oscColor);
        osc.setType(oscLineType);
        osc.setLabel(oscLabel);

        int floop = macd.getSize() - 1;
        int sloop = signal.getSize() - 1;

        while (floop > -1 && sloop > -1)
        {
            osc.prepend((macd.getData(floop) - signal.getData(sloop)));
            floop--;
            sloop--;
        }
        
        v = Math.max(Math.abs(osc.getHigh()), Math.abs(osc.getLow()));
        osc.setHigh(v);
        osc.setLow(-v);
        
        getOutput().add(osc);
        getOutput().add(macd);
        getOutput().add(signal);

        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        label = settings.getString("label", label); //$NON-NLS-1$
        color = settings.getColor("color", color); //$NON-NLS-1$
        fastPeriod = settings.getInteger("fastPeriod", fastPeriod).intValue(); //$NON-NLS-1$
        slowPeriod = settings.getInteger("slowPeriod", slowPeriod).intValue(); //$NON-NLS-1$
        lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
        maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
        input = settings.getInteger("input", input).intValue(); //$NON-NLS-1$
        triggerColor = settings.getColor("triggerColor", triggerColor); //$NON-NLS-1$
        triggerPeriod = settings.getInteger("triggerPeriod", triggerPeriod).intValue(); //$NON-NLS-1$
        triggerLabel = settings.getString("triggerLabel", triggerLabel); //$NON-NLS-1$
        triggerLineType = settings.getInteger("triggerLineType", triggerLineType).intValue(); //$NON-NLS-1$
        oscColor = settings.getColor("oscColor", oscColor); //$NON-NLS-1$
        oscLabel = settings.getString("oscLabel", oscLabel); //$NON-NLS-1$
        oscLineType = settings.getInteger("oscLineType", oscLineType).intValue(); //$NON-NLS-1$
    }
}
