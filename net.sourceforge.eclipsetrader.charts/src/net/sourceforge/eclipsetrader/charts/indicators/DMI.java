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

package net.sourceforge.eclipsetrader.charts.indicators;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class DMI extends IndicatorPlugin
{
    public static final RGB DEFAULT_MDI_COLOR = new RGB(224, 0, 0);
    public static final RGB DEFAULT_PDI_COLOR = new RGB(0, 224, 0);
    public static final RGB DEFAULT_ADX_COLOR = new RGB(224, 224, 0);
    public static final int DEFAULT_PDI_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_MDI_LINETYPE = PlotLine.LINE;
    public static final int DEFAULT_ADX_LINETYPE = PlotLine.LINE;
    public static final String DEFAULT_PDI_LABEL = "+DM";
    public static final String DEFAULT_MDI_LABEL = "-DM";
    public static final String DEFAULT_ADX_LABEL = "ADX";
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_SMOOTHING = 9;
    public static final int DEFAULT_MA_TYPE = EMA;
    public static final String DEFAULT_LINE_REQUEST = "ADX";
    public static final String DEFAULT_LABEL = "DMI";
    private Color mdiColor = new Color(null, DEFAULT_MDI_COLOR);
    private Color pdiColor = new Color(null, DEFAULT_PDI_COLOR);
    private Color adxColor = new Color(null, DEFAULT_ADX_COLOR);
    private int pdiLineType = DEFAULT_PDI_LINETYPE;
    private int mdiLineType = DEFAULT_MDI_LINETYPE;
    private int adxLineType = DEFAULT_ADX_LINETYPE;
    private String pdiLabel = DEFAULT_PDI_LABEL;
    private String mdiLabel = DEFAULT_MDI_LABEL;
    private String adxLabel = DEFAULT_ADX_LABEL;
    private int period = DEFAULT_PERIOD;
    private int smoothing = DEFAULT_SMOOTHING;
    private int maType = DEFAULT_MA_TYPE;

    public DMI()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        getDI(period);
        getADX(maType, smoothing);
    }

    private void getDI(int period)
    {
        PlotLine mdm = new PlotLine();
        PlotLine pdm = new PlotLine();

        int loop;
        for (loop = 1; loop < getBarData().size(); loop++)
        {
            double hdiff = getBarData().getHigh(loop) - getBarData().getHigh(loop - 1);
            double ldiff = getBarData().getLow(loop - 1) - getBarData().getLow(loop);
            double p = 0;
            double m = 0;

            if ((hdiff < 0 && ldiff < 0) || (hdiff == ldiff))
            {
                p = 0;
                m = 0;
            }
            else
            {
                if (hdiff > ldiff)
                {
                    p = hdiff;
                    m = 0;
                }
                else
                {
                    if (hdiff < ldiff)
                    {
                        p = 0;
                        m = ldiff;
                    }
                }
            }

            mdm.append(m);
            pdm.append(p);
        }

        PlotLine tr = getTR();

        PlotLine smamdm = getMA(mdm, 1, period);
        int mdmLoop = smamdm.getSize() - 1;

        PlotLine smapdm = getMA(pdm, 1, period);
        int pdmLoop = smapdm.getSize() - 1;

        PlotLine smatr = getMA(tr, 1, period);
        int trLoop = smatr.getSize() - 1;

        PlotLine mdi = new PlotLine();
        PlotLine pdi = new PlotLine();

        while (mdmLoop > -1 && trLoop > -1)
        {
            int m = (int) ((smamdm.getData(mdmLoop) / smatr.getData(trLoop)) * 100);
            int p = (int) ((smapdm.getData(pdmLoop) / smatr.getData(trLoop)) * 100);

            if (m > 100)
                m = 100;
            if (m < 0)
                m = 0;

            if (p > 100)
                p = 100;
            if (p < 0)
                p = 0;

            mdi.prepend(m);
            pdi.prepend(p);

            mdmLoop--;
            pdmLoop--;
            trLoop--;
        }

        mdi.setColor(mdiColor);
        mdi.setType(mdiLineType);
        mdi.setLabel(mdiLabel);
        getOutput().add(mdi);

        pdi.setColor(pdiColor);
        pdi.setType(pdiLineType);
        pdi.setLabel(pdiLabel);
        getOutput().add(pdi);
    }

    private void getADX(int type, int period)
    {
        if (getOutput().getLines().size() < 2)
            return;

        PlotLine mdi = (PlotLine) getOutput().getLines().get(0);

        PlotLine pdi = (PlotLine) getOutput().getLines().get(1);

        int mdiLoop = mdi.getSize() - 1;
        int pdiLoop = pdi.getSize() - 1;

        PlotLine dx = new PlotLine();

        while (pdiLoop > -1 && mdiLoop > -1)
        {
            double m = Math.abs(pdi.getData(pdiLoop) - mdi.getData(mdiLoop));
            double p = pdi.getData(pdiLoop) + mdi.getData(mdiLoop);
            int t = (int) ((m / p) * 100);
            if (t > 100)
                t = 100;
            if (t < 0)
                t = 0;

            dx.prepend(t);

            pdiLoop--;
            mdiLoop--;
        }

        PlotLine adx = getMA(dx, type, period);
        adx.setColor(adxColor);
        adx.setType(adxLineType);
        adx.setLabel(adxLabel);
        getOutput().add(adx);
    }

    private PlotLine getTR()
    {
        PlotLine tr = new PlotLine();
        int loop;
        for (loop = 0; loop < (int) getBarData().size(); loop++)
        {
            double high = getBarData().getHigh(loop);
            double low = getBarData().getLow(loop);
            double close;
            if (loop > 0)
                close = getBarData().getClose(loop - 1);
            else
                close = high;

            double t = high - low;

            double t2 = Math.abs(high - close);
            if (t2 > t)
                t = t2;

            t2 = Math.abs(low - close);
            if (t2 > t)
                t = t2;

            tr.append(t);
        }

        return tr;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#setParameters(net.sourceforge.eclipsetrader.charts.Settings)
     */
    public void setParameters(Settings settings)
    {
        mdiColor = settings.getColor("mdiColor", mdiColor);
        pdiColor = settings.getColor("pdiColor", pdiColor);
        adxColor = settings.getColor("adxColor", adxColor);
        pdiLineType = settings.getInteger("pdiLineType", pdiLineType).intValue();
        mdiLineType = settings.getInteger("mdiLineType", mdiLineType).intValue();
        adxLineType = settings.getInteger("adxLineType", adxLineType).intValue();
        pdiLabel = settings.getString("pdiLabel", pdiLabel);
        mdiLabel = settings.getString("mdiLabel", mdiLabel);
        adxLabel = settings.getString("adxLabel", adxLabel);
        period = settings.getInteger("period", period).intValue();
        smoothing = settings.getInteger("smoothing", smoothing).intValue();
        maType = settings.getInteger("maType", maType).intValue();
    }
}
