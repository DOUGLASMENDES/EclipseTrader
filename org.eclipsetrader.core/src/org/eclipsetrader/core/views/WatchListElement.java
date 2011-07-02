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
import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Default implementation of the <code>IWatchListElement</code> interface.
 *
 * @since 1.0
 */
public class WatchListElement implements IWatchListElement {

    private ISecurity security;
    private Long position;
    private Double purchasePrice;
    private Date date;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected WatchListElement() {
    }

    public WatchListElement(ISecurity security) {
        this(security, null, null, null);
    }

    public WatchListElement(ISecurity security, Long position, Double purchasePrice) {
        this(security, position, purchasePrice, null);
    }

    public WatchListElement(ISecurity security, Long position, Double purchasePrice, Date date) {
        this.security = security;
        this.position = position;
        this.purchasePrice = purchasePrice;
        this.date = date;
    }

    public WatchListElement(IHolding holding) {
        this(holding.getSecurity(), holding.getPosition(), holding.getPurchasePrice(), holding.getDate());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#setDate(java.util.Date)
     */
    @Override
    public void setDate(Date date) {
        Object oldValue = this.date;
        this.date = date;
        propertyChangeSupport.firePropertyChange(IWatchListElement.DATE, oldValue, this.date);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#getPosition()
     */
    @Override
    public Long getPosition() {
        return position;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#setPosition(java.lang.Long)
     */
    @Override
    public void setPosition(Long position) {
        Object oldValue = this.position;
        this.position = position;
        propertyChangeSupport.firePropertyChange(IWatchListElement.POSITION, oldValue, this.position);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#getPurchasePrice()
     */
    @Override
    public Double getPurchasePrice() {
        return purchasePrice;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IWatchListElement#setPurchasePrice(java.lang.Double)
     */
    @Override
    public void setPurchasePrice(Double purchasePrice) {
        Object oldValue = this.purchasePrice;
        this.purchasePrice = purchasePrice;
        propertyChangeSupport.firePropertyChange(IWatchListElement.PURCHASE_PRICE, oldValue, this.purchasePrice);
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

        if (security != null) {
            Object obj = security.getAdapter(adapter);
            if (obj != null) {
                return obj;
            }
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
