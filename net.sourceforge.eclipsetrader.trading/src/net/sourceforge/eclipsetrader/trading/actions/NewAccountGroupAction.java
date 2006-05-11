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

package net.sourceforge.eclipsetrader.trading.actions;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;

public class NewAccountGroupAction extends Action
{
    private AccountsView view;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public NewAccountGroupAction(AccountsView view)
    {
        this.view = view;
        setText("Create Group");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        InputDialog dlg = new InputDialog(view.getViewSite().getShell(), getText(), "Enter the name of the group to create:", null, null);
        if (dlg.open() == InputDialog.OK && dlg.getValue() != null)
        {
            AccountGroup group = new AccountGroup();
            group.setDescription(dlg.getValue());

            ISelection selection = view.getSite().getSelectionProvider().getSelection();
            if (selection instanceof AccountSelection)
                group.setParent(((AccountSelection)selection).getAccount().getGroup());
            else if (selection instanceof AccountGroupSelection)
                group.setParent(((AccountGroupSelection)selection).getGroup());
            
            CorePlugin.getRepository().save(group);
        }
    }
}
