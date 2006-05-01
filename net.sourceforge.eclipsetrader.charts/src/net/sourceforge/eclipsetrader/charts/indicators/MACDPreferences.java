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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MACDPreferences extends IndicatorPluginPreferencePage
{
    private Combo input;

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
        
        addColorSelector(content, "color", "MACD Color", MACD.DEFAULT_COLOR);
        addIntegerValueSelector(content, "fastPeriod", "Fast Period", 1, 9999, MACD.DEFAULT_FAST_PERIOD);
        addIntegerValueSelector(content, "slowPeriod", "Slow Period", 1, 9999, MACD.DEFAULT_SLOW_PERIOD);
        addLabelField(content, "label", "MACD Label", MACD.DEFAULT_LABEL);
        addLineTypeSelector(content, "lineType", "MACD Line Type", MACD.DEFAULT_LINETYPE);
        addMovingAverageSelector(content, "maType", "MACD MA Type", MACD.DEFAULT_MA_TYPE);

        Label label = new Label(content, SWT.NONE);
        label.setText("MACD Input");
        input = new Combo(content, SWT.READ_ONLY);
        input.add("OPEN");
        input.add("HIGH");
        input.add("LOW");
        input.add("CLOSE");
        input.select(getSettings().getInteger("input", MACD.DEFAULT_INPUT).intValue());
        
        addColorSelector(content, "triggerColor", "Trigger Color", MACD.DEFAULT_TRIGGER_COLOR);
        addIntegerValueSelector(content, "triggerPeriod", "Trigger Period", 1, 9999, MACD.DEFAULT_TRIGGER_PERIOD);
        addLabelField(content, "triggerLabel", "Trigger Label", MACD.DEFAULT_TRIGGER_LABEL);
        addLineTypeSelector(content, "triggerLineType", "Trigger Line Type", MACD.DEFAULT_TRIGGER_LINETYPE);
        
        addColorSelector(content, "oscColor", "Osc Color", MACD.DEFAULT_OSC_COLOR);
        addLabelField(content, "oscLabel", "Osc Label", MACD.DEFAULT_OSC_LABEL);
        addLineTypeSelector(content, "oscLineType", "Osc Line Type", MACD.DEFAULT_OSC_LINETYPE);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("input", input.getSelectionIndex());
        super.performFinish();
    }
}
