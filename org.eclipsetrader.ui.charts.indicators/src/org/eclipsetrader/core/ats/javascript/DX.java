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
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class DX extends ScriptableObject {

    private static final long serialVersionUID = -9191442400382251716L;

    private BarsDataSeries source;

    private int period;

    private int lastSourceSize = -1;
    private IDataSeries series;

    public DX() {
    }

    public DX(List<IBar> bars, int period) {
        this.source = new BarsDataSeries("BARS", bars); //$NON-NLS-1$
        this.period = period;
    }

    @SuppressWarnings("unchecked")
    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        List<IBar> bars = (List<IBar>) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngineInstrument.PROPERTY_BARS);

        if (args.length < 1) {
            return null;
        }

        int period = (int) Context.toNumber(args[0]);

        DX result = new DX(bars, period);

        return result;
    }

    private void checkCalculate() {
        if (source == null || lastSourceSize == source.size()) {
            return;
        }

        IAdaptable[] values = source.getValues();
        lastSourceSize = source.size();

        Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

        int lookback = core.dxLookback(period);
        if (values.length < lookback) {
            series = new NumericDataSeries(String.format("%s%d", getClassName(), period), new Number[0], source); //$NON-NLS-1$
            return;
        }

        int startIdx = 0;
        int endIdx = values.length - 1;
        double[] inHigh = Util.getValuesForField(values, OHLCField.High);
        double[] inLow = Util.getValuesForField(values, OHLCField.Low);
        double[] inClose = Util.getValuesForField(values, OHLCField.Close);

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outReal = new double[values.length - lookback];

        core.dx(startIdx, endIdx, inHigh, inLow, inClose, period, outBegIdx, outNbElement, outReal);

        series = new NumericDataSeries(String.format("%s%d", getClassName(), period), outReal, source); //$NON-NLS-1$
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
            return series != null ? series.size() : 0;
        }
        return super.get(name, start);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#get(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        checkCalculate();
        if (series == null) {
            return Context.getUndefinedValue();
        }
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

    public Object jsFunction_crosses(DX other, Object bar) throws Exception {
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
