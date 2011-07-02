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

import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.OHLC;

public class EmbeddableOHLCTest extends TestCase {

    public void testHashCode() throws Exception {
        OHLC ohlc = new OHLC(new Date(), 10.0, 20.0, 30.0, 40.0, 1000L);
        EmbeddableOHLC o = new EmbeddableOHLC(ohlc);
        assertEquals(ohlc.hashCode(), o.hashCode());
    }

    public void testEquals() throws Exception {
        OHLC ohlc = new OHLC(new Date(), 10.0, 20.0, 30.0, 40.0, 1000L);
        EmbeddableOHLC o = new EmbeddableOHLC(ohlc);
        assertTrue(o.equals(ohlc));
    }
}
