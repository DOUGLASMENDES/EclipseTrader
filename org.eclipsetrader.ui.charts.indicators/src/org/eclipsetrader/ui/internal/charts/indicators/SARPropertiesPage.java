/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import org.eclipsetrader.ui.charts.indicators.SAR;

public class SARPropertiesPage extends PropertyPage {
	private Spinner acceleration;
	private Spinner maximum;

	public SARPropertiesPage() {
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
        setTitle("Parabolic SAR");

        Label label = new Label(content, SWT.NONE);
        label.setText("Acceleration");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        acceleration = new Spinner(content, SWT.BORDER);
        acceleration.setValues(2, 1, 99999, 2, 1, 10);

        label = new Label(content, SWT.NONE);
        label.setText("Maximum");
        maximum = new Spinner(content, SWT.BORDER);
        maximum.setValues(20, 1, 99999, 2, 1, 10);

        performDefaults();

	    return content;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
    	SAR object = (SAR) getElement().getAdapter(SAR.class);
        acceleration.setSelection((int) (object.getAcceleration() * Math.pow(10, acceleration.getDigits())));
        maximum.setSelection((int) (object.getMaximum() * Math.pow(10, maximum.getDigits())));
	    super.performDefaults();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	SAR object = (SAR) getElement().getAdapter(SAR.class);
    	object.setAcceleration(acceleration.getSelection() / Math.pow(10, acceleration.getDigits()));
    	object.setMaximum(maximum.getSelection() / Math.pow(10, maximum.getDigits()));
	    return super.performOk();
    }
}
