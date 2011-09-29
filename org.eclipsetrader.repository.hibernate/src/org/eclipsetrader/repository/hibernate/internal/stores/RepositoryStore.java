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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.net.URI;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.repository.hibernate.HibernateRepository;

public class RepositoryStore implements IStore {

    private IStore store;
    private HibernateRepository repository;

    public RepositoryStore(HibernateRepository repository) {
        this.repository = repository;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void delete(IProgressMonitor monitor) throws CoreException {
        if (store != null) {
            store.delete(monitor);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        return store != null ? store.fetchProperties(monitor) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchChilds(IProgressMonitor monitor) {
        return store != null ? store.fetchChilds(monitor) : new IStore[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#createChild()
     */
    @Override
    public IStore createChild() {
        return store != null ? store.createChild() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#getRepository()
     */
    @Override
    public IRepository getRepository() {
        return store != null ? store.getRepository() : repository;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        if (store == null) {
            String type = (String) properties.getProperty(IPropertyConstants.OBJECT_TYPE);
            if (ISecurity.class.getName().equals(type) || IStock.class.getName().equals(type) || ICurrencyExchange.class.getName().equals(type)) {
                store = new SecurityStore(repository);
            }
            else if (IWatchList.class.getName().equals(type)) {
                store = new WatchListStore(repository);
            }
            else if (IHistory.class.getName().equals(type)) {
                ISecurity security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);
                IStoreObject storeObject = (IStoreObject) security.getAdapter(IStoreObject.class);

                IStore securityStore = storeObject.getStore();
                if (securityStore instanceof RepositoryStore) {
                    securityStore = ((RepositoryStore) securityStore).getStore();
                }
                if (securityStore instanceof SecurityStore) {
                    Date date = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
                    if (date != null) {
                        store = new IntradayHistoryStore(security, date, repository);
                    }
                    else {
                        store = new HistoryStore(security, repository);
                    }
                }
            }
            else if (Script.class.getName().equals(type)) {
                store = new ScriptStore(repository);
            }
            else if (IScriptStrategy.class.getName().equals(type)) {
                store = new StrategyScriptStore(repository);
            }
        }
        if (store != null) {
            store.putProperties(properties, monitor);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStore#toURI()
     */
    @Override
    public URI toURI() {
        return store != null ? store.toURI() : null;
    }

    public IStore getStore() {
        return store;
    }
}
