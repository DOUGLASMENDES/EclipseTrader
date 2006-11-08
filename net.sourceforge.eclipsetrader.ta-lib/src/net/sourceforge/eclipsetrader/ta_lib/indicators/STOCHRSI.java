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

public class STOCHRSI extends Factory
{
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final String DEFAULT_LABEL = "STOCHRSI";
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final int DEFAULT_INPUT = BarData.CLOSE;
    private static final int DEFAULT_PERIOD = 3;
    private static final int DEFAULT_FAST_K_PERIOD = 5;
    private static final int DEFAULT_FAST_D_PERIOD = 3;
    private static final int DEFAULT_FAST_D_MA_TYPE = 0;
    private static final String DEFAULT_D_LABEL = "%D";
    private static final int DEFAULT_D_LINETYPE = PlotLine.DOT;
    private static final RGB DEFAULT_D_COLOR = new RGB(0, 0, 192);

    public STOCHRSI()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.ta_lib.Factory#createIndicator()
     */
    public IndicatorPlugin createIndicator()
    {
        IndicatorPlugin indicator = new TALibIndicatorPlugin() {
            private Color color = new Color(null, DEFAULT_COLOR);
            private String label = DEFAULT_LABEL;
            private int lineType = DEFAULT_LINETYPE;
            private int input = DEFAULT_INPUT;
            private int period = DEFAULT_PERIOD;
            private int fastKPeriod = DEFAULT_FAST_K_PERIOD;
            private Color dcolor = new Color(null, DEFAULT_D_COLOR);
            private String dlabel = DEFAULT_D_LABEL;
            private int dlineType = DEFAULT_D_LINETYPE;
            private int fastDPeriod = DEFAULT_FAST_D_PERIOD;
            private int fastDMAType = DEFAULT_FAST_D_MA_TYPE;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                double[] inReal = getInput(getBarData(), input);
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outSlowK = getOutputArray(getBarData(), TALibPlugin.getCore().STOCHRSI_Lookback(period, fastKPeriod, fastDPeriod, getTA_MAType(fastDMAType)));
                double[] outSlowD = getOutputArray(getBarData(), TALibPlugin.getCore().STOCHRSI_Lookback(period, fastKPeriod, fastDPeriod, getTA_MAType(fastDMAType)));
                
                TALibPlugin.getCore().STOCHRSI(startIdx, endIdx, inReal, period, fastKPeriod, fastDPeriod, getTA_MAType(fastDMAType), outBegIdx, outNbElement, outSlowK, outSlowD);
                
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
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label);
                lineType = settings.getInteger("lineType", lineType).intValue();
                color = settings.getColor("color", color);

                period = settings.getInteger("period", period).intValue();
                fastKPeriod = settings.getInteger("fastKPeriod", fastKPeriod).intValue();

                dlabel = settings.getString("dlabel", dlabel);
                dlineType = settings.getInteger("dlineType", dlineType).intValue();
                dcolor = settings.getColor("dcolor", dcolor);

                fastDPeriod = settings.getInteger("fastDPeriod", fastDPeriod).intValue();
                fastDMAType = settings.getInteger("fastDMAType", fastDMAType).intValue();
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
                addIntegerValueSelector(content, "period", "Period", 1, 9999, DEFAULT_PERIOD);
                addIntegerValueSelector(content, "fastKPeriod", "Fast %K Period", 1, 9999, DEFAULT_FAST_K_PERIOD);
                addColorSelector(content, "dcolor", "%D Color", DEFAULT_D_COLOR);
                addLabelField(content, "dlabel", "%D Label", DEFAULT_D_LABEL);
                addLineTypeSelector(content, "dlineType", "%D Line Type", DEFAULT_D_LINETYPE);
                addIntegerValueSelector(content, "fastDPeriod", "Fast %D Period", 1, 9999, DEFAULT_FAST_D_PERIOD);
                addMovingAverageSelector(content, "fastDMAtype", "Fast %D MA Type", DEFAULT_FAST_D_MA_TYPE);
            }
        };

        return page;
    }

}
