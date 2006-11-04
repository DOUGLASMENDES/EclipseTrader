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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

public class UOPreferencePage extends IndicatorPluginPreferencePage
{

    public UOPreferencePage()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);
        
        addColorSelector(content, "color", Messages.UOPreferencePage_Color, UO.DEFAULT_COLOR); //$NON-NLS-1$
        addLabelField(content, "label", Messages.UOPreferencePage_Label, UO.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.UOPreferencePage_LineType, UO.DEFAULT_LINETYPE); //$NON-NLS-1$
        addIntegerValueSelector(content, "shortPeriod", Messages.UOPreferencePage_ShortPeriod, 1, 9999, UO.DEFAULT_SHORT_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "mediumPeriod", Messages.UOPreferencePage_MediumPeriod, 1, 9999, UO.DEFAULT_MEDIUM_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "longPeriod", Messages.UOPreferencePage_LongPeriod, 1, 9999, UO.DEFAULT_LONG_PERIOD); //$NON-NLS-1$
    }
}
