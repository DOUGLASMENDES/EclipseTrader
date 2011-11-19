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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.TimeSpan;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class BarsDataSeriesFunctionTest extends TestCase {

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

    public void testSetProperty() throws Exception {
        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, BarsDataSeriesFunction.class);

        BarsDataSeriesFunction function = (BarsDataSeriesFunction) cx.newObject(scope, BarsDataSeriesFunction.FUNCTION_NAME);
        ScriptableObject.putProperty(scope, JavaScriptEngineInstrument.PROPERTY_BARS, function);

        Object result = cx.evaluateString(scope, "bars.first();", "Test", 0, null);

        assertNull(result);
    }

    public void testAppend() throws Exception {
        ScriptableObject scope = new ImporterTopLevel(cx);
        ScriptableObject.defineClass(scope, BarsDataSeriesFunction.class);

        BarsDataSeriesFunction function = (BarsDataSeriesFunction) cx.newObject(scope, BarsDataSeriesFunction.FUNCTION_NAME);
        ScriptableObject.putProperty(scope, JavaScriptEngineInstrument.PROPERTY_BARS, function);

        Bar bar = new Bar(new Date(), TimeSpan.days(1), 10.0, 11.0, 9.0, 10.0, 1000L);
        function.append(bar);

        Object result = cx.evaluateString(scope, "bars.first();", "Test", 0, null);

        assertEquals(bar, result);
    }

    public void testUpdateIncapsulatedDataSeries() throws Exception {
        BarsDataSeriesFunction function = new BarsDataSeriesFunction();
        IDataSeries dataSeries = function.getSeries();

        assertEquals(0, dataSeries.getValues().length);

        Bar bar = new Bar(new Date(), TimeSpan.days(1), 10.0, 11.0, 9.0, 10.0, 1000L);
        function.append(bar);

        assertEquals(1, dataSeries.getValues().length);
        assertEquals(bar, dataSeries.getValues()[0]);
    }

    public void testNotifyListeners() throws Exception {
        PropertyChangeListener listener = EasyMock.createMock(PropertyChangeListener.class);
        listener.propertyChange(EasyMock.isA(PropertyChangeEvent.class));
        EasyMock.replay(listener);

        BarsDataSeriesFunction function = new BarsDataSeriesFunction();
        function.addPropertyChangeListener(BarsDataSeriesFunction.PROP_BARS, listener);

        Bar bar = new Bar(new Date(), TimeSpan.days(1), 10.0, 11.0, 9.0, 10.0, 1000L);
        function.append(bar);

        EasyMock.verify(listener);
    }
}
