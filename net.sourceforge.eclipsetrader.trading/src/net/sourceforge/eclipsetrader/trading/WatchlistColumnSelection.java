/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Danilo Tuler     - column selection
 */

package net.sourceforge.eclipsetrader.trading;

import net.sourceforge.eclipsetrader.core.db.WatchlistColumn;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

public class WatchlistColumnSelection extends WatchlistItemSelection
{
    private WatchlistColumn selectedColumn;

    public WatchlistColumnSelection(WatchlistItem watchlistItem, WatchlistColumn selectedColumn)
    {
        super(watchlistItem);
        this.selectedColumn = selectedColumn;
    }

    public WatchlistColumn getSelectedColumn()
    {
        return selectedColumn;
    }
}
