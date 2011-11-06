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

package org.eclipsetrader.directa.internal.core;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;

public class BrokerConnectorTest extends TestCase {

    class BrokerConnectorMock extends BrokerConnector {

        public BrokerConnectorMock() {
        }

        @Override
        public ISecurity getSecurityFromSymbol(String symbol) {
            return new Security(symbol, new FeedIdentifier(symbol, null));
        }
    }

    public void testParseToSameOrderMonitor() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();
        OrderMonitor monitor1 = c.parseOrderLine("18;8;0;U6812380489237;n ;TIT;I;;100;;;;;1;;100;.7700;;C;20090309;120550;;006;A;M;;;");
        c.orders.add(monitor1);

        OrderMonitor monitor2 = c.parseOrderLine("25;8;0;U6812380489237;e ;TIT;I;;;100;.7715;;;;;100;.7700;;C;20090309;123813;;007;A;M;100;;");

        assertSame(monitor2, monitor1);
    }

    public void testParseNewBuyOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();

        OrderMonitor monitor = c.parseOrderLine("18;8;0;U6812380489237;n ;TIT;I;;100;;;;;1;;100;.7700;;C;20090309;120550;;006;A;M;;;");

        assertEquals(IOrderSide.Buy, monitor.getOrder().getSide());
        assertEquals(new Long(100), monitor.getOrder().getQuantity());
        assertEquals(new Double(0.77), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());
    }

    public void testParseExecutedBuyOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();

        OrderMonitor monitor = c.parseOrderLine("18;8;0;U6812380489237;n ;TIT;I;;100;;;;;1;;100;.7700;;C;20090309;120550;;006;A;M;;;");
        c.orders.add(monitor);

        monitor = c.parseOrderLine("25;8;0;U6812380489237;e ;TIT;I;;;100;.7715;;;;;100;.7700;;C;20090309;123813;;007;A;M;100;;");

        assertEquals(IOrderSide.Buy, monitor.getOrder().getSide());
        assertEquals(new Long(100), monitor.getOrder().getQuantity());
        assertEquals(new Double(0.77), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(0.7715), monitor.getAveragePrice());
    }

    public void testParsePendingCanceledBuyOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();
        OrderMonitor monitor = c.parseOrderLine("24;8;0;U7010412397817;n ;TRN;I;;50;;;;;1;;50;2.2700;;C;20090311;104123;;006;A;M;;;");
        c.orders.add(monitor);

        monitor = c.parseOrderLine("25;8;0;U7010412397817;na;TRN;I;;50;;;;;1;;50;2.2700;;C;20090311;104132;;002;A;M;50;;");

        assertEquals(IOrderSide.Buy, monitor.getOrder().getSide());
        assertEquals(new Long(50), monitor.getOrder().getQuantity());
        assertEquals(new Double(2.27), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.PendingCancel, monitor.getStatus());
    }

    public void testParseCanceledBuyOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();
        OrderMonitor monitor = c.parseOrderLine("24;8;0;U7010412397817;n ;TRN;I;;50;;;;;1;;50;2.2700;;C;20090311;104123;;006;A;M;;;");
        c.orders.add(monitor);

        monitor = c.parseOrderLine("26;8;0;U7010412397817;zA;TRN;I;;;;;;;;;;2.2700;;C;20090311;104132;;003;A;M;;;");

        assertEquals(IOrderSide.Buy, monitor.getOrder().getSide());
        assertEquals(new Long(50), monitor.getOrder().getQuantity());
        assertEquals(new Double(2.27), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.Canceled, monitor.getStatus());
    }

    public void testNewSellOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();

        OrderMonitor monitor = c.parseOrderLine("18;8;0;U6909100824299;n ;TIT;I;;-100;100;.7715;;;;1;100;.7920;;V;20090310;91009;;006;A;M;;;");

        assertEquals(IOrderSide.Sell, monitor.getOrder().getSide());
        assertEquals(new Long(100), monitor.getOrder().getQuantity());
        assertEquals(new Double(0.792), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.PendingNew, monitor.getStatus());
    }

    public void testExecutedSellOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();
        OrderMonitor monitor = c.parseOrderLine("18;8;0;U6909100824299;n ;TIT;I;;-100;100;.7715;;;;1;100;.7920;;V;20090310;91009;;006;A;M;;;");
        c.orders.add(monitor);

        monitor = c.parseOrderLine("19;8;0;U6909100824299;e ;TIT;I;;;;;;;;;100;.7920;;V;20090310;91014;;007;A;M;100;;");

        assertEquals(IOrderSide.Sell, monitor.getOrder().getSide());
        assertEquals(new Long(100), monitor.getOrder().getQuantity());
        assertEquals(new Double(0.792), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(0.792), monitor.getAveragePrice());
    }

    public void testExecutedSellOrderHistory() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();

        OrderMonitor monitor = c.parseOrderLine("10;6;5;U6909100824299;e ;TIT;I;;;;;;;;;;.7920;;V;20090310;91014;;019;A;M;100;;");

        assertEquals(IOrderSide.Sell, monitor.getOrder().getSide());
        assertEquals(new Long(100), monitor.getOrder().getQuantity());
        assertEquals(new Double(0.792), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.Filled, monitor.getStatus());
        assertEquals(new Long(100), monitor.getFilledQuantity());
        assertEquals(new Double(0.792), monitor.getAveragePrice());
    }

    public void testParseCanceledSellOrder() throws Exception {
        BrokerConnector c = new BrokerConnectorMock();
        OrderMonitor monitor = c.parseOrderLine("24;8;0;U7010412397817;n ;TRN;I;;50;;;;;1;;50;2.2700;;V;20090311;104123;;006;A;M;;;");
        c.orders.add(monitor);

        monitor = c.parseOrderLine("26;8;0;U7010412397817;zA;TRN;I;;;;;;;;;;2.2700;;V;20090311;104132;;003;A;M;;;");

        assertEquals(IOrderSide.Sell, monitor.getOrder().getSide());
        assertEquals(new Long(50), monitor.getOrder().getQuantity());
        assertEquals(new Double(2.27), monitor.getOrder().getPrice());
        assertEquals(IOrderStatus.Canceled, monitor.getStatus());
    }

    public void testParsePortfolioLine() throws Exception {
        BrokerConnector c = new BrokerConnector() {

            @Override
            public ISecurity getSecurityFromSymbol(String symbol) {
                if ("UCG".equals(symbol)) {
                    return new Security(symbol, null);
                }
                return null;
            }
        };

        Position position = c.parsePositionLine("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");

        assertEquals("UCG", position.getSecurity().getName());
        assertEquals(new Long(100), position.getQuantity());
        assertEquals(new Double(1.83), position.getPrice());
    }
}
