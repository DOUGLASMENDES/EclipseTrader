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

public class ChangePercent extends Column
{
    private NumberFormat formatter = NumberFormat.getInstance();

    public ChangePercent()
    {
        super("Chg.%", RIGHT);

        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return "";
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && quote.getLast() != 0 && item.getSecurity().getClose() != null)
        {
            double change = quote.getLast() - item.getSecurity().getClose().doubleValue();
            double percentage = (change / item.getSecurity().getClose().doubleValue()) * 100.0;

            if (change > 0)
                return "+" + formatter.format(percentage) + "%";
            return formatter.format(percentage) + "%";
        }
        return "";
    }
}
