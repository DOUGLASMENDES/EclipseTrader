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
import java.text.ParseException;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.internal.Messages;

public class PaidPrice extends Column
{
    private NumberFormat formatter = NumberFormat.getInstance();

    public PaidPrice()
    {
        super(Messages.PaidPrice_Label, RIGHT);
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
        if (item != null && item.getPaidPrice() != null)
        {
            if (item.getSecurity() != null)
                return formatter.format(CurrencyConverter.getInstance().convert(item.getPaidPrice(), item.getSecurity().getCurrency(), item.getParent().getCurrency()));
            else
                return formatter.format(item.getPaidPrice());
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#setText(net.sourceforge.eclipsetrader.core.db.WatchlistItem, java.lang.String)
     */
    public void setText(WatchlistItem item, String text)
    {
        try
        {
            item.setPaidPrice(formatter.parse(text));
            item.notifyObservers();
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#compare(java.lang.Object, java.lang.Object)
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
