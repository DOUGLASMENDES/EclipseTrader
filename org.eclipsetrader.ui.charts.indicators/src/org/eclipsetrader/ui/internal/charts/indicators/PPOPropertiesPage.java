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
import org.eclipsetrader.ui.charts.indicators.PPO;
import org.eclipsetrader.ui.internal.charts.MATypeInput;
import org.eclipsetrader.ui.internal.charts.OHLCFieldInput;

public class PPOPropertiesPage extends PropertyPage {
	private OHLCFieldInput input;
	private Spinner fastPeriod;
	private Spinner slowPeriod;
	private MATypeInput type;

	public PPOPropertiesPage() {
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
        setTitle("Percentage Price Oscillator");

        Label label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        label.setText("Input Field");
        input = new OHLCFieldInput(content);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("Fast Period");
        fastPeriod = new Spinner(content, SWT.BORDER);
        fastPeriod.setValues(3, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Slow Period");
        slowPeriod = new Spinner(content, SWT.BORDER);
        slowPeriod.setValues(10, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("MA Type");
        type = new MATypeInput(content);

        performDefaults();

	    return content;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
    	PPO object = (PPO) getElement().getAdapter(PPO.class);
        input.setSelection(object.getField());
        fastPeriod.setSelection(object.getFastPeriod());
        slowPeriod.setSelection(object.getSlowPeriod());
        type.setSelection(object.getMaType());
	    super.performDefaults();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	PPO object = (PPO) getElement().getAdapter(PPO.class);
    	object.setField(input.getSelection());
    	object.setFastPeriod(fastPeriod.getSelection());
    	object.setSlowPeriod(slowPeriod.getSelection());
    	object.setMaType(type.getSelection());
	    return super.performOk();
    }
}
