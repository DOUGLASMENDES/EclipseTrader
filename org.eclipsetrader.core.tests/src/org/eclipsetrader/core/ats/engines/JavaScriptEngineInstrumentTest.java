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

import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.Quote;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptEngineInstrumentTest extends TestCase {

    Security instrument;
    ScriptStrategy strategy;
    Context cx;
    ScriptableObject sharedScope;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        strategy = new ScriptStrategy("Sample Strategy");
        strategy.setText("");
        strategy.setInstruments(new ISecurity[] {
            instrument = new Security("Apple", null)
        });

        cx = Context.enter();
        sharedScope = new ImporterTopLevel(cx);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Context.exit();
    }

    public void testInstanceInstrumentProperty() throws Exception {
        Security instrument1 = new Security("Apple", null);
        Security instrument2 = new Security("Microsoft", null);

        JavaScriptEngineInstrument context1 = new JavaScriptEngineInstrument(sharedScope, instrument1, strategy);
        JavaScriptEngineInstrument context2 = new JavaScriptEngineInstrument(sharedScope, instrument2, strategy);

        Object result1 = context1.get(BaseOrderFunction.PROPERTY_INSTRUMENT);
        assertEquals(instrument1, Context.jsToJava(result1, ISecurity.class));

        Object result2 = context2.get(BaseOrderFunction.PROPERTY_INSTRUMENT);
        assertEquals(instrument2, Context.jsToJava(result2, ISecurity.class));
    }

    public void testOnQuoteSetsQuoteProperty() throws Exception {
        Quote quote = new Quote(1.5, 1.6);

        JavaScriptEngineInstrument context = new JavaScriptEngineInstrument(sharedScope, instrument, strategy);
        context.onQuote(quote);

        Object result = context.get(JavaScriptEngineInstrument.PROPERTY_QUOTE);
        assertEquals(quote, result);
    }

    public void testOnTradeSetsTradeProperty() throws Exception {
        Trade trade = new Trade(1.5);

        JavaScriptEngineInstrument context = new JavaScriptEngineInstrument(sharedScope, instrument, strategy);
        context.onTrade(trade);

        Object result = context.get(JavaScriptEngineInstrument.PROPERTY_TRADE);
        assertEquals(trade, result);
    }

    public void testOnBarSetsBarProperty() throws Exception {
        Bar bar = new Bar(Calendar.getInstance().getTime(), TimeSpan.days(1), 1.2, 1.8, 1.1, 1.5, 1000L);

        JavaScriptEngineInstrument context = new JavaScriptEngineInstrument(sharedScope, instrument, strategy);
        context.onBar(bar);

        Object result = context.get(JavaScriptEngineInstrument.PROPERTY_BAR);
        assertEquals(bar, result);
    }
}
