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

package org.eclipsetrader.core.charts;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Data series implementation that represents numeric values derived from a
 * source series.
 *
 * @since 1.0
 */
public class NumericDataSeries extends DataSeries {

    private static class NumberWrapper implements IAdaptable {

        private Date date;
        private Number number;

        public NumberWrapper(Date date, Number number) {
            this.date = date;
            this.number = number;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(Number.class)) {
                return number;
            }
            if (adapter.isAssignableFrom(Date.class)) {
                return date;
            }
            return null;
        }
    }

    public NumericDataSeries(String name, Number[] values, IDataSeries parent) {
        super(name, convertValues(values, parent));
    }

    public NumericDataSeries(String name, double[] values, IDataSeries parent) {
        super(name, convertValues(values, parent));
    }

    public NumericDataSeries(String name, int[] values, IDataSeries parent) {
        super(name, convertValues(values, parent));
    }

    public NumericDataSeries(String name, long[] values, IDataSeries parent) {
        super(name, convertValues(values, parent));
    }

    protected NumericDataSeries(String name, IAdaptable[] values) {
        super(name, values);
    }

    private static IAdaptable[] convertValues(Number[] values, IDataSeries parent) {
        IAdaptable[] reference = parent.getValues();
        int refIndex = reference.length - values.length;

        IAdaptable[] v = new IAdaptable[values.length];
        for (int i = 0; i < values.length; i++, refIndex++) {
            Date date = (Date) reference[refIndex].getAdapter(Date.class);
            v[i] = new NumberWrapper(date, values[i]);
        }

        return v;
    }

    private static IAdaptable[] convertValues(double[] values, IDataSeries parent) {
        IAdaptable[] reference = parent.getValues();
        int refIndex = reference.length - values.length;

        IAdaptable[] v = new IAdaptable[values.length];
        for (int i = 0; i < values.length; i++, refIndex++) {
            Date date = (Date) reference[refIndex].getAdapter(Date.class);
            v[i] = new NumberWrapper(date, values[i]);
        }

        return v;
    }

    private static IAdaptable[] convertValues(int[] values, IDataSeries parent) {
        IAdaptable[] reference = parent.getValues();
        int refIndex = reference.length - values.length;

        IAdaptable[] v = new IAdaptable[values.length];
        for (int i = 0; i < values.length; i++, refIndex++) {
            Date date = (Date) reference[refIndex].getAdapter(Date.class);
            v[i] = new NumberWrapper(date, values[i]);
        }

        return v;
    }

    private static IAdaptable[] convertValues(long[] values, IDataSeries parent) {
        IAdaptable[] reference = parent.getValues();
        int refIndex = reference.length - values.length;

        IAdaptable[] v = new IAdaptable[values.length];
        for (int i = 0; i < values.length; i++, refIndex++) {
            Date date = (Date) reference[refIndex].getAdapter(Date.class);
            v[i] = new NumberWrapper(date, values[i]);
        }

        return v;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.DataSeries#getSeries(org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public IDataSeries getSeries(IAdaptable first, IAdaptable last) {
        NumericDataSeries series = new NumericDataSeries(getName(), getSubset(first, last));
        if (isHighestOverride()) {
            series.setHighest(getHighest());
        }
        if (isLowestOverride()) {
            series.setLowest(getLowest());
        }
        return series;
    }
}
