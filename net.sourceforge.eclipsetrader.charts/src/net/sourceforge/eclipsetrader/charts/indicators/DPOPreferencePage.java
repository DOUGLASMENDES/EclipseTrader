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

public class DPOPreferencePage extends IndicatorPluginPreferencePage
{

    public DPOPreferencePage()
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
        
        addColorSelector(content, "color", Messages.DPOPreferencePage_Color, DPO.DEFAULT_COLOR); //$NON-NLS-1$
        addLabelField(content, "label", Messages.DPOPreferencePage_Label, DPO.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.DPOPreferencePage_LineType, DPO.DEFAULT_LINETYPE); //$NON-NLS-1$
        addIntegerValueSelector(content, "period", Messages.DPOPreferencePage_Period, 1, 9999, DPO.DEFAULT_PERIOD); //$NON-NLS-1$
        addMovingAverageSelector(content, "maType", Messages.DPOPreferencePage_MAType, DPO.DEFAULT_MA_TYPE); //$NON-NLS-1$
    }
}
