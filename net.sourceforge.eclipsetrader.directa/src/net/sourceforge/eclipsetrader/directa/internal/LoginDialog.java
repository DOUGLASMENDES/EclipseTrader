/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.directa.internal;

import net.sourceforge.eclipsetrader.directa.DirectaPlugin;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 */
public class LoginDialog extends TitleAreaDialog
{
  private String userName = "";
  private String password = "";
  private Text text1;
  private Text text2;
  private Button button;

  /**
   * Create a new instance of this class.
   */
  public LoginDialog()
  {
    super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

    IPreferenceStore ps = DirectaPlugin.getDefault().getPreferenceStore();
    userName = ps.getString("user.name");
    password = ps.getString("user.password");
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Directa");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.GRAB_VERTICAL|GridData.VERTICAL_ALIGN_CENTER|GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER));
    composite.setLayout(new GridLayout(2, false));
    
    Label label = new Label(composite, SWT.NONE);
    label.setText("Codice Utente:");
    label.setLayoutData(new GridData());
    text1 = new Text(composite, SWT.BORDER);
    text1.setText(userName);
    GridData gridData = new GridData();
    gridData.widthHint = 200;
    text1.setLayoutData(gridData);

    label = new Label(composite, SWT.NONE);
    label.setText("Password:");
    label.setLayoutData(new GridData());
    text2 = new Text(composite, SWT.BORDER);
    text2.setEchoChar('*');
    gridData = new GridData();
    gridData.widthHint = 200;
    text2.setLayoutData(gridData);

    label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData());
    button = new Button(composite, SWT.CHECK);
    button.setText("Salva i codici per gli accessi futuri");
    
    return super.createDialogArea(parent);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#open()
   */
  public int open()
  {
    create();
    
    setTitle("Realtime Server Login");
    setMessage("Please enter your user id and password");
    setTitleImage(Images.ICON_KEY.createImage());
    
    return super.open();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed()
  {
    userName = text1.getText();
    password = text2.getText();
    
    IPreferenceStore ps = DirectaPlugin.getDefault().getPreferenceStore();
    if (button.getSelection() == true)
    {
      ps.setValue("user.name", userName);
      ps.setValue("user.password", password);
    }
    else
    {
      ps.setValue("user.name", "");
      ps.setValue("user.password", "");
    }
    
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
