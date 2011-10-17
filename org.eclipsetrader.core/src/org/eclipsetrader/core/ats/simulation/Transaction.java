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

import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IStockTransaction;
import org.eclipsetrader.core.trading.ITransaction;

public class Transaction implements ITransaction, IStockTransaction {

    private final String id;
    private final Date date;
    private final IOrder order;
    private final Long quantity;
    private final Double averagePrice;

    public Transaction(OrderMonitor orderMonitor, Date date) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.order = orderMonitor.getOrder();
        this.quantity = orderMonitor.getFilledQuantity();
        this.averagePrice = orderMonitor.getAveragePrice();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getDescription()
     */
    @Override
    public String getDescription() {
        return order.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getAmount()
     */
    @Override
    public Cash getAmount() {
        Currency currency = (Currency) order.getSecurity().getAdapter(Currency.class);
        if (currency == null) {
            currency = Currency.getInstance(Locale.getDefault());
        }
        return new Cash(quantity * averagePrice, currency);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getOrder()
     */
    @Override
    public IOrder getOrder() {
        return order;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getTransactions()
     */
    @Override
    public ITransaction[] getTransactions() {
        return new ITransaction[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IStockTransaction#getFilledQuantity()
     */
    @Override
    public Long getFilledQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IStockTransaction#getAveragePrice()
     */
    @Override
    public Double getAveragePrice() {
        return averagePrice;
    }
}
