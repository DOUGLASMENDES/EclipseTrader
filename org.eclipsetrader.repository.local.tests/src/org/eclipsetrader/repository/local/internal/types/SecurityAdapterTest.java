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

package org.eclipsetrader.repository.local.internal.types;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.repository.local.TestRepositoryService;
import org.eclipsetrader.repository.local.TestSecurity;

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
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new ISecurity[] {
            new TestSecurity("Test", null, "local:securities#1")
        });
        SecurityAdapter.setRepositoryService(repositoryService);
        SecurityAdapter adapter = new SecurityAdapter();
        ISecurity security = adapter.unmarshal("local:securities#1");
        assertNotNull(security);
        assertEquals("Test", security.getName());
    }
}
