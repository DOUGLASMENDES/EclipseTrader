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

import org.easymock.EasyMock;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class MarketOrderFunctionTest extends TestCase {

    Context cx;
    Security instrument;
    AccountMock account;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        cx = Context.enter();
        cx.setWrapFactory(new EnhancedWrapFactory());

        instrument = new Security("Microsoft", new FeedIdentifier("MSFT", null));
        account = new AccountMock();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Context.exit();
    }

    public void testNewMarketOrder() throws Exception {
        IOrderMonitor monitor = EasyMock.createMock(IOrderMonitor.class);
        IBroker broker = EasyMock.createMock(IBroker.class);
        EasyMock.expect(broker.prepareOrder(EasyMock.isA(IOrder.class))).andStubReturn(monitor);
        EasyMock.replay(broker, monitor);

        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, MarketOrderFunction.class);
        scope.putConst("Buy", scope, BaseOrderFunction.Buy);

        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_BROKER, Context.javaToJS(broker, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, Context.javaToJS(account, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, Context.javaToJS(instrument, scope));

        Object result = cx.evaluateString(scope, "order = new MarketOrder(Buy, 35);", "Test", 0, null);
        assertEquals(MarketOrderFunction.class, result.getClass());

        EasyMock.verify(broker, monitor);
    }

    public void testSendMarketOrder() throws Exception {
        IOrderMonitor monitor = EasyMock.createMock(IOrderMonitor.class);
        monitor.submit();
        IBroker broker = EasyMock.createMock(IBroker.class);
        EasyMock.expect(broker.prepareOrder(EasyMock.isA(IOrder.class))).andStubReturn(monitor);
        EasyMock.replay(broker, monitor);

        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, MarketOrderFunction.class);
        scope.putConst("Buy", scope, BaseOrderFunction.Buy);

        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_BROKER, Context.javaToJS(broker, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, Context.javaToJS(account, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, Context.javaToJS(instrument, scope));

        cx.evaluateString(scope, "order = new MarketOrder(Buy, 35);", "Test", 0, null);
        cx.evaluateString(scope, "order.send();", "Test", 0, null);

        EasyMock.verify(broker, monitor);
    }

    public void testMarketOrderNotInNewExpression() throws Exception {
        IOrderMonitor monitor = EasyMock.createMock(IOrderMonitor.class);
        monitor.submit();
        IBroker broker = EasyMock.createMock(IBroker.class);
        EasyMock.expect(broker.prepareOrder(EasyMock.isA(IOrder.class))).andStubReturn(monitor);
        EasyMock.replay(broker, monitor);

        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, MarketOrderFunction.class);
        scope.putConst("Buy", scope, BaseOrderFunction.Buy);

        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_BROKER, Context.javaToJS(broker, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, Context.javaToJS(account, scope));
        ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, Context.javaToJS(instrument, scope));

        Object result = cx.evaluateString(scope, "MarketOrder(Buy, 35);", "Test", 0, null);
        assertNull(result);

        EasyMock.verify(broker, monitor);
    }
}
