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

package org.eclipsetrader.core.internal.ats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemContext;
import org.eclipsetrader.core.ats.ITradingSystemInstrument;
import org.eclipsetrader.core.ats.engines.EngineEvent;
import org.eclipsetrader.core.ats.engines.JavaScriptEngine;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;

public class TradingSystem implements ITradingSystem {

    private final IStrategy strategy;
    private TradingSystemProperties properties;

    private final Map<ISecurity, TradingSystemInstrument> instruments = new HashMap<ISecurity, TradingSystemInstrument>();
    private JavaScriptEngine engine;

    private int status = STATUS_STOPPED;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final Observer observer = new Observer() {

        @Override
        public void update(Observable o, Object arg) {
            EngineEvent event = (EngineEvent) arg;
            TradingSystemInstrument instrument = instruments.get(event.instrument);
            if (instrument == null) {
                return;
            }
            if (event.value instanceof IPosition) {
                IPosition position = (IPosition) event.value;
                instrument.setPosition(position.getQuantity() != 0 ? position : null);
            }
            if (event.value instanceof IQuote) {
                instrument.setQuote((IQuote) event.value);
            }
            if (event.value instanceof ITrade) {
                instrument.setTrade((ITrade) event.value);
            }
        }
    };

    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!IStrategy.PROP_INSTRUMENTS.equals(evt.getPropertyName())) {
                return;
            }

            boolean changed = false;
            ITradingSystemInstrument[] oldInstruments = getInstruments();

            ISecurity[] security = (ISecurity[]) evt.getNewValue();
            for (int i = 0; i < security.length; i++) {
                if (!instruments.containsKey(security[i])) {
                    instruments.put(security[i], new TradingSystemInstrument(security[i]));
                    changed = true;
                }
            }

            Set<ISecurity> set = new HashSet<ISecurity>(Arrays.asList(security));
            for (Iterator<ISecurity> iter = instruments.keySet().iterator(); iter.hasNext();) {
                if (!set.contains(iter.next())) {
                    iter.remove();
                    changed = true;
                }
            }

            if (changed) {
                changeSupport.firePropertyChange(PROPERTY_INSTRUMENTS, oldInstruments, getInstruments());
            }
        }
    };

    public TradingSystem(IStrategy strategy) {
        this.strategy = strategy;
        this.properties = new TradingSystemProperties();

        for (ISecurity security : strategy.getInstruments()) {
            instruments.put(security, new TradingSystemInstrument(security));
        }

        PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) strategy.getAdapter(PropertyChangeSupport.class);
        if (propertyChangeSupport != null) {
            propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void dispose() {
        PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) strategy.getAdapter(PropertyChangeSupport.class);
        if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#getStatus()
     */
    @Override
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        changeSupport.firePropertyChange(PROPERTY_STATUS, this.status, this.status = status);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#getStrategy()
     */
    @Override
    public IStrategy getStrategy() {
        return strategy;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#getInstruments()
     */
    @Override
    public ITradingSystemInstrument[] getInstruments() {
        Collection<TradingSystemInstrument> c = new ArrayList<TradingSystemInstrument>(instruments.values());
        return c.toArray(new ITradingSystemInstrument[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#start(org.eclipsetrader.core.ats.ITradingSystemContext)
     */
    @Override
    public void start(ITradingSystemContext context) throws Exception {
        for (TradingSystemInstrument instrument : instruments.values()) {
            instrument.setQuote(null);
            instrument.setTrade(null);
        }
        for (IPosition position : context.getAccount().getPositions()) {
            TradingSystemInstrument instrument = instruments.get(position.getSecurity());
            if (instrument != null) {
                instrument.setPosition(position);
            }
        }
        engine = new JavaScriptEngine(this, context);
        engine.addObserver(observer);
        engine.start();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#stop()
     */
    @Override
    public void stop() {
        if (engine != null) {
            engine.deleteObserver(observer);
            engine.stop();
            engine.dispose();
            engine = null;
        }
        for (TradingSystemInstrument instrument : instruments.values()) {
            instrument.setQuote(null);
            instrument.setTrade(null);
        }
    }

    public TradingSystemProperties getProperties() {
        return properties;
    }

    public void setProperties(TradingSystemProperties properties) {
        this.properties = properties;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(strategy.getClass())) {
            return strategy;
        }
        if (adapter.isAssignableFrom(properties.getClass())) {
            return properties;
        }
        if (adapter.isAssignableFrom(changeSupport.getClass())) {
            return changeSupport;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
