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
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

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
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);

        enableHttpProxy = new Button(group, SWT.CHECK);
        enableHttpProxy.setText(Messages.ProxyPreferencePage_EnableHTTP);
        gd = new GridData();
        gd.horizontalSpan = 2;
        enableHttpProxy.setLayoutData(gd);

        httpProxyHostLabel = new Label(group, SWT.NONE);
        httpProxyHostLabel.setText(Messages.ProxyPreferencePage_HostAddress);

        httpProxyHostText = new Text(group, SWT.SINGLE | SWT.BORDER);
        httpProxyHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        httpProxyPortLabel = new Label(group, SWT.NONE);
        httpProxyPortLabel.setText(Messages.ProxyPreferencePage_HostPort);

        httpProxyPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
        httpProxyPortText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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

        enableHttpProxy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                boolean enable = enableHttpProxy.getSelection();
                httpProxyPortLabel.setEnabled(enable);
                httpProxyHostLabel.setEnabled(enable);
                httpProxyPortText.setEnabled(enable);
                httpProxyHostText.setEnabled(enable);
            }
        });

        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
        enableHttpProxy.setSelection(store.getBoolean(CorePlugin.PREFS_ENABLE_HTTP_PROXY));
        httpProxyHostText.setText(store.getString(CorePlugin.PREFS_PROXY_HOST_ADDRESS));
        httpProxyPortText.setText(store.getString(CorePlugin.PREFS_PROXY_PORT_ADDRESS));

        httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
        httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
        httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
        httpProxyHostText.setEnabled(enableHttpProxy.getSelection());
        
        return content;
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
        
        return super.performOk();
    }
}
