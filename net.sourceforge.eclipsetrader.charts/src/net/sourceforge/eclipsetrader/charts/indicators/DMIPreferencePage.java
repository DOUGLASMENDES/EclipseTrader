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
        
        addIntegerValueSelector(content, "period", "Period", 1, 9999, DMI.DEFAULT_PERIOD);
        addIntegerValueSelector(content, "smoothing", "Smoothing", 1, 9999, DMI.DEFAULT_SMOOTHING);
        addMovingAverageSelector(content, "maType", "Smoothing Type", DMI.DEFAULT_MA_TYPE);
        addColorSelector(content, "pdiColor", "+DM Color", DMI.DEFAULT_PDI_COLOR);
        addLabelField(content, "pdiLabel", "+DM Label", DMI.DEFAULT_PDI_LABEL);
        addLineTypeSelector(content, "pdiLineType", "+DM Line Type", DMI.DEFAULT_PDI_LINETYPE);
        addColorSelector(content, "mdiColor", "-DM Color", DMI.DEFAULT_MDI_COLOR);
        addLabelField(content, "mdiLabel", "-DM Label", DMI.DEFAULT_MDI_LABEL);
        addLineTypeSelector(content, "mdiLineType", "-DM Line Type", DMI.DEFAULT_MDI_LINETYPE);
        addColorSelector(content, "adxColor", "ADX Color", DMI.DEFAULT_ADX_COLOR);
        addLabelField(content, "adxLabel", "ADX Label", DMI.DEFAULT_ADX_LABEL);
        addLineTypeSelector(content, "adxLineType", "ADX Line Type", DMI.DEFAULT_ADX_LINETYPE);
    }

}
