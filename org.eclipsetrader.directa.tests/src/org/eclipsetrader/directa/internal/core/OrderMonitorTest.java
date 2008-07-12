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

package org.eclipsetrader.directa.internal.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderSide;
import org.eclipsetrader.core.trading.OrderStatus;
import org.eclipsetrader.core.trading.OrderType;

import junit.framework.TestCase;

public class OrderMonitorTest extends TestCase {
	private List<PropertyChangeEvent> propertyChangeEvents;
	private PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	propertyChangeEvents.add(evt);
        }
	};

	/* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    	propertyChangeEvents = new ArrayList<PropertyChangeEvent>();
    }

	public void testFireAveragePricePropertyChanges() throws Exception {
		ISecurity security = new Security("ENI", new FeedIdentifier("ENI", null));
		OrderMonitor monitor = new OrderMonitor(null, null, new Order(null, OrderType.Limit, OrderSide.Buy, security, 5L, 21.5));
		monitor.getPropertyChangeSupport().addPropertyChangeListener(listener);
		monitor.setAveragePrice(21.5);
		assertEquals(1, propertyChangeEvents.size());
		assertEquals(IOrderMonitor.PROP_AVERAGE_PRICE, propertyChangeEvents.get(0).getPropertyName());
		assertNull(propertyChangeEvents.get(0).getOldValue());
		assertEquals(new Double(21.5), propertyChangeEvents.get(0).getNewValue());
    }

	public void testFireFilledQuantityPropertyChanges() throws Exception {
		ISecurity security = new Security("ENI", new FeedIdentifier("ENI", null));
		OrderMonitor monitor = new OrderMonitor(null, null, new Order(null, OrderType.Limit, OrderSide.Buy, security, 5L, 21.5));
		monitor.getPropertyChangeSupport().addPropertyChangeListener(listener);
		monitor.setFilledQuantity(5L);
		assertEquals(1, propertyChangeEvents.size());
		assertEquals(IOrderMonitor.PROP_FILLED_QUANTITY, propertyChangeEvents.get(0).getPropertyName());
		assertNull(propertyChangeEvents.get(0).getOldValue());
		assertEquals(new Long(5), propertyChangeEvents.get(0).getNewValue());
    }

	public void testFireStatusPropertyChanges() throws Exception {
		ISecurity security = new Security("ENI", new FeedIdentifier("ENI", null));
		OrderMonitor monitor = new OrderMonitor(null, null, new Order(null, OrderType.Limit, OrderSide.Buy, security, 5L, 21.5));
		monitor.getPropertyChangeSupport().addPropertyChangeListener(listener);
		monitor.setStatus(OrderStatus.Filled);
		assertEquals(1, propertyChangeEvents.size());
		assertEquals(IOrderMonitor.PROP_STATUS, propertyChangeEvents.get(0).getPropertyName());
		assertEquals(OrderStatus.New, propertyChangeEvents.get(0).getOldValue());
		assertEquals(OrderStatus.Filled, propertyChangeEvents.get(0).getNewValue());
    }
}
