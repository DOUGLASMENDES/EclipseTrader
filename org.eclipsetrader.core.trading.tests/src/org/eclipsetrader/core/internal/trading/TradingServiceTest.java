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

package org.eclipsetrader.core.internal.trading;

import junit.framework.TestCase;

import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.core.trading.OrderMonitor;

public class TradingServiceTest extends TestCase {

    public void testAddOrdersFromOrderChangedEvent() throws Exception {
        TradingService service = new TradingService();
        assertEquals(0, service.getOrders().length);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, new OrderMonitor(null, null)),
        }));
        assertEquals(1, service.getOrders().length);
    }

    public void testRemoveOrdersFromOrderChangedEvent() throws Exception {
        TradingService service = new TradingService();
        assertEquals(0, service.getOrders().length);
        OrderMonitor monitor = new OrderMonitor(null, null);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, monitor),
        }));
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_REMOVED, monitor),
        }));
        assertEquals(0, service.getOrders().length);
    }

    public void testDontAddSameOrderTwice() throws Exception {
        TradingService service = new TradingService();
        assertEquals(0, service.getOrders().length);
        OrderMonitor monitor = new OrderMonitor(null, null);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, monitor),
        }));
        assertEquals(1, service.getOrders().length);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, monitor),
        }));
        assertEquals(1, service.getOrders().length);
    }

    public void testAddUnknownUpdatedOrdersFromOrderChangedEvent() throws Exception {
        TradingService service = new TradingService();
        assertEquals(0, service.getOrders().length);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_UPDATED, new OrderMonitor(null, null)),
        }));
        assertEquals(1, service.getOrders().length);
    }

    public void testDontAddSameUpdatedOrderTwice() throws Exception {
        TradingService service = new TradingService();
        assertEquals(0, service.getOrders().length);
        OrderMonitor monitor = new OrderMonitor(null, null);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_UPDATED, monitor),
        }));
        assertEquals(1, service.getOrders().length);
        service.processOrderChangedEvent(new OrderChangeEvent(null, new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_UPDATED, monitor),
        }));
        assertEquals(1, service.getOrders().length);
    }
}
