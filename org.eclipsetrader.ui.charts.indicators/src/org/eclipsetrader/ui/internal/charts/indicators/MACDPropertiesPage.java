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
import org.eclipsetrader.ui.charts.indicators.MACD;
import org.eclipsetrader.ui.internal.charts.MATypeInput;
import org.eclipsetrader.ui.internal.charts.OHLCFieldInput;
import org.eclipsetrader.ui.internal.charts.RenderStyleInput;

public class MACDPropertiesPage extends PropertyPage {
	private OHLCFieldInput input;
	private Spinner fastPeriod;
	private Spinner slowPeriod;
	private Spinner signalPeriod;
	private MATypeInput fastMaType;
	private MATypeInput slowMaType;
	private MATypeInput signalMaType;
	private RenderStyleInput macdLineStyle;
	private RenderStyleInput signalLineStyle;
	private RenderStyleInput histLineStyle;

	public MACDPropertiesPage() {
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
        setTitle("M/A Convergence / Divergence");

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
        fastPeriod.setValues(7, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Fast MA Type");
        fastMaType = new MATypeInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Slow Period");
        slowPeriod = new Spinner(content, SWT.BORDER);
        slowPeriod.setValues(21, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Slow MA Type");
        slowMaType = new MATypeInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Signal Period");
        signalPeriod = new Spinner(content, SWT.BORDER);
        signalPeriod.setValues(14, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Signal MA Type");
        signalMaType = new MATypeInput(content);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("MACD Line Style");
        macdLineStyle = new RenderStyleInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Signal Line Style");
        signalLineStyle = new RenderStyleInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Histheresis Line Style");
        histLineStyle = new RenderStyleInput(content);

        performDefaults();

	    return content;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
    	MACD object = (MACD) getElement().getAdapter(MACD.class);
        input.setSelection(object.getField());
        fastPeriod.setSelection(object.getFastPeriod());
        fastMaType.setSelection(object.getFastMaType());
        slowPeriod.setSelection(object.getSlowPeriod());
        slowMaType.setSelection(object.getSlowMaType());
        signalPeriod.setSelection(object.getSignalPeriod());
        signalMaType.setSelection(object.getSignalMaType());

        macdLineStyle.setSelection(object.getMacdLineStyle());
        signalLineStyle.setSelection(object.getSignalLineStyle());
        histLineStyle.setSelection(object.getHistLineStyle());

	    super.performDefaults();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	MACD object = (MACD) getElement().getAdapter(MACD.class);
    	object.setField(input.getSelection());
    	object.setFastPeriod(fastPeriod.getSelection());
    	object.setFastMaType(fastMaType.getSelection());
    	object.setSlowPeriod(slowPeriod.getSelection());
    	object.setSlowMaType(slowMaType.getSelection());
    	object.setSignalPeriod(signalPeriod.getSelection());
    	object.setSignalMaType(signalMaType.getSelection());

    	object.setMacdLineStyle(macdLineStyle.getSelection());
    	object.setSignalLineStyle(signalLineStyle.getSelection());
    	object.setHistLineStyle(histLineStyle.getSelection());

	    return super.performOk();
    }
}
