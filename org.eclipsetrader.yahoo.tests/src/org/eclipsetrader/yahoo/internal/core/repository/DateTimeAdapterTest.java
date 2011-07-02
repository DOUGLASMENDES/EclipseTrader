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

package org.eclipsetrader.yahoo.internal.core.repository;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class DateTimeAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        assertEquals("2007-11-06 15:30:00", new DateTimeAdapter().marshal(getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0)));
    }

    public void testMarshalNull() throws Exception {
        assertNull(new DateTimeAdapter().marshal(null));
    }

    public void testUnmarshal() throws Exception {
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0), new DateTimeAdapter().unmarshal("2007-11-06 15:30:00"));
    }

    public void testUnmarshalNull() throws Exception {
        assertNull(new DateTimeAdapter().unmarshal(null));
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
