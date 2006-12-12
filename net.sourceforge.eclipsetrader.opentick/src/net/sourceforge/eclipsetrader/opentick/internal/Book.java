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

package net.sourceforge.eclipsetrader.opentick.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Level2Ask;
import net.sourceforge.eclipsetrader.core.db.Level2Bid;

public class Book
{
    public static final int MAXIMUM_DEPTH = 15;
    List list = new ArrayList();
    Level2Bid bid = new Level2Bid();
    Level2Ask ask = new Level2Ask();
    Comparator comparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            if (((Order)o1).price > ((Order)o2).price)
                return 1;
            if (((Order)o1).price < ((Order)o2).price)
                return -1;
            return ((Order)o1).timestamp - ((Order)o2).timestamp;
        }
    };
    
    public class Order
    {
        String reference;
        int size;
        double price;
        char side;
        int timestamp;
    }
    
    public Book()
    {
    }

    public void add(int timestamp, String reference, double price, int size, char side)
    {
        Order order = new Order();
        order.timestamp = timestamp;
        order.reference = reference;
        order.price = price;
        order.size = size;
        order.side = side;

        for (int i = 0; i < list.size(); i++)
        {
            if (((Order)list.get(i)).reference.equals(reference))
            {
                list.set(i, order);
                Collections.sort(list, comparator);
                purgeUnbalancedOrders(price, side);
                return;
            }
        }
        
        list.add(order);
        Collections.sort(list, comparator);
        purgeUnbalancedOrders(price, side);
    }
    
    void purgeUnbalancedOrders(double price, char side)
    {
        if (side == 'B')
        {
            for (int i = list.size() - 1; i >= 0; i--)
            {
                if (((Order)list.get(i)).side != side && ((Order)list.get(i)).price <= price)
                    list.remove(i);
            }
        }
        else
        {
            for (int i = list.size() - 1; i >= 0; i--)
            {
                if (((Order)list.get(i)).side != side && ((Order)list.get(i)).price >= price)
                    list.remove(i);
            }
        }
    }

    public void replace(int timestamp, String reference, double price, int size, char side)
    {
        Order order = new Order();
        order.timestamp = timestamp;
        order.reference = reference;
        order.price = price;
        order.size = size;
        order.side = side;

        for (int i = 0; i < list.size(); i++)
        {
            if (((Order)list.get(i)).reference.equals(reference))
            {
                list.set(i, order);
                Collections.sort(list, comparator);
                purgeUnbalancedOrders(price, side);
                return;
            }
        }
        
        list.add(order);
        Collections.sort(list, comparator);
        purgeUnbalancedOrders(price, side);
    }

    public void remove(String reference, int size)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (((Order)list.get(i)).reference.equals(reference))
            {
                ((Order)list.get(i)).size -= size;
                if (((Order)list.get(i)).size <= 0)
                    list.remove(i);
                break;
            }
        }
        
        Collections.sort(list, comparator);
    }

    public void delete(String reference, char side, char type)
    {
        if (type == '1') // Order
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (((Order)list.get(i)).reference.equals(reference))
                {
                    list.remove(i);
                    break;
                }
            }
        }
        else if (type == '2') // Previous
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (((Order)list.get(i)).reference.equals(reference))
                {
                    for (int x = list.size() - 1; x >= i; x--)
                        list.remove(x);
                    break;
                }
            }
        }
        else if (type == '3') // All
        {
            for (int i = list.size() - 1; i >= 0; i--)
            {
                if (((Order)list.get(i)).side == side)
                    list.remove(i);
            }
        }
        else if (type == '4') // After
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (((Order)list.get(i)).reference.equals(reference))
                {
                    for (int x = i; x >= 0; x--)
                        list.remove(x);
                    break;
                }
            }
        }
        
        Collections.sort(list, comparator);
    }
    
    public void clear()
    {
        list.clear();
    }
    
    public Level2Bid getLevel2Bid()
    {
        bid.clear();

        int index = 0;
        for (; index < list.size() && bid.getGrouped().size() < MAXIMUM_DEPTH; index++)
        {
            if (((Order)list.get(index)).side == 'B')
                bid.add(((Order)list.get(index)).price, ((Order)list.get(index)).size);
        }
        
        for (int i = list.size() - 1; i >= index; i--)
        {
            if (((Order)list.get(i)).side == 'B')
                list.remove(i);
        }
        
        return bid;
    }
    
    public Level2Ask getLevel2Ask()
    {
        ask.clear();

        int index = 0;
        for (; index < list.size() && ask.getGrouped().size() < MAXIMUM_DEPTH; index++)
        {
            if (((Order)list.get(index)).side == 'S')
                ask.add(((Order)list.get(index)).price, ((Order)list.get(index)).size);
        }
        
        for (int i = list.size() - 1; i >= index; i--)
        {
            if (((Order)list.get(i)).side == 'S')
                list.remove(i);
        }
        
        return ask;
    }
}
