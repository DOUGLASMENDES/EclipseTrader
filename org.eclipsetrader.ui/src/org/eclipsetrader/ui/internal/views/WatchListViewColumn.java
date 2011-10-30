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

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchListColumn;

public class WatchListViewColumn {

    public static final String PROP_NAME = "name"; //$NON-NLS-1$

    private final IWatchListColumn column;

    private String name;
    private IDataProvider provider;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public WatchListViewColumn(IWatchListColumn column) {
        this.column = column;
        this.name = column.getName();
        this.provider = column.getDataProviderFactory().createProvider();
    }

    public WatchListViewColumn(IColumn column) {
        this.column = new WatchListColumn(column);
        this.name = column.getName();
        this.provider = column.getDataProviderFactory().createProvider();
    }

    public String getId() {
        return column.getDataProviderFactory().getId();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        changeSupport.firePropertyChange(PROP_NAME, this.name, this.name = name);
    }

    public IDataProvider getDataProvider() {
        return provider;
    }

    public IWatchListColumn getColumn() {
        return column;
    }

    public IDataProviderFactory getDataProviderFactory() {
        return column.getDataProviderFactory();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WatchListViewColumn)) {
            return false;
        }
        WatchListViewColumn other = (WatchListViewColumn) obj;
        return getId().equals(other.getId());
    }
}
