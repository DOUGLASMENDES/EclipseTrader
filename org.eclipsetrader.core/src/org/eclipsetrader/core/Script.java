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

package org.eclipsetrader.core;

import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class Script implements IScript, IStoreObject {

    private String name;
    private String language;
    private String text;

    private IStore store;
    private IStoreProperties storeProperties;

    public Script() {
    }

    public Script(String name) {
        this.name = name;
    }

    public Script(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IScript#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IScript#getLanguage()
     */
    @Override
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String type) {
        this.language = type;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IScript#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, Script.class.getName());

        storeProperties.setProperty(IScript.NAME, name);
        storeProperties.setProperty(IScript.LANGUAGE, language);
        storeProperties.setProperty(IScript.TEXT, text);

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;

        this.name = (String) storeProperties.getProperty(IScript.NAME);
        this.language = (String) storeProperties.getProperty(IScript.LANGUAGE);
        this.text = (String) storeProperties.getProperty(IScript.TEXT);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IStoreProperties.class)) {
            return getStoreProperties();
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
        return name;
    }
}
