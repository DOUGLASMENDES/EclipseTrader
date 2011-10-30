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

import java.util.Arrays;

import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.ui.DataProviderFactoryMock;
import org.eclipsetrader.ui.DatabindingTestCase;

public class WatchListViewModelTest extends DatabindingTestCase {

    public void testAddNewColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
        });

        WatchListViewModel model = new WatchListViewModel(watchList, new PricingEnvironment());
        assertEquals(1, model.getColumns().size());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, model.getColumns().size());
        assertEquals(newColumns[0], model.getColumns().get(0));
        assertEquals(newColumns[1], model.getColumns().get(1));
    }

    public void testRemoveOldColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListViewModel model = new WatchListViewModel(watchList, new PricingEnvironment());
        assertEquals(2, model.getColumns().size());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        model.setColumns(Arrays.asList(newColumns));

        assertEquals(1, model.getColumns().size());
        assertEquals(newColumns[0], model.getColumns().get(0));
    }

    public void testReorderColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListViewModel model = new WatchListViewModel(watchList, new PricingEnvironment());
        assertEquals(2, model.getColumns().size());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
        };
        model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, model.getColumns().size());
        assertEquals(newColumns[0], model.getColumns().get(0));
        assertEquals(newColumns[1], model.getColumns().get(1));
    }

    public void testMoreComplexColumnsReorder() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
            new WatchListColumn("Col3", new DataProviderFactoryMock("id3")),
            new WatchListColumn("Col4", new DataProviderFactoryMock("id4")),
            new WatchListColumn("Col5", new DataProviderFactoryMock("id5")),
        });

        WatchListViewModel model = new WatchListViewModel(watchList, new PricingEnvironment());
        assertEquals(5, model.getColumns().size());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col6", new DataProviderFactoryMock("id6"))),
            new WatchListViewColumn(new WatchListColumn("Col3", new DataProviderFactoryMock("id3"))),
            new WatchListViewColumn(new WatchListColumn("Col5", new DataProviderFactoryMock("id5"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
            new WatchListViewColumn(new WatchListColumn("Col7", new DataProviderFactoryMock("id7"))),
        };
        model.setColumns(Arrays.asList(newColumns));

        assertEquals(6, model.getColumns().size());
        assertEquals(newColumns[0], model.getColumns().get(0));
        assertEquals(newColumns[1], model.getColumns().get(1));
        assertEquals(newColumns[2], model.getColumns().get(2));
        assertEquals(newColumns[3], model.getColumns().get(3));
        assertEquals(newColumns[4], model.getColumns().get(4));
        assertEquals(newColumns[5], model.getColumns().get(5));
    }

    public void testRenameColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListViewModel model = new WatchListViewModel(watchList, new PricingEnvironment());
        assertEquals(2, model.getColumns().size());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("NewCol1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, model.getColumns().size());
        assertEquals(newColumns[0].getName(), model.getColumns().get(0).getName());
    }
}
