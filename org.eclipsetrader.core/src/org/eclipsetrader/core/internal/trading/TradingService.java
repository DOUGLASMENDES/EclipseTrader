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

package org.eclipsetrader.core.internal.trading;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradingService implements ITradingService {

    private Map<String, IBroker> brokers = new HashMap<String, IBroker>();
    private Set<IOrderMonitor> orders = new HashSet<IOrderMonitor>();

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private ListenerList positionListeners = new ListenerList(ListenerList.IDENTITY);

    private IOrderChangeListener orderChangeListener = new IOrderChangeListener() {

        @Override
        public void orderChanged(OrderChangeEvent event) {
            processOrderChangedEvent(event);
            fireUpdateNotifications(event.broker, event.deltas);
        }
    };

    public TradingService() {
    }

    public void startUp() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CoreActivator.BROKERS_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                try {
                    IBroker connector = (IBroker) configElements[j].createExecutableExtension("class");
                    brokers.put(connector.getId(), connector);
                } catch (CoreException e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error creating broker instance with id " + configElements[j].getAttribute("id"), e);
                    CoreActivator.log(status);
                }
            }
        }

        for (IBroker connector : brokers.values()) {
            IOrderMonitor[] o = connector.getOrders();
            if (o != null) {
                orders.addAll(Arrays.asList(o));
            }
            connector.addOrderChangeListener(orderChangeListener);
        }
    }

    public void shutDown() {
        for (IBroker connector : brokers.values()) {
            connector.removeOrderChangeListener(orderChangeListener);
            connector.disconnect();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#getBrokers()
     */
    @Override
    public IBroker[] getBrokers() {
        Collection<IBroker> c = brokers.values();
        return c.toArray(new IBroker[c.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#getBroker(java.lang.String)
     */
    @Override
    public IBroker getBroker(String id) {
        return brokers.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#getBrokerForSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IBroker getBrokerForSecurity(ISecurity security) {
        IBroker broker = null;

        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
            if (serviceReference != null) {
                IMarketService service = (IMarketService) context.getService(serviceReference);

                IMarket market = service.getMarketForSecurity(security);
                if (market != null) {
                    broker = (IBroker) market.getAdapter(IBroker.class);
                }

                context.ungetService(serviceReference);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error reading market service", e);
            CoreActivator.getDefault().getLog().log(status);
        }

        if (broker == null) {
            for (IBroker connector : brokers.values()) {
                if (connector.canTrade(security)) {
                    return connector;
                }
            }
        }

        return broker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#getOrders()
     */
    @Override
    public IOrderMonitor[] getOrders() {
        return orders.toArray(new IOrderMonitor[orders.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void removeOrderChangeListener(IOrderChangeListener listener) {
        listeners.remove(listener);
    }

    protected void processOrderChangedEvent(OrderChangeEvent event) {
        for (OrderDelta delta : event.deltas) {
            if (delta.getKind() == OrderDelta.KIND_ADDED) {
                orders.add(delta.getOrder());
            }
            else if (delta.getKind() == OrderDelta.KIND_REMOVED) {
                orders.remove(delta.getOrder());
            }
            else {
                if (!orders.contains(delta.getOrder())) {
                    orders.add(delta.getOrder());
                }
            }
        }
    }

    protected void fireUpdateNotifications(IBroker broker, OrderDelta[] deltas) {
        if (deltas.length != 0) {
            OrderChangeEvent event = new OrderChangeEvent(broker, deltas);
            Object[] l = listeners.getListeners();
            for (int i = 0; i < l.length; i++) {
                try {
                    ((IOrderChangeListener) l[i]).orderChanged(event);
                } catch (Throwable e) {
                    Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error running listener", e); //$NON-NLS-1$
                    CoreActivator.log(status);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void addPositionListener(IPositionListener listener) {
        positionListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITradingService#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void removePositionListener(IPositionListener listener) {
        positionListeners.remove(listener);
    }
}
