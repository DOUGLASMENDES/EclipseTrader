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

package org.eclipsetrader.ui.internal.views;

import junit.framework.TestCase;

import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class WatchListViewColumnTest extends TestCase {

    public void testSetName() throws Exception {
        WatchListViewColumn column = new WatchListViewColumn(null, new DataProviderFactory("id1", "Factory1"));
        assertNull(column.getName());
        column.setName("New Name");
        assertEquals("New Name", column.getName());
    }

    public void testSetNameFromConstructor() throws Exception {
        WatchListViewColumn column = new WatchListViewColumn("Name", new DataProviderFactory("id1", "Factory1"));
        assertEquals("Name", column.getName());
    }

    public void testSetDataProviderFactoryFromConstructor() throws Exception {
        DataProviderFactory factory = new DataProviderFactory("id1", "Factory1");
        WatchListViewColumn column = new WatchListViewColumn("Name", factory);
        assertSame(factory, column.getDataProviderFactory());
    }

    public void testEqualsWithoutName() throws Exception {
        WatchListViewColumn column1 = new WatchListViewColumn(null, new DataProviderFactory("id1", "Factory1"));
        assertTrue(column1.equals(new WatchListViewColumn(null, new DataProviderFactory("id1", "Factory1"))));
        assertFalse(column1.equals(new WatchListViewColumn("Test", new DataProviderFactory("id1", "Factory1"))));
        assertFalse(column1.equals(new WatchListViewColumn(null, new DataProviderFactory("id2", "Factory2"))));
    }

    public void testEqualsWithName() throws Exception {
        WatchListViewColumn column1 = new WatchListViewColumn("Column1", new DataProviderFactory("id1", "Factory1"));
        assertFalse(column1.equals(new WatchListViewColumn(null, new DataProviderFactory("id1", "Factory1"))));
        assertTrue(column1.equals(new WatchListViewColumn("Column1", new DataProviderFactory("id1", "Factory1"))));
        assertFalse(column1.equals(new WatchListViewColumn("Test", new DataProviderFactory("id1", "Factory1"))));
        assertFalse(column1.equals(new WatchListViewColumn(null, new DataProviderFactory("id2", "Factory2"))));
    }

    private class DataProviderFactory implements IDataProviderFactory {

        private String id;
        private String name;

        public DataProviderFactory(String id, String name) {
            this.id = id;
            this.name = name;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
         */
        @Override
        public IDataProvider createProvider() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getId()
         */
        @Override
        public String getId() {
            return id;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getName()
         */
        @Override
        public String getName() {
            return name;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
         */
        @Override
        @SuppressWarnings("unchecked")
        public Class[] getType() {
            return null;
        }
    }
}
