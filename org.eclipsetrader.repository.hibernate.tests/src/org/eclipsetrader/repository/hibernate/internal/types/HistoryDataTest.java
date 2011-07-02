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

package org.eclipsetrader.repository.hibernate.internal.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class HistoryDataTest extends TestCase {

    public void testListContains() throws Exception {
        Date date = new Date();
        List<HistoryData> l = new ArrayList<HistoryData>();
        l.add(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L), TimeSpan.minutes(1)));
        assertTrue(l.contains(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L), TimeSpan.minutes(1))));
        assertFalse(l.contains(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 1L), TimeSpan.minutes(1))));
        assertTrue(l.contains(new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L)));
        assertFalse(l.contains(new OHLC(date, 1.0, 2.0, 3.0, 4.0, 1L)));
    }

    public void testSetContains() throws Exception {
        Date date = new Date();
        Set<IOHLC> l = new HashSet<IOHLC>();
        l.add(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L), TimeSpan.minutes(1)));
        assertTrue(l.contains(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L), TimeSpan.minutes(1))));
        assertFalse(l.contains(new HistoryData(null, new OHLC(date, 1.0, 2.0, 3.0, 4.0, 1L), TimeSpan.minutes(1))));
        assertFalse(l.contains(new OHLC(date, 1.0, 2.0, 3.0, 4.0, 5L)));
        assertFalse(l.contains(new OHLC(date, 1.0, 2.0, 3.0, 4.0, 1L)));
    }
}
