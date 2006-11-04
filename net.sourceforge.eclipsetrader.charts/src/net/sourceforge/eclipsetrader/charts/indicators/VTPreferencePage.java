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

public class VTPreferencePage extends IndicatorPluginPreferencePage
{
    private Combo method;

    public VTPreferencePage()
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
        
        addColorSelector(content, "color", Messages.VTPreferencePage_Color, VT.DEFAULT_COLOR); //$NON-NLS-1$
        addLabelField(content, "label", Messages.VTPreferencePage_Label, VT.DEFAULT_LABEL); //$NON-NLS-1$
        addLineTypeSelector(content, "lineType", Messages.VTPreferencePage_LineType, VT.DEFAULT_LINETYPE); //$NON-NLS-1$

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.VTPreferencePage_Method);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        method = new Combo(content, SWT.READ_ONLY);
        method.add(Messages.VTPreferencePage_NVI);
        method.add(Messages.VTPreferencePage_OBV);
        method.add(Messages.VTPreferencePage_PVI);
        method.add(Messages.VTPreferencePage_PVT);
        method.setText(getSettings().getString("method", VT.DEFAULT_METHOD)); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("method", method.getText()); //$NON-NLS-1$
        super.performFinish();
    }
}
