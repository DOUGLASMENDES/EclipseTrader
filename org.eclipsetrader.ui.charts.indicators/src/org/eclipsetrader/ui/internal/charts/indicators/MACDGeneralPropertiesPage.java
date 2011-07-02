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

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.ui.charts.indicators.MACD;

public class MACDGeneralPropertiesPage extends PropertyPage {

    private Text text;
    private Button override;
    private ColorSelector macdLineColor;
    private ColorSelector signalLineColor;
    private ColorSelector histLineColor;

    public MACDGeneralPropertiesPage() {
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
        setTitle("General");

        Label label = new Label(content, SWT.NONE);
        label.setText("Label");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(75), SWT.DEFAULT));
        text = new Text(content, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        override = new Button(content, SWT.CHECK);
        override.setText("Override color theme");
        override.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        override.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateControlsEnablement();
            }
        });

        label = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText("MACD Line Color");
        macdLineColor = new ColorSelector(content);
        macdLineColor.setColorValue(new RGB(0, 0, 255));
        macdLineColor.getButton().setData("label", label);

        label = new Label(content, SWT.NONE);
        label.setText("Signal Line Color");
        signalLineColor = new ColorSelector(content);
        signalLineColor.setColorValue(new RGB(0, 0, 255));
        signalLineColor.getButton().setData("label", label);

        label = new Label(content, SWT.NONE);
        label.setText("Histeresis Line Color");
        histLineColor = new ColorSelector(content);
        histLineColor.setColorValue(new RGB(0, 0, 255));
        histLineColor.getButton().setData("label", label);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        MACD object = (MACD) getElement().getAdapter(MACD.class);
        text.setText(object.getName());

        override.setSelection(object.getMacdLineColor() != null || object.getSignalLineColor() != null || object.getHistLineColor() != null);
        if (object.getMacdLineColor() != null) {
            macdLineColor.setColorValue(object.getMacdLineColor());
        }
        if (object.getSignalLineColor() != null) {
            signalLineColor.setColorValue(object.getSignalLineColor());
        }
        if (object.getHistLineColor() != null) {
            histLineColor.setColorValue(object.getHistLineColor());
        }

        updateControlsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        MACD object = (MACD) getElement().getAdapter(MACD.class);
        object.setName(text.getText());

        object.setMacdLineColor(override.getSelection() ? macdLineColor.getColorValue() : null);
        object.setSignalLineColor(override.getSelection() ? signalLineColor.getColorValue() : null);
        object.setHistLineColor(override.getSelection() ? histLineColor.getColorValue() : null);

        return super.performOk();
    }

    protected void updateControlsEnablement() {
        macdLineColor.setEnabled(override.getSelection());
        ((Label) macdLineColor.getButton().getData("label")).setEnabled(override.getSelection());
        signalLineColor.setEnabled(override.getSelection());
        ((Label) signalLineColor.getButton().getData("label")).setEnabled(override.getSelection());
        histLineColor.setEnabled(override.getSelection());
        ((Label) histLineColor.getButton().getData("label")).setEnabled(override.getSelection());
    }
}
