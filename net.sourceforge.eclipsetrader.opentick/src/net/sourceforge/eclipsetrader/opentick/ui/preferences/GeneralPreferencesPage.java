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

package net.sourceforge.eclipsetrader.opentick.ui.preferences;

import net.sourceforge.eclipsetrader.opentick.OpenTickPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GeneralPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Combo server;
    private Text userName;
    private Text password;

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
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Server");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        server = new Combo(content, SWT.READ_ONLY);
        server.add("feed1.opentick.com:10015 (delayed)");
        server.add("feed1.opentick.com:10010 (realtime)");
        server.add("feed2.opentick.com:10010 (realtime)");
        server.select(OpenTickPlugin.getDefault().getPreferenceStore().getInt(OpenTickPlugin.PREFS_SERVER));
        
        label = new Label(content, SWT.NONE);
        label.setText("Username");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        userName = new Text(content, SWT.BORDER);
        userName.setText(OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_USERNAME));
        userName.setLayoutData(new GridData(125, SWT.DEFAULT));
        
        label = new Label(content, SWT.NONE);
        label.setText("Password");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        password = new Text(content, SWT.BORDER|SWT.PASSWORD);
        password.setText(OpenTickPlugin.getDefault().getPreferenceStore().getString(OpenTickPlugin.PREFS_PASSWORD));
        password.setLayoutData(new GridData(125, SWT.DEFAULT));
        
        label = new Label(content, SWT.NONE);
        label.setText("Warning! Passwords are saved in clear text.");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, false, true, 2, 1));

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = OpenTickPlugin.getDefault().getPreferenceStore(); 
        store.setValue(OpenTickPlugin.PREFS_SERVER, server.getSelectionIndex());
        store.setValue(OpenTickPlugin.PREFS_USERNAME, userName.getText());
        store.setValue(OpenTickPlugin.PREFS_PASSWORD, password.getText());
        return super.performOk();
    }
}
