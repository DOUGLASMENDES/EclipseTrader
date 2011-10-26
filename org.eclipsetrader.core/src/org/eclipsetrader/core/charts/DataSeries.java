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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IOHLC;

/**
 * Default implementation of a data series.
 *
 * @since 1.0
 */
public class DataSeries implements IDataSeries {

    private final String name;
    private final IAdaptable[] values;

    private IAdaptable first;
    private IAdaptable last;
    private IAdaptable highest;
    private IAdaptable lowest;

    private IDataSeries[] childrens;

    private boolean highestOverride = false;
    private boolean lowestOverride = false;

    private class RangeVisitor implements IDataSeriesVisitor {

        private Double lowestValue;
        private Double highestValue;
        private Date firstValue;
        private Date lastValue;

        public RangeVisitor() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.charts.core.IDataSeriesVisitor#visit(org.eclipsetrader.charts.core.IDataSeries)
         */
        @Override
        public boolean visit(IDataSeries data) {
            for (IAdaptable v : data.getValues()) {
                IOHLC ohlc = (IOHLC) v.getAdapter(IOHLC.class);
                if (ohlc != null) {
                    updateHighestLowest(ohlc.getLow() != null ? ohlc.getLow() : ohlc.getClose(), v);
                    updateHighestLowest(ohlc.getHigh() != null ? ohlc.getHigh() : ohlc.getClose(), v);
                    if (ohlc.getDate() != null) {
                        updateFirstLast(ohlc.getDate(), v);
                    }
                }
                else {
                    Number value = (Number) v.getAdapter(Number.class);
                    updateHighestLowest(value, v);

                    Date date = (Date) v.getAdapter(Date.class);
                    if (date != null) {
                        updateFirstLast(date, v);
                    }
                }
            }

            if (data instanceof DataSeries) {
                if (((DataSeries) data).highestOverride) {
                    highest = data.getHighest();
                }
                if (((DataSeries) data).lowestOverride) {
                    lowest = data.getLowest();
                }
            }

            return true;
        }

        private void updateHighestLowest(Number value, IAdaptable reference) {
            if (lowestValue == null || value.doubleValue() < lowestValue) {
                lowestValue = value.doubleValue();
                lowest = reference;
            }
            if (highestValue == null || value.doubleValue() > highestValue) {
                highestValue = value.doubleValue();
                highest = reference;
            }
        }

        private void updateFirstLast(Date date, IAdaptable reference) {
            if (firstValue == null || date.before(firstValue)) {
                firstValue = date;
                first = reference;
            }
            if (lastValue == null || date.after(lastValue)) {
                lastValue = date;
                last = reference;
            }
        }
    }

    public DataSeries(String name, IAdaptable[] values) {
        this.name = name;
        this.values = values;
        accept(new RangeVisitor());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getFirst()
     */
    @Override
    public IAdaptable getFirst() {
        return first;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getLast()
     */
    @Override
    public IAdaptable getLast() {
        return last;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getHighest()
     */
    @Override
    public IAdaptable getHighest() {
        return highest;
    }

    public void setHighest(IAdaptable highest) {
        this.highest = highest;
        this.highestOverride = true;
    }

    protected boolean isHighestOverride() {
        return highestOverride;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getLowest()
     */
    @Override
    public IAdaptable getLowest() {
        return lowest;
    }

    public void setLowest(IAdaptable lowest) {
        this.lowest = lowest;
        this.lowestOverride = true;
    }

    protected boolean isLowestOverride() {
        return lowestOverride;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.IDataSeries#size()
     */
    @Override
    public int size() {
        return values.length;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getValues()
     */
    @Override
    public IAdaptable[] getValues() {
        return values;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getSeries(org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public IDataSeries getSeries(IAdaptable first, IAdaptable last) {
        DataSeries series = new DataSeries(getName(), getSubset(first, last));
        if (highestOverride) {
            series.setHighest(getHighest());
        }
        if (lowestOverride) {
            series.setLowest(getLowest());
        }
        return series;
    }

    protected IAdaptable[] getSubset(IAdaptable first, IAdaptable last) {
        Date firstValue = first != null ? (Date) first.getAdapter(Date.class) : null;
        Date lastValue = last != null ? (Date) last.getAdapter(Date.class) : null;

        List<IAdaptable> list = new ArrayList<IAdaptable>(values.length);
        for (int i = 0; i < values.length; i++) {
            Date date = (Date) values[i].getAdapter(Date.class);
            if ((firstValue == null || !date.before(firstValue)) && (lastValue == null || !date.after(lastValue))) {
                list.add(values[i]);
            }
        }

        return list.toArray(new IAdaptable[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.IDataSeries#cross(org.eclipsetrader.core.charts.IDataSeries, org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public int cross(IDataSeries series, IAdaptable value) {
        if (values.length <= 1 || series.size() <= 1) {
            return NONE;
        }

        int index = indexOf(this, value);
        if (index <= 0) {
            return NONE;
        }

        int otherIndex = indexOf(series, value);
        if (otherIndex <= 0) {
            return NONE;
        }

        Number ourValue = (Number) values[index - 1].getAdapter(Number.class);
        Number ourNextValue = (Number) values[index].getAdapter(Number.class);
        Number otherValue = (Number) series.getValues()[otherIndex - 1].getAdapter(Number.class);
        Number otherNextValue = (Number) series.getValues()[otherIndex].getAdapter(Number.class);
        if (ourValue.doubleValue() < otherValue.doubleValue() && ourNextValue.doubleValue() > otherNextValue.doubleValue()) {
            return ABOVE;
        }
        if (ourValue.doubleValue() > otherValue.doubleValue() && ourNextValue.doubleValue() < otherNextValue.doubleValue()) {
            return BELOW;
        }

        return NONE;
    }

    protected int indexOf(IDataSeries series, IAdaptable value) {
        IAdaptable[] values = series.getValues();
        Date valueDate = (Date) value.getAdapter(Date.class);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
            Date date = (Date) values[i].getAdapter(Date.class);
            if (date != null && date.equals(valueDate)) {
                return i;
            }
        }

        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#getChildren()
     */
    @Override
    public IDataSeries[] getChildren() {
        return childrens;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#setChildren(org.eclipsetrader.charts.core.IDataSeries[])
     */
    @Override
    public void setChildren(IDataSeries[] childrens) {
        this.childrens = childrens;
        first = null;
        last = null;
        highest = null;
        lowest = null;
        accept(new RangeVisitor());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeries#accept(org.eclipsetrader.charts.core.IDataSeriesVisitor)
     */
    @Override
    public void accept(IDataSeriesVisitor visitor) {
        if (visitor.visit(this)) {
            if (childrens != null) {
                for (int i = 0; i < childrens.length; i++) {
                    childrens[i].accept(visitor);
                }
            }
        }
    }
}
