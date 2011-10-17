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

package org.eclipsetrader.core.ats.engines;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.TimeSpan;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class BaseDataSeriesFunctionTest extends TestCase {

    Context cx;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        cx = Context.enter();
        cx.setWrapFactory(new EnhancedWrapFactory());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Context.exit();
    }

    public void testCrosses() throws Exception {
        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, SMAFunction.class);

        List<IBar> bars = new ArrayList<IBar>();
        bars.add(new Bar(null, TimeSpan.days(1), 169.44, 169.44, 169.44, 169.44, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 172.02, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 172.79, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 171.67, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 169.08, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 166.89, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 169.05, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 166.14, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 168.07, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 166.73, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 165.08, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 164.7, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 160.97, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 161.82, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 164.52, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 162.6, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 165.32, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 171.1, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 170.98, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 169.35, 1L));
        bars.add(new Bar(null, TimeSpan.days(1), 1.0, 1.0, 1.0, 174.62, 1L));

        ScriptableObject.putProperty(scope, JavaScriptEngineInstrument.PROPERTY_BARS, bars);

        cx.evaluateString(scope, "sma1 = new SMA(7);", "Test", 0, null);
        cx.evaluateString(scope, "sma2 = new SMA(21);", "Test", 0, null);
        Object result = cx.evaluateString(scope, "sma1.crosses(sma2, 179.42);", "Test", 0, null);
        assertEquals(new Integer(0), result);
    }
}
