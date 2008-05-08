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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class BoxViewerTest extends TestCase {
	private Shell shell;
	private Dialog dlg;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Display display = Display.getCurrent();
		shell = new Shell(display);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (dlg != null) {
			dlg.close();
			dlg = null;
		}
		shell.dispose();
		while (Display.getDefault().readAndDispatch());
	}

	public void testDoSetItemCount() throws Exception {
	    BoxViewer viewer = new BoxViewer(shell);
	    assertEquals(0, viewer.doGetItemCount());
	    viewer.doSetItemCount(5);
	    assertEquals(5, viewer.doGetItemCount());
	    assertEquals(5, viewer.doGetItems().length);
    }

	public void testDoShrinkItemCount() throws Exception {
	    BoxViewer viewer = new BoxViewer(shell);
	    assertEquals(0, viewer.doGetItemCount());
	    viewer.doSetItemCount(5);
	    Item[] items = viewer.doGetItems();
	    viewer.doSetItemCount(3);
	    assertEquals(3, viewer.doGetItemCount());
	    assertFalse(items[0].isDisposed());
	    assertFalse(items[1].isDisposed());
	    assertFalse(items[2].isDisposed());
	    assertTrue(items[3].isDisposed());
	    assertTrue(items[4].isDisposed());
    }

	public void testDoSetItemCountSetsMenu() throws Exception {
		Menu menu = new Menu(shell);
	    BoxViewer viewer = new BoxViewer(shell);
	    viewer.getControl().setMenu(menu);
	    viewer.doSetItemCount(1);
	    assertSame(menu, viewer.getItem(0).getMenu());
    }

	public void testDoSetItemCountDontDisposeMenu() throws Exception {
		Menu menu = new Menu(shell);
	    BoxViewer viewer = new BoxViewer(shell);
	    viewer.getControl().setMenu(menu);
	    viewer.doSetItemCount(1);
	    viewer.doSetItemCount(0);
	    assertFalse(menu.isDisposed());
    }

	public void testDisposeControlDisposeMenu() throws Exception {
		Menu menu = new Menu(shell);
	    BoxViewer viewer = new BoxViewer(shell);
	    viewer.getControl().setMenu(menu);
	    viewer.getControl().dispose();
	    assertTrue(menu.isDisposed());
    }
}
