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

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.IdentifiersCollection;
import org.eclipsetrader.repository.local.internal.ScriptsCollection;
import org.eclipsetrader.repository.local.internal.SecurityCollection;
import org.eclipsetrader.repository.local.internal.StrategiesCollection;
import org.eclipsetrader.repository.local.internal.TradeCollection;
import org.eclipsetrader.repository.local.internal.WatchListCollection;
import org.eclipsetrader.repository.local.internal.stores.RepositoryStore;

public class LocalRepository implements IRepository, ISchedulingRule {

    public static final String URI_SCHEMA = "local";
    public static final String URI_SECURITY_PART = "securities";
    public static final String URI_SECURITY_HISTORY_PART = "securities/history";
    public static final String URI_SECURITY_INTRADAY_HISTORY_PART = "securities/history/{0}/{1}";
    public static final String URI_WATCHLIST_PART = "watchlists";
    public static final String URI_TRADE_PART = "trades";
    public static final String URI_SCRIPT_PART = "scripts";
    public static final String URI_STRATEGY_PART = "strategies";

    public static final String IDENTIFIERS_FILE = "identifiers.xml"; //$NON-NLS-1$
    public static final String SECURITIES_FILE = "securities.xml"; //$NON-NLS-1$
    public static final String SECURITIES_HISTORY_FILE = ".history"; //$NON-NLS-1$
    public static final String WATCHLISTS_FILE = "watchlists.xml"; //$NON-NLS-1$
    public static final String TRADES_FILE = "trades.xml"; //$NON-NLS-1$
    public static final String SCRIPTS_FILE = "scripts.xml"; //$NON-NLS-1$
    public static final String STRATEGIES_FILE = "strategies.xml"; //$NON-NLS-1$

    private static LocalRepository instance;
    private IPath location;

    private IdentifiersCollection identifiers;
    private SecurityCollection securities;
    private WatchListCollection watchlists;
    private TradeCollection trades;
    private ScriptsCollection scripts;
    private StrategiesCollection strategies;

    private IJobManager jobManager;
    private final ILock lock;

    public LocalRepository(IPath location) {
        this.location = location;
        instance = this;

        jobManager = Job.getJobManager();
        lock = jobManager.newLock();

        identifiers = new IdentifiersCollection();
        securities = new SecurityCollection();
        scripts = new ScriptsCollection();
    }

    public static LocalRepository getInstance() {
        return instance;
    }

    public IPath getLocation() {
        return location;
    }

    public void startUp() {
        File file = location.append(IDENTIFIERS_FILE).toFile();
        identifiers = (IdentifiersCollection) unmarshal(IdentifiersCollection.class, file);
        if (identifiers == null) {
            identifiers = new IdentifiersCollection();
        }

        file = location.append(SECURITIES_FILE).toFile();
        securities = (SecurityCollection) unmarshal(SecurityCollection.class, file);
        if (securities == null) {
            securities = new SecurityCollection();
        }

        file = location.append(SCRIPTS_FILE).toFile();
        scripts = (ScriptsCollection) unmarshal(ScriptsCollection.class, file);
        if (scripts == null) {
            scripts = new ScriptsCollection();
        }
    }

    protected synchronized void initializeWatchListsCollections() {
        if (watchlists == null) {
            if (Activator.getDefault() != null) {
                File file = Activator.getDefault().getStateLocation().append(WATCHLISTS_FILE).toFile();
                watchlists = (WatchListCollection) unmarshal(WatchListCollection.class, file);
            }
            if (watchlists == null) {
                watchlists = WatchListCollection.getInstance();
                if (watchlists == null) {
                    watchlists = new WatchListCollection();
                }
            }
        }
    }

    protected synchronized void initializeStrategiesListsCollections() {
        if (strategies == null) {
            if (Activator.getDefault() != null) {
                File file = Activator.getDefault().getStateLocation().append(STRATEGIES_FILE).toFile();
                strategies = (StrategiesCollection) unmarshal(StrategiesCollection.class, file);
            }
            if (strategies == null) {
                strategies = StrategiesCollection.getInstance();
                if (strategies == null) {
                    strategies = new StrategiesCollection();
                }
            }
        }
    }

    protected synchronized void initializeTradesCollections() {
        if (trades == null) {
            if (Activator.getDefault() != null) {
                File file = Activator.getDefault().getStateLocation().append(TRADES_FILE).toFile();
                trades = (TradeCollection) unmarshal(TradeCollection.class, file);
            }
            if (trades == null) {
                trades = TradeCollection.getInstance();
                if (trades == null) {
                    trades = new TradeCollection();
                }
            }
        }
    }

    public void shutDown() {
        if (watchlists != null) {
            File file = location.append(WATCHLISTS_FILE).toFile();
            marshal(watchlists, WatchListCollection.class, file);
        }

        File file = location.append(SECURITIES_FILE).toFile();
        marshal(securities, SecurityCollection.class, file);

        file = location.append(IDENTIFIERS_FILE).toFile();
        marshal(identifiers, IdentifiersCollection.class, file);

        file = location.append(SCRIPTS_FILE).toFile();
        marshal(scripts, ScriptsCollection.class, file);

        if (strategies != null) {
            file = location.append(STRATEGIES_FILE).toFile();
            marshal(strategies, StrategiesCollection.class, file);
        }

        if (trades != null) {
            file = location.append(TRADES_FILE).toFile();
            marshal(trades, TradeCollection.class, file);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#canDelete()
     */
    @Override
    public boolean canDelete() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#canWrite()
     */
    @Override
    public boolean canWrite() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#createObject()
     */
    @Override
    public IStore createObject() {
        return new RepositoryStore();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#fetchObjects(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStore[] fetchObjects(IProgressMonitor monitor) {
        List<IStore> list = new ArrayList<IStore>();

        list.addAll(securities.getList());
        list.addAll(scripts.getList());

        if (strategies == null) {
            initializeStrategiesListsCollections();
        }
        list.addAll(strategies.getList());

        if (watchlists == null) {
            initializeWatchListsCollections();
        }
        list.addAll(watchlists.getList());

        if (trades == null) {
            initializeTradesCollections();
        }
        list.addAll(trades.getList());

        return list.toArray(new IStore[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#getObject(java.net.URI)
     */
    @Override
    public IStore getObject(URI uri) {
        if (URI_SECURITY_PART.equals(uri.getSchemeSpecificPart())) {
            return securities.get(uri);
        }

        if (URI_WATCHLIST_PART.equals(uri.getSchemeSpecificPart())) {
            if (watchlists == null) {
                initializeWatchListsCollections();
            }
            return watchlists.get(uri);
        }

        if (URI_TRADE_PART.equals(uri.getSchemeSpecificPart())) {
            if (trades == null) {
                initializeTradesCollections();
            }
            return trades.get(uri);
        }

        if (URI_SCRIPT_PART.equals(uri.getSchemeSpecificPart())) {
            return scripts.get(uri);
        }

        if (URI_STRATEGY_PART.equals(uri.getSchemeSpecificPart())) {
            if (strategies == null) {
                initializeStrategiesListsCollections();
            }
            return strategies.get(uri);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInRepository(IRepositoryRunnable runnable, IProgressMonitor monitor) {
        return runInRepository(runnable, this, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
        IStatus status;
        jobManager.beginRule(rule, monitor);
        try {
            lock.acquire();
            try {
                status = runnable.run(monitor);
            } catch (Exception e) {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            } catch (LinkageError e) {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            }
        } catch (Exception e) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        } finally {
            lock.release();
            jobManager.endRule(rule);
        }
        return status;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    @Override
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
    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        if (this == rule) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    protected void marshal(Object object, Class clazz, File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                    return true;
                }
            });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.marshal(object, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error saving securities", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
    }

    @SuppressWarnings("rawtypes")
    protected Object unmarshal(Class clazz, File file) {
        try {
            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        Activator.getDefault().getLog().log(status);
                        return true;
                    }
                });
                return unmarshaller.unmarshal(file);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading identifiers", null); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#getSchema()
     */
    @Override
    public String getSchema() {
        return URI_SCHEMA;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * URI_SCHEMA.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Local";
    }
}
