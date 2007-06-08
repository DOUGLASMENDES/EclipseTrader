/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.core.ui.preferences;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Proxy settings preference page
 * <p></p>
 */
public class ProxyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button enableHttpProxy;
    private Text httpProxyHostText;
    private Text httpProxyPortText;
    private Label httpProxyHostLabel;
    private Label httpProxyPortLabel;
    private Button enableProxyAuthentication;
    private Text httpProxyUserText;
    private Text httpProxyPasswordText;
    private Label httpProxyUserLabel;
    private Label httpProxyPasswordLabel;

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Group group = new Group(content, SWT.NONE);
        group.setText(Messages.ProxyPreferencePage_Settings);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

        enableHttpProxy = new Button(group, SWT.CHECK);
        enableHttpProxy.setText(Messages.ProxyPreferencePage_EnableHTTP);
        enableHttpProxy.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        enableHttpProxy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateControlsEnablement();
            }
        });

        httpProxyHostLabel = new Label(group, SWT.NONE);
        httpProxyHostLabel.setText(Messages.ProxyPreferencePage_HostAddress);

        httpProxyHostText = new Text(group, SWT.BORDER);
        httpProxyHostText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        httpProxyPortLabel = new Label(group, SWT.NONE);
        httpProxyPortLabel.setText(Messages.ProxyPreferencePage_HostPort);

        httpProxyPortText = new Text(group, SWT.BORDER);
        httpProxyPortText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        // Validation of port field
        httpProxyPortText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                try
                {
                    String portValue = httpProxyPortText.getText();
                    int num = 80;
                    if (portValue != null && portValue.trim().length() > 0)
                        num = Integer.valueOf(portValue).intValue();
                    if (0 <= num && num <= 0xFFFF)
                    {
                        // port is valid
                        ProxyPreferencePage.this.setValid(true);
                        setErrorMessage(null);
                        return;
                    }

                    // port is invalid
                }
                catch (NumberFormatException nfe)
                {
                }
                ProxyPreferencePage.this.setValid(false);
                setErrorMessage(Messages.ProxyPreferencePage_PortErrorMessage);
            }
        });

        enableProxyAuthentication = new Button(group, SWT.CHECK);
        enableProxyAuthentication.setText(Messages.ProxyPreferencePage_EnableAuthentication);
        enableProxyAuthentication.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
        enableProxyAuthentication.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateControlsEnablement();
            }
        });

        httpProxyUserLabel = new Label(group, SWT.NONE);
        httpProxyUserLabel.setText(Messages.ProxyPreferencePage_Username);

        httpProxyUserText = new Text(group, SWT.BORDER);
        httpProxyUserText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        httpProxyPasswordLabel = new Label(group, SWT.NONE);
        httpProxyPasswordLabel.setText(Messages.ProxyPreferencePage_Password);

        httpProxyPasswordText = new Text(group, SWT.PASSWORD|SWT.BORDER);
        httpProxyPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        performDefaults();
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
        
        enableHttpProxy.setSelection(store.getBoolean(CorePlugin.PREFS_ENABLE_HTTP_PROXY));
        httpProxyHostText.setText(store.getString(CorePlugin.PREFS_PROXY_HOST_ADDRESS));
        httpProxyPortText.setText(store.getString(CorePlugin.PREFS_PROXY_PORT_ADDRESS));
        enableProxyAuthentication.setSelection(store.getBoolean(CorePlugin.PREFS_ENABLE_PROXY_AUTHENTICATION));
        httpProxyUserText.setText(store.getString(CorePlugin.PREFS_PROXY_USER));
        httpProxyPasswordText.setText(store.getString(CorePlugin.PREFS_PROXY_PASSWORD));
        
        updateControlsEnablement();

        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
        
        store.setValue(CorePlugin.PREFS_ENABLE_HTTP_PROXY, enableHttpProxy.getSelection());
        store.setValue(CorePlugin.PREFS_PROXY_HOST_ADDRESS, httpProxyHostText.getText());
        store.setValue(CorePlugin.PREFS_PROXY_PORT_ADDRESS, httpProxyPortText.getText());
        store.setValue(CorePlugin.PREFS_ENABLE_PROXY_AUTHENTICATION, enableProxyAuthentication.getSelection());
        store.setValue(CorePlugin.PREFS_PROXY_USER, httpProxyUserText.getText());
        store.setValue(CorePlugin.PREFS_PROXY_PASSWORD, httpProxyPasswordText.getText());
        
        return super.performOk();
    }
    
    protected void updateControlsEnablement()
    {
        httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
        httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
        httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
        httpProxyHostText.setEnabled(enableHttpProxy.getSelection());
        enableProxyAuthentication.setEnabled(enableHttpProxy.getSelection());
        httpProxyUserLabel.setEnabled(enableProxyAuthentication.getSelection() && enableProxyAuthentication.isEnabled());
        httpProxyPasswordLabel.setEnabled(enableProxyAuthentication.getSelection() && enableProxyAuthentication.isEnabled());
        httpProxyUserText.setEnabled(enableProxyAuthentication.getSelection() && enableProxyAuthentication.isEnabled());
        httpProxyPasswordText.setEnabled(enableProxyAuthentication.getSelection() && enableProxyAuthentication.isEnabled());
    }
}
