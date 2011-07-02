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
import org.eclipsetrader.ui.charts.indicators.BBANDS;
import org.eclipsetrader.ui.internal.charts.MATypeInput;
import org.eclipsetrader.ui.internal.charts.OHLCFieldInput;
import org.eclipsetrader.ui.internal.charts.RenderStyleInput;

public class BBANDSPropertiesPage extends PropertyPage {

    private OHLCFieldInput input;
    private Spinner period;
    private Spinner upperDeviation;
    private Spinner lowerDeviation;
    private MATypeInput type;
    private RenderStyleInput upperLineStyle;
    private RenderStyleInput middleLineStyle;
    private RenderStyleInput lowerLineStyle;

    public BBANDSPropertiesPage() {
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
        setTitle("Bollinger Bands");

        Label label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        label.setText("Input Field");
        input = new OHLCFieldInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setValues(7, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Upper Deviation");
        upperDeviation = new Spinner(content, SWT.BORDER);
        upperDeviation.setValues(7, 1, 9999, 2, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("Lower Deviation");
        lowerDeviation = new Spinner(content, SWT.BORDER);
        lowerDeviation.setValues(7, 1, 9999, 2, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("MA Type");
        type = new MATypeInput(content);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("Upper Line Style");
        upperLineStyle = new RenderStyleInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("Middle Line Style");
        middleLineStyle = new RenderStyleInput(content);

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
        BBANDS object = (BBANDS) getElement().getAdapter(BBANDS.class);
        input.setSelection(object.getField());
        period.setSelection(object.getPeriod());
        upperDeviation.setSelection((int) (object.getUpperDeviation() * 100));
        lowerDeviation.setSelection((int) (object.getLowerDeviation() * 100));
        type.setSelection(object.getMaType());

        upperLineStyle.setSelection(object.getUpperLineStyle());
        middleLineStyle.setSelection(object.getMiddleLineStyle());
        lowerLineStyle.setSelection(object.getLowerLineStyle());

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        BBANDS object = (BBANDS) getElement().getAdapter(BBANDS.class);
        object.setField(input.getSelection());
        object.setPeriod(period.getSelection());
        object.setUpperDeviation(upperDeviation.getSelection() / 100.0);
        object.setLowerDeviation(lowerDeviation.getSelection() / 100.0);
        object.setMaType(type.getSelection());

        object.setUpperLineStyle(upperLineStyle.getSelection());
        object.setMiddleLineStyle(middleLineStyle.getSelection());
        object.setLowerLineStyle(lowerLineStyle.getSelection());

        return super.performOk();
    }
}
