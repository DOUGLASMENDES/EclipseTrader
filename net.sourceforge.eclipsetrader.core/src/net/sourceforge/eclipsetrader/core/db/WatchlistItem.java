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

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.db.columns.Column;

/**
 */
public class WatchlistItem extends PersistentObject implements Observer
{
    private Security security;
    private Watchlist parent;
    private Integer position;
    private Double paidPrice;
    private List values;
    
    public WatchlistItem()
    {
    }

    public WatchlistItem(Integer id)
    {
        super(id);
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        if (this.security != null)
        {
            this.security.deleteObserver(this);
            this.security.getQuoteMonitor().deleteObserver(this);
        }
        
        this.security = security;
        
        if (this.security != null)
        {
            this.security.addObserver(this);
            this.security.getQuoteMonitor().addObserver(this);
            update(this, null);
        }
    }

    public Watchlist getParent()
    {
        return parent;
    }

    public void setParent(Watchlist parent)
    {
        this.parent = parent;
    }

    public Integer getPosition()
    {
        return position;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
        setChanged();
        parent.getTotals().setChanged();
    }

    public void setPosition(int position)
    {
        this.position = new Integer(position);
        setChanged();
        parent.getTotals().setChanged();
    }

    public void setPosition(Number position)
    {
        this.position = new Integer(position.intValue());
        setChanged();
        parent.getTotals().setChanged();
    }

    public Double getPaidPrice()
    {
        return paidPrice;
    }

    public void setPaidPrice(Double paidPrice)
    {
        this.paidPrice = paidPrice;
        setChanged();
        parent.getTotals().setChanged();
    }

    public void setPaidPrice(double paidPrice)
    {
        this.paidPrice = new Double(paidPrice);
        setChanged();
        parent.getTotals().setChanged();
    }

    public void setPaidPrice(Number paidPrice)
    {
        this.paidPrice = new Double(paidPrice.doubleValue());
        setChanged();
        parent.getTotals().setChanged();
    }

    public List getValues()
    {
        if (values == null)
            update();
        return values;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        setChanged();
        notifyObservers();
        
        WatchlistItem totals = getParent().getTotals();
        if (totals != this)
            totals.update(o, arg);
    }
    
    public void update()
    {
        values = new ArrayList();
        for (Iterator iter = parent.getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column)iter.next();
            values.add(column.getText(this));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Observable#notifyObservers()
     */
    public void notifyObservers()
    {
        if (values != null && hasChanged())
            update();
        super.notifyObservers();
    }
}
