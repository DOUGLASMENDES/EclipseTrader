/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.core.repository;

import junit.framework.TestCase;

public class IdentifiersListTest extends TestCase {
	private IdentifiersList list;

	/* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    	list = new IdentifiersList();
    }

	public void testGetIdentifierForSymbol() throws Exception {
		IdentifierType type = list.getIdentifierFor("MSFT");
		assertEquals("MSFT", type.getSymbol());
		assertEquals("@", type.getExchange());
		type = list.getIdentifierFor("Q:MSFT");
		assertEquals("MSFT", type.getSymbol());
		assertEquals("Q", type.getExchange());
		type = list.getIdentifierFor("ec:/YMH8");
		assertEquals("/YMH8", type.getSymbol());
		assertEquals("ec", type.getExchange());
		type = list.getIdentifierFor("is\\GOOG");
		assertEquals("GOOG", type.getSymbol());
		assertEquals("is", type.getExchange());
    }
}
