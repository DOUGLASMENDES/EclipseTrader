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

public abstract class Level2
{
    protected List list = new ArrayList();

    public Level2()
    {
    }
    
    public void clear()
    {
        list.clear();
    }

    public void add(double price)
    {
        add(price, 1, 1, ""); //$NON-NLS-1$
    }

    public void add(double price, int quantity)
    {
        add(price, quantity, 1, ""); //$NON-NLS-1$
    }

    public void add(double price, String id)
    {
        add(price, 1, 1, id);
    }

    public void add(double price, int quantity, String id)
    {
        add(price, quantity, 1, id);
    }

    public void add(double price, int quantity, int number)
    {
        add(price, quantity, number, ""); //$NON-NLS-1$
    }

    public abstract void add(double price, int quantity, int number, String id);
    
    public int size()
    {
        return list.size();
    }
    
    public Item get(int index)
    {
        return (Item)list.get(index);
    }
    
    public Iterator iterator()
    {
        return list.iterator();
    }
    
    /**
     * Builds a list of level2 items grouped by price.
     * 
     * @return grouped the level2 items list
     */
    public List getGrouped()
    {
        List l = new ArrayList();
        Item item = new Item();
        
        Item[] items = (Item[])list.toArray(new Item[list.size()]);
        for (int i = 0; i < items.length; i++)
        {
            if (item.price != items[i].price)
            {
                if (item.quantity != 0)
                {
                    l.add(item);
                    item = new Item();
                }
                item.price = items[i].price;
            }
            item.quantity += items[i].quantity;
            item.number += items[i].number;
        }

        if (item.quantity != 0)
            l.add(item);
        
        return l;
    }
    
    public List getList()
    {
        return list;
    }
    
    public class Item
    {
        public double price;
        public int quantity;
        public int number;
        public String id;
        
        Item()
        {
        }

        Item(double price, int quantity, int number, String id)
        {
            this.price = price;
            this.quantity = quantity;
            this.number = number;
            this.id = id;
        }
    }
}
