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

package org.eclipsetrader.directa.internal.core;

import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderMonitorListener;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.OrderMonitorEvent;

public class OrderMonitor implements IOrderMonitor, IAdaptable {

    private IOrder order;

    private BrokerConnector brokerConnector;
    private String id;

    private Long filledQuantity;
    private Double averagePrice;
    private IOrderStatus status = IOrderStatus.New;
    private String message;

    private WebConnector connector;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public OrderMonitor(WebConnector connector, BrokerConnector brokerConnector, IOrder order) {
        this.connector = connector;
        this.brokerConnector = brokerConnector;
        this.order = order;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getBrokerConnector()
     */
    @Override
    public IBroker getBrokerConnector() {
        return brokerConnector;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    public void setId(String assignedId) {
        this.id = assignedId;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getOrder()
     */
    @Override
    public IOrder getOrder() {
        return order;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#submit()
     */
    @Override
    public void submit() throws BrokerException {
        if (!connector.isLoggedIn()) {
            connector.login();
            if (!connector.isLoggedIn()) {
                throw new BrokerException(Messages.OrderMonitor_UnableToLogin);
            }
        }

        if (connector.sendOrder(this)) {
            brokerConnector.addWithNotification(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#cancel()
     */
    @Override
    public void cancel() throws BrokerException {
        if (!connector.isLoggedIn()) {
            connector.login();
            if (!connector.isLoggedIn()) {
                throw new BrokerException(Messages.OrderMonitor_UnableToLogin);
            }
        }

        if (getId() == null) {
            throw new BrokerException(Messages.OrderMonitor_InvalidOrder);
        }

        connector.cancelOrder(this);
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
        throw new BrokerException(Messages.OrderMonitor_ModifyNotAllowed);
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
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getStatus()
     */
    @Override
    public IOrderStatus getStatus() {
        return status;
    }

    public void setStatus(IOrderStatus status) {
        IOrderStatus oldValue = this.status;
        if (this.status != status) {
            this.status = status;
            propertyChangeSupport.firePropertyChange(PROP_STATUS, oldValue, this.status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getFilledQuantity()
     */
    @Override
    public Long getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Long filledQuantity) {
        Long oldValue = this.filledQuantity;
        if (filledQuantity != null && !filledQuantity.equals(this.filledQuantity)) {
            this.filledQuantity = filledQuantity;
            propertyChangeSupport.firePropertyChange(PROP_FILLED_QUANTITY, oldValue, this.filledQuantity);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getAveragePrice()
     */
    @Override
    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        Double oldValue = this.averagePrice;
        if (averagePrice != null && !averagePrice.equals(this.averagePrice)) {
            this.averagePrice = averagePrice;
            propertyChangeSupport.firePropertyChange(PROP_AVERAGE_PRICE, oldValue, this.averagePrice);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(propertyChangeSupport.getClass())) {
            return propertyChangeSupport;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
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

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderMonitor)) {
            return false;
        }
        return id != null && id.equals(((OrderMonitor) obj).id);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OrderMonitor: id=" + getId() + ", status=" + getStatus() + ", filledQuantity=" + getFilledQuantity() + ", averagePrice=" + getAveragePrice() + " [" + order.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }
}
