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

package org.eclipsetrader.core.ats.simulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;

public class Broker implements IBroker {

    private final String id;
    private final IPricingEnvironment pricingEnvironment;

    private List<OrderMonitor> orders = new ArrayList<OrderMonitor>();
    private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private final Log log = LogFactory.getLog(getClass());

    private final IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            for (PricingDelta delta : event.getDelta()) {
                if (delta.getNewValue() instanceof ITrade) {
                    processTrade(event.getSecurity(), (ITrade) delta.getNewValue());
                }
            }
        }
    };

    public Broker(IPricingEnvironment pricingEnvironment) {
        this.id = UUID.randomUUID().toString();
        this.pricingEnvironment = pricingEnvironment;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getName()
     */
    @Override
    public String getName() {
        return "Simulation";
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#connect()
     */
    @Override
    public void connect() {
        pricingEnvironment.addPricingListener(pricingListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#disconnect()
     */
    @Override
    public void disconnect() {
        pricingEnvironment.removePricingListener(pricingListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader.core.trading.IOrder)
     */
    @Override
    public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
        if (order.getAccount() != null && !(order.getAccount() instanceof Account)) {
            throw new BrokerException("Invalid account");
        }

        OrderMonitor monitor = new OrderMonitor(this, order) {

            @Override
            public void cancel() throws BrokerException {
                setStatus(IOrderStatus.Canceled);

                if (log.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder("Order Cancelled:");
                    sb.append(" instrument=" + getOrder().getSecurity().getName());
                    sb.append(", type=" + getOrder().getType());
                    sb.append(", side=" + getOrder().getSide());
                    sb.append(", qty=" + getOrder().getQuantity());
                    if (getOrder().getPrice() != null) {
                        sb.append(", price=" + getOrder().getPrice());
                    }
                    if (getOrder().getReference() != null) {
                        sb.append(", reference=" + getOrder().getReference());
                    }
                    log.info(sb.toString());
                }

                fireUpdateNotifications(new OrderDelta[] {
                    new OrderDelta(OrderDelta.KIND_UPDATED, this),
                });
            }

            @Override
            public void submit() throws BrokerException {
                synchronized (orders) {
                    orders.add(this);
                }

                setId(UUID.randomUUID().toString());
                setStatus(IOrderStatus.PendingNew);

                if (log.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder("Order Submitted:");
                    sb.append(" instrument=" + getOrder().getSecurity().getName());
                    sb.append(", type=" + getOrder().getType());
                    sb.append(", side=" + getOrder().getSide());
                    sb.append(", qty=" + getOrder().getQuantity());
                    if (getOrder().getPrice() != null) {
                        sb.append(", price=" + getOrder().getPrice());
                    }
                    if (getOrder().getReference() != null) {
                        sb.append(", reference=" + getOrder().getReference());
                    }
                    log.info(sb.toString());
                }

                fireUpdateNotifications(new OrderDelta[] {
                    new OrderDelta(OrderDelta.KIND_UPDATED, this),
                });
            }
        };

        return monitor;
    }

    protected void fireUpdateNotifications(OrderDelta[] deltas) {
        if (deltas.length != 0) {
            OrderChangeEvent event = new OrderChangeEvent(this, deltas);
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
     * @see org.eclipsetrader.core.trading.IBroker#getOrders()
     */
    @Override
    public IOrderMonitor[] getOrders() {
        synchronized (orders) {
            return orders.toArray(new IOrderMonitor[orders.size()]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
     */
    @Override
    public IOrderType[] getAllowedTypes() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
     */
    @Override
    public IOrderSide[] getAllowedSides() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
     */
    @Override
    public IOrderValidity[] getAllowedValidity() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
     */
    @Override
    public IOrderRoute[] getAllowedRoutes() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean canTrade(ISecurity security) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang.String)
     */
    @Override
    public ISecurity getSecurityFromSymbol(String symbol) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public String getSymbolFromSecurity(ISecurity security) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void removeOrderChangeListener(IOrderChangeListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
     */
    @Override
    public IAccount[] getAccounts() {
        return null;
    }

    protected void processTrade(ISecurity security, ITrade trade) {
        List<OrderDelta> deltas = new ArrayList<OrderDelta>();

        OrderMonitor[] monitors;
        synchronized (orders) {
            monitors = orders.toArray(new OrderMonitor[orders.size()]);
        }
        for (int i = 0; i < monitors.length; i++) {
            if (monitors[i].getStatus() != IOrderStatus.PendingNew && monitors[i].getStatus() != IOrderStatus.Partial) {
                continue;
            }
            IOrder order = monitors[i].getOrder();
            if (order.getSecurity() == security) {
                if (order.getType() == IOrderType.Market) {
                    fillOrder(monitors[i], trade.getTime(), trade.getPrice());
                    deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                }
                else if (order.getType() == IOrderType.Limit) {
                    if (order.getSide() == IOrderSide.Buy && trade.getPrice() <= order.getPrice()) {
                        fillOrder(monitors[i], trade.getTime(), trade.getPrice());
                        deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                    }
                    else if (order.getSide() == IOrderSide.Sell && trade.getPrice() >= order.getPrice()) {
                        fillOrder(monitors[i], trade.getTime(), trade.getPrice());
                        deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                    }
                }
            }
        }

        if (deltas.size() != 0) {
            fireUpdateNotifications(deltas.toArray(new OrderDelta[deltas.size()]));
        }
    }

    protected void fillOrder(OrderMonitor monitor, Date date, Double price) {
        ((Order) monitor.getOrder()).setDate(date);

        monitor.setFilledQuantity(monitor.getOrder().getQuantity());
        monitor.setAveragePrice(price);
        monitor.setStatus(IOrderStatus.Filled);

        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("Order Filled:");
            sb.append(" instrument=" + monitor.getOrder().getSecurity().getName());
            sb.append(", type=" + monitor.getOrder().getType());
            sb.append(", side=" + monitor.getOrder().getSide());
            sb.append(", qty=" + monitor.getOrder().getQuantity());
            if (monitor.getOrder().getPrice() != null) {
                sb.append(", price=" + monitor.getOrder().getPrice());
            }
            sb.append(", fillQty=" + monitor.getFilledQuantity());
            sb.append(", avgPrice=" + monitor.getAveragePrice());
            if (monitor.getOrder().getReference() != null) {
                sb.append(", reference=" + monitor.getOrder().getReference());
            }
            log.info(sb.toString());
        }

        monitor.fireOrderCompletedEvent();

        Account account = (Account) monitor.getOrder().getAccount();
        if (account != null) {
            account.processCompletedOrder(monitor);
        }
    }
}
