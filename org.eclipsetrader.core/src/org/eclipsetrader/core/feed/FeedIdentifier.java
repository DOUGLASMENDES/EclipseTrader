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

package org.eclipsetrader.core.feed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.PlatformObject;

public class FeedIdentifier extends PlatformObject implements IFeedIdentifier, PropertyChangeListener {

    private String symbol;
    private FeedProperties properties;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public FeedIdentifier(String symbol, FeedProperties properties) {
        this.symbol = symbol;
        this.properties = properties;
        if (this.properties != null) {
            this.properties.addPropertyChangeListener(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedIdentifier#getSymbol()
     */
    @Override
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        Object oldValue = this.symbol;
        this.symbol = symbol;
        propertyChangeSupport.firePropertyChange(PROP_SYMBOL, oldValue, this.symbol);
    }

    public IFeedProperties getProperties() {
        return properties;
    }

    public void setProperties(FeedProperties newProperties) {
        if (properties != null) {
            properties.removePropertyChangeListener(this);
        }

        Object oldValue = properties;

        properties = newProperties;
        if (properties != null) {
            properties.addPropertyChangeListener(this);
        }

        propertyChangeSupport.firePropertyChange(PROP_PROPERTIES, oldValue, properties);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(this.getClass())) {
            return this;
        }

        if (adapter.isAssignableFrom(IFeedProperties.class)) {
            return properties;
        }
        if (properties != null && adapter.isAssignableFrom(properties.getClass())) {
            return properties;
        }

        if (adapter.isAssignableFrom(PropertyChangeSupport.class)) {
            return propertyChangeSupport;
        }

        return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IFeedIdentifier)) {
            return false;
        }
        IFeedIdentifier other = (IFeedIdentifier) obj;
        return getSymbol() == other.getSymbol() || getSymbol() != null && getSymbol().equals(other.getSymbol());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * (symbol != null ? symbol.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String p = properties != null ? properties.toString() : "";
        return symbol + (p.length() != 0 ? "(" + p + ")" : "");
    }
}
