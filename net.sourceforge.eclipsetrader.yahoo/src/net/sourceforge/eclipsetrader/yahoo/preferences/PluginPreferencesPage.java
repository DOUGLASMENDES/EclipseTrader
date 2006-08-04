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

package net.sourceforge.eclipsetrader.yahoo.preferences;

import net.sourceforge.eclipsetrader.yahoo.YahooPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PluginPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button updateHistory;

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

        updateHistory = new Button(content, SWT.CHECK);
        updateHistory.setText("Update history with quote data on snapshot");
        updateHistory.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        IPreferenceStore store = YahooPlugin.getDefault().getPreferenceStore();
        updateHistory.setSelection(store.getBoolean(YahooPlugin.PREFS_UPDATE_HISTORY));

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = YahooPlugin.getDefault().getPreferenceStore();

        store.setValue(YahooPlugin.PREFS_UPDATE_HISTORY, updateHistory.getSelection());

        return super.performOk();
    }
}
