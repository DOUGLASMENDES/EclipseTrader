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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemInstrument;
import org.eclipsetrader.ui.NullRealm;
import org.eclipsetrader.ui.internal.ats.ViewItem;

public class TradingSystemItem implements ViewItem {

    private final TradingSystemsViewModel model;
    final ITradingSystem tradingSystem;

    private final List<TradingSystemInstrumentItem> list = new ArrayList<TradingSystemInstrumentItem>();
    private final WritableList childs = new WritableList(list, TradingSystemInstrumentItem.class);

    private final WritableMap observableValues = new WritableMap(NullRealm.getInstance(), String.class, Object.class);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private PropertyChangeListener changeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ITradingSystem.PROPERTY_INSTRUMENTS.equals(evt.getPropertyName())) {
                ITradingSystemInstrument[] instrument = (ITradingSystemInstrument[]) evt.getNewValue();
                for (int i = 0; i < instrument.length; i++) {
                    if (!hasInstrument(instrument[i])) {
                        TradingSystemInstrumentItem instrumentItem = new TradingSystemInstrumentItem(TradingSystemItem.this, instrument[i]);
                        if (tradingSystem.getStatus() != ITradingSystem.STATUS_STOPPED) {
                            instrumentItem.setStatus(TradingSystemInstrumentItem.STATUS_ADDED);
                        }
                        model.updateValues(instrumentItem);
                        childs.add(instrumentItem);
                    }
                }
                if (tradingSystem.getStatus() != ITradingSystem.STATUS_STARTED) {
                    cleanUp();
                }
            }
            else if (ITradingSystem.PROPERTY_STATUS.equals(evt.getPropertyName())) {
                if (tradingSystem.getStatus() == ITradingSystem.STATUS_STOPPED) {
                    cleanUp();
                }
            }
        }
    };

    public TradingSystemItem(TradingSystemsViewModel model, ITradingSystem tradingSystem) {
        this.model = model;
        this.tradingSystem = tradingSystem;

        for (ITradingSystemInstrument instrument : tradingSystem.getInstruments()) {
            list.add(new TradingSystemInstrumentItem(this, instrument));
        }

        PropertyChangeSupport changeSupport = (PropertyChangeSupport) tradingSystem.getAdapter(PropertyChangeSupport.class);
        if (changeSupport != null) {
            changeSupport.addPropertyChangeListener(changeListener);
        }

        observableValues.put("_label_", toString());
    }

    public ITradingSystem getTradingSystem() {
        return tradingSystem;
    }

    public List<TradingSystemInstrumentItem> getList() {
        return list;
    }

    public TradingSystemsViewModel getModel() {
        return model;
    }

    private boolean hasInstrument(ITradingSystemInstrument instrument) {
        for (TradingSystemInstrumentItem instrumentItem : list) {
            if (instrumentItem.getInstrument() == instrument) {
                return true;
            }
        }
        return false;
    }

    private void cleanUp() {
        for (TradingSystemInstrumentItem instrumentItem : list) {
            instrumentItem.setStatus(TradingSystemInstrumentItem.STATUS_NORMAL);
        }
        childs.getRealm().exec(new Runnable() {

            @Override
            public void run() {
                Set<ITradingSystemInstrument> set = new HashSet<ITradingSystemInstrument>(Arrays.asList(tradingSystem.getInstruments()));
                for (TradingSystemInstrumentItem instrumentItem : new ArrayList<TradingSystemInstrumentItem>(list)) {
                    if (!set.contains(instrumentItem.getInstrument())) {
                        childs.remove(instrumentItem);
                    }
                }
            }
        });
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
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getItems()
     */
    @Override
    public ObservableList getItems() {
        return childs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(tradingSystem.getClass())) {
            return tradingSystem;
        }

        Object result = tradingSystem.getAdapter(adapter);
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
        return tradingSystem.getStrategy().getName();
    }
}
