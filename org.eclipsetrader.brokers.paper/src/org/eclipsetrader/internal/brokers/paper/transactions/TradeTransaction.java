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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.internal.brokers.paper.types.DateTimeAdapter;

@XmlRootElement(name = "trade")
public class TradeTransaction implements ITransaction {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    @XmlElement(name = "order")
    private OrderElement order;

    private Cash amount;

    @XmlElementWrapper(name = "details")
    @XmlElementRefs({
            @XmlElementRef(type = ExpenseTransaction.class),
            @XmlElementRef(type = TradeTransaction.class),
            @XmlElementRef(type = StockTransaction.class),
            @XmlElementRef(type = OrderElement.class),
    })
    private List<ITransaction> childs;

    protected TradeTransaction() {
    }

    public TradeTransaction(IOrder order, ITransaction[] chunks, ITransaction expenses) {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        this.order = order != null ? new OrderElement(order) : null;
        this.childs = new ArrayList<ITransaction>(Arrays.asList(chunks));
        if (expenses != null) {
            this.childs.add(expenses);
        }
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
        return NLS.bind("OrderElement {0}", new Object[] {
            order.toString(),
        });
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getAmount()
     */
    @Override
    @XmlTransient
    public Cash getAmount() {
        if (amount == null) {
            double value = 0.0;
            for (ITransaction t : childs) {
                value += t.getAmount().getAmount();
            }
            amount = new Cash(value, Currency.getInstance(Locale.getDefault()));
        }
        return amount;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getOrder()
     */
    @Override
    @XmlTransient
    public IOrder getOrder() {
        return order.getOrder();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ITransaction#getTransactions()
     */
    @Override
    @XmlTransient
    public ITransaction[] getTransactions() {
        return childs.toArray(new ITransaction[childs.size()]);
    }
}
