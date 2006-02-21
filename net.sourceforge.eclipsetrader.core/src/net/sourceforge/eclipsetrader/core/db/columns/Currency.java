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

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

public class Currency extends Column
{

    public Currency()
    {
        super("Curr.", LEFT);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.columns.Column#getText()
     */
    public String getText(WatchlistItem item)
    {
        if (item.getSecurity() == null)
            return "";
        if (item.getSecurity().getCurrency() != null)
            return item.getSecurity().getCurrency().getCurrencyCode();
        return "";
    }
}
