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
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.WatchlistItemSelection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DeleteWatchlistItemAction implements IViewActionDelegate
{
    private String defaultText;
    private WatchlistItem watchlistItem;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        if (watchlistItem != null)
        {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (MessageDialog.openConfirm(window.getShell(), "Watchlist", "Do you really want to delete the selected item ?"))
            {
                Watchlist watchlist = watchlistItem.getParent();
                watchlist.getItems().remove(watchlistItem);
                CorePlugin.getRepository().save(watchlist);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        if (defaultText == null)
            defaultText = action.getText();

        watchlistItem = null;
        if (selection instanceof WatchlistItemSelection)
        {
            watchlistItem = ((WatchlistItemSelection)selection).getWatchlistItem();
            action.setText(defaultText + " " + watchlistItem.getSecurity().getDescription());
            action.setEnabled(true);
        }
        else
        {
            action.setText(defaultText);
            action.setEnabled(false);
        }
    }
}
