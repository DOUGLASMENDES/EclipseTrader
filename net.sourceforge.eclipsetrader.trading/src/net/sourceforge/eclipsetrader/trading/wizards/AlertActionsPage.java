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

package net.sourceforge.eclipsetrader.trading.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class AlertActionsPage extends WizardPage
{
    private Button popup;
    private Button hilight;
    
    public AlertActionsPage()
    {
        super("");
        setTitle("Alert Actions");
        setDescription("Set the actions to perform when the alert is fired");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);
        
        popup = new Button(content, SWT.CHECK);
        popup.setText("Pop-up a message");
        popup.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

        hilight = new Button(content, SWT.CHECK);
        hilight.setText("Hilight watchlist row");
        hilight.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
    }
    
    public boolean getPopupSelection()
    {
        return popup != null ? popup.getSelection() : false;
    }
    
    public boolean getHilightSelection()
    {
        return hilight != null ? hilight.getSelection() : false;
    }
}
