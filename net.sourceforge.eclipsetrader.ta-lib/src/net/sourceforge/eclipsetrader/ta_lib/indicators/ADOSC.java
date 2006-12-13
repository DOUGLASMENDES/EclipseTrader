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

public class ADOSC extends Factory
{
    public static final String DEFAULT_LABEL = Messages.ADOSC_DefaultLabel;
    public static final int DEFAULT_LINETYPE = PlotLine.LINE;
    public static final RGB DEFAULT_COLOR = new RGB(0, 0, 192);
    public static final int DEFAULT_FAST_PERIOD = 3;
    public static final int DEFAULT_SLOW_PERIOD = 10;

    public ADOSC()
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
            private int fastPeriod = DEFAULT_FAST_PERIOD;
            private int slowPeriod = DEFAULT_SLOW_PERIOD;

            public void calculate()
            {
                int startIdx = 0;
                int endIdx = getBarData().size() - 1;

                Object[] values = getInput(getBarData());
                double[] inHigh = (double[])values[BarData.HIGH];
                double[] inLow = (double[])values[BarData.LOW];
                double[] inClose = (double[])values[BarData.CLOSE];
                int[] inVolume = (int[])values[BarData.VOLUME];

                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                
                double[] outReal = getOutputArray(getBarData(), TALibPlugin.getCore().ADOSC_Lookback(fastPeriod, slowPeriod));
                
                TALibPlugin.getCore().ADOSC(startIdx, endIdx, inHigh, inLow, inClose, inVolume, fastPeriod, slowPeriod, outBegIdx, outNbElement, outReal);
                
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

                addColorSelector(content, "color", Messages.ADOSC_Color, DEFAULT_COLOR); //$NON-NLS-1$
                addLabelField(content, "label", Messages.ADOSC_Label, DEFAULT_LABEL); //$NON-NLS-1$
                addLineTypeSelector(content, "lineType", Messages.ADOSC_LineType, DEFAULT_LINETYPE); //$NON-NLS-1$
                addIntegerValueSelector(content, "fastPeriod", Messages.ADOSC_FastPeriod, 1, 9999, DEFAULT_FAST_PERIOD); //$NON-NLS-1$
                addIntegerValueSelector(content, "slowPeriod", Messages.ADOSC_SlowPeriod, 1, 9999, DEFAULT_SLOW_PERIOD); //$NON-NLS-1$
            }
        };

        return page;
    }

}
