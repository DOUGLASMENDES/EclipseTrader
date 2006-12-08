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
    private static final String DEFAULT_LABEL = "MACD";
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final String DEFAULT_SIGNAL_LABEL = "SIG";
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
                label = settings.getString("label", label);
                lineType = settings.getInteger("lineType", lineType).intValue();
                color = settings.getColor("color", color);
                fastPeriod = settings.getInteger("fastPeriod", fastPeriod).intValue();
                fastMaType = settings.getInteger("fastMaType", fastMaType).intValue();
                slowPeriod = settings.getInteger("slowPeriod", slowPeriod).intValue();
                slowMaType = settings.getInteger("slowMaType", slowMaType).intValue();
                signalLabel = settings.getString("signalLabel", signalLabel);
                signalLineType = settings.getInteger("signalLineType", signalLineType).intValue();
                signalColor = settings.getColor("signalColor", signalColor);
                signalPeriod = settings.getInteger("signalPeriod", signalPeriod).intValue();
                signalMaType = settings.getInteger("signalMaType", signalMaType).intValue();
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
                addIntegerValueSelector(content, "fastPeriod", "Fast Period", 1, 9999, DEFAULT_FAST_PERIOD);
                addMovingAverageSelector(content, "fastMaType", "Fast MA Type", DEFAULT_FAST_MA_TYPE);
                addIntegerValueSelector(content, "slowPeriod", "Slow Period", 1, 9999, DEFAULT_SLOW_PERIOD);
                addMovingAverageSelector(content, "slowMaType", "Slow MA Type", DEFAULT_SLOW_MA_TYPE);
                addColorSelector(content, "signalColor", "Signal Color", DEFAULT_SIGNAL_COLOR);
                addLabelField(content, "signalLabel", "Signal Label", DEFAULT_SIGNAL_LABEL);
                addLineTypeSelector(content, "signalLineType", "Signal Line Type", DEFAULT_SIGNAL_LINETYPE);
                addIntegerValueSelector(content, "signalPeriod", "Signal Period", 1, 9999, DEFAULT_SIGNAL_PERIOD);
                addMovingAverageSelector(content, "signalMaType", "Signal MA Type", DEFAULT_SIGNAL_MA_TYPE);
            }
        };

        return page;
    }

}
