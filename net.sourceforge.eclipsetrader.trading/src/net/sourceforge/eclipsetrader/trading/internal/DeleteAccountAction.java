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

package net.sourceforge.eclipsetrader.trading.internal;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;
import net.sourceforge.eclipsetrader.trading.views.TransactionsView;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DeleteAccountAction extends DeleteAction
{
    private AccountsView view;

    public DeleteAccountAction(AccountsView view)
    {
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (MessageDialog.openConfirm(view.getViewSite().getShell(), view.getPartName(), "Do you really want to delete the selected account(s) ?"))
        {
            TreeItem[] items = view.getTree().getSelection();
            
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int w = 0; w < windows.length; w++)
            {
                IWorkbenchPage[] pages = windows[w].getPages();
                for (int p = 0; p < pages.length; p++)
                {
                    for (int i = 0; i < items.length; i++)
                    {
                        if (!(items[i].getData() instanceof Account))
                            continue;
                        IViewReference ref = pages[p].findViewReference(TransactionsView.VIEW_ID, String.valueOf(((Account)items[i].getData()).getId()));
                        if (ref != null)
                            pages[p].hideView(ref);
                    }
                }
            }

            for (int i = 0; i < items.length; i++)
                CorePlugin.getRepository().delete((PersistentObject)items[i].getData());
        }
    }
}
