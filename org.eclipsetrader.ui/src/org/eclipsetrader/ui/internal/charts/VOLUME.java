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

package org.eclipsetrader.ui.internal.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.IDataSeriesVisitor;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.HistogramBarChart;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;

public class VOLUME implements IChartObjectFactory, IExecutableExtension {

    private String id;
    private String name;

    public VOLUME() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public IChartObject createObject(IDataSeries source) {
        if (source != null) {
            IDataSeries result = new VolumeDataSeries(getName(), source.getValues());
            return new HistogramBarChart(result);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    @Override
    public IChartParameters getParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    @Override
    public void setParameters(IChartParameters parameters) {
        name = parameters.hasParameter("name") ? parameters.getString("name") : name;
    }

    private static class VolumeValueWrapper implements IAdaptable {

        private IOHLC ohlc;

        public VolumeValueWrapper(IOHLC ohlc) {
            this.ohlc = ohlc;
        }

        public IOHLC getOhlc() {
            return ohlc;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(Date.class)) {
                return ohlc.getDate();
            }
            if (adapter.isAssignableFrom(Number.class)) {
                return ohlc.getVolume();
            }
            if (adapter.isAssignableFrom(getClass())) {
                return this;
            }
            return null;
        }
    }

    private static class VolumeDataSeries implements IDataSeries {

        private String name;
        private IAdaptable first;
        private IAdaptable last;
        private IAdaptable highest;
        private IAdaptable lowest;
        private IAdaptable[] values;

        private Long lowestValue;
        private Long highestValue;
        private Date firstValue;
        private Date lastValue;

        public VolumeDataSeries(String name, IAdaptable[] values) {
            this.name = name;
            this.values = new IAdaptable[values.length];
            for (int i = 0; i < values.length; i++) {
                IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                this.values[i] = new VolumeValueWrapper(ohlc);
                updateHighestLowest(ohlc.getVolume(), this.values[i]);
                if (ohlc.getDate() != null) {
                    updateFirstLast(ohlc.getDate(), this.values[i]);
                }
            }
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

        /* (non-Javadoc)
         * @see org.eclipsetrader.charts.core.IDataSeries#getLowest()
         */
        @Override
        public IAdaptable getLowest() {
            return lowest;
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
            return new DataSeries(getName(), getSubset(first, last));
        }

        protected IAdaptable[] getSubset(IAdaptable first, IAdaptable last) {
            Date firstValue = first != null ? (Date) first.getAdapter(Date.class) : null;
            Date lastValue = last != null ? (Date) last.getAdapter(Date.class) : null;

            List<IAdaptable> list = new ArrayList<IAdaptable>(values.length);
            for (IAdaptable v : values) {
                Date date = (Date) v.getAdapter(Date.class);
                if ((firstValue == null || !date.before(firstValue)) && (lastValue == null || !date.after(lastValue))) {
                    list.add(v);
                }
            }

            return list.toArray(new IAdaptable[list.size()]);
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.charts.IDataSeries#isHighestOverride()
         */
        @Override
        public boolean isHighestOverride() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.charts.IDataSeries#isLowestOverride()
         */
        @Override
        public boolean isLowestOverride() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.charts.core.IDataSeries#getChildren()
         */
        @Override
        public IDataSeries[] getChildren() {
            return new IDataSeries[0];
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.charts.core.IDataSeries#setChildren(org.eclipsetrader.charts.core.IDataSeries[])
         */
        @Override
        public void setChildren(IDataSeries[] childrens) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.charts.core.IDataSeries#accept(org.eclipsetrader.charts.core.IDataSeriesVisitor)
         */
        @Override
        public void accept(IDataSeriesVisitor visitor) {
            visitor.visit(this);
        }

        private void updateHighestLowest(Long value, IAdaptable reference) {
            if (lowestValue == null || value < lowestValue) {
                lowestValue = value;
                lowest = reference;
            }
            if (highestValue == null || value > highestValue) {
                highestValue = value;
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
}
