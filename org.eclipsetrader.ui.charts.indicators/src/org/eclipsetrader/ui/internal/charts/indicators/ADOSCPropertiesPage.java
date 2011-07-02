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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.ui.charts.indicators.ADOSC;

public class ADOSCPropertiesPage extends PropertyPage {

    private Spinner fastPeriod;
    private Spinner slowPeriod;

    public ADOSCPropertiesPage() {
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
        setTitle("Chaikin A/D Oscillator");

        Label label = new Label(content, SWT.NONE);
        label.setText("Fast Period");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        fastPeriod = new Spinner(content, SWT.BORDER);
        fastPeriod.setValues(3, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Slow Period");
        slowPeriod = new Spinner(content, SWT.BORDER);
        slowPeriod.setValues(10, 1, 9999, 0, 1, 5);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        ADOSC object = (ADOSC) getElement().getAdapter(ADOSC.class);
        fastPeriod.setSelection(object.getFastPeriod());
        slowPeriod.setSelection(object.getSlowPeriod());
        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        ADOSC object = (ADOSC) getElement().getAdapter(ADOSC.class);
        object.setFastPeriod(fastPeriod.getSelection());
        object.setSlowPeriod(slowPeriod.getSelection());
        return super.performOk();
    }
}
