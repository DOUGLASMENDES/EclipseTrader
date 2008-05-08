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

package org.eclipsetrader.core.internal.views;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.views.IWatchListColumn;

public class WatchListTest extends TestCase {

	public void testRemoveItemsFromDerivedViews() throws Exception {
		WatchList list = new WatchList("List", new IWatchListColumn[0]);
	    list.addSecurity(new Security("Security", new FeedIdentifier("ID", null)));
	    WatchListView view = (WatchListView) list.getView();
	    assertEquals(1, view.getElements().length);
	    list.removeItems(list.getItems());
	    assertEquals(0, view.getElements().length);
    }
}
