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

package org.eclipsetrader.core.ats.javascript;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.ats.engines.BarsDataSeriesFunction;
import org.eclipsetrader.core.ats.engines.IndicatorFunction;
import org.eclipsetrader.core.ats.engines.JavaScriptEngineInstrument;
import org.eclipsetrader.core.charts.NumericDataSeries;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class BBU extends IndicatorFunction {

    private static final long serialVersionUID = -9191442400382251716L;

    private final OHLCField field = OHLCField.Close;
    private int period;
    private double deviation;
    private final MAType maType = MAType.SMA;

    public BBU() {
    }

    public BBU(BarsDataSeriesFunction bars, int period, double deviation) {
        super(bars);
        this.period = period;
        this.deviation = deviation;
        calculate();
    }

    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        BarsDataSeriesFunction bars = (BarsDataSeriesFunction) ScriptableObject.getProperty(
            getTopLevelScope(ctorObj),
            JavaScriptEngineInstrument.PROPERTY_BARS);

        int period = (int) Context.toNumber(args[0]);
        double deviation = Context.toNumber(args[1]);

        BBU result = new BBU(bars, period, deviation);

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#calculate()
     */
    @Override
    protected void calculate() {
        IAdaptable[] values = source.getValues();

        Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

        int lookback = core.bbandsLookback(period, deviation, deviation, MAType.getTALib_MAType(maType));
        if (values.length < lookback) {
            series = new NumericDataSeries(String.format("%s%d", getClassName(), period), new Number[0], source); //$NON-NLS-1$
            return;
        }

        int startIdx = 0;
        int endIdx = values.length - 1;
        double[] inReal = Util.getValuesForField(values, field);

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outUpper = new double[values.length - lookback];
        double[] outMiddle = new double[values.length - lookback];
        double[] outLower = new double[values.length - lookback];

        core.bbands(startIdx, endIdx, inReal, period, deviation, deviation, MAType.getTALib_MAType(maType), outBegIdx, outNbElement, outUpper, outMiddle, outLower);

        series = new NumericDataSeries(String.format("%s%d", getClassName(), period), outUpper, source); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#jsFunction_crosses(org.eclipsetrader.core.ats.javascript.IndicatorFunction, java.lang.Object)
     */
    @Override
    public Object jsFunction_crosses(IndicatorFunction other, Object bar) {
        return super.jsFunction_crosses(other, bar);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#jsFunction_first()
     */
    @Override
    public Object jsFunction_first() {
        return super.jsFunction_first();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#jsFunction_last()
     */
    @Override
    public Object jsFunction_last() {
        return super.jsFunction_last();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#jsFunction_highest()
     */
    @Override
    public Object jsFunction_highest() {
        return super.jsFunction_highest();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#jsFunction_lowest()
     */
    @Override
    public Object jsFunction_lowest() {
        return super.jsFunction_lowest();
    }
}
