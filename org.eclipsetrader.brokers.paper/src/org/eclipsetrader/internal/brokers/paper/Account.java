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

import java.util.ArrayList;
import java.util.Currency;
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.PositionEvent;
import org.eclipsetrader.internal.brokers.paper.transactions.ExpenseTransaction;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;
import org.eclipsetrader.internal.brokers.paper.transactions.TradeTransaction;
import org.eclipsetrader.internal.brokers.paper.types.CurrencyAdapter;
import org.eclipsetrader.internal.brokers.paper.types.ExpenseSchemeAdapter;

@XmlRootElement(name = "account")
public class Account implements IAccount {

    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "description")
    private String description;

    @XmlAttribute(name = "currency")
    @XmlJavaTypeAdapter(CurrencyAdapter.class)
    private Currency currency;

    @XmlElement(name = "balance")
    private Double balance = 0.0;

    @XmlElementWrapper(name = "transactions")
    @XmlElementRefs({
        @XmlElementRef(type = ExpenseTransaction.class),
        @XmlElementRef(type = TradeTransaction.class),
        @XmlElementRef(type = StockTransaction.class),
    })
    private List<ITransaction> transactions = new ArrayList<ITransaction>();

    @XmlElementWrapper(name = "portfolio")
    @XmlElementRef
    private List<Position> portfolio = new ArrayList<Position>();

    @XmlAttribute(name = "expense-scheme")
    @XmlJavaTypeAdapter(ExpenseSchemeAdapter.class)
    private IExpenseScheme expenseScheme;

    private ListenerList listeners = new ListenerList();

    protected Account() {
    }

    public Account(String description) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        try {
            this.currency = Currency.getInstance(Locale.getDefault());
        } catch (Exception e) {
            // Ignore, some locales may throw an exception
        }
        this.balance = 0.0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getId()
     */
    @Override
    @XmlTransient
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getDescription()
     */
    @Override
    @XmlTransient
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getBalance()
     */
    @Override
    @XmlTransient
    public Cash getBalance() {
        return new Cash(balance, currency);
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @XmlTransient
    public IExpenseScheme getExpenseScheme() {
        return expenseScheme;
    }

    public void setExpenseScheme(IExpenseScheme expenseScheme) {
        this.expenseScheme = expenseScheme;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
     */
    @Override
    @XmlTransient
    public ITransaction[] getTransactions() {
        return transactions.toArray(new ITransaction[transactions.size()]);
    }

    public void processCompletedOrder(OrderMonitor monitor) {
        Double expenses = null;
        if (expenseScheme != null) {
            if (monitor.getOrder().getSide() == IOrderSide.Buy) {
                expenses = expenseScheme.getBuyExpenses(monitor.getFilledQuantity(), monitor.getAveragePrice());
            }
            else if (monitor.getOrder().getSide() == IOrderSide.Sell) {
                expenses = expenseScheme.getSellExpenses(monitor.getFilledQuantity(), monitor.getAveragePrice());
            }
        }
        TradeTransaction transaction = new TradeTransaction(monitor.getOrder(), monitor.getTransactions(), expenses != null ? new ExpenseTransaction(new Cash(expenses, currency)) : null);
        transactions.add(transaction);

        balance -= transaction.getAmount().getAmount();

        Position position = null;
        for (Position p : portfolio) {
            if (p.getSecurity() == monitor.getOrder().getSecurity()) {
                position = p;
                break;
            }
        }

        Long quantity = monitor.getOrder().getSide() == IOrderSide.Sell ? -monitor.getFilledQuantity() : monitor.getFilledQuantity();
        double averagePrice = transaction.getAmount().getAmount() / monitor.getFilledQuantity();

        if (position == null) {
            position = new Position(monitor.getOrder().getSecurity(), quantity, averagePrice);
            portfolio.add(position);
            firePositionOpenedEvent(position);
        }
        else {
            position.add(quantity, averagePrice);
            if (position.getQuantity() == 0L) {
                portfolio.remove(position);
                firePositionClosedEvent(position);
            }
            else {
                firePositionChangedEvent(position);
            }
        }
    }

    protected void firePositionOpenedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionOpened(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error notifying listeners", t);
                Activator.log(status);
            }
        }
    }

    protected void firePositionClosedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionClosed(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error notifying listeners", t);
                Activator.log(status);
            }
        }
    }

    protected void firePositionChangedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionChanged(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error notifying listeners", t);
                Activator.log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
    @Override
    @XmlTransient
    public IPosition[] getPositions() {
        return portfolio.toArray(new IPosition[portfolio.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void addPositionListener(IPositionListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void removePositionListener(IPositionListener listener) {
        listeners.remove(listener);
    }
}
