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

import java.util.Arrays;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class VIDYA extends IndicatorPlugin
{
    public static final String DEFAULT_LABEL = "VIDYA"; //$NON-NLS-1$
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_PERIOD = 14;
    public static final int DEFAULT_VOLPERIOD = 10;
    private String label = DEFAULT_LABEL;
    private int lineType = DEFAULT_LINETYPE;
    private Color color = new Color(null, DEFAULT_COLOR);
    private int period = DEFAULT_PERIOD;
    private int volPeriod = DEFAULT_VOLPERIOD;

    public VIDYA()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPlugin#calculate()
     */
    public void calculate()
    {
        PlotLine in = new PlotLine(getBarData(), BarData.CLOSE);

        if (in.getSize() < period)
            return;

        PlotLine out = new PlotLine();

        calcVidya(out, in, volPeriod, period);

        out.setColor(color);
        out.setType(lineType);
        out.setLabel(label);
        getOutput().add(out);

        getOutput().setScaleFlag(true);
    }

    void calcVidya(PlotLine outSignal, PlotLine inSignal, int iCmoPeriod, int iVidyaPeriod)
    {
        PlotLine cmo = new PlotLine();

        calcCMO(cmo, inSignal, iCmoPeriod);

        int i = 0;
        int loop = inSignal.getSize();

        double[] inSeries = new double[loop];
        Arrays.fill(inSeries, 0.0);
        double[] offset = new double[loop];
        Arrays.fill(offset, 0.0);
        double[] absCmo = new double[loop];
        Arrays.fill(absCmo, 0.0);
        double[] vidya = new double[loop];
        Arrays.fill(vidya, 0.0);

        double c = 2 / (double) iVidyaPeriod + 1;

        for (i = 0; i < loop; i++)
            inSeries[i] = inSignal.getData(i);

        int index = inSeries.length - 1;
        for (i = cmo.getSize() - 1; i >= 0; i--)
        {
            absCmo[index] = Math.abs(cmo.getData(i) / 100);
            index--;
        }

        for (i = iCmoPeriod + iVidyaPeriod; i < (int) inSeries.length; i++) // period safty
        {
            vidya[i] = (inSeries[i] * c * absCmo[i]) + ((1 - absCmo[i] * c) * vidya[i - 1]);
            //!  (Price*Const*AbsCMO) + ((1-AbsCMO*Const)*VIDYA[1]),Price);
            outSignal.append(vidya[i]);
        }
    }

    void calcCMO(PlotLine outSignal, PlotLine inSignal, int iPeriod)
    {
        //!  Chande Momentum Oscillator
        //!  Raw VIDYA 

        int loop = (int) inSignal.getSize();

        double[] inSeries = new double[loop];
        Arrays.fill(inSeries, 0.0);
        double[] offset = new double[loop];
        Arrays.fill(offset, 0.0);
        double[] mom = new double[loop];
        Arrays.fill(mom, 0.0);
        double[] posSeries = new double[loop];
        Arrays.fill(posSeries, 0.0);
        double[] negSeries = new double[loop];
        Arrays.fill(negSeries, 0.0);
        double[] sumPos = new double[loop];
        Arrays.fill(sumPos, 0.0);
        double[] sumNeg = new double[loop];
        Arrays.fill(sumNeg, 0.0);
        double[] cmoUp = new double[loop];
        Arrays.fill(cmoUp, 0.0);
        double[] cmoDown = new double[loop];
        Arrays.fill(cmoDown, 0.0);
        double[] rawCmo = new double[loop];
        Arrays.fill(rawCmo, 0.0);

        int i = 0;

        for (i = 0; i < loop; i++)
            inSeries[i] = inSignal.getData(i);

        for (i = iPeriod - 1; i < loop; i++)
        {
            offset[i] = inSeries[i - 1];

            mom[i] = inSeries[i] - offset[i];

            if (mom[i] > 0)
                posSeries[i] = mom[i];
            else
                posSeries[i] = 0;

            if (mom[i] < 0)
                negSeries[i] = Math.abs(mom[i]);
            else
                negSeries[i] = 0;

            int j = 0;
            double sumUp = 0;
            double sumDown = 0;

            for (j = 0; j < iPeriod; j++)
            {
                sumUp += posSeries[i - j];
                sumDown += negSeries[i - j];
            }

            sumPos[i] = sumUp;
            sumNeg[i] = sumDown;

            cmoUp[i] = 100 * ((sumPos[i] - sumNeg[i]));

            cmoDown[i] = sumPos[i] + sumNeg[i];

            rawCmo[i] = cmoUp[i] / cmoDown[i];

            if (i > iPeriod - 1)
                outSignal.append(rawCmo[i]);
        }
    }

    void calcAdaptCMO(PlotLine outSignal, PlotLine inSignal, int iStdPeriod, int iMinLook, int iMaxLook)
    {
        //! Chande Momentum Oscillator
        //! Adaptaave VIDYA 
        //! Not used here, but it has possibilities....

        PlotLine currentLookback = new PlotLine();

        getStdDev(currentLookback, inSignal, iStdPeriod);

        getNorm(currentLookback, iMinLook, iMaxLook);

        int i = 0;

        for (i = 0; i < currentLookback.getSize(); i++)
            currentLookback.setData(i, (int) currentLookback.getData(i));

        int loop = inSignal.getSize();

        double[] inSeries = new double[loop];
        Arrays.fill(inSeries, 0.0);
        double[] offset = new double[loop];
        Arrays.fill(offset, 0.0);
        double[] mom = new double[loop];
        Arrays.fill(mom, 0.0);
        double[] posSeries = new double[loop];
        Arrays.fill(posSeries, 0.0);
        double[] negSeries = new double[loop];
        Arrays.fill(negSeries, 0.0);
        double[] sumPos = new double[loop];
        Arrays.fill(sumPos, 0.0);
        double[] sumNeg = new double[loop];
        Arrays.fill(sumNeg, 0.0);
        double[] cmoUp = new double[loop];
        Arrays.fill(cmoUp, 0.0);
        double[] cmoDown = new double[loop];
        Arrays.fill(cmoDown, 0.0);
        double[] currentLook = new double[loop];
        Arrays.fill(currentLook, 0.0);
        double[] adaptCmo = new double[loop];
        Arrays.fill(adaptCmo, 0.0);

        // line up data
        int index = currentLook.length - 1;
        for (i = currentLookback.getSize() - 1; i >= 0; i--)
        {
            currentLook[index] = currentLookback.getData(i);
            index--;
        }

        for (i = 0; i < loop; i++)
            inSeries[i] = inSignal.getData(i);

        for (i = iStdPeriod - 1; i < loop; i++)
        {
            offset[i] = inSeries[i - 1];

            mom[i] = inSeries[i] - offset[i];

            if (mom[i] > 0)
                posSeries[i] = mom[i];
            else
                posSeries[i] = 0;

            if (mom[i] < 0)
                negSeries[i] = Math.abs(mom[i]);
            else
                negSeries[i] = 0;

            int j = 0;
            double sumUp = 0;
            double sumDown = 0;

            for (j = 0; j < (int) currentLook[i]; j++)
            {
                sumUp += posSeries[i - j];
                sumDown += negSeries[i - j];
            }

            sumPos[i] = sumUp;
            sumNeg[i] = sumDown;

            cmoUp[i] = 100 * ((sumPos[i] - sumNeg[i]));

            cmoDown[i] = sumPos[i] + sumNeg[i];

            adaptCmo[i] = cmoUp[i] / cmoDown[i];

            if (i > iStdPeriod - 1)
                outSignal.append(adaptCmo[i]);
        }
    }

    void getStdDev(PlotLine outLine, PlotLine inLine, int iPeriod)
    {
        int loop;

        for (loop = iPeriod - 1; loop < inLine.getSize(); loop++)
        {
            double mean = 0;
            int loop2;
            for (loop2 = 0; loop2 < iPeriod; loop2++)
                mean += inLine.getData(loop - loop2);

            mean /= (double) iPeriod;

            double ds = 0;
            for (loop2 = 0; loop2 < iPeriod; loop2++)
            {
                double t = inLine.getData(loop - loop2) - mean;
                ds += (t * t);
            }

            ds = Math.sqrt(ds / (double) period);
            outLine.append(ds);
        }
    }

    void getNorm(PlotLine inSig, double iMin, double iMax)
    {
        //    I = Imin + (Imax-Imin)*(V-Vmin)/(Vmax-Vmin)

        int i = 0;
        double max = -999999;
        double min = 999999;
        double norm = 0;
        for (i = 0; i < inSig.getSize(); i++)
        {
            if (inSig.getData(i) > max)
                max = inSig.getData(i);

            if (inSig.getData(i) < min)
                min = inSig.getData(i);
        }

        for (i = 0; i < inSig.getSize(); i++)
        {
            norm = iMin + (iMax - iMin) * ((inSig.getData(i) - min) / (max - min));
            inSig.setData(i, norm);
        }
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
        volPeriod = settings.getInteger("volPeriod", volPeriod).intValue(); //$NON-NLS-1$
    }
}
