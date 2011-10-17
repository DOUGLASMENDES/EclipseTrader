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

import junit.framework.TestCase;

import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.internal.ats.TradingSystem;
import org.eclipsetrader.core.trading.IPosition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.UniqueTag;

public class JavaScriptEngineTest extends TestCase {

    Security instrument1;
    Security instrument2;
    ScriptStrategy strategy;
    Context cx;
    PricingEnvironment pricingEnvironment;
    AccountMock account;
    BrokerMock broker;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        strategy = new ScriptStrategy("Sample Strategy");
        strategy.setText("");
        strategy.setInstruments(new ISecurity[] {
            instrument1 = new Security("Apple", new FeedIdentifier("AAPL", null)),
            instrument2 = new Security("Microsoft", new FeedIdentifier("MSFT", null))
        });

        pricingEnvironment = new PricingEnvironment();
        account = new AccountMock();
        broker = new BrokerMock();

        cx = Context.enter();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Context.exit();
    }

    public void testContextInheritsSettings() throws Exception {
        JavaScriptEngine runner = new JavaScriptEngine(new TradingSystem(strategy), pricingEnvironment, account, broker);

        runner.start();

        assertTrue(runner.get(BaseOrderFunction.PROPERTY_ACCOUNT) != UniqueTag.NOT_FOUND);
        assertTrue(runner.get(BaseOrderFunction.PROPERTY_BROKER) != UniqueTag.NOT_FOUND);

        JavaScriptEngineInstrument context = runner.getContextFor(instrument1);

        assertTrue(context.get(BaseOrderFunction.PROPERTY_ACCOUNT) != UniqueTag.NOT_FOUND);
        assertTrue(context.get(BaseOrderFunction.PROPERTY_BROKER) != UniqueTag.NOT_FOUND);
    }

    public void testSetInstrumentsMap() throws Exception {
        JavaScriptEngine runner = new JavaScriptEngine(new TradingSystem(strategy), pricingEnvironment, account, broker);

        runner.start();

        Object result = runner.get("instruments");
        assertNotSame(UniqueTag.NOT_FOUND, result);

        result = cx.evaluateString(runner.getScope(), "instruments['MSFT'].name;", strategy.getName(), 1, null);
        assertEquals("Microsoft", result);
    }

    public void testContextInheritsInstrumentsMap() throws Exception {
        JavaScriptEngine runner = new JavaScriptEngine(new TradingSystem(strategy), pricingEnvironment, account, broker);

        runner.start();

        JavaScriptEngineInstrument context = runner.getContextFor(instrument1);

        Object result = context.get("instruments");
        assertNotSame(UniqueTag.NOT_FOUND, result);

        result = cx.evaluateString(context.getScope(), "instruments['MSFT'].name;", strategy.getName(), 1, null);
        assertEquals("Microsoft", result);
    }

    public void testSetPositionsMap() throws Exception {
        account.setPositions(new IPosition[] {
            new PositionMock(instrument2, 1000L, 25.75)
        });

        JavaScriptEngine runner = new JavaScriptEngine(new TradingSystem(strategy), pricingEnvironment, account, broker);

        runner.start();

        JavaScriptEngineInstrument context = runner.getContextFor(instrument1);

        Object result = context.get("positions");
        assertNotSame(UniqueTag.NOT_FOUND, result);

        result = cx.evaluateString(context.getScope(), "positions['MSFT'].quantity;", strategy.getName(), 1, null);
        assertEquals(1000L, result);
    }

    public void testUpdatePositionsMap() throws Exception {
        account.setPositions(new IPosition[] {
            new PositionMock(instrument2, 1000L, 25.75)
        });

        JavaScriptEngine runner = new JavaScriptEngine(new TradingSystem(strategy), pricingEnvironment, account, broker);

        runner.start();

        JavaScriptEngineInstrument context = runner.getContextFor(instrument1);

        account.setPositions(new IPosition[] {
            new PositionMock(instrument2, 2000L, 25.75)
        });
        runner.updatePositionsMap();

        Object result = context.get("positions");
        assertNotSame(UniqueTag.NOT_FOUND, result);

        result = cx.evaluateString(context.getScope(), "positions['MSFT'].quantity;", strategy.getName(), 1, null);
        assertEquals(2000L, result);
    }
}
