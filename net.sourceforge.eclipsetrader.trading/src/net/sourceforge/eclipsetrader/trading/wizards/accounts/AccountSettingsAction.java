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

package net.sourceforge.eclipsetrader.trading.wizards.accounts;

import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;
import net.sourceforge.eclipsetrader.trading.wizards.AccountSettingsDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;

public class AccountSettingsAction extends Action
{
    private AccountsView view;

    public AccountSettingsAction(AccountsView view)
    {
        this.view = view;
        setText("P&roperties");
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        ISelection selection = view.getSite().getSelectionProvider().getSelection();
        if (selection instanceof AccountSelection)
        {
            AccountSettingsDialog dlg = new AccountSettingsDialog(((AccountSelection)selection).getAccount(), view.getViewSite().getShell());
            dlg.open();
        }
    }
}
