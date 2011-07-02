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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.ui.charts.CrosshairDecorator;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class GeneralPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Button showTooltips;
    private Button showScaleTooltips;
    private Button showCrosshair;
    private Group croshairActivationGroup;
    private Button onMouseDown;
    private Button onMouseHover;
    private Button showSummaryTooltip;

    public GeneralPreferencesPage() {
        super(Messages.GeneralPreferencesPage_Title);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(ChartsUIActivator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(2);
        content.setLayout(gridLayout);

        showTooltips = new Button(content, SWT.CHECK);
        showTooltips.setText(Messages.GeneralPreferencesPage_ShowTooltipsLabel);
        showTooltips.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        showScaleTooltips = new Button(content, SWT.CHECK);
        showScaleTooltips.setText(Messages.GeneralPreferencesPage_ShowScaleTooltipsLabel);
        showScaleTooltips.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        showCrosshair = new Button(content, SWT.CHECK);
        showCrosshair.setText(Messages.GeneralPreferencesPage_ShowCrosshairLabel);
        showCrosshair.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        showCrosshair.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsEnablement();
            }
        });

        croshairActivationGroup = new Group(content, SWT.NONE);
        croshairActivationGroup.setText(Messages.GeneralPreferencesPage_CrosshairModeLabel);
        croshairActivationGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(2);
        croshairActivationGroup.setLayout(gridLayout);

        onMouseDown = new Button(croshairActivationGroup, SWT.RADIO);
        onMouseDown.setText(Messages.GeneralPreferencesPage_OneMouseClickLabel);
        onMouseDown.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        onMouseHover = new Button(croshairActivationGroup, SWT.RADIO);
        onMouseHover.setText(Messages.GeneralPreferencesPage_OnMouseHoverLabel);
        onMouseHover.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        showSummaryTooltip = new Button(croshairActivationGroup, SWT.CHECK);
        showSummaryTooltip.setText(Messages.GeneralPreferencesPage_ShowSummaryTooltipLabel);
        showSummaryTooltip.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        performDefaults();

        return content;
    }

    protected void updateButtonsEnablement() {
        boolean enable = showCrosshair.getSelection();
        croshairActivationGroup.setEnabled(enable);
        onMouseDown.setEnabled(enable);
        onMouseHover.setEnabled(enable);
        showSummaryTooltip.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore preferences = getPreferenceStore();
        showTooltips.setSelection(preferences.getBoolean(ChartsUIActivator.PREFS_SHOW_TOOLTIPS));
        showScaleTooltips.setSelection(preferences.getBoolean(ChartsUIActivator.PREFS_SHOW_SCALE_TOOLTIPS));

        int v = preferences.getInt(ChartsUIActivator.PREFS_CROSSHAIR_ACTIVATION);
        showCrosshair.setSelection(v != CrosshairDecorator.MODE_OFF);
        onMouseDown.setSelection(v == CrosshairDecorator.MODE_OFF || v == CrosshairDecorator.MODE_MOUSE_DOWN);
        onMouseHover.setSelection(v == CrosshairDecorator.MODE_MOUSE_HOVER);
        showSummaryTooltip.setSelection(preferences.getBoolean(ChartsUIActivator.PREFS_CROSSHAIR_SUMMARY_TOOLTIP));
        updateButtonsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore preferences = getPreferenceStore();
        preferences.setValue(ChartsUIActivator.PREFS_SHOW_TOOLTIPS, showTooltips.getSelection());
        preferences.setValue(ChartsUIActivator.PREFS_SHOW_SCALE_TOOLTIPS, showScaleTooltips.getSelection());
        if (showCrosshair.getSelection()) {
            if (onMouseDown.getSelection()) {
                preferences.setValue(ChartsUIActivator.PREFS_CROSSHAIR_ACTIVATION, CrosshairDecorator.MODE_MOUSE_DOWN);
            }
            if (onMouseHover.getSelection()) {
                preferences.setValue(ChartsUIActivator.PREFS_CROSSHAIR_ACTIVATION, CrosshairDecorator.MODE_MOUSE_HOVER);
            }
        }
        else {
            preferences.setValue(ChartsUIActivator.PREFS_CROSSHAIR_ACTIVATION, CrosshairDecorator.MODE_OFF);
        }
        preferences.setValue(ChartsUIActivator.PREFS_CROSSHAIR_SUMMARY_TOOLTIP, showSummaryTooltip.getSelection());
        return super.performOk();
    }
}
