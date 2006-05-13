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

import net.sourceforge.eclipsetrader.core.db.Transaction;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;
import net.sourceforge.eclipsetrader.trading.views.TransactionsView;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.TableItem;

public class TransactionSettingsAction extends Action
{
    private TransactionsView view;

    public TransactionSettingsAction(TransactionsView view)
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
        TableItem[] selection = view.getTable().getSelection();
        if (selection.length == 1)
        {
            Transaction transaction = (Transaction)selection[0].getData(); 
            TransactionDialog dlg = new TransactionDialog(view.getAccount(), view.getViewSite().getShell());
            dlg.open(transaction);
        }
    }
}
