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

package net.sourceforge.eclipsetrader.trading.internal.watchlist;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.eclipse.jface.viewers.LabelProvider;

public class Balance extends LabelProvider implements Comparator
{
    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();

    public Balance()
    {
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
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof WatchlistItem)
        {
            WatchlistItem item = (WatchlistItem)element;
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
        }
        
        if (element instanceof Watchlist)
        {
            double paid = 0;
            double current = 0;
            
            for (Iterator iter = ((Watchlist)element).getItems().iterator(); iter.hasNext(); )
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

        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1)
    {
        if (getValue((WatchlistItem)arg0) > getValue((WatchlistItem)arg1))
            return 1;
        else if (getValue((WatchlistItem)arg0) < getValue((WatchlistItem)arg1))
            return -1;
        return 0;
    }

    private double getValue(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return 0;
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && item.getPosition() != null && item.getPaidPrice() != null)
        {
            double paid = CurrencyConverter.getInstance().convert(item.getPosition().intValue() * item.getPaidPrice().doubleValue(), item.getSecurity().getCurrency(), item.getParent().getCurrency());
            double current = CurrencyConverter.getInstance().convert(item.getPosition().intValue() * quote.getLast(), item.getSecurity().getCurrency(), item.getParent().getCurrency());
            return current - paid;
        }
        return 0;
    }
}
