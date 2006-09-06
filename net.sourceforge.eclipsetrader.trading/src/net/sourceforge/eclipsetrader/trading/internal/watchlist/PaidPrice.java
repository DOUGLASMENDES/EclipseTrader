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
import java.text.ParseException;
import java.util.Comparator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.IEditableLabelProvider;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

import org.eclipse.jface.viewers.LabelProvider;

public class PaidPrice extends LabelProvider implements IEditableLabelProvider, Comparator
{
    private NumberFormat formatter = NumberFormat.getInstance();

    public PaidPrice()
    {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(4);
        formatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof WatchlistItem)
        {
            WatchlistItem item = (WatchlistItem)element;
            if (item != null && item.getPaidPrice() != null)
            {
                if (item.getSecurity() != null)
                    return formatter.format(CurrencyConverter.getInstance().convert(item.getPaidPrice(), item.getSecurity().getCurrency(), item.getParent().getCurrency()));
                else
                    return formatter.format(item.getPaidPrice());
            }
        }

        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IEditableLabelProvider#getEditableText(java.lang.Object)
     */
    public String getEditableText(Object element)
    {
        return getText(element);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IEditableLabelProvider#isEditable(java.lang.Object)
     */
    public boolean isEditable(Object element)
    {
        return (element instanceof WatchlistItem);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.IEditableLabelProvider#setEditableText(java.lang.Object, java.lang.String)
     */
    public void setEditableText(Object element, String text)
    {
        if (element instanceof WatchlistItem)
        {
            WatchlistItem item = (WatchlistItem)element;
            try
            {
                item.setPaidPrice(formatter.parse(text));
                item.notifyObservers();
            }
            catch (ParseException e) {
                CorePlugin.logException(e);
            }
        }
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
        if (item != null && item.getPaidPrice() != null)
        {
            if (item.getSecurity() != null)
                return CurrencyConverter.getInstance().convert(item.getPaidPrice(), item.getSecurity().getCurrency(), item.getParent().getCurrency());
            else
                return item.getPaidPrice().doubleValue();
        }
        return 0;
    }
}
