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

import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.internal.brokers.paper.types.CurrencyAdapter;
import org.eclipsetrader.internal.brokers.paper.types.DateTimeAdapter;
import org.eclipsetrader.internal.brokers.paper.types.DoubleValueAdapter;

@XmlRootElement(name = "expense")
public class ExpenseTransaction implements ITransaction {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlAttribute(name = "amount")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double amount;

    @XmlAttribute(name = "currency")
    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private Currency currency;

    protected ExpenseTransaction() {
    }

    public ExpenseTransaction(Cash amount) {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        this.amount = amount.getAmount();
        this.currency = amount.getCurrency();
    }

    public ExpenseTransaction(Date date, Cash amount) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.amount = amount.getAmount();
        this.currency = amount.getCurrency();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getId()
     */
    @Override
    @XmlTransient
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getDate()
     */
    @Override
    @XmlTransient
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getDescription()
     */
    @Override
    @XmlTransient
    public String getDescription() {
        return "Expenses";
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getAmount()
     */
    @Override
    @XmlTransient
    public Cash getAmount() {
        return new Cash(amount, currency);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getOrder()
     */
    @Override
    @XmlTransient
    public IOrder getOrder() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getTransactions()
     */
    @Override
    @XmlTransient
    public ITransaction[] getTransactions() {
        return null;
    }
}
