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
import com.tictactec.ta.lib.TA_MAType;

public class MACD extends Factory
{
    private static final String DEFAULT_LABEL = Messages.MACD_DefaultLabel;
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final String DEFAULT_SIGNAL_LABEL = Messages.MACD_DefaultLabelSIG;
    private static final int DEFAULT_SIGNAL_LINETYPE = PlotLine.DOT;
    private static final RGB DEFAULT_SIGNAL_COLOR = new RGB(0, 0, 192);
    private static final int DEFAULT_FAST_PERIOD = 12;
    private static final int DEFAULT_FAST_MA_TYPE = 0;
    private static final int DEFAULT_SLOW_PERIOD = 26;
    private static final int DEFAULT_SLOW_MA_TYPE = 0;
    private static final int DEFAULT_SIGNAL_PERIOD = 9;
    private static final int DEFAULT_SIGNAL_MA_TYPE = 0;

    public MACD()
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
            private String signalLabel = DEFAULT_SIGNAL_LABEL;
            private int signalLineType = DEFAULT_SIGNAL_LINETYPE;
            private Color signalColor = new Color(null, DEFAULT_SIGNAL_COLOR);
            private int fastPeriod = DEFAULT_FAST_PERIOD;
            private int fastMaType = DEFAULT_FAST_MA_TYPE;
            private int slowPeriod = DEFAULT_SLOW_PERIOD;
            private int slowMaType = DEFAULT_SLOW_MA_TYPE;
            private int signalPeriod = DEFAULT_SIGNAL_PERIOD;
            private int signalMaType = DEFAULT_SIGNAL_MA_TYPE;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                double[] inReal = getInput(getBarData(), BarData.CLOSE);
                TA_MAType fastMA = getTA_MAType(fastMaType);
                TA_MAType slowMA = getTA_MAType(slowMaType);
                TA_MAType signalMA = getTA_MAType(signalMaType);
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outMACD = getOutputArray(getBarData(), TALibPlugin.getCore().MACDEXT_Lookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA));
                double[] outSignal = getOutputArray(getBarData(), TALibPlugin.getCore().MACDEXT_Lookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA));
                double[] outHist = getOutputArray(getBarData(), TALibPlugin.getCore().MACDEXT_Lookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA));
                
                TALibPlugin.getCore().MACDEXT(startIdx, endIdx, inReal, fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA, outBegIdx, outNbElement, outMACD, outSignal, outHist);
                
                PlotLine line = new PlotLine();
                PlotLine signal = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                {
                    line.append(outMACD[i]);
                    signal.append(outSignal[i]);
                }

                line.setLabel(label);
                line.setType(lineType);
                line.setColor(color);
                getOutput().add(line);

                signal.setLabel(signalLabel);
                signal.setType(signalLineType);
                signal.setColor(signalColor);
                getOutput().add(signal);

                getOutput().setScaleFlag(true);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label); //$NON-NLS-1$
                lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
                color = settings.getColor("color", color); //$NON-NLS-1$
                fastPeriod = settings.getInteger("fastPeriod", fastPeriod).intValue(); //$NON-NLS-1$
                fastMaType = settings.getInteger("fastMaType", fastMaType).intValue(); //$NON-NLS-1$
                slowPeriod = settings.getInteger("slowPeriod", slowPeriod).intValue(); //$NON-NLS-1$
                slowMaType = settings.getInteger("slowMaType", slowMaType).intValue(); //$NON-NLS-1$
                signalLabel = settings.getString("signalLabel", signalLabel); //$NON-NLS-1$
                signalLineType = settings.getInteger("signalLineType", signalLineType).intValue(); //$NON-NLS-1$
                signalColor = settings.getColor("signalColor", signalColor); //$NON-NLS-1$
                signalPeriod = settings.getInteger("signalPeriod", signalPeriod).intValue(); //$NON-NLS-1$
                signalMaType = settings.getInteger("signalMaType", signalMaType).intValue(); //$NON-NLS-1$
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

                addColorSelector(content, "color", Messages.MACD_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.MACD_Label, DEFAULT_LABEL); //$NON-NLS-1$
                addLineTypeSelector(content, "lineType", Messages.MACD_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "fastPeriod", Messages.MACD_FastPeriod, 1, 9999, DEFAULT_FAST_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "fastMaType", Messages.MACD_FastMAType, DEFAULT_FAST_MA_TYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "slowPeriod", Messages.MACD_SlowPeriod, 1, 9999, DEFAULT_SLOW_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "slowMaType", Messages.MACD_SlowMAType, DEFAULT_SLOW_MA_TYPE); //$NON-NLS-1$
                addColorSelector(content, "signalColor", Messages.MACD_SignalColor, DEFAULT_SIGNAL_COLOR); //$NON-NLS-1$
                addLabelField(content, "signalLabel", Messages.MACD_SignalLabel, DEFAULT_SIGNAL_LABEL); //$NON-NLS-1$
                addLineTypeSelector(content, "signalLineType", Messages.MACD_SignalLineType, DEFAULT_SIGNAL_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "signalPeriod", Messages.MACD_SignalPeriod, 1, 9999, DEFAULT_SIGNAL_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "signalMaType", Messages.MACD_SignalMAType, DEFAULT_SIGNAL_MA_TYPE); //$NON-NLS-1$
            }
        };

        return page;
    }

}
