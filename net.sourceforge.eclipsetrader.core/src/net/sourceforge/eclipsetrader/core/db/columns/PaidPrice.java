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

public class PaidPrice extends Column
{
    private NumberFormat formatter = NumberFormat.getInstance();

    public PaidPrice()
    {
        super("Price", RIGHT);
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
        return "";
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
}
