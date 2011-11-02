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

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.Order;

public class PaperBrokerTest extends TestCase {

    ISecurity security;
    MarketPricingEnvironment pricingEnvironment;
    IMarketService marketService;
    IRepositoryService repositoryService;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security = new Security("TEST", null);

        marketService = EasyMock.createNiceMock(IMarketService.class);

        repositoryService = EasyMock.createNiceMock(IRepositoryService.class);

        pricingEnvironment = EasyMock.createNiceMock(MarketPricingEnvironment.class);
        org.easymock.EasyMock.expect(pricingEnvironment.getQuote(security)).andStubReturn(new Quote(10.0, 11.0));

        EasyMock.replay(marketService, repositoryService, pricingEnvironment);
    }

    public void testPrepareOrder() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, new Security("Test", new FeedIdentifier("TEST", null)), 100L));

        assertNull(monitor.getId());
        assertEquals(IOrderStatus.New, monitor.getStatus());
    }

    public void testSubmitPreparedOrder() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, new Security("Test", new FeedIdentifier("TEST", null)), 100L));
        monitor.submit();

        assertNotNull(monitor.getId());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());
    }

    public void testProcessMarketOrder() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, 100L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(10.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testProcessLimitOrder() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 10.0, null));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 11.0, 100L, null));

        assertNull(monitor.getFilledQuantity());
        assertNull(monitor.getAveragePrice());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());

        broker.processTrade(security, new Trade(new Date(), 10.0, 100L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(10.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testProcessPartialFill() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, 50L, null));

        assertEquals(new Long(50), monitor.getFilledQuantity());
        assertEquals(new Double(10.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Partial, monitor.getStatus());
    }

    public void testProcessTradeWithoutSize() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, null, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(10.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testIgnoreTradeWithUnknownSecurity() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(new Security("Test2", new FeedIdentifier("TEST2", null)), new Trade(new Date(), 10.0, 100L, null));

        assertNull(monitor.getFilledQuantity());
        assertNull(monitor.getAveragePrice());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());
    }

    public void testAveragePrice() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 11.0, 50L, null));
        broker.processTrade(security, new Trade(new Date(), 10.0, 150L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(10.5), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testBuyAtBetterPrice() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 10.0, null));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 9.0, 200L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(9.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testSellAtBetterPrice() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderType.Limit, IOrderSide.Sell, security, 100L, 10.0, null));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 11.0, 200L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(11.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testAddBuyTransaction() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, 100L, null));

        ITransaction transaction = ((OrderMonitor) monitor).getTransactions()[0];
        assertEquals(100 * 10.0, transaction.getAmount().getAmount());
    }

    public void testAddSellTransaction() throws Exception {
        PaperBroker broker = new PaperBroker("test", "Test Broker", marketService, repositoryService);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Sell, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, 100L, null));

        ITransaction transaction = ((OrderMonitor) monitor).getTransactions()[0];
        assertEquals(-100 * 10.0, transaction.getAmount().getAmount());
    }
}
