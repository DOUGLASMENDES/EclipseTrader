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

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.PositionEvent;

public class AccountTest extends TestCase {

    public void testSetPositions() throws Exception {
        Account account = new Account("ID", null);

        Position position = new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.setPositions(new Position[] {
            position
        });

        assertEquals(1, account.positions.size());
        assertSame(position, account.positions.get(0));
    }

    public void testReplacePositions() throws Exception {
        Account account = new Account("ID", null);

        Position position1 = new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.setPositions(new Position[] {
            position1
        });

        Position position2 = new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.setPositions(new Position[] {
            position2
        });

        assertEquals(1, account.positions.size());
        assertSame(position2, account.positions.get(0));
    }

    public void testRemovePositions() throws Exception {
        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        account.setPositions(new Position[0]);

        assertEquals(0, account.positions.size());
    }

    public void testFirePositionOpenEvent() throws Exception {
        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionOpened(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        Account account = new Account("ID", null);
        account.addPositionListener(listener);

        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        EasyMock.verify(listener);
    }

    public void testFirePositionUpdateEvent() throws Exception {
        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionChanged(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        EasyMock.verify(listener);
    }

    public void testFirePositionCloseEvent() throws Exception {
        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionClosed(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);
        account.setPositions(new Position[0]);

        EasyMock.verify(listener);
    }

    public void testAddNewPositionWithUpdatePosition() throws Exception {
        Account account = new Account("ID", null);

        Position position = new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.updatePosition(position);

        assertEquals(1, account.positions.size());
        assertSame(position, account.positions.get(0));
    }

    public void testUpdateExistingPosition() throws Exception {
        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        Position position = new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.updatePosition(position);

        assertEquals(new Long(200), account.positions.get(0).getQuantity());
    }

    public void testUpdatePositionFiresPositionOpenEvent() throws Exception {
        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionOpened(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        Account account = new Account("ID", null);
        account.addPositionListener(listener);

        account.updatePosition(new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;"));

        EasyMock.verify(listener);
    }

    public void testUpdatePositionFiresPositionUpdateEvent() throws Exception {
        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionChanged(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);
        account.updatePosition(new Position("4;7;0;;  ;UCG;I;;;200;1.8300;;;;;;;;;;;;015;A;H;;;"));

        EasyMock.verify(listener);
    }

    public void testUpdatePositionFromMonitorFiresPositionOpenEvent() throws Exception {
        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionOpened(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        Account account = new Account("ID", null);
        account.addPositionListener(listener);

        Security security = new Security("Unitcredit", new FeedIdentifier("UCG", null));
        OrderMonitor buyMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 1.83));
        buyMonitor.setFilledQuantity(100L);
        buyMonitor.setAveragePrice(1.87);
        account.updatePosition(buyMonitor);

        EasyMock.verify(listener);
    }

    public void testUpdatePositionFromMonitorFiresPositionUpdateEvent() throws Exception {
        Security security = new Security("Unitcredit", new FeedIdentifier("UCG", null));

        Account account = new Account("ID", null);
        OrderMonitor buyMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 1.83));
        buyMonitor.setFilledQuantity(100L);
        buyMonitor.setAveragePrice(1.87);
        account.updatePosition(buyMonitor);

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionChanged(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);

        buyMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 1.83));
        buyMonitor.setFilledQuantity(100L);
        buyMonitor.setAveragePrice(1.87);
        account.updatePosition(buyMonitor);

        EasyMock.verify(listener);
    }

    public void testUpdatePositionFromMonitorFiresPositionCloseEvent() throws Exception {
        Security security = new Security("Unitcredit", new FeedIdentifier("UCG", null));

        Account account = new Account("ID", null);
        account.setPositions(new Position[] {
            new Position("4;7;0;;  ;UCG;I;;;100;1.8300;;;;;;;;;;;;015;A;H;;;")
        });

        OrderMonitor buyMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Buy, security, 100L, 1.83));
        buyMonitor.setFilledQuantity(100L);
        buyMonitor.setAveragePrice(1.87);
        account.updatePosition(buyMonitor);

        OrderMonitor sellMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Sell, security, 100L, 1.87));
        sellMonitor.setFilledQuantity(100L);
        sellMonitor.setAveragePrice(1.87);

        IPositionListener listener = EasyMock.createMock(IPositionListener.class);
        listener.positionClosed(org.easymock.EasyMock.isA(PositionEvent.class));
        EasyMock.replay(listener);

        account.addPositionListener(listener);
        account.updatePosition(sellMonitor);

        EasyMock.verify(listener);
    }

    public void testSetShortPositionFromMonitor() throws Exception {
        Account account = new Account("ID", null);
        Security security = new Security("Unitcredit", new FeedIdentifier("UCG", null));

        OrderMonitor buyMonitor = new OrderMonitor(null, null, new Order(null, IOrderType.Limit, IOrderSide.Sell, security, 100L, 1.83));
        buyMonitor.setFilledQuantity(100L);
        buyMonitor.setAveragePrice(1.87);
        account.updatePosition(buyMonitor);

        assertEquals(new Long(-100), account.positions.get(0).getQuantity());
    }

    public void testSetShortPositionFromStream() throws Exception {
        Account account = new Account("ID", null);

        Position position = new Position("4;7;0;;  ;UCG;I;;;-100;1.8300;;;;;;;;;;;;015;A;H;;;");
        account.updatePosition(position);

        assertEquals(new Long(-100), account.positions.get(0).getQuantity());
    }
}
