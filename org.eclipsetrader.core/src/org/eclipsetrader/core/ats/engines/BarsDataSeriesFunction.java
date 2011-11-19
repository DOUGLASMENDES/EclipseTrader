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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipsetrader.core.charts.BarsDataSeries;
import org.eclipsetrader.core.feed.IBar;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class BarsDataSeriesFunction extends ScriptableObject {

    private static final long serialVersionUID = 5619130687789322561L;

    public static final String FUNCTION_NAME = "BarsDataSeries";
    public static final String PROP_BARS = "BARS";

    private final List<IBar> list = new ArrayList<IBar>();
    private final BarsDataSeries series = new BarsDataSeries(PROP_BARS, list);

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public BarsDataSeriesFunction() {
    }

    public BarsDataSeries getSeries() {
        return series;
    }

    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) {
        return new BarsDataSeriesFunction();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public void append(IBar element) {
        list.add(element);
        changeSupport.firePropertyChange(PROP_BARS, null, toArray());
    }

    public void prepend(IBar element) {
        list.add(0, element);
        changeSupport.firePropertyChange(PROP_BARS, null, toArray());
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return FUNCTION_NAME;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#get(int, org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        try {
            int s = list.size();
            if (index >= 0 && index < s) {
                return list.get(index);
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
        int size = list.size();
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
        return index >= 0 && index < list.size();
    }

    public Object jsFunction_size() {
        return list.size();
    }

    public Object jsFunction_first() {
        return list.size() != 0 ? list.get(0) : null;
    }

    public Object jsFunction_last() {
        return list.size() != 0 ? list.get(list.size() - 1) : null;
    }

    public IBar[] toArray() {
        return list.toArray(new IBar[list.size()]);
    }
}
