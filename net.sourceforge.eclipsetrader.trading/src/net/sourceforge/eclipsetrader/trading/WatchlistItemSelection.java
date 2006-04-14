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

package net.sourceforge.eclipsetrader.trading;

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;

public class WatchlistItemSelection extends SecuritySelection
{
    private WatchlistItem watchlistItem;

    public WatchlistItemSelection(WatchlistItem watchlistItem)
    {
        super(watchlistItem.getSecurity());
        this.watchlistItem = watchlistItem;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty()
    {
        return watchlistItem == null;
    }

    public WatchlistItem getWatchlistItem()
    {
        return watchlistItem;
    }
}
