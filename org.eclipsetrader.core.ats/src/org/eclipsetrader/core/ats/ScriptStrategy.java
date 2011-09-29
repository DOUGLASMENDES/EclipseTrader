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

package org.eclipsetrader.core.ats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

/**
 * Default implementation a strategy script.
 * 
 * @since 1.0
 */
public class ScriptStrategy implements IScriptStrategy, IStoreObject, IAdaptable {

    private String name;
    private String language;
    private String text;
    private List<ISecurity> instruments = new ArrayList<ISecurity>();
    private List<TimeSpan> barsTimeSpan = new ArrayList<TimeSpan>();

    private IStore store;
    private IStoreProperties storeProperties;

    public ScriptStrategy() {
    }

    public ScriptStrategy(String name) {
        this.name = name;
    }

    public ScriptStrategy(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IStrategy#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IScriptStrategy#getLanguage()
     */
    @Override
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IScriptStrategy#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IStrategy#getInstruments()
     */
    @Override
    public ISecurity[] getInstruments() {
        return instruments.toArray(new ISecurity[instruments.size()]);
    }

    public void setInstruments(ISecurity[] instruments) {
        this.instruments = new ArrayList<ISecurity>();
        if (instruments != null) {
            this.instruments.addAll(Arrays.asList(instruments));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.IStrategy#getBarsTimeSpan()
     */
    @Override
    public TimeSpan[] getBarsTimeSpan() {
        return barsTimeSpan.toArray(new TimeSpan[barsTimeSpan.size()]);
    }

    public void setBarsTimeSpan(TimeSpan[] barsTimeSpan) {
        this.barsTimeSpan = new ArrayList<TimeSpan>();
        if (barsTimeSpan != null) {
            this.barsTimeSpan.addAll(Arrays.asList(barsTimeSpan));
        }
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
     * @see org.eclipsetrader.core.internal.ats.Strategy#getStoreProperties()
     */
    @Override
    public IStoreProperties getStoreProperties() {
        if (storeProperties == null) {
            storeProperties = new StoreProperties();
        }

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IScriptStrategy.class.getName());

        storeProperties.setProperty(IScriptStrategy.PROP_NAME, name);
        storeProperties.setProperty(IScriptStrategy.PROP_LANGUAGE, language);
        storeProperties.setProperty(IScriptStrategy.PROP_TEXT, text);
        storeProperties.setProperty(IScriptStrategy.PROP_INSTRUMENTS, instruments.toArray(new ISecurity[instruments.size()]));
        storeProperties.setProperty(IScriptStrategy.PROP_BARS_TIMESPAN, barsTimeSpan.toArray(new TimeSpan[barsTimeSpan.size()]));

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.internal.ats.Strategy#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;

        this.name = (String) storeProperties.getProperty(IScriptStrategy.PROP_NAME);
        this.language = (String) storeProperties.getProperty(IScriptStrategy.PROP_LANGUAGE);
        this.text = (String) storeProperties.getProperty(IScriptStrategy.PROP_TEXT);

        this.instruments = new ArrayList<ISecurity>();
        ISecurity[] instruments = (ISecurity[]) storeProperties.getProperty(IScriptStrategy.PROP_INSTRUMENTS);
        if (instruments != null) {
            this.instruments.addAll(Arrays.asList(instruments));
        }

        this.barsTimeSpan = new ArrayList<TimeSpan>();
        TimeSpan[] barsTimeSpan = (TimeSpan[]) storeProperties.getProperty(IScriptStrategy.PROP_BARS_TIMESPAN);
        if (barsTimeSpan != null) {
            this.barsTimeSpan.addAll(Arrays.asList(barsTimeSpan));
        }
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
