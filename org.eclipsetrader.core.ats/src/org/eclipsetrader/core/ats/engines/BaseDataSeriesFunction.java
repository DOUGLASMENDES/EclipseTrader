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
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.tictactec.ta.lib.Core;

public abstract class BaseDataSeriesFunction extends ScriptableObject {

    private static final long serialVersionUID = 3172411434588521607L;

    private List<IBar> bars;
    private double[] data;

    public static final Core core = new Core();

    public BaseDataSeriesFunction() {
    }

    public BaseDataSeriesFunction(List<IBar> bars) {
        this.bars = bars;
    }

    protected Double getLast() {
        return data != null && data.length != 0 ? data[data.length - 1] : null;
    }

    protected Double getFirst() {
        return data != null && data.length != 0 ? data[0] : null;
    }

    public List<IBar> getBars() {
        return bars;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(String name, Scriptable start) {
        if (name.equals("length")) {
            return data.length;
        }
        return super.get(name, start);
    }

    @Override
    public Object get(int index, Scriptable start) {
        try {
            int s = data.length;
            if (index >= 0 && index < s) {
                return data[index];
            }
            else {
                return Context.getUndefinedValue();
            }
        } catch (RuntimeException e) {
            throw Context.throwAsScriptRuntimeEx(e);
        }
    }

    @Override
    public Object[] getIds() {
        int size = data.length;
        Integer[] ids = new Integer[size];
        for (int i = 0; i < size; ++i) {
            ids[i] = i;
        }
        return ids;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return index >= 0 && index < data.length;
    }
}
