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

package org.eclipsetrader.ui.internal.charts.views;

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
import org.eclipse.ui.dialogs.PropertyPage;

public class MainPropertiesPage extends PropertyPage {

    private MainRenderStyleInput style;
    private Button override;
    private ColorSelector lineColor;
    private ColorSelector barPositiveColor;
    private ColorSelector barNegativeColor;
    private ColorSelector candleOutlineColor;
    private ColorSelector candlePositiveColor;
    private ColorSelector candleNegativeColor;

    public MainPropertiesPage() {
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setTitle("General");

        Label label = new Label(content, SWT.NONE);
        label.setText("Style");
        style = new MainRenderStyleInput(content);
        style.getCombo().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        ((GridData) label.getLayoutData()).heightHint = convertVerticalDLUsToPixels(5);

        override = new Button(content, SWT.CHECK);
        override.setText("Override color theme");
        override.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
        override.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateControlsEnablement();
            }
        });

        label = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Line");
        lineColor = new ColorSelector(content);
        lineColor.setColorValue(new RGB(0, 0, 255));
        lineColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        label = new Label(content, SWT.NONE);
        label.setText("Bars");
        barPositiveColor = new ColorSelector(content);
        barPositiveColor.setColorValue(new RGB(0, 0, 255));
        barPositiveColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setText("Positive");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        barPositiveColor.getButton().setData("label2", label);

        label = new Label(content, SWT.NONE);
        barNegativeColor = new ColorSelector(content);
        barNegativeColor.setColorValue(new RGB(0, 0, 255));
        barNegativeColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setText("Negative");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        barNegativeColor.getButton().setData("label2", label);

        label = new Label(content, SWT.NONE);
        label.setText("Candles");
        candlePositiveColor = new ColorSelector(content);
        candlePositiveColor.setColorValue(new RGB(0, 0, 255));
        candlePositiveColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setText("Positive");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        candlePositiveColor.getButton().setData("label2", label);

        label = new Label(content, SWT.NONE);
        candleNegativeColor = new ColorSelector(content);
        candleNegativeColor.setColorValue(new RGB(0, 0, 255));
        candleNegativeColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setText("Negative");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        candleNegativeColor.getButton().setData("label2", label);

        label = new Label(content, SWT.NONE);
        candleOutlineColor = new ColorSelector(content);
        candleOutlineColor.setColorValue(new RGB(0, 0, 255));
        candleOutlineColor.getButton().setData("label", label);
        label = new Label(content, SWT.NONE);
        label.setText("Outline");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        candleOutlineColor.getButton().setData("label2", label);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        MainChartFactory object = (MainChartFactory) getElement().getAdapter(MainChartFactory.class);

        style.setSelection(object.getStyle());

        boolean isOverride = false;
        if (object.getLineColor() != null) {
            isOverride = true;
        }
        else if (object.getBarPositiveColor() != null) {
            isOverride = true;
        }
        else if (object.getBarNegativeColor() != null) {
            isOverride = true;
        }
        else if (object.getCandlePositiveColor() != null) {
            isOverride = true;
        }
        else if (object.getCandleNegativeColor() != null) {
            isOverride = true;
        }
        else if (object.getCandleOutlineColor() != null) {
            isOverride = true;
        }
        override.setSelection(isOverride);

        if (object.getLineColor() != null) {
            lineColor.setColorValue(object.getLineColor());
        }

        if (object.getBarPositiveColor() != null) {
            barPositiveColor.setColorValue(object.getBarPositiveColor());
        }
        if (object.getBarNegativeColor() != null) {
            barNegativeColor.setColorValue(object.getBarNegativeColor());
        }

        if (object.getCandlePositiveColor() != null) {
            candlePositiveColor.setColorValue(object.getCandlePositiveColor());
        }
        if (object.getCandleNegativeColor() != null) {
            candleNegativeColor.setColorValue(object.getCandleNegativeColor());
        }
        if (object.getCandleOutlineColor() != null) {
            candleOutlineColor.setColorValue(object.getCandleOutlineColor());
        }

        updateControlsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        MainChartFactory object = (MainChartFactory) getElement().getAdapter(MainChartFactory.class);

        object.setStyle(style.getSelection());

        object.setLineColor(override.getSelection() ? lineColor.getColorValue() : null);

        object.setBarPositiveColor(override.getSelection() ? barPositiveColor.getColorValue() : null);
        object.setBarNegativeColor(override.getSelection() ? barNegativeColor.getColorValue() : null);

        object.setCandlePositiveColor(override.getSelection() ? candlePositiveColor.getColorValue() : null);
        object.setCandleNegativeColor(override.getSelection() ? candleNegativeColor.getColorValue() : null);
        object.setCandleOutlineColor(override.getSelection() ? candleOutlineColor.getColorValue() : null);

        return super.performOk();
    }

    protected void updateControlsEnablement() {
        lineColor.setEnabled(override.getSelection());
        ((Label) lineColor.getButton().getData("label")).setEnabled(override.getSelection());

        barPositiveColor.setEnabled(override.getSelection());
        ((Label) barPositiveColor.getButton().getData("label")).setEnabled(override.getSelection());
        ((Label) barPositiveColor.getButton().getData("label2")).setEnabled(override.getSelection());
        barNegativeColor.setEnabled(override.getSelection());
        ((Label) barNegativeColor.getButton().getData("label")).setEnabled(override.getSelection());
        ((Label) barNegativeColor.getButton().getData("label2")).setEnabled(override.getSelection());

        candlePositiveColor.setEnabled(override.getSelection());
        ((Label) candlePositiveColor.getButton().getData("label")).setEnabled(override.getSelection());
        ((Label) candlePositiveColor.getButton().getData("label2")).setEnabled(override.getSelection());
        candleNegativeColor.setEnabled(override.getSelection());
        ((Label) candleNegativeColor.getButton().getData("label")).setEnabled(override.getSelection());
        ((Label) candleNegativeColor.getButton().getData("label2")).setEnabled(override.getSelection());
        candleOutlineColor.setEnabled(override.getSelection());
        ((Label) candleOutlineColor.getButton().getData("label")).setEnabled(override.getSelection());
        ((Label) candleOutlineColor.getButton().getData("label2")).setEnabled(override.getSelection());
    }
}
