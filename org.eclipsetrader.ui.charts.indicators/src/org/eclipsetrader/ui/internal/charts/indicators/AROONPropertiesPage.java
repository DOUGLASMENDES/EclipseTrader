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
import org.eclipsetrader.ui.charts.indicators.AROON;
import org.eclipsetrader.ui.internal.charts.RenderStyleInput;

public class AROONPropertiesPage extends PropertyPage {
	private Spinner period;
	private RenderStyleInput upperLineStyle;
	private RenderStyleInput lowerLineStyle;

	public AROONPropertiesPage() {
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
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setTitle("Moving Average");

        Label label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        period = new Spinner(content, SWT.BORDER);
        period.setValues(7, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("Upper Line Style");
        upperLineStyle = new RenderStyleInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Lower Line Style");
        lowerLineStyle = new RenderStyleInput(content);

        performDefaults();

	    return content;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
    	AROON object = (AROON) getElement().getAdapter(AROON.class);
        period.setSelection(object.getPeriod());

        upperLineStyle.setSelection(object.getUpperLineStyle());
        lowerLineStyle.setSelection(object.getMiddleLineStyle());

        super.performDefaults();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	AROON object = (AROON) getElement().getAdapter(AROON.class);
    	object.setPeriod(period.getSelection());

    	object.setUpperLineStyle(upperLineStyle.getSelection());
    	object.setMiddleLineStyle(lowerLineStyle.getSelection());

	    return super.performOk();
    }
}
