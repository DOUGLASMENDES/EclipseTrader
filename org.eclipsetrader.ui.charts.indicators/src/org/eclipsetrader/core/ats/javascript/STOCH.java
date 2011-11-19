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

public class STOCH extends IndicatorFunction {

    private static final long serialVersionUID = -9191442400382251716L;

    private int kFastPeriod;
    private int kSlowPeriod;
    private MAType kMaType = MAType.EMA;
    private int dPeriod = 0;
    private MAType dMaType = MAType.EMA;

    public STOCH() {
    }

    public STOCH(BarsDataSeriesFunction bars, int kFastPeriod, int kSlowPeriod) {
        super(bars);
        this.kFastPeriod = kFastPeriod;
        this.kSlowPeriod = kSlowPeriod;
        calculate();
    }

    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        BarsDataSeriesFunction bars = (BarsDataSeriesFunction) ScriptableObject.getProperty(
            getTopLevelScope(ctorObj),
            JavaScriptEngineInstrument.PROPERTY_BARS);

        int kFastPeriod = (int) Context.toNumber(args[0]);
        int kSlowPeriod = (int) Context.toNumber(args[1]);

        STOCH result = new STOCH(bars, kFastPeriod, kSlowPeriod);

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.javascript.IndicatorFunction#calculate()
     */
    @Override
    protected void calculate() {
        IAdaptable[] values = source.getValues();

        Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

        int lookback = core.stochLookback(kFastPeriod, kSlowPeriod, MAType.getTALib_MAType(kMaType), dPeriod, MAType.getTALib_MAType(dMaType));
        if (values.length < lookback) {
            series = new NumericDataSeries(getClassName(), new Number[0], source);
            return;
        }

        int startIdx = 0;
        int endIdx = values.length - 1;
        double[] inHigh = Util.getValuesForField(values, OHLCField.High);
        double[] inLow = Util.getValuesForField(values, OHLCField.Low);
        double[] inClose = Util.getValuesForField(values, OHLCField.Close);

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outK = new double[values.length - lookback];
        double[] outD = new double[values.length - lookback];

        core.stoch(startIdx, endIdx, inHigh, inLow, inClose, kFastPeriod, kSlowPeriod, MAType.getTALib_MAType(kMaType), dPeriod, MAType.getTALib_MAType(dMaType), outBegIdx, outNbElement, outK, outD);

        series = new NumericDataSeries(getClassName(), outK, source);
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
