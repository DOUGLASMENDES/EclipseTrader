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

package org.eclipsetrader.internal.brokers.paper.schemes;

import junit.framework.TestCase;

public class LimitedProportional1SchemeTest extends TestCase {

    public void testGetBuyExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(8.55, scheme.getBuyExpenses(1000L, 4.50));
    }

    public void testGetMinimumBuyExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(2.0, scheme.getBuyExpenses(100L, 4.50));
    }

    public void testGetMaximumBuyExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(19.0, scheme.getBuyExpenses(10000L, 4.50));
    }

    public void testGetSellExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(8.55, scheme.getSellExpenses(1000L, 4.50));
    }

    public void testGetMinimumSellExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(2.0, scheme.getSellExpenses(100L, 4.50));
    }

    public void testGetMaximumSellExpenses() throws Exception {
        LimitedProportional1Scheme scheme = new LimitedProportional1Scheme();
        assertEquals(19.0, scheme.getSellExpenses(10000L, 4.50));
    }
}
