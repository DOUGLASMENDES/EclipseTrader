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

public class MAPreferences extends IndicatorPluginPreferencePage
{

    public MAPreferences()
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

        addBooleanSelector(content, "scaleFlag", Messages.IndicatorPreferences_ScaleFlag, MA.DEFAULT_SCALE_FLAG); //$NON-NLS-1$
        addLabelField(content, "label", Messages.MAPreferences_Label, MA.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.MAPreferences_LineType, MA.DEFAULT_LINETYPE); //$NON-NLS-1$
        addColorSelector(content, "color", Messages.MAPreferences_Color, MA.DEFAULT_COLOR); //$NON-NLS-1$
        addInputSelector(content, "input", Messages.MAPreferences_Input, MA.DEFAULT_INPUT, true); //$NON-NLS-1$
        addIntegerValueSelector(content, "period", Messages.MAPreferences_Period, 1, 9999, MA.DEFAULT_PERIOD); //$NON-NLS-1$
        addMovingAverageSelector(content, "type", Messages.MAPreferences_Type, MA.DEFAULT_TYPE); //$NON-NLS-1$
    }
}
