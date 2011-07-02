/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts.indicators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.ui.charts.indicators.OBV;
import org.eclipsetrader.ui.internal.charts.OHLCFieldInput;

public class OBVPropertiesPage extends PropertyPage {

    private OHLCFieldInput input;

    public OBVPropertiesPage() {
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setTitle("On Balance Volume");

        Label label = new Label(content, SWT.NONE);
        label.setText("Input Field");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        input = new OHLCFieldInput(content);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        OBV object = (OBV) getElement().getAdapter(OBV.class);
        input.setSelection(object.getField());
        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        OBV object = (OBV) getElement().getAdapter(OBV.class);
        object.setField(input.getSelection());
        return super.performOk();
    }
}
