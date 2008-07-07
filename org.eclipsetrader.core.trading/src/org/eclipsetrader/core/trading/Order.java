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
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Default implementation of the <code>IOrder</code> interface.
 *
 * <p>Clients that needs to be notified of property changes can
 * request an adapter to <code>PropertyChangeSupport</code> class.</p>
 *
 * @since 1.0
 */
public class Order implements IOrder, IAdaptable {
	private String id;

	private Date date;
	private IBrokerConnector broker;
	private IOrderRoute route;
	private IAccount account;

	private ISecurity security;
	private Long quantity;
	private Double price;
	private Double stopPrice;

	private OrderType type;
	private OrderSide side;
	private OrderValidity validity;
	private Date expireDate;

	private Long filledQuantity;
	private Double averagePrice;
	private OrderStatus status = OrderStatus.New;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	protected Order() {
	}

	public Order(IBrokerConnector broker, IAccount account, OrderType type, OrderSide side, ISecurity security, Long quantity, Double price) {
	    this.broker = broker;
	    this.account = account;
	    this.type = type;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.price = price;
	    this.date = new Date();
    }

	public Order(IBrokerConnector broker, IAccount account, OrderType type, OrderSide side, ISecurity security, Long quantity, Double price, IOrderRoute route) {
	    this.broker = broker;
	    this.account = account;
	    this.type = type;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.price = price;
	    this.route = route;
	    this.date = new Date();
    }

	public Order(IBrokerConnector broker, IAccount account, OrderSide side, ISecurity security, Long quantity) {
	    this.broker = broker;
	    this.account = account;
	    this.type = OrderType.Market;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.date = new Date();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getId()
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
    	Object oldValue = this.id;
		if (id != null && !id.equals(this.id)) {
	    	this.id = id;
    		propertyChangeSupport.firePropertyChange(PROP_ID, oldValue, this.id);
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getDate()
	 */
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
    	Object oldValue = this.date;
		if (date != null && !date.equals(this.date)) {
	    	this.date = date;
    		propertyChangeSupport.firePropertyChange("date", oldValue, this.date);
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getBroker()
	 */
	public IBrokerConnector getBroker() {
		return broker;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getRoute()
     */
    public IOrderRoute getRoute() {
	    return route;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getAccount()
	 */
	public IAccount getAccount() {
		return account;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getSecurity()
	 */
	public ISecurity getSecurity() {
		return security;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getQuantity()
	 */
	public Long getQuantity() {
		return quantity;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getPrice()
	 */
	public Double getPrice() {
		return price;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getSide()
	 */
	public OrderSide getSide() {
		return side;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getType()
	 */
	public OrderType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getStopPrice()
	 */
	public Double getStopPrice() {
		return stopPrice;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getValidity()
	 */
	public OrderValidity getValidity() {
		return validity;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getExpire()
	 */
	public Date getExpire() {
		return expireDate;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#submit()
     */
    public void submit() throws BrokerException {
    	broker.submitOrder(this);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#cancel()
     */
    public void cancel() throws BrokerException {
    	broker.cancelOrder(this);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#modify()
     */
    public void modify() throws BrokerException {
    	broker.modifyOrder(this);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getStatus()
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
	 * @see org.eclipsetrader.core.trading.IOrder#getFilledQuantity()
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
	 * @see org.eclipsetrader.core.trading.IOrder#getAveragePrice()
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

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "Order: id=" + getId()
			+ ", date=" + getDate()
			+ ", instrument=" + getSecurity().getName()
    	    + ", type=" + getType()
			+ ", side=" + getSide()
			+ ", quantity=" + getQuantity()
			+ ", price=" + getPrice()
			+ ", stopPrice=" + getStopPrice()
			+ ", timeInForce=" + getValidity()
			+ ", expiration=" + getExpire()
			+ ", status=" + getStatus()
			+ ", filledQuantity=" + getFilledQuantity()
			+ ", averagePrice=" + getAveragePrice();
    }
}
