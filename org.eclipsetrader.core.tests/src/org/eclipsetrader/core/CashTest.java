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

package org.eclipsetrader.core;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class CashTest extends TestCase {

    public void testEquals() throws Exception {
        Cash cash = new Cash(1.0, Currency.getInstance("USD"));
        assertTrue(cash.equals(new Cash(1.0, Currency.getInstance("USD"))));
    }

    public void testEqualsToOtherObjects() throws Exception {
        Cash cash = new Cash(1.0, Currency.getInstance("USD"));
        assertFalse(cash.equals(new Object()));
    }

    public void testAddToHashSet() throws Exception {
        Set<Cash> set = new HashSet<Cash>();
        set.add(new Cash(1.0, Currency.getInstance("USD")));
        assertTrue(set.contains(new Cash(1.0, Currency.getInstance("USD"))));
        assertFalse(set.contains(new Cash(1.0, Currency.getInstance("EUR"))));
    }
}
