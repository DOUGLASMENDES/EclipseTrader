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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.ats.ITradeStrategy;
import org.eclipsetrader.core.ats.ITradeSystem;
import org.eclipsetrader.core.ats.ITradeSystemContext;
import org.eclipsetrader.core.ats.ITradeSystemListener;
import org.eclipsetrader.core.ats.ITradeSystemMonitor;
import org.eclipsetrader.core.ats.ITradeSystemService;
import org.eclipsetrader.core.ats.TradeSystemEvent;
import org.eclipsetrader.core.internal.ats.repository.TradeSystemRepository;
import org.eclipsetrader.core.internal.trading.Activator;

public class TradeSystemService implements ITradeSystemService, IAdapterFactory {

    private Map<ITradeSystem, TradeSystemContext> tradeSystems = new HashMap<ITradeSystem, TradeSystemContext>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private TradeSystemRepository repository;

    public TradeSystemService() {
    }

    public void startUp() {
        loadRepository(Activator.getDefault().getStateLocation().append("trade_systems.xml").toFile());
    }

    public void shutDown() {
        saveRepository(Activator.getDefault().getStateLocation().append("trade_systems.xml").toFile());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#getStrategy(java.lang.String)
     */
    @Override
    public ITradeStrategy getStrategy(String id) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.STRATEGIES_EXTENSION_ID);
        if (extensionPoint == null) {
            return null;
        }

        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        for (int j = 0; j < configElements.length; j++) {
            if (id.equals(configElements[j].getAttribute("id"))) { //$NON-NLS-1$
                try {
                    ITradeStrategy strategy = (ITradeStrategy) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
                    return strategy;
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Unable to create strategy with id " + id, e);
                    Activator.log(status);
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#getTradeSystems()
     */
    @Override
    public ITradeSystem[] getTradeSystems() {
        Set<ITradeSystem> s = tradeSystems.keySet();
        return s.toArray(new ITradeSystem[s.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#addTradeSystem(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public void addTradeSystem(ITradeSystem system) {
        if (!tradeSystems.containsKey(system)) {
            tradeSystems.put(system, null);
            if (system instanceof TradeSystem) {
                repository.add((TradeSystem) system);
            }

            TradeSystemEvent event = new TradeSystemEvent();
            event.kind = TradeSystemEvent.KIND_ADDED;
            event.service = this;
            event.tradeSystem = system;
            notifyListeners(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#removeTradeSystem(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public void removeTradeSystem(ITradeSystem system) {
        if (tradeSystems.containsKey(system)) {
            TradeSystemEvent event = new TradeSystemEvent();
            event.kind = TradeSystemEvent.KIND_REMOVED;
            event.service = this;
            event.tradeSystem = system;
            event.tradeSystemContext = tradeSystems.get(system);

            tradeSystems.remove(system);
            if (system instanceof TradeSystem) {
                repository.remove((TradeSystem) system);
            }

            notifyListeners(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#start(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public ITradeSystemMonitor start(ITradeSystem system) {
        if (!tradeSystems.containsKey(system)) {
            addTradeSystem(system);
        }

        TradeSystemContext context = new TradeSystemContext(this, system);
        tradeSystems.put(system, context);

        TradeSystemEvent event = new TradeSystemEvent();
        event.kind = TradeSystemEvent.KIND_STARTED;
        event.service = this;
        event.tradeSystem = system;
        event.tradeSystemContext = context;
        notifyListeners(event);

        return context.start();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#stop(org.eclipsetrader.core.ats.ITradeSystem)
     */
    @Override
    public void stop(ITradeSystem system) {
        TradeSystemContext context = tradeSystems.get(system);
        if (context != null) {
            context.stop();
            tradeSystems.put(system, null);

            TradeSystemEvent event = new TradeSystemEvent();
            event.kind = TradeSystemEvent.KIND_STOPPED;
            event.service = this;
            event.tradeSystem = system;
            event.tradeSystemContext = context;
            notifyListeners(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof ITradeSystem && adapterType.isAssignableFrom(ITradeSystemContext.class)) {
            return tradeSystems.get(adaptableObject);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] {
            ITradeSystemContext.class,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#addTradeSystemListener(org.eclipsetrader.core.ats.ITradeSystemListener)
     */
    @Override
    public void addTradeSystemListener(ITradeSystemListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemService#removeTradeSystemListener(org.eclipsetrader.core.ats.ITradeSystemListener)
     */
    @Override
    public void removeTradeSystemListener(ITradeSystemListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(TradeSystemEvent event) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((ITradeSystemListener) l[i]).tradeSystemServiceUpdate(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", t); //$NON-NLS-1$
                Activator.log(status);
            }
        }
    }

    private void loadRepository(File file) {
        if (file.exists() == true) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(TradeSystemRepository.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setEventHandler(new ValidationEventHandler() {

                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                        Activator.log(status);
                        return true;
                    }
                });
                repository = (TradeSystemRepository) unmarshaller.unmarshal(file);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error loading repository", e); //$NON-NLS-1$
                Activator.log(status);
            }
        }

        // Fail safe, create an empty repository
        if (repository == null) {
            repository = new TradeSystemRepository();
        }
    }

    private void saveRepository(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(TradeSystemRepository.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
            marshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.log(status);
                    return true;
                }
            });
            marshaller.marshal(repository, new FileWriter(file));
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error saving repository", e); //$NON-NLS-1$
            Activator.log(status);
        }
    }
}
