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

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.transfers.WatchlistItemTransfer;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CutAction extends Action
{
    private WatchlistView view;

    public CutAction(WatchlistView view)
    {
        this.view = view;
        setText("Cu&t");
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        WatchlistItem[] selection = view.getSelection();
        
        Object[] items = new Object[selection.length * 2];
        Transfer[] transfer = new Transfer[items.length];
        
        int index = 0;
        for (int i = 0; i < selection.length; i++)
        {
            items[index] = selection[i];
            transfer[index] = WatchlistItemTransfer.getInstance();
            index++;
            items[index] = selection[i].getSecurity();
            transfer[index] = SecurityTransfer.getInstance();
            index++;
        }
        
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(items, transfer);
        clipboard.dispose();

        for (int i = 0; i < selection.length; i++)
            view.getWatchlist().getItems().remove(selection[i]);
    }
}
