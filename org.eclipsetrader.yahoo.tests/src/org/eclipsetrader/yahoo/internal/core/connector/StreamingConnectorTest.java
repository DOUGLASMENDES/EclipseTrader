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

package org.eclipsetrader.yahoo.internal.core.connector;

import java.util.Map;

import junit.framework.TestCase;

public class StreamingConnectorTest extends TestCase {

    public void testParseUnixTime() throws Exception {
        StreamingConnector connector = new StreamingConnector();

        String s = "parent.yfs_mktmcb({\"unixtime\":1246097383,\"open\":0,\"close\":0});";
        Map<String, String> map = connector.parseScript(s);

        assertEquals("1246097383", map.get("unixtime"));
    }

    public void testParseLastPrice() throws Exception {
        StreamingConnector connector = new StreamingConnector();

        String s = "parent.yfs_u1f({\"MSFT\":{l10:\"23.35\",c10:\"-0.44\",p20:\"-1.85\"}});";
        Map<String, String> map = connector.parseScript(s);

        assertEquals("MSFT", map.get(StreamingConnector.K_SYMBOL));
        assertEquals("23.35", map.get(StreamingConnector.K_LAST));
    }
}
