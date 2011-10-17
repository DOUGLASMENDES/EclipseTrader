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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.PositionEvent;

public class AccountTest extends TestCase {

    ISecurity security;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security = new Stock("TEST", null, Currency.getInstance("EUR"));
    }

    public void testAddTradeTransactionWithoutExpenseScheme() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.setTransaction(new Transaction(monitor, null));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getTransactions().length);
        assertEquals(1500.0, account.getTransactions()[0].getAmount().getAmount());
    }

    public void testAddPositionToPortfolio() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.setTransaction(new Transaction(monitor, null));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(1000), account.getPositions()[0].getQuantity());
        assertEquals(1.5, account.getPositions()[0].getPrice());
    }

    public void testNotifyPositionOpened() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.setTransaction(new Transaction(monitor, null));

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionOpened(EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        Account account = new Account();
        account.addPositionListener(listener);
        account.processCompletedOrder(monitor);

        EasyMock.verify(listener);
    }

    public void testClosePosition() throws Exception {
        OrderMonitor monitor = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5));
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);

        Account account = new Account();
        account.processCompletedOrder(monitor);

        OrderMonitor monitor2 = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Sell, security, 1000L, 1.5));
        monitor2.setFilledQuantity(1000L);
        monitor2.setAveragePrice(1.5);
        account.processCompletedOrder(monitor2);

        assertEquals(0, account.getPositions().length);
    }

    public void testNotifyPositionClosed() throws Exception {
        OrderMonitor monitor = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5));
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);

        Account account = new Account();
        account.processCompletedOrder(monitor);

        OrderMonitor monitor2 = new OrderMonitor(null, new Order(null, IOrderType.Market, IOrderSide.Sell, security, 1000L, 1.5));
        monitor2.setFilledQuantity(1000L);
        monitor2.setAveragePrice(1.5);

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionClosed(EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);
        account.processCompletedOrder(monitor2);

        EasyMock.verify(listener);
    }

    public void testAddShortPositionToPortfolio() throws Exception {
        Order order = new Order(null, IOrderType.Market, IOrderSide.Sell, security, 1000L, 1.5);

        OrderMonitor monitor = new OrderMonitor(null, order);
        monitor.setFilledQuantity(1000L);
        monitor.setAveragePrice(1.5);
        monitor.setTransaction(new Transaction(monitor, null));

        Account account = new Account();
        account.processCompletedOrder(monitor);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(-1000), account.getPositions()[0].getQuantity());
        assertEquals(1.5, account.getPositions()[0].getPrice());
    }

    public void testAddToExistingPosition() throws Exception {
        Order order1 = new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.5);
        OrderMonitor monitor1 = new OrderMonitor(null, order1);
        monitor1.setFilledQuantity(1000L);
        monitor1.setAveragePrice(1.5);
        monitor1.setTransaction(new Transaction(monitor1, null));

        Order order2 = new Order(null, IOrderType.Market, IOrderSide.Buy, security, 1000L, 1.7);
        OrderMonitor monitor2 = new OrderMonitor(null, order2);
        monitor2.setFilledQuantity(1000L);
        monitor2.setAveragePrice(1.7);
        monitor2.setTransaction(new Transaction(monitor2, null));

        Account account = new Account();
        account.processCompletedOrder(monitor1);
        account.processCompletedOrder(monitor2);

        assertEquals(1, account.getPositions().length);
        assertEquals(new Long(2000), account.getPositions()[0].getQuantity());
        assertEquals(1.6, account.getPositions()[0].getPrice());
    }
}
