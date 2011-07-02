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
import org.eclipsetrader.ui.charts.indicators.STOCHRSI;
import org.eclipsetrader.ui.internal.charts.MATypeInput;
import org.eclipsetrader.ui.internal.charts.OHLCFieldInput;
import org.eclipsetrader.ui.internal.charts.RenderStyleInput;

public class STOCHRSIPropertiesPage extends PropertyPage {

    private OHLCFieldInput input;
    private Spinner kFastPeriod;
    private Spinner kSlowPeriod;
    private Spinner dPeriod;
    private MATypeInput dMaType;
    private RenderStyleInput kLineStyle;
    private RenderStyleInput dLineStyle;

    public STOCHRSIPropertiesPage() {
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
        setTitle("Stochastic RSI");

        Label label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        label.setText("Input Field");
        input = new OHLCFieldInput(content);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("K Fast Period");
        kFastPeriod = new Spinner(content, SWT.BORDER);
        kFastPeriod.setValues(7, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("K Slow Period");
        kSlowPeriod = new Spinner(content, SWT.BORDER);
        kSlowPeriod.setValues(21, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("D Period");
        dPeriod = new Spinner(content, SWT.BORDER);
        dPeriod.setValues(14, 1, 9999, 0, 1, 5);

        label = new Label(content, SWT.NONE);
        label.setText("D MA Type");
        dMaType = new MATypeInput(content);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        label = new Label(content, SWT.NONE);
        label.setText("K Line Style");
        kLineStyle = new RenderStyleInput(content);

        label = new Label(content, SWT.NONE);
        label.setText("D Line Style");
        dLineStyle = new RenderStyleInput(content);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        STOCHRSI object = (STOCHRSI) getElement().getAdapter(STOCHRSI.class);
        input.setSelection(object.getField());
        kFastPeriod.setSelection(object.getKFastPeriod());
        kSlowPeriod.setSelection(object.getKSlowPeriod());
        dPeriod.setSelection(object.getDPeriod());
        dMaType.setSelection(object.getDMaType());

        kLineStyle.setSelection(object.getKLineStyle());
        dLineStyle.setSelection(object.getDLineStyle());

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        STOCHRSI object = (STOCHRSI) getElement().getAdapter(STOCHRSI.class);
        object.setField(input.getSelection());
        object.setKFastPeriod(kFastPeriod.getSelection());
        object.setKSlowPeriod(kSlowPeriod.getSelection());
        object.setDPeriod(dPeriod.getSelection());
        object.setDMaType(dMaType.getSelection());

        object.setKLineStyle(kLineStyle.getSelection());
        object.setDLineStyle(dLineStyle.getSelection());

        return super.performOk();
    }
}
