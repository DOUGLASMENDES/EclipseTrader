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
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.columns.Column;


/**
 */
public class Watchlist extends PersistentObject
{
    private int style = 0;
    private String description = "";
    private Currency currency;
    private String defaultFeed;
    private List columns = new ArrayList();
    private ObservableList items = new ObservableList();
    private WatchlistItem totals = new WatchlistItem() {
        public void update()
        {
            values = new ArrayList();
            for (Iterator iter = parent.getColumns().iterator(); iter.hasNext(); )
            {
                Column column = (Column)iter.next();
                values.add(column.getTotalsText(Watchlist.this));
            }
        }
    };
    
    public Watchlist()
    {
        totals.setParent(this);
    }

    public Watchlist(Integer id)
    {
        super(id);
        totals.setParent(this);
    }

    public int getStyle()
    {
        return style;
    }

    public void setStyle(int style)
    {
        this.style = style;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        setChanged();
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public void setCurrency(Currency currency)
    {
        this.currency = currency;
        for (Iterator iter = items.iterator(); iter.hasNext(); )
            ((WatchlistItem)iter.next()).update();
        setChanged();
    }

    public String getDefaultFeed()
    {
        return defaultFeed;
    }

    public void setDefaultFeed(String defaultFeed)
    {
        this.defaultFeed = defaultFeed;
    }

    public List getColumns()
    {
        return columns;
    }

    public void setColumns(List columns)
    {
        this.columns = columns;
        for (Iterator iter = items.iterator(); iter.hasNext(); )
            ((WatchlistItem)iter.next()).update();
        setChanged();
        totals.update();
    }
    
    public ObservableList getItems()
    {
        return items;
    }
    
    public void setItems(List items)
    {
        this.items.clear();
        for (Iterator iter = items.iterator(); iter.hasNext(); )
            ((WatchlistItem) iter.next()).setParent(this);
        this.items.addAll(items);
        setChanged();
        totals.update();
    }

    public WatchlistItem getTotals()
    {
        return totals;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#setChanged()
     */
    public synchronized void setChanged()
    {
        super.setChanged();
        totals.setChanged();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.db.PersistentObject#clearChanged()
     */
    public synchronized void clearChanged()
    {
        super.clearChanged();
        totals.clearChanged();
    }

    /* (non-Javadoc)
     * @see java.util.Observable#notifyObservers()
     */
    public void notifyObservers()
    {
        super.notifyObservers();
        totals.notifyObservers();
    }
}
