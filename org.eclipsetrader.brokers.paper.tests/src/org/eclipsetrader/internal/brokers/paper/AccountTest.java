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

import java.util.Currency;
import java.util.Locale;

import junit.framework.TestCase;

import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.internal.brokers.paper.schemes.SimpleFixedScheme;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;

public class AccountTest extends TestCase {

    public void testAddTradeTransactionWithoutExpenseScheme() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getTransactions().length);
        assertEquals(1500.0, account.getTransactions()[0].getAmount().getAmount());
    }

    public void testAddBuyTransactionWithExpenseScheme() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.setExpenseScheme(new SimpleFixedScheme());
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getTransactions().length);
        assertEquals(1509.95, account.getTransactions()[0].getAmount().getAmount());
    }

    public void testAddSellTransactionWithExpenseScheme() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Sell, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.setExpenseScheme(new SimpleFixedScheme());
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getTransactions().length);
        assertEquals(1509.95, account.getTransactions()[0].getAmount().getAmount());
    }

    public void testAddPositionToPortfolio() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(1000), account.getPositions()[0].getQuantity());
        assertEquals(1.5, account.getPositions()[0].getPrice());
    }

    public void testClosePosition() throws Exception {
        OrderMonitor monitor = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5));
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);

        Account account = new Account();
        account.processCompletedOrder(monitor);

        OrderMonitor monitor2 = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Sell, null, 1000L, 1.5));
        monitor2.setFilledQuantity(1000L);
        monitor2.setAveragePrice(1.5);
        account.processCompletedOrder(monitor2);

        assertEquals(0, account.getPositions().length);
    }

    public void testAddShortPositionToPortfolio() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Sell, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(-1000), account.getPositions()[0].getQuantity());
        assertEquals(1.5, account.getPositions()[0].getPrice());
    }

    public void testAddToExistingPosition() throws Exception {
        Order order1 = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5);
        OrderMonitor monitor1 = new OrderMonitor(null, order1);
        monitor1.setFilledQuantity(1000L);
        monitor1.setAveragePrice(1.5);
        monitor1.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Order order2 = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.7);
        OrderMonitor monitor2 = new OrderMonitor(null, order2);
        monitor2.setFilledQuantity(1000L);
        monitor2.setAveragePrice(1.7);
        monitor2.addTransaction(new StockTransaction(null, 1000L, 1.7));

        Account account = new Account();
        account.processCompletedOrder(monitor1);
        account.processCompletedOrder(monitor2);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(2000), account.getPositions()[0].getQuantity());
        assertEquals(1.6, account.getPositions()[0].getPrice());
    }

    public void testBuyUpdateBalance() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, 1000L, 1.5));

        Account account = new Account();
        account.setCurrency(Currency.getInstance(Locale.getDefault()));
        account.setBalance(10000.0);
        account.setExpenseScheme(new SimpleFixedScheme());
        account.processCompletedOrder(monitor);

        assertEquals(10000.0 - 1500.0 - 9.95, account.getBalance().getAmount());
    }

    public void testSellUpdateBalance() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Sell, null, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.addTransaction(new StockTransaction(null, -1000L, 1.5));

        Account account = new Account();
        account.setCurrency(Currency.getInstance(Locale.getDefault()));
        account.setBalance(10000.0);
        account.setExpenseScheme(new SimpleFixedScheme());
        account.processCompletedOrder(monitor);

        assertEquals(10000.0 + 1500.0 - 9.95, account.getBalance().getAmount());
    }
}
