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
import org.eclipsetrader.ui.charts.indicators.TREND;

public class TRENDGeneralPropertiesPage extends PropertyPage {

    private Text text;
    private Button override;
    private ColorSelector upperLineColor;
    private ColorSelector middleLineColor;
    private ColorSelector lowerLineColor;

    public TRENDGeneralPropertiesPage() {
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
        label.setText("Upper Line Color");
        upperLineColor = new ColorSelector(content);
        upperLineColor.setColorValue(new RGB(0, 0, 255));
        upperLineColor.getButton().setData("label", label);

        label = new Label(content, SWT.NONE);
        label.setText("Middle Line Color");
        middleLineColor = new ColorSelector(content);
        middleLineColor.setColorValue(new RGB(0, 0, 255));
        middleLineColor.getButton().setData("label", label);

        label = new Label(content, SWT.NONE);
        label.setText("Lower Line Color");
        lowerLineColor = new ColorSelector(content);
        lowerLineColor.setColorValue(new RGB(0, 0, 255));
        lowerLineColor.getButton().setData("label", label);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        TREND object = (TREND) getElement().getAdapter(TREND.class);
        text.setText(object.getName());

        override.setSelection(object.getUpperLineColor() != null || object.getMiddleLineColor() != null || object.getLowerLineColor() != null);
        if (object.getUpperLineColor() != null) {
            upperLineColor.setColorValue(object.getUpperLineColor());
        }
        if (object.getMiddleLineColor() != null) {
            middleLineColor.setColorValue(object.getMiddleLineColor());
        }
        if (object.getLowerLineColor() != null) {
            lowerLineColor.setColorValue(object.getLowerLineColor());
        }

        updateControlsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        TREND object = (TREND) getElement().getAdapter(TREND.class);
        object.setName(text.getText());

        object.setUpperLineColor(override.getSelection() ? upperLineColor.getColorValue() : null);
        object.setMiddleLineColor(override.getSelection() ? middleLineColor.getColorValue() : null);
        object.setLowerLineColor(override.getSelection() ? lowerLineColor.getColorValue() : null);

        return super.performOk();
    }

    protected void updateControlsEnablement() {
        upperLineColor.setEnabled(override.getSelection());
        ((Label) upperLineColor.getButton().getData("label")).setEnabled(override.getSelection());
        middleLineColor.setEnabled(override.getSelection());
        ((Label) middleLineColor.getButton().getData("label")).setEnabled(override.getSelection());
        lowerLineColor.setEnabled(override.getSelection());
        ((Label) lowerLineColor.getButton().getData("label")).setEnabled(override.getSelection());
    }
}
