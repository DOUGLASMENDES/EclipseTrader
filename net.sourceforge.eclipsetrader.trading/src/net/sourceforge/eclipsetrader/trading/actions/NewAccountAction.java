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

import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;
import net.sourceforge.eclipsetrader.trading.wizards.accounts.NewAccountWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class NewAccountAction extends Action implements IViewActionDelegate
{
    private AccountsView view;
    
    public NewAccountAction(AccountsView view)
    {
        init(view);
        setText("Create Account");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
        this.view = (AccountsView) view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        run(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        AccountGroup group = null;
        ISelection selection = view.getSite().getSelectionProvider().getSelection();
        if (selection instanceof AccountGroupSelection)
            group = ((AccountGroupSelection)selection).getGroup();

        NewAccountWizard wizard = new NewAccountWizard();
        if (group == null)
            wizard.open();
        else
            wizard.open(group);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
