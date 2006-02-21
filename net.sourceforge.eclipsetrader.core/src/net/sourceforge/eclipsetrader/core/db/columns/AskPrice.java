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

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

public class AskPrice extends Column
{
    private NumberFormat formatter = NumberFormat.getInstance();

    public AskPrice()
    {
        super("Ask", RIGHT);
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(4);
        formatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return "";
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && quote.getAsk() != 0)
            return formatter.format(quote.getAsk());
        return "";
    }
}
