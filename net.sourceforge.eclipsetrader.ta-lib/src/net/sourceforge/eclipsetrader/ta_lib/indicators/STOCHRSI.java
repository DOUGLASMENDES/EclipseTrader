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
    private static final String DEFAULT_LABEL = Messages.STOCHRSI_DefaultLabel;
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final int DEFAULT_INPUT = BarData.CLOSE;
    private static final int DEFAULT_PERIOD = 3;
    private static final int DEFAULT_FAST_K_PERIOD = 5;
    private static final int DEFAULT_FAST_D_PERIOD = 3;
    private static final int DEFAULT_FAST_D_MA_TYPE = 0;
    private static final String DEFAULT_D_LABEL = Messages.STOCHRSI_DefaultLabelD;
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

                getOutput().setScaleFlag(true);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label); //$NON-NLS-1$
                lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
                color = settings.getColor("color", color); //$NON-NLS-1$

                period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
                fastKPeriod = settings.getInteger("fastKPeriod", fastKPeriod).intValue(); //$NON-NLS-1$

                dlabel = settings.getString("dlabel", dlabel); //$NON-NLS-1$
                dlineType = settings.getInteger("dlineType", dlineType).intValue(); //$NON-NLS-1$
                dcolor = settings.getColor("dcolor", dcolor); //$NON-NLS-1$

                fastDPeriod = settings.getInteger("fastDPeriod", fastDPeriod).intValue(); //$NON-NLS-1$
                fastDMAType = settings.getInteger("fastDMAType", fastDMAType).intValue(); //$NON-NLS-1$
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

                addColorSelector(content, "color", Messages.STOCHRSI_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.STOCHRSI_Label, DEFAULT_LABEL); //$NON-NLS-1$
                addLineTypeSelector(content, "lineType", Messages.STOCHRSI_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "period", Messages.STOCHRSI_Period, 1, 9999, DEFAULT_PERIOD); //$NON-NLS-1$
                addIntegerValueSelector(content, "fastKPeriod", Messages.STOCHRSI_FastKPeriod, 1, 9999, DEFAULT_FAST_K_PERIOD); //$NON-NLS-1$
                addColorSelector(content, "dcolor", Messages.STOCHRSI_ColorD, DEFAULT_D_COLOR); //$NON-NLS-1$
                addLabelField(content, "dlabel", Messages.STOCHRSI_LabelD, DEFAULT_D_LABEL); //$NON-NLS-1$
                addLineTypeSelector(content, "dlineType", Messages.STOCHRSI_LineTypeD, DEFAULT_D_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "fastDPeriod", Messages.STOCHRSI_FastDPeriod, 1, 9999, DEFAULT_FAST_D_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "fastDMAtype", Messages.STOCHRSI_FastDMAType, DEFAULT_FAST_D_MA_TYPE); //$NON-NLS-1$
            }
        };

        return page;
    }

}
