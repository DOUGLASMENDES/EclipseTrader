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

package net.sourceforge.eclipsetrader.core.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 */
public class History extends PersistentObject
{
    List list = new ArrayList();
    
    public History()
    {
        
    }
    
    public History(Integer id)
    {
        super(id);
    }
    
    public boolean add(Bar obj)
    {
        boolean result = list.add(obj);
        if (result)
            setChanged();
        return result;
    }
    
    public void addAll(Collection collection)
    {
        for (Iterator iter = collection.iterator(); iter.hasNext(); )
        {
            Object obj = iter.next();
            if (obj instanceof Bar)
            {
                if (list.add(obj))
                    setChanged();
            }
        }
    }

    public Bar remove(int index)
    {
        Bar result = (Bar)list.remove(index);
        setChanged();
        return result;
    }

    public boolean remove(Bar obj)
    {
        boolean result = list.remove(obj);
        if (result)
            setChanged();
        return result;
    }
    
    public void clear()
    {
        list.clear();
        setChanged();
    }
    
    public int size()
    {
        return list.size();
    }
    
    public Bar get(int index)
    {
        return (Bar)list.get(index);
    }
    
    public Bar get(Date date)
    {
        int index = Collections.binarySearch(list, date, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Date d1 = (o1 instanceof Bar) ? ((Bar)o1).getDate() : (Date)o1;
                Date d2 = (o2 instanceof Bar) ? ((Bar)o2).getDate() : (Date)o2;
                if (d1.after(d2) == true)
                    return 1;
                else if (d1.before(d2) == true)
                    return -1;
                return 0;
            }
        });
        Bar bar = (index >= 0 && index < list.size()) ? (Bar)list.get(index) : null;
        if (bar != null && !bar.getDate().equals(date))
            bar = null;
        return bar;
    }
    
    public Iterator iterator()
    {
        return new ArrayList(list).iterator();
    }
    
    public boolean isEmpty()
    {
        return list.isEmpty();
    }
    
    public List getList()
    {
        return Collections.unmodifiableList(list);
    }
    
    public void sort()
    {
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Bar d1 = (Bar) o1;
                Bar d2 = (Bar) o2;
                if (d1.getDate().after(d2.getDate()) == true)
                    return 1;
                else if (d1.getDate().before(d2.getDate()) == true)
                    return -1;
                return 0;
            }
        });
    }
}
