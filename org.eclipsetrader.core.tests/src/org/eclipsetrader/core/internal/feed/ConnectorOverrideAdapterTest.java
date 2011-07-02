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

package org.eclipsetrader.core.internal.feed;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

public class ConnectorOverrideAdapterTest extends TestCase {

    private String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private File file = new File("overrides.xml");

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (file.exists()) {
            file.delete();
        }
    }

    public void testLoadMissingFile() throws Exception {
        if (file.exists()) {
            file.delete();
        }
        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        assertEquals(0, adapter.getList().size());
    }

    public void testLoadEmptyFile() throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(header + "<list/>\n");
        writer.close();

        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        assertEquals(0, adapter.getList().size());
    }

    public void testLoadFile() throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(header + "<list><item security=\"local:securities#1\"/></list>\n");
        writer.close();

        ConnectorOverrideAdapter adapter = new ConnectorOverrideAdapter(file);
        assertEquals(1, adapter.getList().size());
    }
}
