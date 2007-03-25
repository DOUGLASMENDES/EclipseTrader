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

public class BBANDS extends Factory
{
    private static final String DEFAULT_LABEL = Messages.BBANDS_DefaultLabel;
    private static final int DEFAULT_LINETYPE = PlotLine.LINE;
    private static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    private static final int DEFAULT_INPUT = BarData.CLOSE;
    private static final int DEFAULT_PERIOD = 14;
    private static final double DEFAULT_UPPER_DEVIATION = 2;
    private static final double DEFAULT_LOWER_DEVIATION = 2;
    private static final int DEFAULT_MA_TYPE = 0;
    private static final boolean DEFAULT_SCALE_FLAG = false;

    public BBANDS()
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
            private int period = DEFAULT_PERIOD;
            private double upperDeviation = DEFAULT_UPPER_DEVIATION;
            private double lowerDeviation = DEFAULT_LOWER_DEVIATION;
            private int maType = DEFAULT_MA_TYPE;
            private boolean scaleFlag = DEFAULT_SCALE_FLAG;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                double[] inReal = getInput(getBarData(), input);
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outUpper = getOutputArray(getBarData(), TALibPlugin.getCore().bbandsLookback(period, upperDeviation, lowerDeviation, getTA_MAType(maType)));
                double[] outMiddle = getOutputArray(getBarData(), TALibPlugin.getCore().bbandsLookback(period, upperDeviation, lowerDeviation, getTA_MAType(maType)));
                double[] outLower = getOutputArray(getBarData(), TALibPlugin.getCore().bbandsLookback(period, upperDeviation, lowerDeviation, getTA_MAType(maType)));
                
                TALibPlugin.getCore().bbands(startIdx, endIdx, inReal, period, upperDeviation, lowerDeviation, getTA_MAType(maType), outBegIdx, outNbElement, outUpper, outMiddle, outLower);
                
                PlotLine upperLine = new PlotLine();
                PlotLine middleLine = new PlotLine();
                PlotLine lowerLine = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                {
                    upperLine.append(outUpper[i]);
                    middleLine.append(outMiddle[i]);
                    lowerLine.append(outLower[i]);
                }
                
                upperLine.setColor(color);
                upperLine.setType(lineType);
                upperLine.setLabel(label + Messages.BBANDS_DefaultLabelUp);
                
                middleLine.setColor(color);
                middleLine.setType(PlotLine.DOT);
                middleLine.setLabel(Messages.BBANDS_DefaultLabelMid);
                
                lowerLine.setColor(color);
                lowerLine.setType(lineType);
                lowerLine.setLabel(Messages.BBANDS_DefaultLabelLow);

                getOutput().add(upperLine);
                getOutput().add(middleLine);
                getOutput().add(lowerLine);

                getOutput().setScaleFlag(scaleFlag);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label); //$NON-NLS-1$
                lineType = settings.getInteger("lineType", lineType).intValue(); //$NON-NLS-1$
                color = settings.getColor("color", color); //$NON-NLS-1$
                input = settings.getInteger("input", input).intValue(); //$NON-NLS-1$
                period = settings.getInteger("period", period).intValue(); //$NON-NLS-1$
                maType = settings.getInteger("maType", maType).intValue(); //$NON-NLS-1$
                upperDeviation = settings.getDouble("upperDeviation", upperDeviation).doubleValue(); //$NON-NLS-1$
                lowerDeviation = settings.getDouble("lowerDeviation", lowerDeviation).doubleValue(); //$NON-NLS-1$
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

                addColorSelector(content, "color", Messages.BBANDS_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.BBANDS_Label, DEFAULT_LABEL); //$NON-NLS-1$
                addBooleanSelector(content, "scaleFlag", Messages.BBANDS_UseOwnScale, DEFAULT_SCALE_FLAG); //$NON-NLS-1$

                addLineTypeSelector(content, "lineType", Messages.BBANDS_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addInputSelector(content, "input", Messages.BBANDS_Input, DEFAULT_INPUT, false); //$NON-NLS-1$
                addIntegerValueSelector(content, "period", Messages.BBANDS_Period, 1, 9999, DEFAULT_PERIOD); //$NON-NLS-1$
                addMovingAverageSelector(content, "maType", Messages.BBANDS_MAType, DEFAULT_MA_TYPE); //$NON-NLS-1$
                addDoubleValueSelector(content, "upperDeviation", Messages.BBANDS_UpperDeviation, 2, 0, 999999, DEFAULT_UPPER_DEVIATION); //$NON-NLS-1$
                addDoubleValueSelector(content, "lowerDeviation", Messages.BBANDS_LowerDeviation, 2, 0, 999999, DEFAULT_LOWER_DEVIATION); //$NON-NLS-1$
            }
        };

        return page;
    }

}
