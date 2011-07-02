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

public class TwoLevelsPerShareSchemeTest extends TestCase {

    public void testMinimumExpenses() throws Exception {
        assertEquals(1.0, new TwoLevelsPerShareScheme().getBuyExpenses(50L, 5.5));
        assertEquals(1.0, new TwoLevelsPerShareScheme().getSellExpenses(50L, 5.5));
    }

    public void testGetLessThan500SharesExpenses() throws Exception {
        assertEquals(3.0, new TwoLevelsPerShareScheme().getBuyExpenses(300L, 5.5));
        assertEquals(3.0, new TwoLevelsPerShareScheme().getSellExpenses(300L, 5.5));
    }

    public void testMoreThanThan500SharesExpenses() throws Exception {
        assertEquals(5.0, new TwoLevelsPerShareScheme().getBuyExpenses(500L, 5.5));
        assertEquals(5.0, new TwoLevelsPerShareScheme().getSellExpenses(500L, 5.5));
        assertEquals(6.25, new TwoLevelsPerShareScheme().getBuyExpenses(750L, 5.5));
        assertEquals(6.25, new TwoLevelsPerShareScheme().getSellExpenses(750L, 5.5));
    }
}
