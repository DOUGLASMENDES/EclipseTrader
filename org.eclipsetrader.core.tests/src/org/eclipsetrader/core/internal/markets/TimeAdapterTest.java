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

public class TimeAdapterTest extends TestCase {

    public void testMarshal() throws Exception {
        assertEquals("15:30", new TimeAdapter().marshal(getTime(15, 30)));
    }

    public void testMarshalNull() throws Exception {
        assertNull(new TimeAdapter().marshal(null));
    }

    public void testUnmarshal() throws Exception {
        assertEquals(getTime(15, 30), new TimeAdapter().unmarshal("15:30"));
    }

    public void testUnmarshalNull() throws Exception {
        assertNull(new TimeAdapter().unmarshal(null));
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(0);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        return date.getTime();
    }
}
