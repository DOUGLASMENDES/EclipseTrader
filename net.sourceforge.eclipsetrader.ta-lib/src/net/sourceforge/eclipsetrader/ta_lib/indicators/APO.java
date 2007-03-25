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

public class APO extends Factory
{
    private static final String DEFAULT_LABEL = Messages.APO_DefaultLabel;
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final int DEFAULT_INPUT = BarData.CLOSE;
    private static final int DEFAULT_FAST_PERIOD = 3;
    private static final int DEFAULT_SLOW_PERIOD = 10;
    private static final int DEFAULT_MA_TYPE = 0;

    public APO()
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
            private int input = DEFAULT_INPUT;
            private int fastPeriod = DEFAULT_FAST_PERIOD;
            private int slowPeriod = DEFAULT_SLOW_PERIOD;
            private int maType = DEFAULT_MA_TYPE;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                double[] inReal = getInput(getBarData(), input);
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outReal = getOutputArray(getBarData(), TALibPlugin.getCore().apoLookback(fastPeriod, slowPeriod, getTA_MAType(maType)));
                
                TALibPlugin.getCore().apo(startIdx, endIdx, inReal, fastPeriod, slowPeriod, getTA_MAType(maType), outBegIdx, outNbElement, outReal);
                
                PlotLine line = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                    line.append(outReal[i]);

                line.setLabel(label);
                line.setType(lineType);
                line.setColor(color);
                getOutput().add(line);

                getOutput().setScaleFlag(true);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label); //$NON-NLS-1$
                lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
                color = settings.getColor("color", color); //$NON-NLS-1$
                fastPeriod = settings.getInteger("fastPeriod", fastPeriod).intValue(); //$NON-NLS-1$
                slowPeriod = settings.getInteger("slowPeriod", slowPeriod).intValue(); //$NON-NLS-1$
                maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
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

                addColorSelector(content, "color", Messages.APO_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.APO_Label, DEFAULT_LABEL); //$NON-NLS-1$

                addLineTypeSelector(content, "lineType", Messages.APO_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "fastPeriod", Messages.APO_FastPeriod, 1, 9999, DEFAULT_FAST_PERIOD); //$NON-NLS-1$
                addIntegerValueSelector(content, "slowPeriod", Messages.APO_SlowPeriod, 1, 9999, DEFAULT_SLOW_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "type", Messages.APO_MAType, DEFAULT_MA_TYPE); //$NON-NLS-1$
            }
        };

        return page;
    }

}
