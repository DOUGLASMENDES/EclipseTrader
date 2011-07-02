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

package org.eclipsetrader.news.internal.repository;

import java.net.URI;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.internal.repository.SecurityAdapter.FailsafeSecurity;
import org.eclipsetrader.news.tests.TestSecurity;

public class SecurityAdapterTest extends TestCase {

    public void testMarshalNull() throws Exception {
        SecurityAdapter adapter = new SecurityAdapter();
        assertNull(adapter.marshal(null));
    }

    public void testUnmarshalNull() throws Exception {
        SecurityAdapter adapter = new SecurityAdapter();
        assertNull(adapter.unmarshal(null));
    }

    public void testMarshalSecurity() throws Exception {
        SecurityAdapter adapter = new SecurityAdapter();
        assertEquals("local:securities#1", adapter.marshal(new TestSecurity("Test", null, "local:securities#1")));
    }

    public void testUnmarshalSecurity() throws Exception {
        final TestSecurity repositorySecurity = new TestSecurity("Test", null, "local:securities#1");
        SecurityAdapter adapter = new SecurityAdapter() {

            @Override
            protected ISecurity getSecurity(URI uri) {
                if (repositorySecurity.toURI().equals(uri)) {
                    return repositorySecurity;
                }
                return null;
            }
        };
        ISecurity security = adapter.unmarshal("local:securities#1");
        assertSame(repositorySecurity, security);
    }

    public void testUnmarshalUnknownSecurity() throws Exception {
        SecurityAdapter adapter = new SecurityAdapter() {

            @Override
            protected ISecurity getSecurity(URI uri) {
                return null;
            }
        };
        ISecurity security = adapter.unmarshal("local:securities#1");
        assertTrue(security instanceof FailsafeSecurity);
    }
}
