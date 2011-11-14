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

package org.eclipsetrader.ui.internal.charts;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.TimeSpan;

public class PeriodTest extends TestCase {

    public void testEqualsTo() throws Exception {
        Period period = new Period("Test", TimeSpan.years(1), TimeSpan.days(1));
        assertTrue(period.equalsTo(TimeSpan.years(1), TimeSpan.days(1)));
        assertFalse(period.equalsTo(TimeSpan.months(1), TimeSpan.days(1)));
        assertFalse(period.equalsTo(TimeSpan.years(1), TimeSpan.days(5)));
    }

    public void testEqualsToNull() throws Exception {
        Period period = new Period("Test", TimeSpan.years(1), TimeSpan.days(1));
        assertFalse(period.equalsTo(TimeSpan.years(1), null));
        assertFalse(period.equalsTo(null, TimeSpan.days(1)));
    }
}
