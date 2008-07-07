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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.NameValuePair;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderSide;
import org.eclipsetrader.core.trading.OrderStatus;
import org.eclipsetrader.core.trading.OrderType;

public class WebConnectorTest extends TestCase {

	public void testParseBuyOrderLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;n;4/07/2008;09:56:38;C;5;21,5000;4/07/2008;10:03:02;;;;M;\";");
	    assertNotNull(order);
	    assertEquals(getTime(2008, Calendar.JULY, 4, 9, 56, 38), order.getDate());
	    assertEquals("R8609563873834", order.getId());
	    assertEquals("ENI", order.getSecurity().getName());
	    assertEquals(OrderSide.Buy, order.getSide());
	    assertEquals(new Long(5), order.getQuantity());
	    assertEquals(21.5, order.getPrice());
	    assertEquals(OrderStatus.PendingNew, order.getStatus());
	    assertNull(order.getFilledQuantity());
	    assertNull(order.getAveragePrice());
	    assertNotNull(order.getBroker());
    }

	public void testParseSellOrderLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;n;4/07/2008;09:56:38;V;5;21,5000;4/07/2008;10:03:02;;;;M;\";");
	    assertNotNull(order);
	    assertEquals(getTime(2008, Calendar.JULY, 4, 9, 56, 38), order.getDate());
	    assertEquals("R8609563873834", order.getId());
	    assertEquals("ENI", order.getSecurity().getName());
	    assertEquals(OrderSide.Sell, order.getSide());
	    assertEquals(new Long(5), order.getQuantity());
	    assertEquals(21.5, order.getPrice());
	    assertEquals(OrderStatus.PendingNew, order.getStatus());
	    assertNull(order.getFilledQuantity());
	    assertNull(order.getAveragePrice());
	    assertNotNull(order.getBroker());
    }

	public void testParseCanceledOrderLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;zA;4/07/2008;09:56:38;C;5;21,0000;4/07/2008;10:03:02;;;;M;\";");
	    assertNotNull(order);
	    assertEquals(OrderStatus.Canceled, order.getStatus());
    }

	public void testParseFilledOrderLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;e;4/07/2008;09:56:38;C;5;21,0000;4/07/2008;10:03:02;5;21,500;;M;\";");
	    assertNotNull(order);
	    assertEquals(OrderStatus.Filled, order.getStatus());
	    assertEquals(new Long(5), order.getFilledQuantity());
	    assertEquals(21.5, order.getAveragePrice());
    }

	public void testParsePartialOrderLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;e;4/07/2008;09:56:38;C;5;21,0000;4/07/2008;10:03:02;2;21,500;;M;\";");
	    assertNotNull(order);
	    assertEquals(OrderStatus.Partial, order.getStatus());
	    assertEquals(new Long(2), order.getFilledQuantity());
	    assertEquals(21.5, order.getAveragePrice());
    }

	public void testParseUnknownOrderStatusLine() throws Exception {
	    IOrder order = new WebConnector().parseOrderLine("tr01[0] =\"ENI;R8609563873834;??;4/07/2008;09:56:38;C;5;21,0000;4/07/2008;10:03:02;;;;M;\";");
	    assertNotNull(order);
	    assertEquals(OrderStatus.PendingNew, order.getStatus());
    }

	public void testUpdateSameOrderInstance() throws Exception {
		ISecurity security = new Security("ENI", new FeedIdentifier("ENI", null));
		Order order = new Order(null, null, OrderType.Limit, OrderSide.Buy, security, 5L, 21.5);
		order.setId("R8609563873834");
		WebConnector connector = new WebConnector();
		connector.orders.put(order.getId(), order);
	    connector.parseOrderLine("tr01[0] =\"ENI;R8609563873834;e;4/07/2008;09:56:38;C;5;21,0000;4/07/2008;10:03:02;5;21,500;;M;\";");
	    assertEquals(OrderStatus.Filled, order.getStatus());
	    assertEquals(new Long(5), order.getFilledQuantity());
	    assertEquals(21.5, order.getAveragePrice());
    }

	public void testModeUpdate() throws Exception {
		List<NameValuePair> query = new ArrayList<NameValuePair>();

		query.add(new NameValuePair("MODO", "C"));
		assertEquals(1, query.size());
		assertTrue(query.contains(new NameValuePair("MODO", "C")));

		query.remove(new NameValuePair("MODO", "C"));
		assertEquals(0, query.size());

		query.add(new NameValuePair("MODO", "V"));
		assertEquals(1, query.size());
		assertTrue(query.contains(new NameValuePair("MODO", "V")));
    }

	private Date getTime(int year, int month, int day, int hour, int minute, int second) {
	    Calendar date = Calendar.getInstance();
	    date.setTimeInMillis(0);
	    date.set(Calendar.YEAR, year);
	    date.set(Calendar.MONTH, month);
	    date.set(Calendar.DAY_OF_MONTH, day);
		date.set(Calendar.HOUR_OF_DAY, hour);
		date.set(Calendar.MINUTE, minute);
		date.set(Calendar.SECOND, second);
		return date.getTime();
	}
}
