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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class TradeTest extends TestCase {

    public void testGetTime() throws Exception {
        Calendar now = Calendar.getInstance();
        Trade trade = new Trade(now.getTime(), null, null, null);
        assertEquals(now.getTime(), trade.getTime());
    }

    public void testGetPrice() throws Exception {
        Trade trade = new Trade(null, 14.5, null, null);
        assertEquals(14.5, trade.getPrice());
    }

    public void testGetSize() throws Exception {
        Trade trade = new Trade(null, null, 15000L, null);
        assertEquals(new Long(15000), trade.getSize());
    }

    public void testGetVolume() throws Exception {
        Trade trade = new Trade(null, null, null, 2500000L);
        assertEquals(new Long(2500000), trade.getVolume());
    }

    public void testSerializable() throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(new Trade(new Date(), 3.5, 100L, 2500000L));
        os.close();
    }
}
