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

public class SecurityContainerObjectTest extends TestCase {

	public void testHashCode() throws Exception {
	    assertEquals(new SecurityContainerObject("c1").hashCode(), new SecurityContainerObject("c1").hashCode());
    }

	public void testEquals() throws Exception {
	    assertTrue(new SecurityContainerObject("c1").equals(new SecurityContainerObject("c1")));
	    assertFalse(new SecurityContainerObject("c1").equals(new SecurityContainerObject("c2")));
    }

	public void testEqualsString() throws Exception {
	    assertTrue(new SecurityContainerObject("c1").equals("c1"));
	    assertFalse(new SecurityContainerObject("c1").equals("c2"));
    }
}
