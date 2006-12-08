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

package net.sourceforge.eclipsetrader.ta_lib.indicators;

import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.ta_lib.Factory;
import net.sourceforge.eclipsetrader.ta_lib.TALibPlugin;
import net.sourceforge.eclipsetrader.ta_lib.internal.TALibIndicatorPlugin;
import net.sourceforge.eclipsetrader.ta_lib.internal.TALibIndicatorPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.tictactec.ta.lib.MInteger;

public class STOCH extends Factory
{
    public static final String DEFAULT_LABEL = "STOCH";
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_FAST_K_PERIOD = 5;
    public static final int DEFAULT_SLOW_K_PERIOD = 3;
    private static final int DEFAULT_SLOW_K_MA_TYPE = 0;
    public static final String DEFAULT_D_LABEL = "%D";
    public static final int DEFAULT_D_LINETYPE = PlotLine.DOT;
    public static final RGB DEFAULT_D_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_SLOW_D_PERIOD = 3;
    private static final int DEFAULT_SLOW_D_MA_TYPE = 0;

    public STOCH()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.ta_lib.Factory#createIndicator()
     */
    public IndicatorPlugin createIndicator()
    {
        IndicatorPlugin indicator = new TALibIndicatorPlugin() {
            private String label = DEFAULT_LABEL;
            private int lineType = DEFAULT_LINETYPE;
            private Color color = new Color(null, DEFAULT_COLOR);
            private int fastKPeriod = DEFAULT_FAST_K_PERIOD;
            private int slowKPeriod = DEFAULT_SLOW_K_PERIOD;
            private int slowKMAType = DEFAULT_SLOW_K_MA_TYPE;
            private String dlabel = DEFAULT_D_LABEL;
            private int dlineType = DEFAULT_D_LINETYPE;
            private Color dcolor = new Color(null, DEFAULT_D_COLOR);
            private int slowDPeriod = DEFAULT_SLOW_D_PERIOD;
            private int slowDMAType = DEFAULT_SLOW_D_MA_TYPE;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                Object[] values = getInput(getBarData());
                double[] inHigh = (double[])values[BarData.HIGH];
                double[] inLow = (double[])values[BarData.LOW];
                double[] inClose = (double[])values[BarData.CLOSE];
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outSlowK = getOutputArray(getBarData(), TALibPlugin.getCore().STOCH_Lookback(fastKPeriod, slowKPeriod, getTA_MAType(slowKMAType), slowDPeriod, getTA_MAType(slowDMAType)));
                double[] outSlowD = getOutputArray(getBarData(), TALibPlugin.getCore().STOCH_Lookback(fastKPeriod, slowKPeriod, getTA_MAType(slowKMAType), slowDPeriod, getTA_MAType(slowDMAType)));
                
                TALibPlugin.getCore().STOCH(startIdx, endIdx, inHigh, inLow, inClose, fastKPeriod, slowKPeriod, getTA_MAType(slowKMAType), slowDPeriod, getTA_MAType(slowDMAType), outBegIdx, outNbElement, outSlowK, outSlowD);
                
                PlotLine line = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                    line.append(outSlowK[i]);
                line.setLabel(label);
                line.setType(lineType);
                line.setColor(color);
                getOutput().add(line);
                
                line = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                    line.append(outSlowD[i]);
                line.setLabel(dlabel);
                line.setType(dlineType);
                line.setColor(dcolor);
                getOutput().add(line);

                getOutput().setScaleFlag(true);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label);
                lineType = settings.getInteger("lineType", lineType).intValue();
                color = settings.getColor("color", color);

                fastKPeriod = settings.getInteger("fastKPeriod", fastKPeriod).intValue();
                slowKPeriod = settings.getInteger("slowKPeriod", slowKPeriod).intValue();
                slowKMAType = settings.getInteger("slowKMAType", slowKMAType).intValue();

                dlabel = settings.getString("dlabel", dlabel);
                dlineType = settings.getInteger("dlineType", dlineType).intValue();
                dcolor = settings.getColor("dcolor", dcolor);

                slowDPeriod = settings.getInteger("slowDPeriod", slowDPeriod).intValue();
                slowDMAType = settings.getInteger("slowDMAType", slowDMAType).intValue();
            }
        };

        return indicator;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.ta_lib.Factory#createPreferencePage()
     */
    public IndicatorPluginPreferencePage createPreferencePage()
    {
        IndicatorPluginPreferencePage page = new TALibIndicatorPreferencePage() {

            public void createControl(Composite parent)
            {
                Composite content = new Composite(parent, SWT.NONE);
                GridLayout gridLayout = new GridLayout(2, false);
                gridLayout.marginWidth = gridLayout.marginHeight = 0;
                content.setLayout(gridLayout);
                content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
                setControl(content);

                addColorSelector(content, "color", "Color", DEFAULT_COLOR);
                addLabelField(content, "label", "Label", DEFAULT_LABEL);
                addLineTypeSelector(content, "lineType", "Line Type", DEFAULT_LINETYPE);
                addIntegerValueSelector(content, "fastKPeriod", "Fast %K Period", 1, 9999, DEFAULT_FAST_K_PERIOD);
                addIntegerValueSelector(content, "slowKPeriod", "Slow %K Period", 1, 9999, DEFAULT_SLOW_K_PERIOD);
                addMovingAverageSelector(content, "slowKMAtype", "Slow %K MA Type", DEFAULT_SLOW_K_MA_TYPE);
                addColorSelector(content, "dcolor", "%D Color", DEFAULT_D_COLOR);
                addLabelField(content, "dlabel", "%D Label", DEFAULT_D_LABEL);
                addLineTypeSelector(content, "dlineType", "%D Line Type", DEFAULT_D_LINETYPE);
                addIntegerValueSelector(content, "slowDPeriod", "Slow %D Period", 1, 9999, DEFAULT_SLOW_D_PERIOD);
                addMovingAverageSelector(content, "slowDMAtype", "Slow %D MA Type", DEFAULT_SLOW_K_MA_TYPE);
            }
        };

        return page;
    }

}
