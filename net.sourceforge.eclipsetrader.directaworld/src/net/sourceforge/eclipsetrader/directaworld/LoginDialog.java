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

package net.sourceforge.eclipsetrader.directaworld;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class LoginDialog extends TitleAreaDialog
{
    private String userName = ""; //$NON-NLS-1$
    private String password = ""; //$NON-NLS-1$
    private Text text1;
    private Text text2;
    private Button button;
    private Image image = DirectaWorldPlugin.getImageDescriptor("icons/key.gif").createImage(); //$NON-NLS-1$

    public LoginDialog(String userName, String password)
    {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        this.userName = userName;
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.LoginDialog_Caption);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.LoginDialog_UserName);
        text1 = new Text(composite, SWT.BORDER);
        text1.setText(userName);
        text1.setLayoutData(new GridData(200, SWT.DEFAULT));

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.LoginDialog_Password);
        text2 = new Text(composite, SWT.BORDER|SWT.PASSWORD);
        text2.setLayoutData(new GridData(200, SWT.DEFAULT));
        
        label = new Label(composite, SWT.NONE);
        button = new Button(composite, SWT.CHECK);
        button.setText(Messages.LoginDialog_SavePassword);

        return super.createDialogArea(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#open()
     */
    public int open()
    {
        create();
        
        setTitle(Messages.LoginDialog_Title);
        setMessage(Messages.LoginDialog_Description);
        setTitleImage(image);

        return super.open();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        userName = text1.getText();
        password = text2.getText();

        DirectaWorldPlugin.getDefault().getPreferenceStore().setValue(DirectaWorldPlugin.USERNAME_PREFS, userName);
        DirectaWorldPlugin.getDefault().getPreferenceStore().setValue(DirectaWorldPlugin.PASSWORD_PREFS, button.getSelection() ? password : ""); //$NON-NLS-1$
        
        image.dispose();
        super.okPressed();
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }
}
