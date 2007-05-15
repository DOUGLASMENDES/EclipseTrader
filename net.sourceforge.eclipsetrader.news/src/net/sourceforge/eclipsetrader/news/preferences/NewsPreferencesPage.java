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

package net.sourceforge.eclipsetrader.news.preferences;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NewsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button updateStartup;
    private Button followQuoteFeed;
    private Spinner daysToKeep;
    private Table table;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        updateStartup = new Button(content, SWT.CHECK);
        updateStartup.setText(Messages.NewsPreferencesPage_StartupUpdate);
        updateStartup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        
        followQuoteFeed = new Button(content, SWT.CHECK);
        followQuoteFeed.setText(Messages.NewsPreferencesPage_FollowQuoteFeed);
        followQuoteFeed.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        
        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.NewsPreferencesPage_DaysToKeep);
        daysToKeep = new Spinner(content, SWT.BORDER);
        daysToKeep.setMinimum(1);
        daysToKeep.setMaximum(9999);
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.SINGLE|SWT.CHECK);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.NewsPreferencesPage_Provider);

        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();
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
        
        table.getColumn(0).pack();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();

        store.setValue(NewsPlugin.PREFS_UPDATE_ON_STARTUP, updateStartup.getSelection());
        store.setValue(NewsPlugin.PREFS_FOLLOW_QUOTE_FEED, followQuoteFeed.getSelection());
        store.setValue(CorePlugin.PREFS_NEWS_DATE_RANGE, daysToKeep.getSelection());
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            String id = (String) items[i].getData();
            store.setValue(id, items[i].getChecked());
        }
        return super.performOk();
    }
}
