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

public class DPO extends IndicatorPlugin
{
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final String DEFAULT_LABEL = "DPO"; //$NON-NLS-1$
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_PERIOD = 21;
    public static final int DEFAULT_MA_TYPE = EMA;
    private Color color = new Color(null, DEFAULT_COLOR);
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private int period = DEFAULT_PERIOD;
    private int maType = DEFAULT_MA_TYPE;

    public DPO()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), BarData.CLOSE);
        PlotLine ma = getMA(in, maType, period);

        PlotLine dpo = new PlotLine();
        dpo.setColor(color);
        dpo.setType(lineType);
        dpo.setLabel(label);

        int maLoop = ma.getSize() - 1;
        int closeLoop = in.getSize() - 1;
        int t = (int) ((period / 2) + 1);

        while (maLoop >= t)
        {
            dpo.prepend(in.getData(closeLoop) - ma.getData(maLoop - t));
            closeLoop--;
            maLoop--;
        }
        
        getOutput().add(dpo);
        getOutput().setScaleFlag(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        color = settings.getColor("color", color); //$NON-NLS-1$
        label = settings.getString("label", label); //$NON-NLS-1$
        lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
        period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
        maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
    }
}
