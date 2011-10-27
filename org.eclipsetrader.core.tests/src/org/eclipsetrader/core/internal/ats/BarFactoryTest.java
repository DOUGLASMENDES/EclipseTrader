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

package org.eclipsetrader.core.internal.ats;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.Security;

public class BarFactoryTest extends TestCase {

    Security security;
    PricingEnvironment pricingEnvironment;
    BarFactory factory;
    Calendar currentTime;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security = new Security("Test", new FeedIdentifier("TST", null));

        factory = new BarFactory();

        pricingEnvironment = new PricingEnvironment();
        pricingEnvironment.addPricingListener(new IPricingListener() {

            @Override
            public void pricingUpdate(PricingEvent event) {
                factory.pricingUpdate(event);
            }
        });

        currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MILLISECOND, 0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        factory.dispose();
    }

    public void testSetInitialValues() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertEquals(1.0, data.open);
        assertEquals(1.0, data.high);
        assertEquals(1.0, data.low);
        assertEquals(1.0, data.close);
        assertEquals(new Long(100), data.volume);
    }

    public void testSetHighestValue() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(new Date(System.currentTimeMillis()), 1.1, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertEquals(1.1, data.high);
        assertEquals(1.0, data.low);
    }

    public void testSetLowestValue() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertEquals(1.0, data.high);
        assertEquals(0.9, data.low);
    }

    public void testSetCloseToLatestTrade() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertEquals(0.9, data.close);
    }

    public void testSetOpenToFirstTrade() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertEquals(1.0, data.open);
    }

    public void testTradesGeneratesBarOpen() throws Exception {
        IBarFactoryListener listener = EasyMock.createMock(IBarFactoryListener.class);
        listener.barOpen(EasyMock.isA(BarFactoryEvent.class));
        EasyMock.replay(listener);

        factory.add(security, TimeSpan.minutes(1));
        factory.addBarFactoryListener(listener);

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        EasyMock.verify(listener);
    }

    public void testTradesGeneratesBarClose() throws Exception {
        IBarFactoryListener listener = EasyMock.createNiceMock(IBarFactoryListener.class);
        listener.barClose(EasyMock.isA(BarFactoryEvent.class));
        EasyMock.replay(listener);

        factory.add(security, TimeSpan.minutes(1));
        factory.addBarFactoryListener(listener);

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 30);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 29);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        currentTime.add(Calendar.SECOND, 1);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        EasyMock.verify(listener);
    }

    public void testDontGenerateBarOnAggregatedTrades() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        IBarFactoryListener listener = EasyMock.createMock(IBarFactoryListener.class);
        EasyMock.replay(listener);
        factory.addBarFactoryListener(listener);

        currentTime.add(Calendar.SECOND, 30);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.1, 100L, 1000L));
        currentTime.add(Calendar.SECOND, 29);
        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 0.9, 100L, 1000L));

        EasyMock.verify(listener);
    }

    public void testSetBarCloseTime() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(currentTime.getTime(), 1.0, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();

        currentTime.add(Calendar.SECOND, 60);
        assertEquals(currentTime.getTime(), data.dateClose);
    }

    public void testIgnoreTradeWithNullDate() throws Exception {
        factory.add(security, TimeSpan.minutes(1));

        pricingEnvironment.setTrade(security, new Trade(null, 1.0, 100L, 1000L));

        BarFactory.Data data = factory.map.get(security).iterator().next();
        assertNull(data.open);
        assertNull(data.high);
        assertNull(data.low);
        assertNull(data.close);
        assertNull(data.volume);
    }
}
