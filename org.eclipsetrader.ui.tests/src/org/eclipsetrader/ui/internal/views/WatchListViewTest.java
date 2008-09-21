/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.views.WatchList;
import org.eclipsetrader.core.views.WatchListColumn;
import org.eclipsetrader.core.views.WatchListElement;

public class WatchListViewTest extends TestCase {
	private Shell shell;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getCurrent());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		while (Display.getCurrent().readAndDispatch());
	}

	public void testBuildColumns() throws Exception {
	    WatchList watchList = new WatchList("Test", new WatchListColumn[] {
	    		new WatchListColumn("Col1", null),
	    		new WatchListColumn("Col2", null)
	    	});

	    WatchListView view = new WatchListView(watchList, null);
	    WatchListViewColumn[] columns = view.getColumns();
	    assertEquals(2, columns.length);
	    assertEquals("Col1", columns[0].getName());
	    assertEquals("Col2", columns[1].getName());
    }

	public void testBuildItems() throws Exception {
	    WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
	    watchList.setItems(new WatchListElement[] {
	    		new WatchListElement(new Security("Test1", new FeedIdentifier("ID1", null))),
	    		new WatchListElement(new Security("Test2", new FeedIdentifier("ID2", null))),
	    	});

	    WatchListView view = new WatchListView(watchList, null);
	    WatchListViewItem[] items = view.getItems();
	    assertEquals(2, items.length);
    }

	public void testAggregateItemsWithSameSecurity() throws Exception {
		Security security = new Security("Test1", new FeedIdentifier("ID1", null));
	    WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
	    watchList.setItems(new WatchListElement[] {
	    		new WatchListElement(security),
	    		new WatchListElement(security),
	    	});

	    WatchListView view = new WatchListView(watchList, null);
	    assertEquals(1, view.getItemsMap().size());
	    assertEquals(2, view.getItemsMap().get(security).size());
    }

	public void testNotifyAddedElement() throws Exception {
		WatchListElement element1 = new WatchListElement(new Security("Test1", new FeedIdentifier("ID1", null)));
		WatchListElement element2 = new WatchListElement(new Security("Test2", new FeedIdentifier("ID2", null)));

		WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
	    watchList.setItems(new WatchListElement[] { element1, element2 });

	    WatchListView view = new WatchListView(watchList, null);
	    WatchListViewItem[] items = view.getItems();
	    assertEquals(2, items.length);

	    WatchListElement element3 = new WatchListElement(new Security("Test3", new FeedIdentifier("ID3", null)));
	    watchList.setItems(new WatchListElement[] { element1, element2, element3 });

	    items = view.getItems();
	    assertEquals(3, items.length);
	    assertEquals(3, view.getItemsMap().size());
    }

	public void testNotifyRemovedElement() throws Exception {
		WatchListElement element1 = new WatchListElement(new Security("Test1", new FeedIdentifier("ID1", null)));
		WatchListElement element2 = new WatchListElement(new Security("Test2", new FeedIdentifier("ID2", null)));

		WatchList watchList = new WatchList("Test", new WatchListColumn[0]);
	    watchList.setItems(new WatchListElement[] { element1, element2 });

	    WatchListView view = new WatchListView(watchList, null);
	    WatchListViewItem[] items = view.getItems();
	    assertEquals(2, items.length);

	    watchList.setItems(new WatchListElement[] { element1 });

	    items = view.getItems();
	    assertEquals(1, items.length);
	    assertEquals(1, view.getItemsMap().size());
    }
}
