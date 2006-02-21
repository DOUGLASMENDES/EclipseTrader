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

package net.sourceforge.eclipsetrader.core.db.columns;

import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

public class Description extends Column
{

    public Description()
    {
        super("Description", LEFT);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return "";
        return item.getSecurity().getDescription();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getTotalsText(net.sourceforge.eclipsetrader.core.db.Watchlist)
     */
    public String getTotalsText(Watchlist watchlist)
    {
        return "TOTALS";
    }
}
