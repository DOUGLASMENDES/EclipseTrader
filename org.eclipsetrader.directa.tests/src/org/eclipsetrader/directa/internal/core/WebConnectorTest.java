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

package org.eclipsetrader.directa.internal.core;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.NameValuePair;

public class WebConnectorTest extends TestCase {

    public void testModeUpdate() throws Exception {
        List<NameValuePair> query = new ArrayList<NameValuePair>();

        query.add(new NameValuePair("MODO", "C"));
        assertEquals(1, query.size());
        assertTrue(query.contains(new NameValuePair("MODO", "C")));

        query.remove(new NameValuePair("MODO", "C"));
        assertEquals(0, query.size());

        query.add(new NameValuePair("MODO", "V"));
        assertEquals(1, query.size());
        assertTrue(query.contains(new NameValuePair("MODO", "V")));
    }
}
