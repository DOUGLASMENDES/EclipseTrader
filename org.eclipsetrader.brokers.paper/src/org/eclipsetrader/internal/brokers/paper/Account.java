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

import org.eclipsetrader.core.trading.Cash;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.internal.brokers.paper.transactions.ExpenseTransaction;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;
import org.eclipsetrader.internal.brokers.paper.transactions.TradeTransaction;
import org.eclipsetrader.internal.brokers.paper.types.CurrencyAdapter;

@XmlRootElement(name = "account")
public class Account implements IAccount {
	@XmlAttribute(name = "id")
	private String id;

	@XmlElement(name = "description")
	private String description;

	@XmlAttribute(name = "currency")
	@XmlJavaTypeAdapter(CurrencyAdapter.class)
	private Currency currency;

	@XmlElement(name = "initial-balance")
	private Double initialBalance;

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

	private IExpenseScheme expenseScheme;

	protected Account() {
	}

	public Account(String description) {
		this.id = Long.toHexString(UUID.randomUUID().getLeastSignificantBits());
	    this.description = description;
	    try {
	    	this.currency = Currency.getInstance(Locale.getDefault());
	    } catch(Exception e) {
			// Ignore, some locales may throw an exception
	    }
	    this.initialBalance = 0.0;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getId()
	 */
	@XmlTransient
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IAccount#getDescription()
	 */
	@XmlTransient
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
    	this.description = description;
    }

	public Currency getCurrency() {
    	return currency;
    }

	public void setCurrency(Currency currency) {
    	this.currency = currency;
    }

	public Double getInitialBalance() {
    	return initialBalance;
    }

	public void setInitialBalance(Double initialBalance) {
    	this.initialBalance = initialBalance;
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
	@XmlTransient
	public ITransaction[] getTransactions() {
		return transactions.toArray(new ITransaction[transactions.size()]);
	}

	public void processCompletedOrder(OrderMonitor monitor) {
		Double expenses = null;
		if (expenseScheme != null) {
			if (monitor.getOrder().getSide() == IOrderSide.Buy)
				expenses = expenseScheme.getBuyExpenses(monitor.getFilledQuantity(), monitor.getAveragePrice());
			else if (monitor.getOrder().getSide() == IOrderSide.Sell)
				expenses = expenseScheme.getSellExpenses(monitor.getFilledQuantity(), monitor.getAveragePrice());
		}
		TradeTransaction transaction = new TradeTransaction(
				monitor.getOrder(),
				monitor.getTransactions(),
				expenses != null ? new ExpenseTransaction(new Cash(expenses, currency)) : null
			);
		transactions.add(transaction);

		Position position = null;
		for (Position p : portfolio) {
			if (p.getSecurity() == monitor.getOrder().getSecurity()) {
				position = p;
				break;
			}
		}

		Long quantity = monitor.getOrder().getSide() == IOrderSide.Sell ? - monitor.getFilledQuantity() : monitor.getFilledQuantity();
		if (position == null) {
			position = new Position(monitor.getOrder().getSecurity(), quantity, monitor.getAveragePrice());
			portfolio.add(position);
		}
		else {
			position.add(quantity, monitor.getAveragePrice());
			if (position.getQuantity() == 0L)
				portfolio.remove(position);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
	@XmlTransient
    public IPosition[] getPositions() {
		return portfolio.toArray(new IPosition[portfolio.size()]);
    }
}
