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

package org.eclipsetrader.core.internal.repositories;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.WatchList;

public class DefaultElementFactory implements IRepositoryElementFactory, IExecutableExtension {

    private static DefaultElementFactory instance;
    private String id;

    protected DefaultElementFactory() {
    }

    public static DefaultElementFactory getInstance() {
        if (instance == null) {
            instance = new DefaultElementFactory();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        if (instance == null) {
            instance = this;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryElementFactory#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryElementFactory#createElement(org.eclipsetrader.core.repositories.IStore, org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public IStoreObject createElement(IStore store, IStoreProperties properties) {
        String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
        if (type != null) {
            if (ICurrencyExchange.class.getName().equals(type)) {
                return new CurrencyExchange(store, properties);
            }
            if (ISecurity.class.getName().equals(type)) {
                return new Security(store, properties);
            }
            if (IStock.class.getName().equals(type)) {
                return new Stock(store, properties);
            }
            if (IWatchList.class.getName().equals(type)) {
                return new WatchList(store, properties);
            }
            if (IHistory.class.getName().equals(type)) {
                return new History(store, properties);
            }
            if (IHolding.class.getName().equals(type)) {
                return new Holding(store, properties);
            }
            if (IScript.class.getName().equals(type)) {
                return new Script(store, properties);
            }
            if (IScriptStrategy.class.getName().equals(type)) {
                return new ScriptStrategy(store, properties);
            }

            // Attempt to construct the referenced class object directly
            try {
                Class clazz = Class.forName(type);
                if (!clazz.isInterface()) {
                    Constructor constructor = clazz.getConstructor(IStore.class, IStoreProperties.class);
                    if (constructor != null) {
                        return (IStoreObject) constructor.newInstance(store, properties);
                    }
                }
            } catch (Exception e) {
                // Do nothing
            }
        }
        return null;
    }
}
