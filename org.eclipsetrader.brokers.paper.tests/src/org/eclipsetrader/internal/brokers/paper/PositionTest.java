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

package org.eclipsetrader.internal.brokers.paper;

import junit.framework.TestCase;

public class PositionTest extends TestCase {

    public void testAdd() throws Exception {
        Position p = new Position();
        p.add(100L, 1.5);
        assertEquals(new Long(100), p.getQuantity());
        assertEquals(1.5, p.getPrice());
    }

    public void testAddToExisting() throws Exception {
        Position p = new Position(null, 100L, 1.7);
        p.add(100L, 1.5);
        assertEquals(new Long(200), p.getQuantity());
        assertEquals(1.6, p.getPrice());
    }

    public void testRemoveFromExisting() throws Exception {
        Position p = new Position(null, 100L, 1.7);
        p.add(-50L, 1.5);
        assertEquals(new Long(50), p.getQuantity());
        assertEquals(1.7, p.getPrice());
    }

    public void testAddShort() throws Exception {
        Position p = new Position();
        p.add(-100L, 1.5);
        assertEquals(new Long(-100), p.getQuantity());
        assertEquals(1.5, p.getPrice());
    }

    public void test1PassSwitchToShort() throws Exception {
        Position p = new Position(null, 100L, 1.7);
        p.add(-200L, 1.5);
        assertEquals(new Long(-100), p.getQuantity());
        assertEquals(1.5, p.getPrice());
    }

    public void test2PassSwitchToShort() throws Exception {
        Position p = new Position(null, 100L, 1.7);
        p.add(-100L, 1.5);
        p.add(-100L, 1.5);
        assertEquals(new Long(-100), p.getQuantity());
        assertEquals(1.5, p.getPrice());
    }
}
