/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.trading;

import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;

public class OrderMonitor implements IOrderMonitor, IAdaptable {
	private IOrder order;

	private IBrokerConnector brokerConnector;
	private String id;

	private Long filledQuantity;
	private Double averagePrice;
	private OrderStatus status = OrderStatus.New;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	public OrderMonitor(IBrokerConnector brokerConnector, IOrder order) {
	    this.brokerConnector = brokerConnector;
		this.order = order;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#getBrokerConnector()
     */
    public IBrokerConnector getBrokerConnector() {
	    return brokerConnector;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#getId()
	 */
	public String getId() {
		return id;
	}

	public void setId(String assignedId) {
    	this.id = assignedId;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#getOrder()
	 */
	public IOrder getOrder() {
		return order;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#submit()
	 */
	public void submit() throws BrokerException {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#cancel()
	 */
	public void cancel() throws BrokerException {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#allowModify()
	 */
	public boolean allowModify() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#modify(org.eclipsetrader.core.trading.IOrder)
	 */
	public void modify(IOrder order) throws BrokerException {
		throw new BrokerException("Modify not allowed");
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#addOrderMonitorListener(org.eclipsetrader.core.trading.IOrderMonitorListener)
     */
    public void addOrderMonitorListener(IOrderMonitorListener listener) {
    	listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrderMonitor#removeOrderMonitorListener(org.eclipsetrader.core.trading.IOrderMonitorListener)
     */
    public void removeOrderMonitorListener(IOrderMonitorListener listener) {
    	listeners.remove(listener);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#getStatus()
	 */
	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
    	OrderStatus oldValue = this.status;
    	if (this.status != status) {
    		this.status = status;
    		propertyChangeSupport.firePropertyChange(PROP_STATUS, oldValue, this.status);
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#getFilledQuantity()
	 */
	public Long getFilledQuantity() {
		return filledQuantity;
	}

	public void setFilledQuantity(Long filledQuantity) {
    	Long oldValue = this.filledQuantity;
    	if (filledQuantity !=null && !filledQuantity.equals(this.filledQuantity)) {
    		this.filledQuantity = filledQuantity;
    		propertyChangeSupport.firePropertyChange(PROP_FILLED_QUANTITY, oldValue, this.filledQuantity);
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrderMonitor#getAveragePrice()
	 */
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
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(propertyChangeSupport.getClass()))
    		return propertyChangeSupport;
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

    protected void fireOrderCompletedEvent() {
    	OrderMonitorEvent event = new OrderMonitorEvent(this, order);

    	Object[] l = listeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
    		try {
    			((IOrderMonitorListener) l[i]).orderCompleted(event);
    		} catch(Throwable e) {
    			e.printStackTrace();
    		}
    	}
    }

	public PropertyChangeSupport getPropertyChangeSupport() {
    	return propertyChangeSupport;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "OrderMonitor: id=" + getId()
			+ ", status=" + getStatus()
			+ ", filledQuantity=" + getFilledQuantity()
			+ ", averagePrice=" + getAveragePrice()
			+ " [" + order.toString() + "]";
    }
}
