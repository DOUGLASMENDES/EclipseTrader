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
import net.sourceforge.eclipsetrader.trading.views.TransactionsView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class DeleteTransactionAction extends Action
{
    private TransactionsView view;

    public DeleteTransactionAction(TransactionsView view)
    {
        this.view = view;
        setText("&Delete");
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (MessageDialog.openConfirm(view.getViewSite().getShell(), view.getPartName(), "Do you really want to delete the selected transaction(s) ?"))
        {
            Account account = view.getAccount();
            TableItem[] items = view.getTable().getSelection();
            for (int i = 0; i < items.length; i++)
                account.getTransactions().remove(items[i].getData());
            CorePlugin.getRepository().save(account);
        }
    }
}
