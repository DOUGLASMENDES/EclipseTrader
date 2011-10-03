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

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IRepositoryProvider;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.core.views.WatchList;

public class RepositoryService implements IRepositoryService {

    private Map<String, IRepository> repositoryMap = new HashMap<String, IRepository>();
    private Map<URI, IStoreObject> objectsMap = new HashMap<URI, IStoreObject>();

    private Map<URI, ISecurity> uriMap = new HashMap<URI, ISecurity>();
    private Map<String, ISecurity> nameMap = new HashMap<String, ISecurity>();

    private Map<String, IFeedIdentifier> identifiersMap = new HashMap<String, IFeedIdentifier>();

    private Map<URI, IWatchList> watchlistUriMap = new HashMap<URI, IWatchList>();
    private Map<String, IWatchList> watchlistNameMap = new HashMap<String, IWatchList>();

    private Map<ISecurity, WeakReference<IHistory>> historyMap = new HashMap<ISecurity, WeakReference<IHistory>>();

    private List<IHolding> trades = new ArrayList<IHolding>();

    private IJobManager jobManager;
    private final ILock lock;

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private List<RepositoryResourceDelta> deltas;

    public RepositoryService() {
        jobManager = Job.getJobManager();
        lock = jobManager.newLock();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getRepositories()
     */
    @Override
    public IRepository[] getRepositories() {
        Collection<IRepository> c = repositoryMap.values();
        return c.toArray(new IRepository[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getRepository(java.lang.String)
     */
    @Override
    public IRepository getRepository(String scheme) {
        return repositoryMap.get(scheme);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getSecurities()
     */
    @Override
    public ISecurity[] getSecurities() {
        Collection<ISecurity> c = uriMap.values();
        return c.toArray(new ISecurity[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getSecurityFromName(java.lang.String)
     */
    @Override
    public ISecurity getSecurityFromName(String name) {
        return nameMap.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getSecurityFromURI(java.net.URI)
     */
    @Override
    public ISecurity getSecurityFromURI(URI uri) {
        if (uriMap.containsKey(uri)) {
            return uriMap.get(uri);
        }

        String schema = uri.getScheme();
        IRepository repository = getRepository(schema);
        if (repository != null) {
            IStore store = repository.getObject(uri);
            if (store != null) {
                IStoreObject element = createElement(store, store.fetchProperties(null));
                if (element instanceof ISecurity) {
                    putSecurity(store, (ISecurity) element);
                    return (ISecurity) element;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getHistoryFor(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IHistory getHistoryFor(ISecurity security) {
        WeakReference<IHistory> reference = historyMap.get(security);
        IHistory history = reference != null ? reference.get() : null;

        if (history == null) {
            IStoreObject storeObject = (IStoreObject) security.getAdapter(IStoreObject.class);
            if (storeObject != null && storeObject.getStore() != null) {
                IStore[] stores = storeObject.getStore().fetchChilds(null);
                for (int i = 0; i < stores.length; i++) {
                    IStoreObject object = createElement(stores[i], stores[i].fetchProperties(null));
                    if (object instanceof IHistory) {
                        history = (IHistory) object;
                        historyMap.put(security, new WeakReference<IHistory>(history));
                        break;
                    }
                }
            }
        }

        if (history == null) {
            history = new History(security, new IOHLC[0]);
            historyMap.put(security, new WeakReference<IHistory>(history));
        }

        return history;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchLists()
     */
    @Override
    public IWatchList[] getWatchLists() {
        Collection<IWatchList> c = watchlistUriMap.values();
        return c.toArray(new IWatchList[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromName(java.lang.String)
     */
    @Override
    public IWatchList getWatchListFromName(String name) {
        return watchlistNameMap.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromURI(java.net.URI)
     */
    @Override
    public IWatchList getWatchListFromURI(URI uri) {
        if (watchlistUriMap.containsKey(uri)) {
            return watchlistUriMap.get(uri);
        }

        String schema = uri.getScheme();
        IRepository repository = getRepository(schema);
        if (repository != null) {
            IStore store = repository.getObject(uri);
            if (store != null) {
                IStoreObject element = createElement(store, store.fetchProperties(null));
                if (element instanceof IWatchList) {
                    putWatchList(store, (IWatchList) element);
                    return (IWatchList) element;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getTrades()
     */
    @Override
    public IHolding[] getTrades() {
        return trades.toArray(new IHolding[trades.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#deleteAdaptable(org.eclipse.core.runtime.IAdaptable[])
     */
    @Override
    public void deleteAdaptable(IAdaptable[] adaptables) {
        Map<IRepository, Set<IAdaptable>> repositories = new HashMap<IRepository, Set<IAdaptable>>();
        Map<IRepository, Set<ISchedulingRule>> rules = new HashMap<IRepository, Set<ISchedulingRule>>();

        // Computes the rules needed to access all repositories
        for (IAdaptable adaptable : adaptables) {
            IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
            if (storeObjects == null) {
                IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                if (object == null) {
                    continue;
                }
                storeObjects = new IStoreObject[] {
                    object
                };
            }

            for (IStoreObject object : storeObjects) {
                IStore store = object.getStore();
                if (store != null) {
                    IRepository repository = store.getRepository();

                    Set<IAdaptable> objectSet = repositories.get(repository);
                    if (objectSet == null) {
                        objectSet = new HashSet<IAdaptable>();
                        repositories.put(repository, objectSet);
                    }
                    objectSet.add(adaptable);

                    Set<ISchedulingRule> ruleSet = rules.get(repository);
                    if (ruleSet == null) {
                        ruleSet = new HashSet<ISchedulingRule>();
                        rules.put(repository, ruleSet);
                    }
                    if (repository instanceof ISchedulingRule) {
                        ruleSet.add((ISchedulingRule) repository);
                    }
                }
            }
        }

        final Set<IAdaptable> saveCascade = new HashSet<IAdaptable>();

        for (IRepository repository : repositories.keySet()) {
            final Set<IAdaptable> set = repositories.get(repository);

            Set<ISchedulingRule> ruleSet = rules.get(repository);
            MultiRule rule = new MultiRule(ruleSet.toArray(new ISchedulingRule[ruleSet.size()]));

            IStatus status = repository.runInRepository(new IRepositoryRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    try {
                        for (IAdaptable adaptable : set) {
                            if (monitor != null && monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }

                            IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
                            if (storeObjects == null) {
                                IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                                if (object == null) {
                                    continue;
                                }
                                storeObjects = new IStoreObject[] {
                                    object
                                };
                            }

                            for (IStoreObject object : storeObjects) {
                                IStore store = object.getStore();
                                if (store != null) {
                                    store.delete(monitor);
                                    object.setStore(null);

                                    uriMap.remove(store.toURI());

                                    if (adaptable instanceof ISecurity) {
                                        nameMap.remove(((ISecurity) adaptable).getName());
                                        Set<IAdaptable> containers = removeSecurityFromContainers((ISecurity) adaptable);
                                        saveCascade.addAll(containers);
                                        historyMap.remove(adaptable);
                                    }
                                    if (adaptable instanceof IWatchList) {
                                        watchlistUriMap.remove(store.toURI());
                                        watchlistNameMap.remove(((IWatchList) adaptable).getName());
                                    }
                                    if (adaptable instanceof IHolding) {
                                        trades.remove(adaptable);
                                    }

                                    if (deltas != null) {
                                        int kind = RepositoryResourceDelta.MOVED_FROM | RepositoryResourceDelta.REMOVED;
                                        deltas.add(new RepositoryResourceDelta(kind, adaptable, store.getRepository(), null, null, null));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error deleting object", e); //$NON-NLS-1$
                        CoreActivator.getDefault().getLog().log(status);
                        return status;
                    } catch (LinkageError e) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error deleting object", e); //$NON-NLS-1$
                        CoreActivator.getDefault().getLog().log(status);
                        return status;
                    }
                    return Status.OK_STATUS;
                }
            }, rule, null);
            if (status == Status.CANCEL_STATUS) {
                break;
            }
        }

        if (saveCascade.size() != 0) {
            saveAdaptable(saveCascade.toArray(new IAdaptable[saveCascade.size()]));
        }
    }

    protected Set<IAdaptable> removeSecurityFromContainers(ISecurity security) {
        Set<IAdaptable> saveCascade = new HashSet<IAdaptable>();

        for (IWatchList list : watchlistUriMap.values()) {
            if (!(list instanceof WatchList)) {
                continue;
            }
            IWatchListElement[] elements = list.getItem(security);
            if (elements != null && elements.length != 0) {
                List<IWatchListElement> allElements = new ArrayList<IWatchListElement>();
                allElements.addAll(Arrays.asList(list.getItems()));
                allElements.removeAll(Arrays.asList(elements));
                ((WatchList) list).setItems(allElements.toArray(new IWatchListElement[allElements.size()]));
                saveCascade.add(list);
            }
        }

        return saveCascade;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[])
     */
    @Override
    public void saveAdaptable(IAdaptable[] adaptables) {
        saveAdaptable(adaptables, getRepository("local"));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
     */
    @Override
    public void saveAdaptable(IAdaptable[] adaptables, IRepository defaultRepository) {
        Map<IRepository, Set<IAdaptable>> repositories = new HashMap<IRepository, Set<IAdaptable>>();
        Map<IRepository, Set<ISchedulingRule>> rules = new HashMap<IRepository, Set<ISchedulingRule>>();

        // Computes the rules needed to access all repositories
        for (IAdaptable adaptable : adaptables) {
            IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
            if (storeObjects == null) {
                IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                if (object == null) {
                    continue;
                }
                storeObjects = new IStoreObject[] {
                    object
                };
            }

            for (IStoreObject object : storeObjects) {
                IRepository repository = defaultRepository;
                IStore store = object.getStore();
                if (store != null && store.getRepository() != null) {
                    repository = store.getRepository();
                }

                Set<IAdaptable> objectSet = repositories.get(repository);
                if (objectSet == null) {
                    objectSet = new HashSet<IAdaptable>();
                    repositories.put(repository, objectSet);
                }
                objectSet.add(adaptable);

                Set<ISchedulingRule> ruleSet = rules.get(repository);
                if (ruleSet == null) {
                    ruleSet = new HashSet<ISchedulingRule>();
                    rules.put(repository, ruleSet);
                }
                if (repository instanceof ISchedulingRule) {
                    ruleSet.add((ISchedulingRule) repository);
                }
            }
        }

        for (IRepository r : repositories.keySet()) {
            final IRepository repository = r;
            final Set<IAdaptable> set = repositories.get(repository);

            Set<ISchedulingRule> ruleSet = rules.get(repository);
            MultiRule rule = new MultiRule(ruleSet.toArray(new ISchedulingRule[ruleSet.size()]));

            IStatus status = repository.runInRepository(new IRepositoryRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    try {
                        for (IAdaptable adaptable : set) {
                            if (monitor != null && monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }

                            IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
                            if (storeObjects == null) {
                                IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                                storeObjects = new IStoreObject[] {
                                    object
                                };
                            }

                            for (IStoreObject object : storeObjects) {
                                int kind = RepositoryResourceDelta.MOVED_TO;

                                IStore store = object.getStore();
                                if (store == null) {
                                    store = repository.createObject();
                                    kind |= RepositoryResourceDelta.ADDED;
                                }
                                else {
                                    kind |= RepositoryResourceDelta.CHANGED;
                                }

                                IStoreProperties properties = object.getStoreProperties();
                                store.putProperties(properties, monitor);
                                object.setStore(store);

                                if (adaptable instanceof ISecurity) {
                                    uriMap.put(store.toURI(), (ISecurity) adaptable);
                                    nameMap.put(((ISecurity) adaptable).getName(), (ISecurity) adaptable);
                                }
                                if (adaptable instanceof IWatchList) {
                                    watchlistUriMap.put(store.toURI(), (IWatchList) adaptable);
                                    watchlistNameMap.put(((IWatchList) adaptable).getName(), (IWatchList) adaptable);
                                }
                                if (adaptable instanceof IHolding) {
                                    trades.add((IHolding) adaptable);
                                }

                                if (deltas != null) {
                                    deltas.add(new RepositoryResourceDelta(kind, adaptable, null, store.getRepository(), null, properties));
                                }
                            }
                        }
                    } catch (Exception e) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error saving object", e); //$NON-NLS-1$
                        CoreActivator.getDefault().getLog().log(status);
                        return status;
                    } catch (LinkageError e) {
                        Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error saving object", e); //$NON-NLS-1$
                        CoreActivator.getDefault().getLog().log(status);
                        return status;
                    }
                    return Status.OK_STATUS;
                }
            }, rule, null);
            if (status == Status.CANCEL_STATUS) {
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#moveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
     */
    @Override
    public void moveAdaptable(final IAdaptable[] adaptables, final IRepository destination) {
        // Computes the rules needed to access all source and destination repositories
        List<ISchedulingRule> rules = new ArrayList<ISchedulingRule>();
        if (destination instanceof ISchedulingRule) {
            rules.add((ISchedulingRule) destination);
        }

        for (IAdaptable adaptable : adaptables) {
            IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
            if (storeObjects == null) {
                IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                if (object == null) {
                    continue;
                }
                storeObjects = new IStoreObject[] {
                    object
                };
            }

            for (IStoreObject object : storeObjects) {
                IStore store = object.getStore();
                if (store != null && store.getRepository() instanceof ISchedulingRule) {
                    rules.add((ISchedulingRule) store.getRepository());
                }
            }
        }

        destination.runInRepository(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                try {
                    for (IAdaptable adaptable : adaptables) {
                        if (monitor != null && monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }

                        IStoreObject[] storeObjects = (IStoreObject[]) adaptable.getAdapter(IStoreObject[].class);
                        if (storeObjects == null) {
                            IStoreObject object = (IStoreObject) adaptable.getAdapter(IStoreObject.class);
                            if (object == null) {
                                continue;
                            }
                            storeObjects = new IStoreObject[] {
                                object
                            };
                        }

                        List<IStoreObject> childStoreObjects = new ArrayList<IStoreObject>();
                        if (adaptable instanceof ISecurity) {
                            IHistory history = getHistoryFor((ISecurity) adaptable);
                            if (history != null) {
                                IStoreObject historyStoreObject = (IStoreObject) history.getAdapter(IStoreObject.class);
                                if (historyStoreObject != null && historyStoreObject.getStore() != null) {
                                    childStoreObjects.add(historyStoreObject);
                                }

                                IOHLC[] ohlc = history.getOHLC();
                                if (ohlc != null && ohlc.length != 0) {
                                    Date first = ohlc[0].getDate();
                                    Date last = ohlc[ohlc.length - 1].getDate();
                                    IHistory historySubset = history.getSubset(first, last, TimeSpan.minutes(1));
                                    if (historySubset != null) {
                                        IStoreObject[] subsetStoreObject = (IStoreObject[]) historySubset.getAdapter(IStoreObject[].class);
                                        if (subsetStoreObject != null) {
                                            childStoreObjects.addAll(Arrays.asList(subsetStoreObject));
                                        }
                                    }
                                }
                            }
                        }

                        for (IStoreObject object : storeObjects) {
                            final IStore oldStore = object.getStore();
                            if (oldStore != null && oldStore.getRepository() == destination) {
                                continue;
                            }

                            IStoreProperties properties = object.getStoreProperties();

                            IStore newStore = destination.createObject();
                            newStore.putProperties(properties, monitor);
                            object.setStore(newStore);

                            for (IStoreObject childObject : childStoreObjects) {
                                final IStore oldChildStore = childObject.getStore();
                                if (oldChildStore != null && oldChildStore.getRepository() == destination) {
                                    continue;
                                }

                                IStoreProperties childProperties = childObject.getStoreProperties();

                                IStore newChildStore = destination.createObject();
                                newChildStore.putProperties(childProperties, monitor);
                                childObject.setStore(newStore);

                                IStatus status = oldChildStore.getRepository().runInRepository(new IRepositoryRunnable() {

                                    @Override
                                    public IStatus run(IProgressMonitor monitor) throws Exception {
                                        oldChildStore.delete(monitor);
                                        return Status.OK_STATUS;
                                    }
                                }, monitor);
                                if (status != Status.OK_STATUS) {
                                    return status;
                                }
                            }

                            if (oldStore != null) {
                                IStatus status = oldStore.getRepository().runInRepository(new IRepositoryRunnable() {

                                    @Override
                                    public IStatus run(IProgressMonitor monitor) throws Exception {
                                        oldStore.delete(monitor);
                                        return Status.OK_STATUS;
                                    }
                                }, monitor);
                                if (status != Status.OK_STATUS) {
                                    return status;
                                }
                            }

                            if (adaptable instanceof ISecurity) {
                                if (oldStore != null) {
                                    uriMap.remove(oldStore.toURI());
                                }
                                uriMap.put(newStore.toURI(), (ISecurity) adaptable);
                                nameMap.put(((ISecurity) adaptable).getName(), (ISecurity) adaptable);

                                for (IWatchList watchList : watchlistUriMap.values()) {
                                    if (watchList.getItem((ISecurity) adaptable) != null) {
                                        IStoreObject otherObject = (IStoreObject) watchList.getAdapter(IStoreObject.class);
                                        IStoreProperties otherProperties = otherObject.getStoreProperties();
                                        otherObject.getStore().putProperties(otherProperties, monitor);
                                    }
                                }
                            }
                            if (adaptable instanceof IWatchList) {
                                if (oldStore != null) {
                                    watchlistUriMap.remove(oldStore.toURI());
                                }
                                watchlistUriMap.put(newStore.toURI(), (IWatchList) adaptable);
                                watchlistNameMap.put(((IWatchList) adaptable).getName(), (IWatchList) adaptable);
                            }

                            if (deltas != null) {
                                int kind = RepositoryResourceDelta.MOVED_TO;
                                if (oldStore != null) {
                                    kind |= RepositoryResourceDelta.MOVED_FROM;
                                }
                                else {
                                    kind |= RepositoryResourceDelta.ADDED;
                                }

                                deltas.add(new RepositoryResourceDelta(kind, adaptable, oldStore != null ? oldStore.getRepository() : null, newStore.getRepository(), null, null));
                            }
                        }
                    }
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error moving object", e); //$NON-NLS-1$
                    CoreActivator.getDefault().getLog().log(status);
                    return status;
                } catch (LinkageError e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error moving object", e); //$NON-NLS-1$
                    CoreActivator.getDefault().getLog().log(status);
                    return status;
                }

                return Status.OK_STATUS;
            }
        }, new MultiRule(rules.toArray(new ISchedulingRule[rules.size()])), null);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getFeedIdentifierFromSymbol(java.lang.String)
     */
    @Override
    public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol) {
        return identifiersMap.get(symbol);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#getFeedIdentifiers()
     */
    @Override
    public IFeedIdentifier[] getFeedIdentifiers() {
        Collection<IFeedIdentifier> c = identifiersMap.values();
        return c.toArray(new IFeedIdentifier[c.size()]);
    }

    public void startUp() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CoreActivator.REPOSITORY_ID);
        if (extensionPoint == null) {
            return;
        }

        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();

        // Reads the static repositories
        for (int j = 0; j < configElements.length; j++) {
            if (!"repository".equals(configElements[j].getName())) {
                continue;
            }
            String id = configElements[j].getAttribute("id"); //$NON-NLS-1$
            String schema = configElements[j].getAttribute("scheme"); //$NON-NLS-1$
            try {
                IRepository repository = (IRepository) configElements[j].createExecutableExtension("class");
                repositoryMap.put(schema, repository);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Unable to create repository with id " + id, e);
                CoreActivator.log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Unable to create repository with id " + id, e);
                CoreActivator.log(status);
            }
        }

        // Reads the user-created repositories from providers
        for (int j = 0; j < configElements.length; j++) {
            if (!"provider".equals(configElements[j].getName())) {
                continue;
            }
            String clazz = configElements[j].getAttribute("class"); //$NON-NLS-1$
            try {
                IRepositoryProvider provider = (IRepositoryProvider) configElements[j].createExecutableExtension("class");
                IRepository[] repository = provider.getRepositories(new NullProgressMonitor());
                for (int i = 0; i < repository.length; i++) {
                    repositoryMap.put(repository[i].getSchema(), repository[i]);
                }
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Unable to instantiate repository provider " + clazz, e);
                CoreActivator.log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Unable to instantiate repository provider " + clazz, e);
                CoreActivator.log(status);
            }
        }

        for (IRepository repository : repositoryMap.values()) {
            for (IStore store : repository.fetchObjects(null)) {
                if (uriMap.containsKey(store.toURI())) {
                    continue;
                }
                if (watchlistUriMap.containsKey(store.toURI())) {
                    continue;
                }
                IStoreObject element = createElement(store, store.fetchProperties(null));
                if (element instanceof ISecurity) {
                    putSecurity(store, (ISecurity) element);
                }
                else if (element instanceof IWatchList) {
                    putWatchList(store, (IWatchList) element);
                }
                else if (element instanceof IHolding) {
                    trades.add((IHolding) element);
                }
            }
        }
    }

    public void shutDown() {

    }

    protected void putSecurity(IStore store, ISecurity security) {
        uriMap.put(store.toURI(), security);
        nameMap.put(security.getName(), security);

        IFeedIdentifier identifier = security.getIdentifier();
        if (identifier != null) {
            identifiersMap.put(identifier.getSymbol(), identifier);
        }
    }

    protected void putWatchList(IStore store, IWatchList watchlist) {
        watchlistUriMap.put(store.toURI(), watchlist);
        watchlistNameMap.put(watchlist.getName(), watchlist);
    }

    protected IStoreObject createElement(IStore store, IStoreProperties properties) {
        // Build the object using the element factory, if defined
        try {
            IRepositoryElementFactory factory = (IRepositoryElementFactory) properties.getProperty(IPropertyConstants.ELEMENT_FACTORY);
            if (factory != null) {
                return factory.createElement(store, properties);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error creating element " + store.toURI().toString(), e);
            CoreActivator.getDefault().getLog().log(status);
        } catch (LinkageError e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error creating element " + store.toURI().toString(), e);
            CoreActivator.getDefault().getLog().log(status);
        }

        return DefaultElementFactory.getInstance().createElement(store, properties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(IRepositoryRunnable runnable, IProgressMonitor monitor) {
        return runInService(runnable, null, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
        IStatus status;
        if (rule != null) {
            jobManager.beginRule(rule, monitor);
        }
        try {
            lock.acquire();
            deltas = new ArrayList<RepositoryResourceDelta>();

            try {
                status = runnable.run(monitor);
            } catch (Exception e) {
                status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running service task", e); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            } catch (LinkageError e) {
                status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running service task", e); //$NON-NLS-1$
                CoreActivator.getDefault().getLog().log(status);
            }

            if (deltas.size() != 0) {
                final RepositoryChangeEvent event = new RepositoryChangeEvent(getDeltas());
                Object[] l = listeners.getListeners();
                for (int i = 0; i < l.length; i++) {
                    final IRepositoryChangeListener listener = (IRepositoryChangeListener) l[i];
                    SafeRunner.run(new ISafeRunnable() {

                        @Override
                        public void run() throws Exception {
                            listener.repositoryResourceChanged(event);
                        }

                        @Override
                        public void handleException(Throwable exception) {
                            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running repository listener", exception); //$NON-NLS-1$
                            CoreActivator.getDefault().getLog().log(status);
                        }
                    });
                }
            }

        } catch (Exception e) {
            status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
            CoreActivator.getDefault().getLog().log(status);
        } finally {
            lock.release();
            if (rule != null) {
                jobManager.endRule(rule);
            }
        }
        return status;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean contains(ISchedulingRule rule) {
        if (this == rule) {
            return true;
        }
        if (rule instanceof MultiRule) {
            MultiRule multi = (MultiRule) rule;
            ISchedulingRule[] children = multi.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!contains(children[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean isConflicting(ISchedulingRule rule) {
        if (this == rule) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#addRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
     */
    @Override
    public void addRepositoryResourceListener(IRepositoryChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#removeRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
     */
    @Override
    public void removeRepositoryResourceListener(IRepositoryChangeListener listener) {
        listeners.remove(listener);
    }

    protected RepositoryResourceDelta[] getDeltas() {
        if (deltas == null) {
            return new RepositoryResourceDelta[0];
        }
        return deltas.toArray(new RepositoryResourceDelta[deltas.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getObjectFromURI(java.net.URI)
     */
    @Override
    public IStoreObject getObjectFromURI(URI uri) {
        String schema = uri.getScheme();
        IRepository repository = getRepository(schema);
        if (repository != null) {
            IStore store = repository.getObject(uri);
            if (store != null) {
                IStoreObject element = objectsMap.get(store.toURI());
                if (element == null) {
                    element = createElement(store, store.fetchProperties(null));
                    if (element != null) {
                        objectsMap.put(store.toURI(), element);
                    }
                }
                return element;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepositoryService#getAllObjects()
     */
    @Override
    public IStoreObject[] getAllObjects() {
        List<IStoreObject> result = new ArrayList<IStoreObject>();
        for (IRepository repository : getRepositories()) {
            IStore[] objects = repository.fetchObjects(new NullProgressMonitor());
            for (IStore store : objects) {
                IStoreObject element = objectsMap.get(store.toURI());
                if (element == null) {
                    element = createElement(store, store.fetchProperties(null));
                    if (element != null) {
                        objectsMap.put(store.toURI(), element);
                    }
                }
                if (element != null) {
                    result.add(element);
                }
            }
        }
        return result.toArray(new IStoreObject[result.size()]);
    }
}
