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
import org.eclipsetrader.ui.charts.indicators.ULTOSC;

public class ULTOSCPropertiesPage extends PropertyPage {

    private Spinner shortPeriod;
    private Spinner mediumPeriod;
    private Spinner longPeriod;

    public ULTOSCPropertiesPage() {
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
        setTitle("Ultimate Oscillator");

        Label label = new Label(content, SWT.NONE);
        label.setText("Short Period");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        shortPeriod = new Spinner(content, SWT.BORDER);
        shortPeriod.setValues(7, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Medium Period");
        mediumPeriod = new Spinner(content, SWT.BORDER);
        mediumPeriod.setValues(14, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Long Period");
        longPeriod = new Spinner(content, SWT.BORDER);
        longPeriod.setValues(21, 1, 9999, 0, 1, 5);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        ULTOSC object = (ULTOSC) getElement().getAdapter(ULTOSC.class);
        shortPeriod.setSelection(object.getShortPeriod());
        mediumPeriod.setSelection(object.getMediumPeriod());
        longPeriod.setSelection(object.getLongPeriod());
        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        ULTOSC object = (ULTOSC) getElement().getAdapter(ULTOSC.class);
        object.setShortPeriod(shortPeriod.getSelection());
        object.setMediumPeriod(mediumPeriod.getSelection());
        object.setLongPeriod(longPeriod.getSelection());
        return super.performOk();
    }
}
