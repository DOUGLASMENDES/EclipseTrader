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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

/**
 * Base abstract class for all watchlist columns.
 */
public abstract class Column implements Cloneable, Comparator
{
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;
    private int style;
    private String label = ""; //$NON-NLS-1$
    private int width = 0;

    public Column(String label, int style)
    {
        this.label = label;
        this.style = style;
    }
    
    public static List allColumns()
    {
        List list = new ArrayList();
        
        list.add(new Code());
        list.add(new Description());
        list.add(new LastPrice());
        list.add(new BidPrice());
        list.add(new BidSize());
        list.add(new AskPrice());
        list.add(new AskSize());
        list.add(new Volume());
        list.add(new Position());
        list.add(new PaidPrice());
        list.add(new Balance());
        list.add(new DateTime());
        list.add(new Date());
        list.add(new Time());
        list.add(new Currency());
        list.add(new OpenPrice());
        list.add(new HighPrice());
        list.add(new LowPrice());
        list.add(new ClosePrice());
        list.add(new Change());
        list.add(new ChangePercent());

        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Column)arg0).getLabel().compareToIgnoreCase(((Column)arg1).getLabel());
            }
        });
        return list;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getStyle()
    {
        return style;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public abstract String getText(WatchlistItem item);
    
    public void setText(WatchlistItem item, String text)
    {
    }
    
    public String getTotalsText(Watchlist watchlist)
    {
        return ""; //$NON-NLS-1$
    }
    
    /**
     * Returns wether the receiver is user-editable or not.
     * 
     * @return true if editable, false otherwise
     */
    public boolean isEditable()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1)
    {
        return getText((WatchlistItem)arg0).compareTo(getText((WatchlistItem)arg1));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        return this.getClass().equals(obj.getClass());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        try
        {
            Column obj = (Column)this.getClass().newInstance();
            obj.label = this.label;
            obj.style = this.style;
            return obj;
        }
        catch (Exception e) {
            throw new CloneNotSupportedException(e.toString());
        }
    }
}
