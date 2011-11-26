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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IPosition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class HasPositionFunctionTest extends TestCase {

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

    public void testHasPosition() throws Exception {
        Security instrument = new Security("Microsoft", new FeedIdentifier("MSFT", null));
        AccountMock account = new AccountMock();

        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, HasPositionFunction.class);

        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, Context.javaToJS(account, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, Context.javaToJS(instrument, scope));

        Object result = cx.evaluateString(scope, "hasPosition();", "Test", 0, null);
        assertEquals(Boolean.FALSE, result);

        account.setPositions(new IPosition[] {
            new PositionMock(instrument, 1000L, 25.75)
        });

        result = cx.evaluateString(scope, "hasPosition();", "Test", 0, null);
        assertEquals(Boolean.TRUE, result);
    }

    public void testHasPositionArgument() throws Exception {
        Security instrument = new Security("Microsoft", new FeedIdentifier("MSFT", null));
        AccountMock account = new AccountMock();

        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, HasPositionFunction.class);

        Map<String, ISecurity> map = new HashMap<String, ISecurity>();
        map.put(instrument.getIdentifier().getSymbol(), instrument);

        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, Context.javaToJS(account, scope));
        ScriptableObject.putProperty(scope, "instruments", map);

        Object result = cx.evaluateString(scope, "hasPosition(instruments['MSFT']);", "Test", 0, null);
        assertEquals(Boolean.FALSE, result);

        account.setPositions(new IPosition[] {
            new PositionMock(instrument, 1000L, 25.75)
        });

        result = cx.evaluateString(scope, "hasPosition(instruments['MSFT']);", "Test", 0, null);
        assertEquals(Boolean.TRUE, result);
        result = cx.evaluateString(scope, "hasPosition();", "Test", 0, null);
        assertEquals(Boolean.FALSE, result);
    }
}
