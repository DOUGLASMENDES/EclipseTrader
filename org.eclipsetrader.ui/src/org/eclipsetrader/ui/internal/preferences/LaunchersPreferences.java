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

package org.eclipsetrader.ui.internal.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.ui.internal.UIActivator;

public class LaunchersPreferences extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String LAUNCHERS_EXTENSION_ID = "org.eclipsetrader.core.launchers";

    CheckboxTableViewer startupLaunchers;
    Button startAllLaunchers;
    Button startSelectedLaunchers;
    CheckboxTableViewer runLaunchers;

    public LaunchersPreferences() {
        super("Launchers");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
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

        startAllLaunchers = new Button(content, SWT.RADIO);
        startAllLaunchers.setText("Start All Services");
        startAllLaunchers.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        startAllLaunchers.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runLaunchers.getControl().setEnabled(startSelectedLaunchers.getSelection());
            }
        });

        startSelectedLaunchers = new Button(content, SWT.RADIO);
        startSelectedLaunchers.setText("Start Services Selected Below");
        startSelectedLaunchers.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        startSelectedLaunchers.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runLaunchers.getControl().setEnabled(startSelectedLaunchers.getSelection());
            }
        });

        runLaunchers = CheckboxTableViewer.newCheckList(content, SWT.BORDER);
        runLaunchers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
        ((GridData) runLaunchers.getControl().getLayoutData()).heightHint = convertHeightInCharsToPixels(6);
        runLaunchers.setContentProvider(new ArrayContentProvider());
        runLaunchers.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IConfigurationElement) element).getAttribute("name");
            }
        });

        Label label = new Label(content, SWT.NONE);
        label.setText("Services Launched at Startup");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        startupLaunchers = CheckboxTableViewer.newCheckList(content, SWT.BORDER);
        startupLaunchers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
        ((GridData) startupLaunchers.getControl().getLayoutData()).heightHint = convertHeightInCharsToPixels(6);
        startupLaunchers.setContentProvider(new ArrayContentProvider());
        startupLaunchers.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IConfigurationElement) element).getAttribute("name");
            }
        });

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
        if (extensionPoint != null) {
            List<IConfigurationElement> configElements = Arrays.asList(extensionPoint.getConfigurationElements());

            Collections.sort(configElements, new Comparator<IConfigurationElement>() {

                @Override
                public int compare(IConfigurationElement o1, IConfigurationElement o2) {
                    return o1.getAttribute("name").compareToIgnoreCase(o2.getAttribute("name"));
                }
            });

            startupLaunchers.setInput(configElements);
            runLaunchers.setInput(configElements);
        }

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore preferenceStore = getPreferenceStore();

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LAUNCHERS_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            startupLaunchers.setAllChecked(false);
            runLaunchers.setAllChecked(false);

            Set<String> startupSet = new HashSet<String>(Arrays.asList(preferenceStore.getString("STARTUP_LAUNCHERS").split(";")));
            Set<String> runSet = new HashSet<String>(Arrays.asList(preferenceStore.getString("RUN_LAUNCHERS").split(";")));

            for (int i = 0; i < configElements.length; i++) {
                String id = configElements[i].getAttribute("id");
                if (startupSet.contains(id)) {
                    startupLaunchers.setChecked(configElements[i], true);
                }
                if (runSet.contains(id)) {
                    runLaunchers.setChecked(configElements[i], true);
                }
            }
        }

        startAllLaunchers.setSelection(preferenceStore.getBoolean("RUN_ALL_LAUNCHERS"));
        startSelectedLaunchers.setSelection(!startAllLaunchers.getSelection());
        runLaunchers.getControl().setEnabled(startSelectedLaunchers.getSelection());

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore preferenceStore = getPreferenceStore();

        StringBuffer sb = new StringBuffer();
        Object[] o = startupLaunchers.getCheckedElements();
        for (int i = 0; i < o.length; i++) {
            String id = ((IConfigurationElement) o[i]).getAttribute("id");
            if (i != 0) {
                sb.append(";");
            }
            sb.append(id);
        }
        preferenceStore.setValue("STARTUP_LAUNCHERS", sb.toString());

        sb = new StringBuffer();
        o = runLaunchers.getCheckedElements();
        for (int i = 0; i < o.length; i++) {
            String id = ((IConfigurationElement) o[i]).getAttribute("id");
            if (i != 0) {
                sb.append(";");
            }
            sb.append(id);
        }
        preferenceStore.setValue("RUN_LAUNCHERS", sb.toString());

        preferenceStore.setValue("RUN_ALL_LAUNCHERS", startAllLaunchers.getSelection());

        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
     */
    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return UIActivator.getDefault().getPreferenceStore();
    }
}
