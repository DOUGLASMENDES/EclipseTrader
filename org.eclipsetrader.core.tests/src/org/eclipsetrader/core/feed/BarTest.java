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

package org.eclipsetrader.core.feed;

import java.util.Date;

import junit.framework.TestCase;

public class BarTest extends TestCase {

    public void testHashCode() throws Exception {
        Date date = new Date();
        Bar o1 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Bar o2 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    public void testEquals() throws Exception {
        Date date = new Date();
        Bar o1 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        Bar o2 = new Bar(date, TimeSpan.days(1), 10.0, 20.0, 30.0, 40.0, 1000L);
        assertTrue(o1.equals(o2));
    }

}
