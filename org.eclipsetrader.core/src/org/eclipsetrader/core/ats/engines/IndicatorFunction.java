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
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.IDataSeries;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public abstract class IndicatorFunction extends ScriptableObject {

    private static final long serialVersionUID = -9191442400382251716L;

    public static final String PROP_SERIES = "SERIES";

    protected IDataSeries source;

    protected IDataSeries series;

    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final PropertyChangeListener changeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            calculate();
        }
    };

    public IndicatorFunction() {
    }

    public IndicatorFunction(BarsDataSeriesFunction bars) {
        this.source = bars.getSeries();
        bars.addPropertyChangeListener(BarsDataSeriesFunction.PROP_BARS, changeListener);
    }

    public IndicatorFunction(IndicatorFunction indicator) {
        this.source = indicator.getSeries();
        indicator.addPropertyChangeListener(IndicatorFunction.PROP_SERIES, changeListener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected abstract void calculate();

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
        return index >= 0 && index < series.size();
    }

    public Object jsFunction_crosses(IndicatorFunction other, Object bar) {
        return series.cross(other.series, (IAdaptable) bar);
    }

    public Object jsFunction_first() {
        return series.getFirst();
    }

    public Object jsFunction_last() {
        return series.getLast();
    }

    public Object jsFunction_highest() {
        return series.getHighest();
    }

    public Object jsFunction_lowest() {
        return series.getLowest();
    }

    public IDataSeries getSeries() {
        return series;
    }
}
