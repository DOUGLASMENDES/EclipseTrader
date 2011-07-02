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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

/**
 * Default implementation of the <code>IWatchList</code> interface.
 *
 * @since 1.0
 */
public class WatchList implements IWatchList, IStoreObject {

    private String name;
    private List<IWatchListColumn> columns = new ArrayList<IWatchListColumn>();
    private List<IWatchListElement> items = new ArrayList<IWatchListElement>();

    private IStore store;
    private IStoreProperties storeProperties;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected WatchList() {
    }

    public WatchList(String name, IWatchListColumn[] columns) {
        this.name = name;
        setColumns(columns);
    }

    public WatchList(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(IWatchList.NAME, oldValue, this.name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return columns.size();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getColumns()
     */
    @Override
    public IWatchListColumn[] getColumns() {
        return columns.toArray(new IWatchListColumn[columns.size()]);
    }

    public void setColumns(IWatchListColumn[] columns) {
        IWatchListColumn[] oldValue = this.columns.toArray(new IWatchListColumn[this.columns.size()]);
        this.columns = new ArrayList<IWatchListColumn>(Arrays.asList(columns));
        propertyChangeSupport.firePropertyChange(IWatchList.COLUMNS, oldValue, this.columns.toArray(new IWatchListColumn[this.columns.size()]));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getItemCount()
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getItems()
     */
    @Override
    public IWatchListElement[] getItems() {
        return items.toArray(new IWatchListElement[items.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getItem(int)
     */
    @Override
    public IWatchListElement getItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException(index + " index out of range");
        }
        return items.get(index);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#getItem(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IWatchListElement[] getItem(ISecurity security) {
        List<IWatchListElement> list = new ArrayList<IWatchListElement>();
        for (IWatchListElement element : items) {
            if (element.getSecurity() == security) {
                list.add(element);
            }
        }
        return list.toArray(new IWatchListElement[list.size()]);
    }

    public void setItems(IWatchListElement[] items) {
        IWatchListElement[] oldValue = this.items.toArray(new IWatchListElement[this.items.size()]);
        this.items = new ArrayList<IWatchListElement>(Arrays.asList(items));
        propertyChangeSupport.firePropertyChange(IWatchList.HOLDINGS, oldValue, this.items.toArray(new IWatchListElement[this.items.size()]));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchList#accept(org.eclipsetrader.core.views.IWatchListVisitor)
     */
    @Override
    public void accept(IWatchListVisitor visitor) {
        if (visitor.visit(this)) {
            for (IWatchListColumn c : columns) {
                visitor.visit(c);
            }
            for (IWatchListElement e : items) {
                visitor.visit(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(propertyChangeSupport.getClass())) {
            return propertyChangeSupport;
        }
        if (adapter.isAssignableFrom(IStoreProperties.class)) {
            return getStoreProperties();
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
     */
    @Override
    public IStore getStore() {
        return store;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
     */
    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
     */
    @Override
    public IStoreProperties getStoreProperties() {
        if (storeProperties == null) {
            storeProperties = new StoreProperties();
        }

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IWatchList.class.getName());
        storeProperties.setProperty(IPropertyConstants.NAME, getName());
        storeProperties.setProperty(IPropertyConstants.COLUMNS, columns.toArray(new IColumn[columns.size()]));
        storeProperties.setProperty(IPropertyConstants.HOLDINGS, items.toArray(new IHolding[items.size()]));

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;
        this.name = (String) storeProperties.getProperty(IPropertyConstants.NAME);

        this.columns = new ArrayList<IWatchListColumn>();
        IColumn[] columns = (IColumn[]) storeProperties.getProperty(IPropertyConstants.COLUMNS);
        if (columns != null) {
            for (IColumn column : columns) {
                this.columns.add(new WatchListColumn(column));
            }
        }

        this.items = new ArrayList<IWatchListElement>();
        IHolding[] holdings = (IHolding[]) storeProperties.getProperty(IPropertyConstants.HOLDINGS);
        if (holdings != null) {
            for (IHolding holding : holdings) {
                this.items.add(new WatchListElement(holding));
            }
        }
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
