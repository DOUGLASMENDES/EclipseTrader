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

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStoreProperties;

public class IntradayHistoryStoreTest extends TestCase {

    private Date date = new Date();
    private Security security = new Security("Test", null);

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        File f = new File("test.xml");
        if (f.exists()) {
            f.delete();
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        File f = new File("test.xml");
        if (f.exists()) {
            f.delete();
        }
    }

    public void testFetchPropertiesFromNewStore() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(1, security, date) {

            @Override
            protected File getFile() {
                return new File("test.xml");
            }
        };
        IStoreProperties properties = store.fetchProperties(null);
        assertEquals(3, properties.getPropertyNames().length);
        assertSame(security, properties.getProperty(IPropertyConstants.SECURITY));
        assertEquals(date, properties.getProperty(IPropertyConstants.BARS_DATE));
        assertSame(IHistory.class.getName(), properties.getProperty(IPropertyConstants.OBJECT_TYPE));
    }

    public void testUpdateStoreProperties() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(1, security, date) {

            @Override
            protected File getFile() {
                return new File("test.xml");
            }
        };
        IStoreProperties properties = store.fetchProperties(null);
        properties.setProperty(TimeSpan.minutes(1).toString(), new IOHLC[] {});
        store.putProperties(properties, null);

        properties = store.fetchProperties(null);
        assertEquals(4, properties.getPropertyNames().length);
        assertSame(security, properties.getProperty(IPropertyConstants.SECURITY));
        assertEquals(date, properties.getProperty(IPropertyConstants.BARS_DATE));
        assertNotNull(properties.getProperty(TimeSpan.minutes(1).toString()));
        assertSame(IHistory.class.getName(), properties.getProperty(IPropertyConstants.OBJECT_TYPE));
    }
}
