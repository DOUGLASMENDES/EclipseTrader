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

import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

/**
 * Default implementation of the <code>IHolding</code> interface.
 * Clients may subclass.
 *
 * @since 1.0
 */
public class Holding implements IHolding, IStoreObject {

    public static final String PROP_QUANTITY = "position";
    public static final String PROP_PRICE = "purchasePrice";

    private ISecurity security;
    private Long position;
    private Double purchasePrice;
    private Date date;

    private IStore store;
    private IStoreProperties storeProperties;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    protected Holding() {
    }

    public Holding(ISecurity security) {
        this(security, null, null, null);
    }

    public Holding(ISecurity security, Long position, Double purchasePrice) {
        this(security, position, purchasePrice, null);
    }

    public Holding(ISecurity security, Long position, Double purchasePrice, Date date) {
        this.security = security;
        this.position = position;
        this.purchasePrice = purchasePrice;
        this.date = date;
    }

    public Holding(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getPosition()
     */
    @Override
    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        changeSupport.firePropertyChange(PROP_QUANTITY, this.position, this.position = position);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IHolding#getPurchasePrice()
     */
    @Override
    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        changeSupport.firePropertyChange(PROP_PRICE, this.purchasePrice, this.purchasePrice = purchasePrice);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (security != null) {
            if (security.getIdentifier() != null && adapter.isAssignableFrom(security.getIdentifier().getClass())) {
                return security;
            }
            if (adapter.isAssignableFrom(security.getClass())) {
                return security;
            }
        }

        if (adapter.isAssignableFrom(changeSupport.getClass())) {
            return changeSupport;
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

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHolding.class.getName());

        storeProperties.setProperty(IPropertyConstants.PURCHASE_DATE, date);
        storeProperties.setProperty(IPropertyConstants.SECURITY, security);
        storeProperties.setProperty(IPropertyConstants.PURCHASE_DATE, security);
        storeProperties.setProperty(IPropertyConstants.PURCHASE_QUANTITY, position);
        storeProperties.setProperty(IPropertyConstants.PURCHASE_PRICE, purchasePrice);

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;

        date = (Date) storeProperties.getProperty(IPropertyConstants.PURCHASE_DATE);
        security = (ISecurity) storeProperties.getProperty(IPropertyConstants.SECURITY);
        position = (Long) storeProperties.getProperty(IPropertyConstants.PURCHASE_QUANTITY);
        purchasePrice = (Double) storeProperties.getProperty(IPropertyConstants.PURCHASE_PRICE);
    }
}
