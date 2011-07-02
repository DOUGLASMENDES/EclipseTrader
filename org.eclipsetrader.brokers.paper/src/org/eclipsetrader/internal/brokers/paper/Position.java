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

package org.eclipsetrader.internal.brokers.paper;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.internal.brokers.paper.types.DateTimeAdapter;
import org.eclipsetrader.internal.brokers.paper.types.SecurityAdapter;

@XmlRootElement(name = "position")
public class Position implements IPosition {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlAttribute(name = "quantity")
    private long quantity = 0L;

    @XmlAttribute(name = "price")
    private double price = 0.0;

    public Position() {
    }

    public Position(ISecurity security, Long quantity, Double price) {
        this.date = new Date();
        this.security = security;
        this.quantity = quantity;
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getSecurity()
     */
    @Override
    @XmlTransient
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getQuantity()
     */
    @Override
    @XmlTransient
    public Long getQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getPrice()
     */
    @Override
    @XmlTransient
    public Double getPrice() {
        return price;
    }

    public void add(long quantity, double price) {
        if (Math.signum(quantity) == Math.signum(this.quantity) || Math.signum(this.quantity) == 0.0) {
            double total = this.quantity * this.price + quantity * price;
            this.quantity += quantity;
            this.price = total / this.quantity;
        }
        else {
            this.quantity += quantity;
            if (Math.signum(quantity) == Math.signum(this.quantity)) {
                this.price = price;
            }
        }
    }
}
