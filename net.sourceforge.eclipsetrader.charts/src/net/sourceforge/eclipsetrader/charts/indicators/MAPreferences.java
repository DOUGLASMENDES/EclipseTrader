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

        addLabelField(content, "label", "Label", MA.DEFAULT_LABEL);
        addLineTypeSelector(content, "lineType", "Line Type", MA.DEFAULT_LINETYPE);
        addColorSelector(content, "color", "Color", MA.DEFAULT_COLOR);
        addInputSelector(content, "input", "Input", MA.DEFAULT_INPUT, true);
        addIntegerValueSelector(content, "period", "Period", 1, 9999, MA.DEFAULT_PERIOD);
        addMovingAverageSelector(content, "type", "Type", MA.DEFAULT_TYPE);
    }
}
