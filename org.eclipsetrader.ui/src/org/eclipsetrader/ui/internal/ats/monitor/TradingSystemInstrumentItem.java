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

package org.eclipsetrader.ui.internal.ats.monitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipsetrader.core.ats.ITradingSystemInstrument;
import org.eclipsetrader.ui.NullRealm;
import org.eclipsetrader.ui.internal.ats.ViewItem;

public class TradingSystemInstrumentItem implements ViewItem {

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_ADDED = 1;
    public static final int STATUS_REMOVED = 2;

    public static final String PROP_STATUS = "status";

    final TradingSystemItem parent;
    final ITradingSystemInstrument instrument;

    private int status = STATUS_NORMAL;

    private final WritableMap observableValues = new WritableMap(NullRealm.getInstance(), String.class, Object.class);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final PropertyChangeListener changeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            parent.getModel().updateValues(TradingSystemInstrumentItem.this);
        }
    };

    public TradingSystemInstrumentItem(TradingSystemItem parent, ITradingSystemInstrument instrument) {
        this.parent = parent;
        this.instrument = instrument;

        observableValues.put("_label_", toString());

        PropertyChangeSupport changeSupport = (PropertyChangeSupport) instrument.getAdapter(PropertyChangeSupport.class);
        if (changeSupport != null) {
            changeSupport.addPropertyChangeListener(changeListener);
        }
    }

    public ITradingSystemInstrument getInstrument() {
        return instrument;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        changeSupport.firePropertyChange(PROP_STATUS, this.status, this.status = status);
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
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getParent()
     */
    @Override
    public ViewItem getParent() {
        return parent;
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
        if (adapter.isAssignableFrom(instrument.getClass())) {
            return instrument;
        }

        Object result = instrument.getAdapter(adapter);
        if (result != null) {
            return result;
        }

        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return instrument.getInstrument().getName();
    }
}
