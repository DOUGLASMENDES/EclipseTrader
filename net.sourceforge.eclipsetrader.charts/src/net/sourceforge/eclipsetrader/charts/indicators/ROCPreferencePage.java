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

public class ROCPreferencePage extends IndicatorPluginPreferencePage
{
    private Combo input;

    public ROCPreferencePage()
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
        
        addColorSelector(content, "color", "Color", ROC.DEFAULT_COLOR);
        addLabelField(content, "label", "Label", ROC.DEFAULT_LABEL);
        addLineTypeSelector(content, "lineType", "Line Type", ROC.DEFAULT_LINETYPE);
        addIntegerValueSelector(content, "period", "Period", 1, 9999, ROC.DEFAULT_PERIOD);
        addIntegerValueSelector(content, "smoothing", "Smoothing", 0, 9999, ROC.DEFAULT_SMOOTHING);
        addMovingAverageSelector(content, "maType", "Smoothing Type", ROC.DEFAULT_MA_TYPE);

        Label label = new Label(content, SWT.NONE);
        label.setText("Input");
        input = new Combo(content, SWT.READ_ONLY);
        input.add("OPEN");
        input.add("HIGH");
        input.add("LOW");
        input.add("CLOSE");
        input.select(getSettings().getInteger("input", ROC.DEFAULT_INPUT).intValue());
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
