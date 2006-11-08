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

public class SAR extends Factory
{
    public static final String DEFAULT_LABEL = "SAR";
    public static final int DEFAULT_LINETYPE = PlotLine.DOT;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final double DEFAULT_ACCELERATION = 0.02;
    public static final double DEFAULT_MAXIMUM = 0.20;

    public SAR()
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
            private double acceleration = DEFAULT_ACCELERATION;
            private double maximum = DEFAULT_MAXIMUM;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                Object[] values = getInput(getBarData());
                double[] inHigh = (double[])values[BarData.HIGH];
                double[] inLow = (double[])values[BarData.LOW];
                
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outReal = getOutputArray(getBarData(), TALibPlugin.getCore().SAR_Lookback(acceleration, maximum));
                
                TALibPlugin.getCore().SAR(startIdx, endIdx, inHigh, inLow, acceleration, maximum, outBegIdx, outNbElement, outReal);
                
                PlotLine line = new PlotLine();
                for (int i = 0; i < outNbElement.value; i++)
                    line.append(outReal[i]);

                line.setLabel(label);
                line.setType(lineType);
                line.setColor(color);
                getOutput().add(line);
            }

            public void setParameters(Settings settings)
            {
                label = settings.getString("label", label);
                lineType = settings.getInteger("lineType", lineType).intValue();
                color = settings.getColor("color", color);
                acceleration = settings.getDouble("acceleration", acceleration).doubleValue();
                maximum = settings.getDouble("maximum", maximum).doubleValue();
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
                addDoubleValueSelector(content, "acceleration", "Acceleration", 4, 0.0001, 9.9999, DEFAULT_ACCELERATION);
                addDoubleValueSelector(content, "maximum", "Maximum", 4, 0.0001, 9.9999, DEFAULT_MAXIMUM);
            }
        };

        return page;
    }

}
