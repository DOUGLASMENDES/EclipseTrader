/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Default implementation of the <code>IWatchListColumn</code> interface.
 *
 * @since 1.0
 */
public class WatchListColumn implements IWatchListColumn {

    private String name;
    private IDataProviderFactory dataProviderFactory;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListColumn#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        Object oldValue = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(IWatchListColumn.NAME, oldValue, this.name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IColumn#getDataProviderFactory()
     */
    @Override
    public IDataProviderFactory getDataProviderFactory() {
        return dataProviderFactory;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(PropertyChangeSupport.class)) {
            return propertyChangeSupport;
        }

        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
}
