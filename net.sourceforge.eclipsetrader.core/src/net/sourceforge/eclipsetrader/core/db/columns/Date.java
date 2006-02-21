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

import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

public class Date extends Column
{
    protected SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    public Date()
    {
        super("Date", RIGHT);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return "";
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && quote.getDate() != null)
            return formatter.format(quote.getDate());
        return "";
    }
}
