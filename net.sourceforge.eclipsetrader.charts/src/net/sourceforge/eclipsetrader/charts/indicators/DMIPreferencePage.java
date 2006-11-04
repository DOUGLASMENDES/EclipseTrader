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

public class DMIPreferencePage extends IndicatorPluginPreferencePage
{

    public DMIPreferencePage()
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
        
        addIntegerValueSelector(content, "period", Messages.DMIPreferencePage_Period, 1, 9999, DMI.DEFAULT_PERIOD); //$NON-NLS-1$
        addIntegerValueSelector(content, "smoothing", Messages.DMIPreferencePage_Smoothing, 1, 9999, DMI.DEFAULT_SMOOTHING); //$NON-NLS-1$
        addMovingAverageSelector(content, "maType", Messages.DMIPreferencePage_SmoothingType, DMI.DEFAULT_MA_TYPE); //$NON-NLS-1$
        addColorSelector(content, "pdiColor", Messages.DMIPreferencePage_PDMColor, DMI.DEFAULT_PDI_COLOR); //$NON-NLS-1$
        addLabelField(content, "pdiLabel", Messages.DMIPreferencePage_PDMLabel, DMI.DEFAULT_PDI_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "pdiLineType", Messages.DMIPreferencePage_PDMLineType, DMI.DEFAULT_PDI_LINETYPE); //$NON-NLS-1$
        addColorSelector(content, "mdiColor", Messages.DMIPreferencePage_MDMColor, DMI.DEFAULT_MDI_COLOR); //$NON-NLS-1$
        addLabelField(content, "mdiLabel", Messages.DMIPreferencePage_MDMLabel, DMI.DEFAULT_MDI_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "mdiLineType", Messages.DMIPreferencePage_MDMLineType, DMI.DEFAULT_MDI_LINETYPE); //$NON-NLS-1$
        addColorSelector(content, "adxColor", Messages.DMIPreferencePage_ADXColor, DMI.DEFAULT_ADX_COLOR); //$NON-NLS-1$
        addLabelField(content, "adxLabel", Messages.DMIPreferencePage_ADXLabel, DMI.DEFAULT_ADX_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "adxLineType", Messages.DMIPreferencePage_ADXLineType, DMI.DEFAULT_ADX_LINETYPE); //$NON-NLS-1$
    }

}
