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

package org.eclipsetrader.internal.brokers.paper.transactions;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.internal.brokers.paper.types.DateTimeAdapter;
import org.eclipsetrader.internal.brokers.paper.types.DoubleValueAdapter;
import org.eclipsetrader.internal.brokers.paper.types.OrderSideAdapter;
import org.eclipsetrader.internal.brokers.paper.types.OrderTypeAdapter;
import org.eclipsetrader.internal.brokers.paper.types.SecurityAdapter;

@XmlRootElement(name = "order")
public class OrderElement {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(OrderTypeAdapter.class)
    private IOrderType type;

    @XmlAttribute(name = "side")
    @XmlJavaTypeAdapter(OrderSideAdapter.class)
    private IOrderSide side;

    @XmlAttribute(name = "quantity")
    private Long quantity;

    @XmlAttribute(name = "price")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double price;

    @XmlAttribute(name = "stop-price")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double stopPrice;

    @XmlAttribute(name = "expire")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date expire;

    @XmlElement(name = "reference")
    private String reference;

    private IOrder order;

    private class Order implements IOrder {

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getDate()
         */
        @Override
        public Date getDate() {
            return date;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getAccount()
         */
        @Override
        public IAccount getAccount() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getSecurity()
         */
        @Override
        public ISecurity getSecurity() {
            return security;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getPrice()
         */
        @Override
        public Double getPrice() {
            return price;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getQuantity()
         */
        @Override
        public Long getQuantity() {
            return quantity;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getRoute()
         */
        @Override
        public IOrderRoute getRoute() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getSide()
         */
        @Override
        public IOrderSide getSide() {
            return side;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getStopPrice()
         */
        @Override
        public Double getStopPrice() {
            return stopPrice;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getType()
         */
        @Override
        public IOrderType getType() {
            return type;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getValidity()
         */
        @Override
        public IOrderValidity getValidity() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getExpire()
         */
        @Override
        public Date getExpire() {
            return expire;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.trading.IOrder#getReference()
         */
        @Override
        public String getReference() {
            return reference;
        }
    }

    public OrderElement() {
        order = new Order();
    }

    public OrderElement(IOrder order) {
        this.date = order.getDate();
        this.side = order.getSide();
        this.type = order.getType();
        this.security = order.getSecurity();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.stopPrice = order.getStopPrice();
        this.expire = order.getExpire();
        this.reference = order.getReference();

        this.order = order;
    }

    @XmlTransient
    public IOrder getOrder() {
        return order;
    }
}
