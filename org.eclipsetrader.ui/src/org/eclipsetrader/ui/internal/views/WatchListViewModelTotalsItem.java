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

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipsetrader.ui.NullRealm;
import org.eclipsetrader.ui.internal.ats.ViewItem;

public class WatchListViewModelTotalsItem implements ViewItem {

    private final WritableMap observableValues = new WritableMap(NullRealm.getInstance(), String.class, Object.class);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public WatchListViewModelTotalsItem() {
        observableValues.put("org.eclipsetrader.ui.providers.SecurityName", "Totals");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getValue(java.lang.String)
     */
    @Override
    public Object getValue(String name) {
        return observableValues.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#putValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void putValue(String name, Object value) {
        Object oldValue = observableValues.get(name);
        if (oldValue != null && value != null && oldValue.equals(value)) {
            return;
        }
        observableValues.put(name, value);
        changeSupport.firePropertyChange(name, oldValue, value);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getParent()
     */
    @Override
    public ViewItem getParent() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getItems()
     */
    @Override
    public ObservableList getItems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
