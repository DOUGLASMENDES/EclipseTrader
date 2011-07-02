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

package org.eclipsetrader.directa.internal.core.messages;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

    public void testGetDataOra() throws Exception {
        byte[] packet = new byte[] {
                1,
                124,
                37,
                -25,
                -62,
                2,
                80,
                76,
                84,
                -49,
                -9,
                -61,
                63,
                124,
                37,
                -25,
                -62,
                0,
                0,
                39,
                16,
                -37,
                45,
                -32,
                75,
                1,
                36,
                74,
                52,
                92,
                -113,
                -62,
                63,
                -57,
                75,
                -57,
                63,
                0,
                0,
                12,
                68,
        };
        assertEquals(getTime(2008, Calendar.JULY, 15, 10, 50, 29).getTime(), Util.getDataOra(packet, 1, 0));
    }

    public void testByteToInt() throws Exception {
        assertEquals(25, Util.byteToInt((byte) 25));
        assertEquals(231, Util.byteToInt((byte) 231));
        assertEquals(231, Util.byteToInt((byte) -25));
    }

    private Date getTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, second);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
