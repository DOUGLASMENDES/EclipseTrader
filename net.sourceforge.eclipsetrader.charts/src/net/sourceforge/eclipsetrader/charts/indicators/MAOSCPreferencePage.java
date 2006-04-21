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

public class MAOSCPreferencePage extends IndicatorPluginPreferencePage
{

    public MAOSCPreferencePage()
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
        
        addColorSelector(content, "color", "Color", MAOSC.DEFAULT_COLOR);
        addLabelField(content, "label", "Label", MAOSC.DEFAULT_LABEL);
        addLineTypeSelector(content, "lineType", "Line Type", MAOSC.DEFAULT_LINETYPE);
        addIntegerValueSelector(content, "fastPeriod", "Fast Period", 1, 9999, MAOSC.DEFAULT_FAST_PERIOD);
        addIntegerValueSelector(content, "slowPeriod", "Slow Period", 1, 9999, MAOSC.DEFAULT_SLOW_PERIOD);
        addMovingAverageSelector(content, "fastMaType", "Fast MA Type", MAOSC.DEFAULT_FAST_MA_TYPE);
        addMovingAverageSelector(content, "slowMaType", "Slow MA Type", MAOSC.DEFAULT_SLOW_MA_TYPE);
    }
}
