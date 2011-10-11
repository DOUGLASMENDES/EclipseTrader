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

package org.eclipsetrader.core.trading;

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Default implementation of the <code>IOrder</code> interface.
 *
 * <p>Clients that needs to be notified of property changes can
 * request an adapter to <code>PropertyChangeSupport</code> class.</p>
 *
 * @since 1.0
 */
public class Order implements IOrder {

    private Date date;
    private IOrderRoute route;
    private IAccount account;

    private ISecurity security;
    private Long quantity;
    private Double price;
    private Double stopPrice;

    private IOrderType type;
    private IOrderSide side;
    private IOrderValidity validity;
    private Date expireDate;

    private String reference;

    protected Order() {
    }

    public Order(IAccount account, IOrderType type, IOrderSide side, ISecurity security, Long quantity, Double price) {
        this.account = account;
        this.type = type;
        this.side = side;
        this.security = security;
        this.quantity = quantity;
        this.price = price;
        this.date = new Date();
    }

    public Order(IAccount account, IOrderType type, IOrderSide side, ISecurity security, Long quantity, Double price, IOrderRoute route) {
        this.account = account;
        this.type = type;
        this.side = side;
        this.security = security;
        this.quantity = quantity;
        this.price = price;
        this.route = route;
        this.date = new Date();
    }

    public Order(IAccount account, IOrderSide side, ISecurity security, Long quantity) {
        this.account = account;
        this.type = IOrderType.Market;
        this.side = side;
        this.security = security;
        this.quantity = quantity;
        this.date = new Date();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getRoute()
     */
    @Override
    public IOrderRoute getRoute() {
        return route;
    }

    public void setRoute(IOrderRoute route) {
        this.route = route;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getAccount()
     */
    @Override
    public IAccount getAccount() {
        return account;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getQuantity()
     */
    @Override
    public Long getQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getPrice()
     */
    @Override
    public Double getPrice() {
        return price;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getSide()
     */
    @Override
    public IOrderSide getSide() {
        return side;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getType()
     */
    @Override
    public IOrderType getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getStopPrice()
     */
    @Override
    public Double getStopPrice() {
        return stopPrice;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getValidity()
     */
    @Override
    public IOrderValidity getValidity() {
        return validity;
    }

    public void setValidity(IOrderValidity validity) {
        this.validity = validity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getExpire()
     */
    @Override
    public Date getExpire() {
        return expireDate;
    }

    public void setExpire(Date expireDate) {
        this.expireDate = expireDate;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getReference()
     */
    @Override
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Order: date=" + getDate() + ", instrument=" + getSecurity().getName() + ", type=" + getType() + ", side=" + getSide() + ", quantity=" + getQuantity());
        if (getPrice() != null) {
            sb.append(", price=" + getPrice());
        }
        if (getStopPrice() != null) {
            sb.append(", stopPrice=" + getStopPrice());
        }
        if (getValidity() != null) {
            sb.append(", timeInForce=" + getValidity());
        }
        if (getExpire() != null) {
            sb.append(", expiration=" + getExpire());
        }
        if (getReference() != null) {
            sb.append(", reference=" + getReference());
        }
        return sb.toString();
    }
}
