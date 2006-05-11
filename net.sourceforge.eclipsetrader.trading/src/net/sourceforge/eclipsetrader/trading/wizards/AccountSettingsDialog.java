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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.trading.wizards.accounts.CommissionsPage;
import net.sourceforge.eclipsetrader.trading.wizards.accounts.GeneralPage;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

public class AccountSettingsDialog extends PreferenceDialog
{
    private Account account;
    private GeneralPage generalPage;
    private CommissionsPage commissionsPage;

    public AccountSettingsDialog(Account account, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.account = account;
        
        generalPage = new GeneralPage(account);
        getPreferenceManager().addToRoot(new PreferenceNode("general", new CommonDialogPage(generalPage)));
        
        commissionsPage = new CommissionsPage(account);
        getPreferenceManager().addToRoot(new PreferenceNode("commissions", new CommonDialogPage(commissionsPage)));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Account Properties");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#handleSave()
     */
    protected void handleSave()
    {
        CorePlugin.getRepository().save(account);
        super.handleSave();
    }
}
