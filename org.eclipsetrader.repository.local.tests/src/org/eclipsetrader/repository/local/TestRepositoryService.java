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

package org.eclipsetrader.repository.local;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;

public class TestRepositoryService implements IRepositoryService {

    private Map<String, IRepository> repositories = new HashMap<String, IRepository>();

    private Map<String, IFeedIdentifier> identifiersMap = new HashMap<String, IFeedIdentifier>();
    private Map<String, ISecurity> securities = new HashMap<String, ISecurity>();
    private Map<URI, ISecurity> securitiesUriMap = new HashMap<URI, ISecurity>();

    private Map<String, IWatchList> watchlists = new HashMap<String, IWatchList>();
    private Map<URI, IWatchList> watchlistsUriMap = new HashMap<URI, IWatchList>();

    private Map<URI, IStoreObject> uriMap = new HashMap<URI, IStoreObject>();

    public TestRepositoryService() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#deleteAdaptable(org.eclipse.core.runtime.IAdaptable[])
     */
    @Override
    public void deleteAdaptable(IAdaptable[] adaptables) {
        for (IAdaptable a : adaptables) {
            if (a instanceof ISecurity) {
                securities.remove(((ISecurity) a).getName());
            }
            if (a instanceof IWatchList) {
                watchlists.remove(((IWatchList) a).getName());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#moveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
     */
    @Override
    public void moveAdaptable(IAdaptable[] adaptables, IRepository repository) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
     */
    @Override
    public void saveAdaptable(IAdaptable[] adaptables, IRepository defaultRepository) {
        saveAdaptable(adaptables);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[])
     */
    @Override
    public void saveAdaptable(IAdaptable[] adaptables) {
        for (IAdaptable a : adaptables) {
            IStoreObject so = (IStoreObject) a.getAdapter(IStoreObject.class);
            this.uriMap.put(so.getStore().toURI(), so);
            if (a instanceof ISecurity) {
                this.securities.put(((ISecurity) a).getName(), (ISecurity) a);
                this.securitiesUriMap.put(so.getStore().toURI(), (ISecurity) a);
                IFeedIdentifier i = (IFeedIdentifier) a.getAdapter(IFeedIdentifier.class);
                if (i != null) {
                    this.identifiersMap.put(i.getSymbol(), i);
                }
            }
            else if (a instanceof IWatchList) {
                this.watchlists.put(((IWatchList) a).getName(), (IWatchList) a);
                this.watchlistsUriMap.put(so.getStore().toURI(), (IWatchList) a);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getFeedIdentifierFromSymbol(java.lang.String)
     */
    @Override
    public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol) {
        return identifiersMap.get(symbol);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getFeedIdentifiers()
     */
    @Override
    public IFeedIdentifier[] getFeedIdentifiers() {
        Collection<IFeedIdentifier> c = identifiersMap.values();
        return c.toArray(new IFeedIdentifier[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getRepositories()
     */
    @Override
    public IRepository[] getRepositories() {
        Collection<IRepository> c = repositories.values();
        return c.toArray(new IRepository[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getRepository(java.lang.String)
     */
    @Override
    public IRepository getRepository(String scheme) {
        return repositories.get(scheme);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurities()
     */
    @Override
    public ISecurity[] getSecurities() {
        Collection<ISecurity> c = securities.values();
        return c.toArray(new ISecurity[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurityFromName(java.lang.String)
     */
    @Override
    public ISecurity getSecurityFromName(String name) {
        return securities.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurityFromURI(java.net.URI)
     */
    @Override
    public ISecurity getSecurityFromURI(URI uri) {
        return securitiesUriMap.get(uri);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromName(java.lang.String)
     */
    @Override
    public IWatchList getWatchListFromName(String name) {
        return watchlists.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromURI(java.net.URI)
     */
    @Override
    public IWatchList getWatchListFromURI(URI uri) {
        return watchlistsUriMap.get(uri);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchLists()
     */
    @Override
    public IWatchList[] getWatchLists() {
        Collection<IWatchList> c = watchlists.values();
        return c.toArray(new IWatchList[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getTrades()
     */
    @Override
    public IHolding[] getTrades() {
        return new IHolding[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(IRepositoryRunnable runnable, IProgressMonitor monitor) {
        try {
            runnable.run(monitor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
        try {
            runnable.run(monitor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void saveFeedIdentifiers(IFeedIdentifier[] identifiers) {
        for (IFeedIdentifier i : identifiers) {
            this.identifiersMap.put(i.getSymbol(), i);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#addRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
     */
    @Override
    public void addRepositoryResourceListener(IRepositoryChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#removeRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
     */
    @Override
    public void removeRepositoryResourceListener(IRepositoryChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getHistoryFor(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IHistory getHistoryFor(ISecurity security) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getObjectFromURI(java.net.URI)
     */
    @Override
    public IStoreObject getObjectFromURI(URI uri) {
        return uriMap.get(uri);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getAllObjects()
     */
    @Override
    public IStoreObject[] getAllObjects() {
        return uriMap.values().toArray(new IStoreObject[uriMap.values().size()]);
    }
}
