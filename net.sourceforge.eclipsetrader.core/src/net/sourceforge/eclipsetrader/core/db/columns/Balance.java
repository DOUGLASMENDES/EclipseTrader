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
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.core.db.internal.Messages;

public class Balance extends Column
{
    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();

    public Balance()
    {
        super(Messages.Balance_Label, RIGHT);

        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        
        percentFormatter.setGroupingUsed(false);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return ""; //$NON-NLS-1$
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && item.getPosition() != null && item.getPaidPrice() != null)
        {
            double paid = CurrencyConverter.getInstance().convert(item.getPosition().intValue() * item.getPaidPrice().doubleValue(), item.getSecurity().getCurrency(), item.getParent().getCurrency());
            double current = CurrencyConverter.getInstance().convert(item.getPosition().intValue() * quote.getLast(), item.getSecurity().getCurrency(), item.getParent().getCurrency());
            if (current > paid)
                return "+" + formatter.format(current - paid) + " (+" + percentFormatter.format((current - paid) / paid * 100.0) + "%)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return formatter.format(current - paid) + " (" + percentFormatter.format((current - paid) / paid * 100.0) + "%)"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getTotalsText(net.sourceforge.eclipsetrader.core.db.Watchlist)
     */
    public String getTotalsText(Watchlist watchlist)
    {
        double paid = 0;
        double current = 0;
        
        for (Iterator iter = watchlist.getItems().iterator(); iter.hasNext(); )
        {
            WatchlistItem item = (WatchlistItem)iter.next();
            if (item.getSecurity() == null)
                continue;

            Quote quote = item.getSecurity().getQuote();
            if (quote != null && item.getPosition() != null && item.getPaidPrice() != null)
            {
                paid += item.getPosition().intValue() * item.getPaidPrice().doubleValue();
                current += item.getPosition().intValue() * quote.getLast();
            }
        }
        
        if (paid == 0 || current == 0)
            return ""; //$NON-NLS-1$
        
        if (current > paid)
            return "+" + formatter.format(current - paid) + " (+" + percentFormatter.format((current - paid) / paid * 100.0) + "%)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return formatter.format(current - paid) + " (" + percentFormatter.format((current - paid) / paid * 100.0) + "%)"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
