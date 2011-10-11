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

import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.Order;

public class BrokerTest extends TestCase {

    ISecurity security;
    PricingEnvironment pricingEnvironment;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security = new Security("TEST", null);

        pricingEnvironment = new PricingEnvironment();
    }

    public void testPrepareOrder() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, new Security("Test", new FeedIdentifier("TEST", null)), 100L));

        assertNull(monitor.getId());
        assertEquals(IOrderStatus.New, monitor.getStatus());
    }

    public void testSubmitPreparedOrder() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, new Security("Test", new FeedIdentifier("TEST", null)), 100L));
        monitor.submit();

        assertNotNull(monitor.getId());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());
    }

    public void testProcessMarketOrder() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderSide.Buy, security, 100L));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 10.0, 100L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(10.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testProcessLimitOrder() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

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

    public void testBuyAtBetterPrice() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 10.0, null));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 9.0, 200L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(9.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }

    public void testSellAtBetterPrice() throws Exception {
        Broker broker = new Broker(pricingEnvironment);

        Security security = new Security("Test", new FeedIdentifier("TEST", null));
        IOrderMonitor monitor = broker.prepareOrder(new Order(null, IOrderType.Limit, IOrderSide.Sell, security, 100L, 10.0, null));
        monitor.submit();

        broker.processTrade(security, new Trade(new Date(), 11.0, 200L, null));

        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(11.0), monitor.getAveragePrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
    }
}
