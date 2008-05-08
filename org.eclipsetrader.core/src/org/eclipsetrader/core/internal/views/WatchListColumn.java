/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IWatchListColumn;

/**
 * Default implementation of the <code>IWatchListColumn</code> interface.
 *
 * @since 1.0
 */
public class WatchListColumn implements IWatchListColumn, ISessionData, Cloneable {
	private String name;
	private IDataProviderFactory dataProviderFactory;

	private Map<Object, Object> sessionData = new HashMap<Object, Object>();

	protected WatchListColumn() {
	}

	public WatchListColumn(IColumn column) {
		this(column.getName(), column.getDataProviderFactory());
	}

	public WatchListColumn(String name, IDataProviderFactory dataProviderFactory) {
	    this.name = name;
	    this.dataProviderFactory = dataProviderFactory;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IColumn#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IColumn#getDataProviderFactory()
	 */
	public IDataProviderFactory getDataProviderFactory() {
		return dataProviderFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListColumn#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#getData(java.lang.Object)
     */
    public Object getData(Object key) {
	    return sessionData.get(key);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#setData(java.lang.Object, java.lang.Object)
     */
    public void setData(Object key, Object value) {
    	sessionData.put(key, value);
    }

	/* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	    return super.clone();
    }
}
