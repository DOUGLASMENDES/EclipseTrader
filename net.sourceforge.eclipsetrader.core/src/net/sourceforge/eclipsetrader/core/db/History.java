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
 * Historical data.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class History extends PersistentObject
{
    List list = new ArrayList();
    Comparator dateSearchComparator = new Comparator() {
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
    };
    Comparator sortComparator = new Comparator() {
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
    };

    /**
     * Constructs an empty history object.
     */
    public History()
    {
    }
    
    /**
     * Constructs an empty history object with the given unique id.
     * 
     * @param id the unique id of this object
     */
    public History(Integer id)
    {
        super(id);
    }

    /**
     * Adds the specified element to this history, in date ascending
     * order.
     *
     * @param obj element to be added to this list.
     * @return the index of the element in the list.
     * 
     * @throws IllegalArgumentException if the specified element is null.
     */
    public int add(Bar obj)
    {
        if (obj == null) throw new IllegalArgumentException();
        int index = Collections.binarySearch(list, obj.getDate(), dateSearchComparator);
        if (index < 0)
            index = -(index + 1);
        list.add(index, obj);
        setChanged();
        return index;
    }
    
    /**
     * Adds all of the elements in the specified collection to
     * this list, in date ascending order. Elements that are not
     * Bar objects are ignored.
     *
     * @param collection collection whose elements are to be added to this list.
     */
    public void addAll(Collection collection)
    {
        for (Iterator iter = collection.iterator(); iter.hasNext(); )
        {
            Object obj = iter.next();
            if (obj instanceof Bar)
                add((Bar)obj);
        }
    }

    /**
     * Removes the element at the specified position in this list. Shifts 
     * any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public Bar remove(int index)
    {
        Bar result = (Bar)list.remove(index);
        setChanged();
        return result;
    }

    /**
     * Removes the first occurrence in this list of the specified element. 
     * If this list does not contain the element, it is unchanged.
     *
     * @param obj element to be removed from this list, if present.
     * @return <tt>true</tt> if this list contained the specified element.
     * 
     * @throws IllegalArgumentException if the specified element is null.
     */
    public boolean remove(Bar obj)
    {
        if (obj == null) throw new IllegalArgumentException();
        boolean result = list.remove(obj);
        if (result)
            setChanged();
        return result;
    }
    
    /**
     * Removes all of the elements from this history.
     */
    public void clear()
    {
        list.clear();
        setChanged();
    }
    
    /**
     * Returns the number of elements in this history.
     *
     * @return the number of elements in this history.
     */
    public int size()
    {
        return list.size();
    }
    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt;= size()).
     */
    public Bar get(int index)
    {
        return (Bar)list.get(index);
    }
    
    /**
     * Returns the first element in this history.
     * 
     * @return the first element in this history, or null if the list is empty
     */
    public Bar getFirst()
    {
        return list.size() == 0 ? null : (Bar)list.get(0);
    }
    
    /**
     * Returns the last element in this history.
     * 
     * @return the last element in this history, or null if the list is empty
     */
    public Bar getLast()
    {
        return list.size() == 0 ? null : (Bar)list.get(list.size() - 1);
    }

    /**
     * Returns the element with the specified date.
     * 
     * @param date the date of the element.
     * @return the element with the specified date, or null if an element
     *        with the specified date is not found.
     */
    public Bar get(Date date)
    {
        if (date == null) throw new IllegalArgumentException();
        int index = Collections.binarySearch(list, date, dateSearchComparator);
        Bar bar = (index >= 0 && index < list.size()) ? (Bar)list.get(index) : null;
        if (bar != null && !bar.getDate().equals(date))
            bar = null;
        return bar;
    }
    
    /**
     * Returns the index in this list of the first occurrence of the specified
     * date, or -1 if this list does not contain this date.
     *
     * @param date date to search for.
     * @return the index in this list of the first occurrence of the specified
     *         date, or -1 if this list does not contain this date.
     */
    public int indexOf(Date date)
    {
        if (date == null) throw new IllegalArgumentException();
        int index = Collections.binarySearch(list, date, dateSearchComparator);
        Bar bar = (index >= 0 && index < list.size()) ? (Bar)list.get(index) : null;
        if (bar == null || !bar.getDate().equals(date))
            index = -1;
        return index;
    }
    
    /**
     * Returns an iterator over the elements in this history in proper sequence.
     *
     * @return an iterator over the elements in this history.
     */
    public Iterator iterator()
    {
        return new ArrayList(list).iterator();
    }
    
    /**
     * Returns <tt>true</tt> if this history contains no elements.
     *
     * @return <tt>true</tt> if this history contains no elements.
     */
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    /**
     * Returns an unmodifiable list of the elements in this history.
     * 
     * @return an unmodifiable list.
     */
    public List getList()
    {
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns an array containing all of the elements in this history in proper
     * sequence.
     *
     * @return an array containing all of the elements in this history in proper
     *         sequence.
     */
    public Bar[] toArray()
    {
        return (Bar[])list.toArray(new Bar[list.size()]);
    }
}
