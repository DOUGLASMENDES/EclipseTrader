/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.ui.preferences;

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
import org.eclipsetrader.opentick.internal.OTActivator;

public class AuthenticationPage extends PreferencePage implements IWorkbenchPreferencePage {
    private Combo server;
	private Text userName;
	private Text password;

	public AuthenticationPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(OTActivator.getDefault().getPreferenceStore());
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
        label.setText("Server");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        server = new Combo(content, SWT.READ_ONLY);
        server.add("delayed1.opentick.com:10015 (delayed)");
        server.add("delayed2.opentick.com:10015 (delayed)");
        server.add("feed1.opentick.com:10010 (realtime)");
        server.add("feed2.opentick.com:10010 (realtime)");
        server.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		label = new Label(content, SWT.NONE);
		label.setText("User Name");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		userName = new Text(content, SWT.BORDER);
		userName.setLayoutData(new GridData(convertHorizontalDLUsToPixels(90), SWT.DEFAULT));

		label = new Label(content, SWT.NONE);
		label.setText("Password");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		password = new Text(content, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(convertHorizontalDLUsToPixels(90), SWT.DEFAULT));

		label = new Label(content, SWT.NONE);
		label.setText("Warning! Passwords are saved in clear text.");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, false, true, 2, 1));

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
    	switch(server.getSelectionIndex()) {
    		case 0:
    	    	getPreferenceStore().setValue(OTActivator.PREFS_SERVER, "delayed1.opentick.com");
    	    	getPreferenceStore().setValue(OTActivator.PREFS_PORT, 10015);
    			break;
    		case 1:
    	    	getPreferenceStore().setValue(OTActivator.PREFS_SERVER, "delayed2.opentick.com");
    	    	getPreferenceStore().setValue(OTActivator.PREFS_PORT, 10015);
    			break;
    		case 2:
    	    	getPreferenceStore().setValue(OTActivator.PREFS_SERVER, "feed1.opentick.com");
    	    	getPreferenceStore().setValue(OTActivator.PREFS_PORT, 10010);
    			break;
    		case 3:
    	    	getPreferenceStore().setValue(OTActivator.PREFS_SERVER, "feed2.opentick.com");
    	    	getPreferenceStore().setValue(OTActivator.PREFS_PORT, 10010);
    			break;
    	}
        getPreferenceStore().setValue(OTActivator.PREFS_USERNAME, userName.getText());
        getPreferenceStore().setValue(OTActivator.PREFS_PASSWORD, password.getText());
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        server.select(getServerIndex());
		userName.setText(getPreferenceStore().getString(OTActivator.PREFS_USERNAME));
		password.setText(getPreferenceStore().getString(OTActivator.PREFS_PASSWORD));
    }

    protected int getServerIndex() {
    	String server = getPreferenceStore().getString(OTActivator.PREFS_SERVER);
    	int port = getPreferenceStore().getInt(OTActivator.PREFS_PORT);
    	if (server.equals("feed1.opentick.com") && port == 10015)
    		return 0;
    	if (server.equals("delayed1.opentick.com") && port == 10015)
    		return 0;
    	if (server.equals("delayed2.opentick.com") && port == 10015)
    		return 1;
    	if (server.equals("feed1.opentick.com") && port == 10010)
    		return 2;
    	if (server.equals("feed2.opentick.com") && port == 10010)
    		return 3;
    	return -1;
    }
}
