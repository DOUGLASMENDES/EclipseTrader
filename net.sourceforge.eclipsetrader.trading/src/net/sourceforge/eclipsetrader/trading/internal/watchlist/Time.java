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

import java.text.SimpleDateFormat;
import java.util.Comparator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.eclipse.jface.viewers.LabelProvider;

public class Time extends LabelProvider implements Comparator
{
    protected SimpleDateFormat formatter = CorePlugin.getTimeFormat();

    public Time()
    {
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
            if (quote != null && quote.getDate() != null)
                return formatter.format(quote.getDate());
        }

        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1)
    {
        java.util.Date d0 = getValue((WatchlistItem)arg0); 
        java.util.Date d1 = getValue((WatchlistItem)arg1);
        if (d0 == null || d1 == null)
            return 0;
        return d0.compareTo(d1);
    }

    private java.util.Date getValue(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return null;
        Quote quote = item.getSecurity().getQuote();
        if (quote != null && quote.getDate() != null)
            return quote.getDate();
        return null;
    }
}
