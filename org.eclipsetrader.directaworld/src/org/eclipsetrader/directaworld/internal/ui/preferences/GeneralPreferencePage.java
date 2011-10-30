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

package org.eclipsetrader.directaworld.internal.ui.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.directaworld.internal.Activator;

public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Button useSecureStorage;
    private Text userName;
    private Text password;
    private Label warningLabel;

    public GeneralPreferencePage() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Label label = new Label(content, SWT.NONE);
        label.setText("User Code");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        userName = new Text(content, SWT.BORDER);
        userName.setLayoutData(new GridData(convertHorizontalDLUsToPixels(90), SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Password");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        password = new Text(content, SWT.BORDER | SWT.PASSWORD);
        password.setLayoutData(new GridData(convertHorizontalDLUsToPixels(90), SWT.DEFAULT));

        new Label(content, SWT.NONE);
        useSecureStorage = new Button(content, SWT.CHECK);
        useSecureStorage.setText("Use secure password store");
        useSecureStorage.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                warningLabel.setVisible(!useSecureStorage.getSelection());
            }
        });

        warningLabel = new Label(content, SWT.NONE);
        warningLabel.setText("Warning! Passwords are saved in clear text.");
        warningLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, false, true, 2, 1));

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        performApply();
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        IPreferenceStore preferenceStore = getPreferenceStore();
        preferenceStore.setValue(Activator.PREFS_USE_SECURE_PREFERENCE_STORE, useSecureStorage.getSelection());

        if (useSecureStorage.getSelection()) {
            ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(Activator.PLUGIN_ID);
            try {
                securePreferences.put(Activator.PREFS_USERNAME, userName.getText(), true);
                securePreferences.put(Activator.PREFS_PASSWORD, password.getText(), true);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error accessing secure storage", e); //$NON-NLS-1$
                Activator.log(status);
                ErrorDialog.openError(null, null, null, status);
            }
            preferenceStore.setValue(Activator.PREFS_USERNAME, "");
            preferenceStore.setValue(Activator.PREFS_PASSWORD, "");
        }
        else {
            preferenceStore.setValue(Activator.PREFS_USERNAME, userName.getText());
            preferenceStore.setValue(Activator.PREFS_PASSWORD, password.getText());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore preferenceStore = getPreferenceStore();
        useSecureStorage.setSelection(preferenceStore.getBoolean(Activator.PREFS_USE_SECURE_PREFERENCE_STORE));

        if (useSecureStorage.getSelection()) {
            ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(Activator.PLUGIN_ID);
            try {
                userName.setText(securePreferences.get(Activator.PREFS_USERNAME, ""));
                password.setText(securePreferences.get(Activator.PREFS_PASSWORD, ""));
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error accessing secure storage", e); //$NON-NLS-1$
                Activator.log(status);
                ErrorDialog.openError(null, null, null, status);
            }
            preferenceStore.setValue(Activator.PREFS_USERNAME, "");
            preferenceStore.setValue(Activator.PREFS_PASSWORD, "");
        }
        else {
            userName.setText(preferenceStore.getString(Activator.PREFS_USERNAME));
            password.setText(preferenceStore.getString(Activator.PREFS_PASSWORD));
        }

        warningLabel.setVisible(!preferenceStore.getBoolean(Activator.PREFS_USE_SECURE_PREFERENCE_STORE));
    }
}
