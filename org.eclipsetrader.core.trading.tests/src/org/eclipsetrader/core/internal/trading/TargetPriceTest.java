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

package org.eclipsetrader.core.internal.trading;

import junit.framework.TestCase;

public class TargetPriceTest extends TestCase {

    public void testUpperPrice() throws Exception {
        TargetPrice o = new TargetPrice();
        o.price = 1.5;
        o.initialPrice = 1.4;

        o.updateTrigger(1.5);

        assertTrue(o.triggered);
    }

    public void testLowerPrice() throws Exception {
        TargetPrice o = new TargetPrice();
        o.price = 1.5;
        o.initialPrice = 1.6;

        o.updateTrigger(1.5);

        assertTrue(o.triggered);
    }

    public void testCrossUp() throws Exception {
        TargetPrice o = new TargetPrice();
        o.price = 1.5;
        o.cross = true;
        o.initialPrice = 1.4;

        o.updateTrigger(1.5);
        assertFalse(o.triggered);

        o.updateTrigger(1.51);
        assertTrue(o.triggered);
    }

    public void testCrossDown() throws Exception {
        TargetPrice o = new TargetPrice();
        o.price = 1.5;
        o.cross = true;
        o.initialPrice = 1.6;

        o.updateTrigger(1.5);
        assertFalse(o.triggered);

        o.updateTrigger(1.49);
        assertTrue(o.triggered);
    }
}
