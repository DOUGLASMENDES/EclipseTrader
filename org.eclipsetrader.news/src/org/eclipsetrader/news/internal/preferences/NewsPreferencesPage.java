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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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

public class NewsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button updateStartup;
	private Button followQuoteFeed;
	private Spinner daysToKeep;
	private CheckboxTableViewer providers;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
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

		updateStartup = new Button(content, SWT.CHECK);
		updateStartup.setText("Update on startup");
		updateStartup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

		followQuoteFeed = new Button(content, SWT.CHECK);
		followQuoteFeed.setText("Follow quote feed running status");
		followQuoteFeed.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

		Label label = new Label(content, SWT.NONE);
		label.setText("Days to keep");
		daysToKeep = new Spinner(content, SWT.BORDER);
		daysToKeep.setMinimum(1);
		daysToKeep.setMaximum(9999);

		providers = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gridData.heightHint = providers.getTable().getItemHeight() * 15 + providers.getTable().getBorderWidth() * 2;
		providers.getTable().setLayoutData(gridData);

		/*IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();
		updateStartup.setSelection(store.getBoolean(NewsPlugin.PREFS_UPDATE_ON_STARTUP));
		followQuoteFeed.setSelection(store.getBoolean(NewsPlugin.PREFS_FOLLOW_QUOTE_FEED));
		daysToKeep.setSelection(store.getInt(CorePlugin.PREFS_NEWS_DATE_RANGE));

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(NewsPlugin.PROVIDER_EXTENSION_POINT);
		if (extensionPoint != null)
		{
		    IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		    for (int i = 0; i < elements.length; i++)
		    {
		        String id = elements[i].getAttribute("id"); //$NON-NLS-1$
		        TableItem tableItem = new TableItem(table, SWT.NONE);
		        tableItem.setText(elements[i].getAttribute("name")); //$NON-NLS-1$
		        tableItem.setChecked(store.getBoolean(id));
		        tableItem.setData(id);
		    }
		}

		table.getColumn(0).pack();*/

		return content;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
    public boolean performOk() {
		/*IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();

		store.setValue(NewsPlugin.PREFS_UPDATE_ON_STARTUP, updateStartup.getSelection());
		store.setValue(NewsPlugin.PREFS_FOLLOW_QUOTE_FEED, followQuoteFeed.getSelection());
		store.setValue(CorePlugin.PREFS_NEWS_DATE_RANGE, daysToKeep.getSelection());

		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			String id = (String) items[i].getData();
			store.setValue(id, items[i].getChecked());
		}*/

		return super.performOk();
	}
}
