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

package org.eclipsetrader.repository.local.internal.stores;

import junit.framework.TestCase;

import org.eclipsetrader.repository.local.Helper;

public class TradeStoreTest extends TestCase {

    public void testMarshalEmpty() throws Exception {
        TradeStore object = new TradeStore();
        assertEquals(Helper.XML_PREFIX + "<trade/>", Helper.marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        TradeStore object = (TradeStore) Helper.unmarshal(Helper.XML_PREFIX + "<trade/>", TradeStore.class);
        assertNotNull(object);
    }
}
