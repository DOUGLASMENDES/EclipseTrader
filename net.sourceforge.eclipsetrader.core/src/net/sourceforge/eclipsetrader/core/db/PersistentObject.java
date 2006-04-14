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

import java.util.Observable;

import net.sourceforge.eclipsetrader.core.Repository;


/**
 * Base abstract class for all persistent object classes.
 */
public abstract class PersistentObject extends Observable
{
    private Integer id;
    private Object data;
    private Repository repository;

    public PersistentObject()
    {
    }

    public PersistentObject(Integer id)
    {
        this.id = id;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }
    
    public Repository getRepository()
    {
        return repository;
    }
    
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    /* (non-Javadoc)
     * @see java.util.Observable#setChanged()
     */
    public synchronized void setChanged()
    {
        super.setChanged();
    }

    /* (non-Javadoc)
     * @see java.util.Observable#clearChanged()
     */
    public synchronized void clearChanged()
    {
        super.clearChanged();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !this.getClass().getName().equals(obj.getClass().getName()))
            return false;
        PersistentObject that = (PersistentObject)obj;
        if (this.getId() == null || that.getId() == null)
            return false;
        return this.getId().equals(that.getId());
    }
}
