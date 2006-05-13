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

import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

public class TransactionAction extends Action
{
    private IViewPart view;

    public TransactionAction(IViewPart view)
    {
        this.view = view;
        setText("New Transaction");
        setEnabled(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        ISelection selection = view.getSite().getSelectionProvider().getSelection();
        if (selection instanceof AccountSelection)
        {
            TransactionDialog dlg = new TransactionDialog(((AccountSelection)selection).getAccount(), view.getViewSite().getShell());
            dlg.open();
        }
    }
}
