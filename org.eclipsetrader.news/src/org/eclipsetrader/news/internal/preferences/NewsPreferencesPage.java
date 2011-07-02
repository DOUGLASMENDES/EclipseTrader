/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsetrader.news.internal.Activator;

public class NewsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final String K_ID = "id"; //$NON-NLS-1$
    private static final String K_NAME = "name"; //$NON-NLS-1$

    private Button followQuoteFeed;
    private Spinner daysToKeep;
    private Button enableDecorators;
    private CheckboxTableViewer providers;

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
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        enableDecorators = new Button(content, SWT.CHECK);
        enableDecorators.setText(Messages.NewsPreferencesPage_EnableDecorators);
        enableDecorators.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        followQuoteFeed = new Button(content, SWT.CHECK);
        followQuoteFeed.setText(Messages.NewsPreferencesPage_FollowQuoteFeed);
        followQuoteFeed.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.NewsPreferencesPage_DaysToKeep);
        daysToKeep = new Spinner(content, SWT.BORDER);
        daysToKeep.setMinimum(1);
        daysToKeep.setMaximum(9999);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.NewsPreferencesPage_Providers);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        providers = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        providers.setContentProvider(new ArrayContentProvider());
        providers.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IConfigurationElement) element).getAttribute(K_NAME);
            }
        });
        providers.setSorter(new ViewerSorter());

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        followQuoteFeed.setSelection(store.getBoolean(Activator.PREFS_FOLLOW_QUOTE_FEED));
        daysToKeep.setSelection(store.getInt(Activator.PREFS_DATE_RANGE));
        enableDecorators.setSelection(store.getBoolean(Activator.PREFS_ENABLE_DECORATORS));

        IConfigurationElement[] elements = getProvidersConfigurationElements();
        providers.setInput(elements);
        for (int i = 0; i < elements.length; i++) {
            providers.setChecked(elements[i], store.getBoolean(elements[i].getAttribute(K_ID)));
        }

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        store.setValue(Activator.PREFS_FOLLOW_QUOTE_FEED, followQuoteFeed.getSelection());
        store.setValue(Activator.PREFS_DATE_RANGE, daysToKeep.getSelection());
        store.setValue(Activator.PREFS_ENABLE_DECORATORS, enableDecorators.getSelection());

        IConfigurationElement[] elements = getProvidersConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            String id = elements[i].getAttribute(K_ID);
            store.setValue(id, providers.getChecked(elements[i]));
        }

        return super.performOk();
    }

    protected IConfigurationElement[] getProvidersConfigurationElements() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PROVIDER_EXTENSION_POINT);
        return extensionPoint != null ? extensionPoint.getConfigurationElements() : new IConfigurationElement[0];
    }
}
