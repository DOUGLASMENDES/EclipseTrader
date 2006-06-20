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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
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
            Account account = ((AccountSelection)selection).getAccount();
            String previousDescription = account.getDescription();

            AccountSettingsDialog dlg = new AccountSettingsDialog(account, view.getViewSite().getShell());
            if (dlg.open() == AccountSettingsDialog.OK)
            {
                if (!previousDescription.equals(account.getDescription()))
                {
                    if (account.getGroup() != null)
                    {
                        account.getGroup().getAccounts().remove(account);
                        account.getGroup().getAccounts().add(account);
                    }
                    else
                    {
                        CorePlugin.getRepository().allAccounts().remove(account);
                        CorePlugin.getRepository().allAccounts().add(account);
                    }
                }
            }
        }
        else if (selection instanceof AccountGroupSelection)
        {
            AccountGroup group = ((AccountGroupSelection)selection).getGroup();
            String previousDescription = group.getDescription();

            InputDialog dlg = new InputDialog(view.getViewSite().getShell(), "Edit Group", "Enter the name of the group to edit:", group.getDescription(), null);
            if (dlg.open() == InputDialog.OK && dlg.getValue() != null && !previousDescription.equals(group.getDescription()))
            {
                group.setDescription(dlg.getValue());
                CorePlugin.getRepository().save(group);
                
                if (group.getParent() != null)
                {
                    group.getParent().getGroups().remove(group);
                    group.getParent().getGroups().add(group);
                }
                else
                {
                    CorePlugin.getRepository().allAccountGroups().remove(group);
                    CorePlugin.getRepository().allAccountGroups().add(group);
                }
            }
        }
    }
}
