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

public class RSIPreferences extends IndicatorPluginPreferencePage
{

    public RSIPreferences()
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

        addColorSelector(content, "color", Messages.RSIPreferences_Color, RSI.DEFAULT_COLOR); //$NON-NLS-1$
        addLabelField(content, "label", Messages.RSIPreferences_Label, RSI.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.RSIPreferences_LineType, RSI.DEFAULT_LINETYPE); //$NON-NLS-1$
        addIntegerValueSelector(content, "period", Messages.RSIPreferences_Period, 1, 9999, RSI.DEFAULT_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "smoothing", Messages.RSIPreferences_Smoothing, 0, 9999, RSI.DEFAULT_SMOOTHING); //$NON-NLS-1$
        addMovingAverageSelector(content, "type", Messages.RSIPreferences_SmoothingType, RSI.DEFAULT_SMOOTHING_TYPE); //$NON-NLS-1$
        addInputSelector(content, "input", Messages.RSIPreferences_Input, RSI.DEFAULT_INPUT, false); //$NON-NLS-1$
        addIntegerValueSelector(content, "buyLine", Messages.RSIPreferences_BuyLine, 2, 99, RSI.DEFAULT_BUYLINE); //$NON-NLS-1$
        addColorSelector(content, "buyLineColor", Messages.RSIPreferences_BuyLineColor, RSI.DEFAULT_BUYLINE_COLOR); //$NON-NLS-1$
        addIntegerValueSelector(content, "sellLine", Messages.RSIPreferences_SellLine, 2, 99, RSI.DEFAULT_BUYLINE); //$NON-NLS-1$
        addColorSelector(content, "sellLineColor", Messages.RSIPreferences_SellLineColor, RSI.DEFAULT_SELLLINE_COLOR); //$NON-NLS-1$
    }
}
