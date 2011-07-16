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

package org.eclipsetrader.directa.internal.core.connector;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.directa.internal.Activator;

public class LoginDialog extends TitleAreaDialog {

    private String userName = ""; //$NON-NLS-1$
    private String password = ""; //$NON-NLS-1$
    private boolean savePassword;
    private Text text1;
    private Text text2;
    private Button button;
    private Image image;

    private DisposeListener disposeListener = new DisposeListener() {

        @Override
        public void widgetDisposed(DisposeEvent e) {
            if (image != null) {
                image.dispose();
            }
        }
    };

    public LoginDialog(Shell parentShell, String userName, String password) {
        super(parentShell);
        this.userName = userName;
        this.password = password;
        try {
            this.image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/key.gif").createImage(); //$NON-NLS-1$
        } catch (Exception e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.LoginDialog_ShellText);
        newShell.addDisposeListener(disposeListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.LoginDialog_UserCode);
        text1 = new Text(composite, SWT.BORDER);
        if (userName != null) {
            text1.setText(userName);
        }
        text1.setLayoutData(new GridData(convertHorizontalDLUsToPixels(140), SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.LoginDialog_Password);
        text2 = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        text2.setLayoutData(new GridData(convertHorizontalDLUsToPixels(140), SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        button = new Button(composite, SWT.CHECK);
        button.setText(Messages.LoginDialog_SavePassword);

        if (text1.getText().equals("")) { //$NON-NLS-1$
            text1.setFocus();
        }
        else {
            text2.setFocus();
        }

        return super.createDialogArea(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#open()
     */
    @Override
    public int open() {
        create();

        setTitle(Messages.LoginDialog_Title);
        setMessage(Messages.LoginDialog_Message);
        setTitleImage(image);

        return super.open();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        userName = text1.getText();
        password = text2.getText();
        savePassword = button.getSelection();
        super.okPressed();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSavePassword() {
        return savePassword;
    }
}
