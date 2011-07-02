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

package org.eclipsetrader.core.instruments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

/**
 * Basic security instrument implementation.
 *
 * @since 1.0
 */
public class Security implements ISecurity, IStoreObject {

    private String name;
    private IFeedIdentifier identifier;
    private IUserProperties userProperties;

    private IStore store;
    private IStoreProperties storeProperties;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChangeSupport.firePropertyChange(IPropertyConstants.IDENTIFIER, null, identifier);
        }
    };

    protected Security() {
    }

    public Security(String name, IFeedIdentifier identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public Security(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ISecurity#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        Object oldValue = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(IPropertyConstants.NAME, oldValue, this.name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ISecurity#getIdentifier()
     */
    @Override
    public IFeedIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(IFeedIdentifier identifier) {
        Object oldValue = this.identifier;

        if (this.identifier != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) this.identifier.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        this.identifier = identifier;

        if (this.identifier != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) this.identifier.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
            }
        }

        propertyChangeSupport.firePropertyChange(IPropertyConstants.IDENTIFIER, oldValue, this.identifier);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ISecurity#getProperties()
     */
    @Override
    public IUserProperties getProperties() {
        return userProperties;
    }

    public void setProperties(IUserProperties userProperties) {
        Object oldValue = this.userProperties;
        this.userProperties = userProperties;
        propertyChangeSupport.firePropertyChange(IPropertyConstants.USER_PROPERTIES, oldValue, this.userProperties);
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

        if (identifier != null && adapter.isAssignableFrom(identifier.getClass())) {
            return identifier;
        }
        if (userProperties != null && adapter.isAssignableFrom(userProperties.getClass())) {
            return userProperties;
        }

        if (adapter.isAssignableFrom(PropertyChangeSupport.class)) {
            return propertyChangeSupport;
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

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, ISecurity.class.getName());

        storeProperties.setProperty(IPropertyConstants.NAME, getName());
        storeProperties.setProperty(IPropertyConstants.IDENTIFIER, getIdentifier());
        storeProperties.setProperty(IPropertyConstants.USER_PROPERTIES, getProperties());

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;

        if (this.identifier != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) this.identifier.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
            }
        }

        this.name = (String) storeProperties.getProperty(IPropertyConstants.NAME);
        this.identifier = (IFeedIdentifier) storeProperties.getProperty(IPropertyConstants.IDENTIFIER);
        this.userProperties = (IUserProperties) storeProperties.getProperty(IPropertyConstants.USER_PROPERTIES);

        if (this.identifier != null) {
            PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) this.identifier.getAdapter(PropertyChangeSupport.class);
            if (propertyChangeSupport != null) {
                propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ISecurity)) {
            return false;
        }

        ISecurity other = (ISecurity) obj;
        if (!name.equals(other.getName())) {
            return false;
        }
        if (identifier == null && other.getIdentifier() != null) {
            return false;
        }
        if (identifier != null && !identifier.equals(other.getIdentifier())) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
