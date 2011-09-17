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

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.TimeSpan.Units;

public class TimeSpanTest extends TestCase {

    public void testMinutesToString() throws Exception {
        assertEquals("5min", TimeSpan.minutes(5).toString());
    }

    public void testMinutesFromString() throws Exception {
        TimeSpan aggr = TimeSpan.fromString("5min");
        assertEquals(Units.Minutes, aggr.getUnits());
        assertEquals(5, aggr.getLength());
    }

    public void testDaysToString() throws Exception {
        assertEquals("5d", TimeSpan.days(5).toString());
    }

    public void testDaysFromString() throws Exception {
        TimeSpan aggr = TimeSpan.fromString("5d");
        assertEquals(Units.Days, aggr.getUnits());
        assertEquals(5, aggr.getLength());
    }

    public void testLowerThan() throws Exception {
        TimeSpan ref = TimeSpan.minutes(30);
        assertTrue(ref.lowerThan(TimeSpan.minutes(60)));
        assertFalse(ref.lowerThan(TimeSpan.minutes(10)));
        assertTrue(ref.lowerThan(TimeSpan.days(30)));
    }

    public void testHigherThan() throws Exception {
        TimeSpan ref = TimeSpan.minutes(30);
        assertFalse(ref.higherThan(TimeSpan.minutes(60)));
        assertTrue(ref.higherThan(TimeSpan.minutes(10)));
        assertFalse(ref.higherThan(TimeSpan.days(30)));
    }
}
