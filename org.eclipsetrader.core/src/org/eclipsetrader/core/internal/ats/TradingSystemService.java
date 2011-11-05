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

package org.eclipsetrader.core.internal.ats;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemListener;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.core.ats.TradingSystemEvent;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.ats.repository.SettingsCollection;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;

public class TradingSystemService implements ITradingSystemService {

    private final IRepositoryService repositoryService;
    private final IMarketService marketService;

    private final List<TradingSystem> list = new ArrayList<TradingSystem>();

    private SettingsCollection collection;
    private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private final IRepositoryChangeListener repositoryChangeListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            RepositoryResourceDelta[] delta = event.getDeltas();
            for (int i = 0; i < delta.length; i++) {
                if (delta[i].getResource() instanceof IStrategy) {
                    IStrategy strategy = (IStrategy) delta[i].getResource();
                    if ((delta[i].getKind() & RepositoryResourceDelta.ADDED) != 0) {
                        TradingSystem tradingSystem = new TradingSystem(strategy);
                        TradingSystemProperties properties = collection.getSettingsFor(strategy);
                        if (properties != null) {
                            tradingSystem.setProperties(properties);
                        }
                        list.add(tradingSystem);
                        fireTradingSystemEvent(new TradingSystemEvent(TradingSystemEvent.KIND_ADDED, tradingSystem));
                    }
                    else if ((delta[i].getKind() & RepositoryResourceDelta.REMOVED) != 0) {
                        for (TradingSystem tradingSystem : list) {
                            if (tradingSystem.getStrategy() == strategy) {
                                tradingSystem.stop();
                                list.remove(tradingSystem);
                                fireTradingSystemEvent(new TradingSystemEvent(TradingSystemEvent.KIND_REMOVED, tradingSystem));
                                break;
                            }
                        }
                    }
                }
            }
        }
    };

    public TradingSystemService(IRepositoryService repositoryService, IMarketService marketService) {
        this.repositoryService = repositoryService;
        this.marketService = marketService;
    }

    public void startUp() {
        for (IStoreObject object : repositoryService.getAllObjects()) {
            if (object instanceof IStrategy) {
                list.add(new TradingSystem((IStrategy) object));
            }
        }
        loadSettings(CoreActivator.getDefault().getStateLocation().append("trade_systems.xml").toFile());
        repositoryService.addRepositoryResourceListener(repositoryChangeListener);
    }

    public void shutDown() {
        repositoryService.removeRepositoryResourceListener(repositoryChangeListener);
        for (TradingSystem system : list) {
            try {
                system.stop();
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error stopping trading system", t);
                CoreActivator.log(status);
            }
        }
        saveSettings(CoreActivator.getDefault().getStateLocation().append("trade_systems.xml").toFile());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#getTradeSystems()
     */
    @Override
    public ITradingSystem[] getTradeSystems() {
        return list.toArray(new ITradingSystem[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#start()
     */
    @Override
    public void start() {
        for (TradingSystem system : list) {
            TradingSystemProperties properties = system.getProperties();
            if (properties.isAutostart()) {
                start(system);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#stop()
     */
    @Override
    public void stop() {
        for (TradingSystem system : list) {
            stop(system);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#start(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public void start(final ITradingSystem system) {
        ((TradingSystem) system).setStatus(ITradingSystem.STATUS_STARTING);
        Job job = new Job("Starting " + system.getStrategy().getName()) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                TradingSystemProperties properties = ((TradingSystem) system).getProperties();

                TradingSystemContext context = new TradingSystemContext(marketService, system.getStrategy(), properties.getBroker(), properties.getAccount());
                context.setInitialBackfillSize(properties.getBackfill());
                try {
                    system.start(context);
                    ((TradingSystem) system).setStatus(ITradingSystem.STATUS_STARTED);
                } catch (Exception e) {
                    ((TradingSystem) system).setStatus(ITradingSystem.STATUS_STOPPED);
                    return new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error starting trade system", e);
                }

                return Status.OK_STATUS;
            }

        };
        job.setUser(false);
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#stop(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public void stop(final ITradingSystem system) {
        ((TradingSystem) system).setStatus(ITradingSystem.STATUS_STOPPING);
        Job job = new Job("Stopping " + system.getStrategy().getName()) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    system.stop();
                } catch (Exception e) {
                    return new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error starting trade system", e);
                } finally {
                    ((TradingSystem) system).setStatus(ITradingSystem.STATUS_STOPPED);
                }

                return Status.OK_STATUS;
            }

        };
        job.setUser(false);
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#addTradingSystemListener(org.eclipsetrader.core.ats.ITradingSystemListener)
     */
    @Override
    public void addTradingSystemListener(ITradingSystemListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemService#removeTradingSystemListener(org.eclipsetrader.core.ats.ITradingSystemListener)
     */
    @Override
    public void removeTradingSystemListener(ITradingSystemListener listener) {
        listeners.remove(listener);
    }

    protected void fireTradingSystemEvent(TradingSystemEvent event) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((ITradingSystemListener) l[i]).tradingSystemChanged(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error notifying listeners", t); //$NON-NLS-1$
                CoreActivator.log(status);
            }
        }
    }

    private void loadSettings(File file) {
        if (file.exists() == true) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(SettingsCollection.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        CoreActivator.log(status);
                        return true;
                    }
                });
                collection = (SettingsCollection) unmarshaller.unmarshal(file);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error loading repository", e); //$NON-NLS-1$
                CoreActivator.log(status);
            }
        }

        if (collection == null) {
            collection = new SettingsCollection();
        }

        for (TradingSystem system : list) {
            TradingSystemProperties properties = collection.getSettingsFor(system.getStrategy());
            if (properties != null) {
                system.setProperties(properties);
            }
        }
    }

    private void saveSettings(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }

            for (TradingSystem system : list) {
                collection.setSettingsFor(system.getStrategy(), system.getProperties());
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(SettingsCollection.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    CoreActivator.log(status);
                    return true;
                }
            });
            marshaller.marshal(collection, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error saving repository", e); //$NON-NLS-1$
            CoreActivator.log(status);
        }
    }
}
