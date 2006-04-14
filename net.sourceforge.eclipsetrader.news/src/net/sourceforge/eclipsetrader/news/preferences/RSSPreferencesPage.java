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

import net.sourceforge.eclipsetrader.news.NewsPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RSSPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Text interval;

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
        
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true, 2, 1));
        Label label = new Label(group, SWT.NONE);
        label.setText("Automatic update every");
        interval = new Text(group, SWT.BORDER);
        interval.setLayoutData(new GridData(60, SWT.DEFAULT));
        label = new Label(group, SWT.NONE);
        label.setText("minutes");

        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();
        interval.setText(store.getString(NewsPlugin.PREFS_UPDATE_INTERVAL));

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = NewsPlugin.getDefault().getPreferenceStore();

        store.setValue(NewsPlugin.PREFS_UPDATE_INTERVAL, interval.getText());

        return super.performOk();
    }
}
