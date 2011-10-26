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

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.ats.engines.JavaScriptEngineInstrument;
import org.eclipsetrader.core.charts.BarsDataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.NumericDataSeries;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class STOCH extends ScriptableObject {

    private static final long serialVersionUID = -9191442400382251716L;

    private BarsDataSeries source;

    private int kFastPeriod;
    private int kSlowPeriod;
    private MAType kMaType = MAType.EMA;
    private int dPeriod = 0;
    private MAType dMaType = MAType.EMA;

    private int lastSourceSize = -1;
    private IDataSeries series;

    public STOCH() {
    }

    public STOCH(List<IBar> bars, int kFastPeriod, int kSlowPeriod) {
        this.source = new BarsDataSeries("BARS", bars); //$NON-NLS-1$
        this.kFastPeriod = kFastPeriod;
        this.kSlowPeriod = kSlowPeriod;
    }

    @SuppressWarnings("unchecked")
    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        List<IBar> bars = (List<IBar>) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngineInstrument.PROPERTY_BARS);

        if (args.length < 2) {
            return null;
        }

        int kFastPeriod = (int) Context.toNumber(args[0]);
        int kSlowPeriod = (int) Context.toNumber(args[1]);

        STOCH result = new STOCH(bars, kFastPeriod, kSlowPeriod);

        return result;
    }

    private void checkCalculate() {
        if (lastSourceSize == source.size()) {
            return;
        }

        IAdaptable[] values = source.getValues();
        lastSourceSize = source.size();

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
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(String name, Scriptable start) {
        checkCalculate();
        if (name.equals("length")) { //$NON-NLS-1$
            return series.size();
        }
        return super.get(name, start);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#get(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        checkCalculate();
        try {
            int s = series.size();
            if (index >= 0 && index < s) {
                return series.getValues()[index];
            }
            else {
                return Context.getUndefinedValue();
            }
        } catch (RuntimeException e) {
            throw Context.throwAsScriptRuntimeEx(e);
        }
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getIds()
     */
    @Override
    public Object[] getIds() {
        checkCalculate();
        int size = series.size();
        Integer[] ids = new Integer[size];
        for (int i = 0; i < size; ++i) {
            ids[i] = i;
        }
        return ids;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#has(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public boolean has(int index, Scriptable start) {
        checkCalculate();
        return index >= 0 && index < series.size();
    }

    public Object jsFunction_crosses(STOCH other, Object bar) throws Exception {
        checkCalculate();
        other.checkCalculate();
        return series.cross(other.series, (IAdaptable) bar);
    }

    public Object jsFunction_first() throws Exception {
        checkCalculate();
        return series.getFirst();
    }

    public Object jsFunction_last() throws Exception {
        checkCalculate();
        return series.getLast();
    }

    public Object jsFunction_highest() throws Exception {
        checkCalculate();
        return series.getHighest();
    }

    public Object jsFunction_lowest() throws Exception {
        checkCalculate();
        return series.getLowest();
    }

    public Object jsFunction_size() throws Exception {
        checkCalculate();
        return series.size();
    }
}
