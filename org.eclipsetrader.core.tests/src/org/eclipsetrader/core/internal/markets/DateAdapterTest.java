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

package org.eclipsetrader.core.internal.markets;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class DateAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        assertEquals("2007-11-05", new DateAdapter().marshal(getTime(2007, Calendar.NOVEMBER, 5)));
    }

    public void testMarshalNull() throws Exception {
        assertNull(new DateAdapter().marshal(null));
    }

    public void testUnmarshal() throws Exception {
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5), new DateAdapter().unmarshal("2007-11-05"));
    }

    public void testUnmarshalNull() throws Exception {
        assertNull(new DateAdapter().unmarshal(null));
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
