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

import java.util.List;

import org.eclipsetrader.core.feed.IBar;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.tictactec.ta.lib.MInteger;

public class BBUFunction extends BaseDataSeriesFunction {

    private static final long serialVersionUID = 5300423853160908831L;

    private int field = Util.FIELD_CLOSE;
    private int period;
    private int type;
    private double devs;

    public BBUFunction() {
    }

    public BBUFunction(List<IBar> bars, int type, int period, double devs) {
        super(bars);
        this.type = type;
        this.period = period;
        this.devs = devs;
        updateData();
    }

    @SuppressWarnings("unchecked")
    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        List<IBar> bars = (List<IBar>) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngineInstrument.PROPERTY_BARS);

        if (args.length < 2) {
            return null;
        }

        int period = (int) Context.toNumber(args[0]);
        double devs = Context.toNumber(args[1]);

        BBUFunction result = new BBUFunction(bars, Util.TYPE_SMA, period, devs);

        return result;
    }

    public Object jsFunction_crosses(BBUFunction other, Object value) throws Exception {
        if (value instanceof IBar) {
            IBar bar = (IBar) value;
            switch (field) {
                case Util.FIELD_OPEN:
                    return crosses(other, bar.getOpen());

                case Util.FIELD_HIGH:
                    return crosses(other, bar.getHigh());

                case Util.FIELD_LOW:
                    return crosses(other, bar.getLow());

                case Util.FIELD_CLOSE:
                    return crosses(other, bar.getClose());

                case Util.FIELD_VOLUME:
                    return crosses(other, bar.getVolume());
            }
            return 0;
        }
        return crosses(other, Context.toNumber(value));
    }

    private Object crosses(BBUFunction other, double value) throws Exception {
        updateData();
        other.updateData();

        Double ourLast = getLast();
        Double otherLast = other.getLast();
        if (ourLast == null || otherLast == null) {
            return 0;
        }

        Double ourNext = calculateNextValue(value);
        Double otherNext = other.calculateNextValue(value);

        if (ourLast < otherLast && ourNext > otherNext) {
            return 1;
        }
        if (ourLast > otherLast && ourNext < otherNext) {
            return -1;
        }

        return 0;
    }

    protected Double calculateNextValue(double value) {
        List<IBar> bars = getBars();
        if (bars == null) {
            return null;
        }

        int lookback = core.bbandsLookback(period, devs, devs, Util.getTALib_MAType(type));
        if (bars.size() < lookback) {
            return null;
        }

        double[] inReal = new double[bars.size() + 1];
        Util.copyValuesTo(bars, field, inReal);
        inReal[bars.size()] = value;

        double[] outReal = new double[inReal.length - lookback];
        double[] outTemp1 = new double[inReal.length - lookback];
        double[] outTemp2 = new double[inReal.length - lookback];

        int startIdx = 0;
        int endIdx = inReal.length - 1;
        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        core.bbands(startIdx, endIdx, inReal, period, devs, devs, Util.getTALib_MAType(type), outBegIdx, outNbElement, outReal, outTemp1, outTemp2);

        return outReal[outReal.length - 1];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.BaseDataSeriesFunction#get(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(String name, Scriptable start) {
        updateData();
        return super.get(name, start);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.BaseDataSeriesFunction#get(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        updateData();
        return super.get(index, start);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.BaseDataSeriesFunction#getIds()
     */
    @Override
    public Object[] getIds() {
        updateData();
        return super.getIds();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.engines.BaseDataSeriesFunction#has(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public boolean has(int index, Scriptable start) {
        updateData();
        return super.has(index, start);
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return "BBU";
    }

    private void updateData() {
        List<IBar> bars = getBars();
        if (bars == null) {
            return;
        }

        int lookback = core.bbandsLookback(period, devs, devs, Util.getTALib_MAType(type));
        if (bars.size() < lookback) {
            return;
        }

        double[] existingData = getData();
        if (existingData != null && existingData.length == (bars.size() - lookback)) {
            return;
        }

        int startIdx = 0;
        int endIdx = bars.size() - 1;
        double[] inReal = new double[bars.size()];
        Util.copyValuesTo(bars, field, inReal);

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outReal = new double[inReal.length - lookback];
        double[] outTemp1 = new double[inReal.length - lookback];
        double[] outTemp2 = new double[inReal.length - lookback];

        core.bbands(startIdx, endIdx, inReal, period, devs, devs, Util.getTALib_MAType(type), outBegIdx, outNbElement, outReal, outTemp1, outTemp2);

        setData(outReal);
    }
}
