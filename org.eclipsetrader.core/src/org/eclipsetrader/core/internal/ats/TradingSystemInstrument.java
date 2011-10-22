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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipsetrader.core.ats.ITradingSystemInstrument;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;

public class TradingSystemInstrument implements ITradingSystemInstrument {

    public static final String PROPERTY_QUOTE = "quote";
    public static final String PROPERTY_TRADE = "trade";
    public static final String PROPERTY_BARS = "bars";
    public static final String PROPERTY_POSITION = "position";

    private final ISecurity instrument;

    private ITrade trade;
    private IQuote quote;
    private IPosition position;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public TradingSystemInstrument(ISecurity instrument) {
        this.instrument = instrument;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemInstrument#getInstrument()
     */
    @Override
    public ISecurity getInstrument() {
        return instrument;
    }

    public IPosition getPosition() {
        return position;
    }

    public void setPosition(IPosition position) {
        changeSupport.firePropertyChange(PROPERTY_POSITION, this.position, this.position = position);
    }

    public ITrade getTrade() {
        return trade;
    }

    public void setTrade(ITrade trade) {
        changeSupport.firePropertyChange(PROPERTY_TRADE, this.trade, this.trade = trade);
    }

    public IQuote getQuote() {
        return quote;
    }

    public void setQuote(IQuote quote) {
        changeSupport.firePropertyChange(PROPERTY_TRADE, this.quote, this.quote = quote);
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

        if (adapter.isAssignableFrom(IPosition.class)) {
            return position;
        }
        if (position != null && adapter.isAssignableFrom(position.getClass())) {
            return position;
        }

        if (adapter.isAssignableFrom(ITrade.class)) {
            return trade;
        }
        if (trade != null && adapter.isAssignableFrom(trade.getClass())) {
            return trade;
        }

        if (adapter.isAssignableFrom(IQuote.class)) {
            return quote;
        }
        if (quote != null && adapter.isAssignableFrom(quote.getClass())) {
            return quote;
        }

        if (adapter.isAssignableFrom(changeSupport.getClass())) {
            return changeSupport;
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
}
