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

package org.eclipsetrader.core.feed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

public class BarGeneratorTest extends TestCase {

    public void testSetBarCloseTime() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));

        assertEquals(new Date(60 * 1000), generator.dateClose);
    }

    public void testSetInitialValues() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));

        assertEquals(1.0, generator.open);
        assertEquals(1.0, generator.high);
        assertEquals(1.0, generator.low);
        assertEquals(1.0, generator.close);
        assertEquals(new Long(100), generator.volume);
    }

    public void testSetHighestValue() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));

        assertEquals(1.1, generator.high);
        assertEquals(1.0, generator.low);
    }

    public void testSetLowestValue() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));

        assertEquals(1.0, generator.high);
        assertEquals(0.9, generator.low);
    }

    public void testSetCloseToLatestTrade() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));

        assertEquals(0.9, generator.close);
    }

    public void testSetOpenToFirstTrade() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));

        assertEquals(1.0, generator.open);
    }

    public void testAddTradeGeneratesBarOpen() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));

        IBarOpen barOpen = (IBarOpen) events.get(0);
        assertEquals(new Date(0), barOpen.getDate());
        assertEquals(TimeSpan.minutes(1), barOpen.getTimeSpan());
        assertEquals(1.0, barOpen.getOpen());
    }

    public void testAddTradeGeneratesBar() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(30 * 1000), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(59 * 1000), 0.9, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });
        generator.addTrade(new Trade(new Date(60 * 1000), 1.0, 100L, 1000L));

        IBar bar = (IBar) events.get(0);
        assertEquals(new Date(0), bar.getDate());
        assertEquals(TimeSpan.minutes(1), bar.getTimeSpan());
        assertEquals(1.0, bar.getOpen());
        assertEquals(1.1, bar.getHigh());
        assertEquals(0.9, bar.getLow());
        assertEquals(0.9, bar.getClose());
        assertEquals(new Long(300), bar.getVolume());
    }

    public void testDontGenerateBarOnAggregatedTrades() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0 * 1000), 1.0, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });

        generator.addTrade(new Trade(new Date(30 * 1000), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(59 * 1000), 0.9, 100L, 1000L));

        assertEquals(0, events.size());
    }

    public void testForceBarClose() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));
        generator.addTrade(new Trade(new Date(0), 1.0, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 1.1, 100L, 1000L));
        generator.addTrade(new Trade(new Date(0), 0.9, 100L, 1000L));

        final List<Object> events = new ArrayList<Object>();
        generator.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                events.add(arg);
            }
        });

        generator.forceBarClose();

        IBar bar = (IBar) events.get(0);
        assertEquals(new Date(0), bar.getDate());
        assertEquals(TimeSpan.minutes(1), bar.getTimeSpan());
        assertEquals(1.0, bar.getOpen());
        assertEquals(1.1, bar.getHigh());
        assertEquals(0.9, bar.getLow());
        assertEquals(0.9, bar.getClose());
        assertEquals(new Long(300), bar.getVolume());
    }

    public void testBarNotExpired() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(), 1.0, 100L, 1000L));

        assertFalse(generator.isBarExpired());
    }

    public void testBarExpired() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(new Date(new Date().getTime() - 60 * 1000), 1.0, 100L, 1000L));

        assertTrue(generator.isBarExpired());
    }

    public void testIgnoreTradeWithNullDate() throws Exception {
        BarGenerator generator = new BarGenerator(TimeSpan.minutes(1));

        generator.addTrade(new Trade(null, 1.0, 100L, 1000L));

        assertNull(generator.open);
        assertNull(generator.high);
        assertNull(generator.low);
        assertNull(generator.close);
        assertNull(generator.volume);
    }
}
