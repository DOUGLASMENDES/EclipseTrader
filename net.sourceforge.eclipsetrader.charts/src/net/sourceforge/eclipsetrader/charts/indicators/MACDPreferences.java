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

package net.sourceforge.eclipsetrader.charts.indicators;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MACDPreferences extends IndicatorPluginPreferencePage
{

    public MACDPreferences()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);
        
        addColorSelector(content, "color", Messages.MACDPreferences_Color, MACD.DEFAULT_COLOR); //$NON-NLS-1$
        addIntegerValueSelector(content, "fastPeriod", Messages.MACDPreferences_FastPeriod, 1, 9999, MACD.DEFAULT_FAST_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "slowPeriod", Messages.MACDPreferences_SlowPeriod, 1, 9999, MACD.DEFAULT_SLOW_PERIOD); //$NON-NLS-1$
        addLabelField(content, "label", Messages.MACDPreferences_Label, MACD.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.MACDPreferences_LineType, MACD.DEFAULT_LINETYPE); //$NON-NLS-1$
        addMovingAverageSelector(content, "maType", Messages.MACDPreferences_MAType, MACD.DEFAULT_MA_TYPE); //$NON-NLS-1$
        addInputSelector(content, "input", Messages.MACDPreferences_Input, MACD.DEFAULT_INPUT, false); //$NON-NLS-1$
        
        addColorSelector(content, "triggerColor", Messages.MACDPreferences_TriggerColor, MACD.DEFAULT_TRIGGER_COLOR); //$NON-NLS-1$
        addIntegerValueSelector(content, "triggerPeriod", Messages.MACDPreferences_TriggerPeriod, 1, 9999, MACD.DEFAULT_TRIGGER_PERIOD); //$NON-NLS-1$
        addLabelField(content, "triggerLabel", Messages.MACDPreferences_TriggerLabel, MACD.DEFAULT_TRIGGER_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "triggerLineType", Messages.MACDPreferences_TriggerLineType, MACD.DEFAULT_TRIGGER_LINETYPE); //$NON-NLS-1$
        
        addColorSelector(content, "oscColor", Messages.MACDPreferences_OscColor, MACD.DEFAULT_OSC_COLOR); //$NON-NLS-1$
        addLabelField(content, "oscLabel", Messages.MACDPreferences_OscLabel, MACD.DEFAULT_OSC_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "oscLineType", Messages.MACDPreferences_OscLineType, MACD.DEFAULT_OSC_LINETYPE); //$NON-NLS-1$
    }
}
