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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;

public class AccountTreeNode implements Observer
{
    Account value;
    AccountGroupTreeNode parent;
    List childs = new ArrayList();
    IStructuredViewerListener listener;
    
    AccountTreeNode(AccountGroupTreeNode parent, Account account)
    {
        this.parent = parent;
        value = account;

        for (Iterator iter = value.getPortfolio().iterator(); iter.hasNext(); )
            childs.add(new PositionTreeNode(this, (PortfolioPosition)iter.next()));
        
        value.addObserver(this);
    }
    
    public void dispose()
    {
        value.deleteObserver(this);
        listener = null;
        
        Object[] items = childs.toArray();
        for (int i = 0; i < items.length; i++)
            ((PositionTreeNode)items[i]).dispose();
    }
    
    public AccountGroupTreeNode getParent()
    {
        return parent;
    }

    public boolean hasChildren()
    {
        return childs.size() != 0;
    }
    
    public Object[] getChildren()
    {
        return childs.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        Object[] items = childs.toArray();
        for (int i = 0; i < items.length; i++)
            ((PositionTreeNode)items[i]).dispose();

        childs.clear();
        
        for (Iterator iter = value.getPortfolio().iterator(); iter.hasNext(); )
            childs.add(new PositionTreeNode(this, (PortfolioPosition)iter.next()));

        items = childs.toArray();
        for (int i = 0; i < items.length; i++)
            ((PositionTreeNode)items[i]).setListener(listener);
        
        if (listener != null)
            listener.refresh(this);
    }

    public void setListener(IStructuredViewerListener listener)
    {
        this.listener = listener;

        Object[] items = childs.toArray();
        for (int i = 0; i < items.length; i++)
            ((PositionTreeNode)items[i]).setListener(listener);
    }
}
