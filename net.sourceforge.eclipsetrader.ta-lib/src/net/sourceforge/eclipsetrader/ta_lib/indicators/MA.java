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

public class MA extends Factory
{
    private static final String DEFAULT_LABEL = Messages.MA_DefaultLabel;
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final int DEFAULT_INPUT = BarData.CLOSE;
    private static final int DEFAULT_PERIOD = 14;
    private static final int DEFAULT_TYPE = 0;
    private static final boolean DEFAULT_SCALE_FLAG = false;

    public MA()
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
            private int input = DEFAULT_INPUT;
            private int type = DEFAULT_TYPE;
            private int period = DEFAULT_PERIOD;
            private Color color = new Color(null, DEFAULT_COLOR);
            private boolean scaleFlag = DEFAULT_SCALE_FLAG;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;
                double[] inReal = getInput(getBarData(), input);

                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outReal = getOutputArray(getBarData(), TALibPlugin.getCore().MA_Lookback(period, getTA_MAType(type)));
                
                TALibPlugin.getCore().MA(startIdx, endIdx, inReal, period, getTA_MAType(type), outBegIdx, outNbElement, outReal);
                
                PlotLine line = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                    line.append(outReal[i]);

                if (getBarData().getMax() > line.getHigh())
                    line.setHigh(getBarData().getMax());
                if (getBarData().getMin() < line.getLow())
                    line.setLow(getBarData().getMin());

                line.setLabel(label);
                line.setType(lineType);
                line.setColor(color);
                getOutput().add(line);

                getOutput().setScaleFlag(scaleFlag);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label); //$NON-NLS-1$
                lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
                input = settings.getInteger("input", input).intValue(); //$NON-NLS-1$
                period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
                type = settings.getInteger("type", type).intValue(); //$NON-NLS-1$
                color = settings.getColor("color", color); //$NON-NLS-1$
                scaleFlag = settings.getBoolean("scaleFlag", scaleFlag); //$NON-NLS-1$
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

                addColorSelector(content, "color", Messages.MA_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.MA_Label, DEFAULT_LABEL); //$NON-NLS-1$
                addBooleanSelector(content, "scaleFlag", Messages.MA_UseOwnScale, DEFAULT_SCALE_FLAG); //$NON-NLS-1$

                addLineTypeSelector(content, "lineType", Messages.MA_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addInputSelector(content, "input", Messages.MA_Input, DEFAULT_INPUT, true); //$NON-NLS-1$
                addIntegerValueSelector(content, "period", Messages.MA_Period, 1, 9999, DEFAULT_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "type", Messages.MA_Type, DEFAULT_TYPE); //$NON-NLS-1$
            }
        };

        return page;
    }

}
