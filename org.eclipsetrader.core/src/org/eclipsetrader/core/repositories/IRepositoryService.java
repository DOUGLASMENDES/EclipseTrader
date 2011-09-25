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

package org.eclipsetrader.core.repositories;

import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;

/**
 * Interface to access resources located in a repository.
 *
 * @since 1.0
 */
public interface IRepositoryService {

    /**
     * Gets all repositories known to this service.
     *
     * @return the repositories.
     */
    public IRepository[] getRepositories();

    /**
     * Gets the repository instance that handles the given URI scheme.
     *
     * @param scheme the URI scheme.
     * @return the repository instance, or null.
     */
    public IRepository getRepository(String scheme);

    /**
     * Saves the given objects to their' repositories. If the object doesn't have
     * a repository (i.e. newly created objects) they are saved to the local
     * repository.
     *
     * @param adaptables the objects to save.
     */
    public void saveAdaptable(IAdaptable[] adaptables);

    /**
     * Saves the given objects to their' repositories. If the object doesn't have
     * a repository (i.e. newly created objects) they are saved to the specified
     * default repository.
     *
     * @param adaptables the objects to save.
     * @param defaultRepository the default destination repository.
     */
    public void saveAdaptable(IAdaptable[] adaptables, IRepository defaultRepository);

    /**
     * Moves a set of objects to a new repository.
     *
     * @param adaptables the objects to move.
     * @param repository the destination repository.
     */
    public void moveAdaptable(IAdaptable[] adaptables, IRepository repository);

    /**
     * Deletes a set of objects.
     *
     * @param adaptables the objects to delete.
     */
    public void deleteAdaptable(IAdaptable[] adaptables);

    /**
     * Returns a possibly empty array of all security instruments.
     *
     * @return the securities.
     */
    public ISecurity[] getSecurities();

    /**
     * Returns the security with the given name.
     *
     * @param name the name.
     * @return the security, or null.
     */
    public ISecurity getSecurityFromName(String name);

    /**
     * Returns the security known with the given URI, or <code>null</code> if no
     * security with the given URI exists.
     *
     * @param uri the URI.
     * @return the security, or null.
     */
    public ISecurity getSecurityFromURI(URI uri);

    /**
     * Returns a possibly empty array of all feed identifiers.
     *
     * @return the feed identifiers.
     */
    public IFeedIdentifier[] getFeedIdentifiers();

    /**
     * Returns the security with the given symbol, or null if no identifiers are found.
     *
     * @param name the symbol.
     * @return the identifier, or null.
     */
    public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol);

    /**
     * Returns the history object for the given security.
     *
     * @param security the security.
     * @return the history.
     */
    public IHistory getHistoryFor(ISecurity security);

    /**
     * Returns a possibly empty array of all watchlists.
     *
     * @return the watchlists.
     */
    public IWatchList[] getWatchLists();

    /**
     * Returns the watchlist with the given name.
     *
     * @param name the name.
     * @return the watchlist, or null.
     */
    public IWatchList getWatchListFromName(String name);

    /**
     * Returns the watchlist with the given URI, or <code>null</code> if no
     * watchlist with the given URI exists.
     *
     * @param uri the URI.
     * @return the watchlist, or <code>null</code>.
     */
    public IWatchList getWatchListFromURI(URI uri);

    /**
     * Runs the specified task within the service.
     *
     * @param runnable - the task to run.
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired.
     */
    public IStatus runInService(IRepositoryRunnable runnable, IProgressMonitor monitor);

    /**
     * Runs the specified task within the service.
     *
     * @param runnable - the task to run.
     * @param rule the scheduling rule to use when running this operation, or null if there are no scheduling restrictions for this operation.
     * @param monitor - a progress monitor, or null if progress reporting and cancellation are not desired.
     */
    public IStatus runInService(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor);

    /**
     * Adds a listener to the collection of listeners that receives notifications about
     * repository resources changes.
     *
     * @param listener the listener to add
     */
    public void addRepositoryResourceListener(IRepositoryChangeListener listener);

    /**
     * Removes a listener from the collection of listeners that receives notifications about
     * repository resources changes.
     *
     * @param listener the listener to remove
     */
    public void removeRepositoryResourceListener(IRepositoryChangeListener listener);

    public IHolding[] getTrades();

    /**
     * Returns the store object known with the given URI, or <code>null</code> if no
     * store object with the given URI exists.
     *
     * @param uri the URI.
     * @return the store object, or null.
     */
    public IStoreObject getObjectFromURI(URI uri);

    public IStoreObject[] getAllObjects();
}
