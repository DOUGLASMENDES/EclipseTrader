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

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.ui.DataProviderFactoryMock;
import org.eclipsetrader.ui.DatabindingTestCase;

public class WatchListViewTest extends DatabindingTestCase {

    Shell shell;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getDefault());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testCreateViewer() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(2, viewer.getTable().getColumnCount());
    }

    public void testAddNewColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(1, viewer.getTable().getColumnCount());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        view.model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, viewer.getTable().getColumnCount());
        assertEquals(newColumns[0].getName(), viewer.getTable().getColumn(0).getText());
        assertEquals(newColumns[1].getName(), viewer.getTable().getColumn(1).getText());
    }

    public void testRemoveOldColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(2, viewer.getTable().getColumnCount());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        view.model.setColumns(Arrays.asList(newColumns));

        assertEquals(1, viewer.getTable().getColumnCount());
        assertEquals(newColumns[0].getName(), viewer.getTable().getColumn(0).getText());
    }

    public void testReorderColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(2, viewer.getTable().getColumnCount());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
        };
        view.model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, viewer.getTable().getColumnCount());
        assertEquals(newColumns[0].getName(), viewer.getTable().getColumn(0).getText());
        assertEquals(newColumns[1].getName(), viewer.getTable().getColumn(1).getText());
    }

    public void testMoreComplexColumnsReorder() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
            new WatchListColumn("Col3", new DataProviderFactoryMock("id3")),
            new WatchListColumn("Col4", new DataProviderFactoryMock("id4")),
            new WatchListColumn("Col5", new DataProviderFactoryMock("id5")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(5, viewer.getTable().getColumnCount());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("Col1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col6", new DataProviderFactoryMock("id6"))),
            new WatchListViewColumn(new WatchListColumn("Col3", new DataProviderFactoryMock("id3"))),
            new WatchListViewColumn(new WatchListColumn("Col5", new DataProviderFactoryMock("id5"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
            new WatchListViewColumn(new WatchListColumn("Col7", new DataProviderFactoryMock("id7"))),
        };
        view.model.setColumns(Arrays.asList(newColumns));

        assertEquals(6, viewer.getTable().getColumnCount());
        assertEquals(newColumns[0].getName(), viewer.getTable().getColumn(0).getText());
        assertEquals(newColumns[1].getName(), viewer.getTable().getColumn(1).getText());
        assertEquals(newColumns[2].getName(), viewer.getTable().getColumn(2).getText());
        assertEquals(newColumns[3].getName(), viewer.getTable().getColumn(3).getText());
        assertEquals(newColumns[4].getName(), viewer.getTable().getColumn(4).getText());
        assertEquals(newColumns[5].getName(), viewer.getTable().getColumn(5).getText());
    }

    public void testRenameColumns() throws Exception {
        WatchList watchList = new WatchList("Test", new IWatchListColumn[] {
            new WatchListColumn("Col1", new DataProviderFactoryMock("id1")),
            new WatchListColumn("Col2", new DataProviderFactoryMock("id2")),
        });

        WatchListView view = new WatchListView();
        view.preferenceStore = new PreferenceStore();
        view.watchList = watchList;
        view.model = new WatchListViewModel(watchList, new PricingEnvironment());

        TableViewer viewer = view.createViewer(shell);

        assertEquals(2, viewer.getTable().getColumnCount());

        WatchListViewColumn[] newColumns = new WatchListViewColumn[] {
            new WatchListViewColumn(new WatchListColumn("NewCol1", new DataProviderFactoryMock("id1"))),
            new WatchListViewColumn(new WatchListColumn("Col2", new DataProviderFactoryMock("id2"))),
        };
        view.model.setColumns(Arrays.asList(newColumns));

        assertEquals(2, viewer.getTable().getColumnCount());
        assertEquals(newColumns[0].getName(), viewer.getTable().getColumn(0).getText());
        assertEquals(newColumns[1].getName(), viewer.getTable().getColumn(1).getText());
    }
}
