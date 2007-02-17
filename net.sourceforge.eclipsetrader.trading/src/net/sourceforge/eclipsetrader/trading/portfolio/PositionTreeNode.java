/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.portfolio;

import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

public class PositionTreeNode implements Observer
{
    PortfolioPosition value;
    AccountTreeNode parent;
    Quote quote;
    IStructuredViewerListener listener;
    
    PositionTreeNode(AccountTreeNode parent, PortfolioPosition position)
    {
        this.parent = parent;
        value = position;
        value.getSecurity().addObserver(this);
        value.getSecurity().getQuoteMonitor().addObserver(this);
        quote = this.value.getSecurity().getQuote();
    }
    
    public void dispose()
    {
        value.getSecurity().deleteObserver(this);
        value.getSecurity().getQuoteMonitor().deleteObserver(this);
        listener = null;
    }

    public AccountTreeNode getParent()
    {
        return parent;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        quote = this.value.getSecurity().getQuote();

        if (listener != null)
            listener.update(this, null);
    }

    public void setListener(IStructuredViewerListener listener)
    {
        this.listener = listener;
    }
}
