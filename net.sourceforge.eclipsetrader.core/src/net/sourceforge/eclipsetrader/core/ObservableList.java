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

package net.sourceforge.eclipsetrader.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 */
public class ObservableList extends ArrayList
{
    private static final long serialVersionUID = 7282371672763711235L;
    private List originalList;
    private List observers = new ArrayList();

    public ObservableList()
    {
    }

    public ObservableList(List list)
    {
        super(list);
        this.originalList = list;
    }

    public ObservableList(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    public Collection getOriginalList()
    {
        return originalList;
    }
    
    public void addCollectionObserver(ICollectionObserver observer)
    {
        if (!observers.contains(observer))
            observers.add(observer);
    }
    
    public void removeCollectionObserver(ICollectionObserver observer)
    {
        observers.remove(observer);
    }
    
    protected void notifyItemAdded(Object o)
    {
        Object[] obs = observers.toArray();
        for (int i = 0; i < obs.length; i++)
            ((ICollectionObserver)obs[i]).itemAdded(o);
    }
    
    protected void notifyItemRemoved(Object o)
    {
        Object[] obs = observers.toArray();
        for (int i = 0; i < obs.length; i++)
            ((ICollectionObserver)obs[i]).itemRemoved(o);
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    public boolean add(Object o)
    {
        boolean result = super.add(o);
        if (originalList != null)
            originalList.add(o);
        notifyItemAdded(o);
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    public void add(int index, Object element)
    {
        super.add(index, element);
        if (originalList != null)
            originalList.add(index, element);
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public boolean remove(Object o)
    {
        boolean result = super.remove(o);
        if (originalList != null)
            originalList.remove(o);
        notifyItemRemoved(o);
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#remove(int)
     */
    public Object remove(int index)
    {
        Object result = super.remove(index); 
        if (originalList != null)
            originalList.remove(index);
        notifyItemRemoved(result);
        return result;
    }
    
    public int countObservers()
    {
        return observers.size();
    }
}
