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

package org.eclipsetrader.ui.internal.repositories;

import junit.framework.TestCase;

public class RepositoryViewItemTest extends TestCase {

	public void testGetChildString() throws Exception {
	    RepositoryViewItem root = new RepositoryViewItem();
	    RepositoryViewItem e1 = root.createChild("E1");
	    assertSame(e1, root.getChild("E1"));
	    assertNull(root.getChild("ELEMENT"));
    }

	public void testGetChildObject() throws Exception {
		RepositoryViewItem root = new RepositoryViewItem();
	    Object o1 = new Object();
	    RepositoryViewItem e1 = root.createChild(o1);
	    assertSame(e1, root.getChild(o1));
	    assertNull(root.getChild(new Object()));
    }

	public void testGetSecurityContainerChild() throws Exception {
		RepositoryViewItem root = new RepositoryViewItem();
	    SecurityContainerObject c1 = new SecurityContainerObject("Container");
	    RepositoryViewItem e1 = root.createChild(c1);
	    assertSame(e1, root.getChild(c1));
	    assertSame(e1, root.getChild(new SecurityContainerObject("Container")));
	    assertNull(root.getChild("ELEMENT"));
    }

	public void testGetWatchListContainerChild() throws Exception {
		RepositoryViewItem root = new RepositoryViewItem();
	    WatchListContainerObject c1 = new WatchListContainerObject("Container");
	    RepositoryViewItem e1 = root.createChild(c1);
	    assertSame(e1, root.getChild(c1));
	    assertSame(e1, root.getChild(new WatchListContainerObject("Container")));
	    assertNull(root.getChild("ELEMENT"));
    }
}
