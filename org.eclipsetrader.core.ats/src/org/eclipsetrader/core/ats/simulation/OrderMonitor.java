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

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderMonitorListener;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.OrderMonitorEvent;

public class OrderMonitor implements IOrderMonitor {

    private String id;
    private final Broker broker;
    private final IOrder order;

    private IOrderStatus status;
    private Long filledQuantity;
    private Double averagePrice;

    private Transaction transaction;

    private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public OrderMonitor(Broker broker, IOrder order) {
        this.broker = broker;
        this.order = order;
        this.status = IOrderStatus.New;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getOrder()
     */
    @Override
    public IOrder getOrder() {
        return order;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getBrokerConnector()
     */
    @Override
    public IBroker getBrokerConnector() {
        return broker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#addOrderMonitorListener(org.eclipsetrader.core.trading.IOrderMonitorListener)
     */
    @Override
    public void addOrderMonitorListener(IOrderMonitorListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#removeOrderMonitorListener(org.eclipsetrader.core.trading.IOrderMonitorListener)
     */
    @Override
    public void removeOrderMonitorListener(IOrderMonitorListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#submit()
     */
    @Override
    public void submit() throws BrokerException {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#cancel()
     */
    @Override
    public void cancel() throws BrokerException {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#allowModify()
     */
    @Override
    public boolean allowModify() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#modify(org.eclipsetrader.core.trading.IOrder)
     */
    @Override
    public void modify(IOrder order) throws BrokerException {
        throw new UnsupportedOperationException("Modify not allowed");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getStatus()
     */
    @Override
    public IOrderStatus getStatus() {
        return status;
    }

    public void setStatus(IOrderStatus status) {
        this.status = status;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getFilledQuantity()
     */
    @Override
    public Long getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Long filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getAveragePrice()
     */
    @Override
    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getMessage()
     */
    @Override
    public String getMessage() {
        return null;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    protected void fireOrderCompletedEvent() {
        OrderMonitorEvent event = new OrderMonitorEvent(this, order);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IOrderMonitorListener) l[i]).orderCompleted(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OrderMonitor: status=" + getStatus() + ", filledQuantity=" + getFilledQuantity() + ", averagePrice=" + getAveragePrice() + " [" + order.toString() + "]";
    }
}
