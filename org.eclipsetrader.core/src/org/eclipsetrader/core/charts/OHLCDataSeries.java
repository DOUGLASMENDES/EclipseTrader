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
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;

/**
 * Data series implementation representing <code>IOHLC</code> values.
 *
 * @since 1.0
 * @see org.eclipsetrader.core.feed.IOHLC
 */
public class OHLCDataSeries extends DataSeries {

    private TimeSpan resolution;

    private static class OHLCWrapper implements IAdaptable {

        private IOHLC ohlc;

        public OHLCWrapper(IOHLC ohlc) {
            this.ohlc = ohlc;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        public Object getAdapter(Class adapter) {
            if (ohlc != null && adapter.isAssignableFrom(ohlc.getClass())) {
                return ohlc;
            }
            if (adapter.isAssignableFrom(Date.class)) {
                return ohlc.getDate();
            }
            if (adapter.isAssignableFrom(Double.class) || adapter.isAssignableFrom(Number.class)) {
                return ohlc.getClose();
            }
            return null;
        }
    }

    private static IAdaptable[] convertValues(IOHLC[] values) {
        IAdaptable[] v = new OHLCWrapper[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = new OHLCWrapper(values[i]);
        }
        return v;
    }

    public OHLCDataSeries(String name, IOHLC[] values, TimeSpan resolution) {
        super(name, convertValues(values));
        this.resolution = resolution;
    }

    protected OHLCDataSeries(String name, IAdaptable[] values, TimeSpan resolution) {
        super(name, values);
        this.resolution = resolution;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.DataSeries#getSeries(org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public IDataSeries getSeries(IAdaptable first, IAdaptable last) {
        return new OHLCDataSeries(getName(), getSubset(first, last), resolution);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.DataSeries#getHighest()
     */
    @Override
    public IAdaptable getHighest() {
        IAdaptable v = super.getHighest();
        if (v != null) {
            IOHLC ohlc = (IOHLC) v.getAdapter(IOHLC.class);
            if (ohlc != null) {
                v = new NumberValue(ohlc.getDate(), ohlc.getHigh());
            }
        }
        return v;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.DataSeries#getLowest()
     */
    @Override
    public IAdaptable getLowest() {
        IAdaptable v = super.getLowest();
        if (v != null) {
            IOHLC ohlc = (IOHLC) v.getAdapter(IOHLC.class);
            if (ohlc != null) {
                v = new NumberValue(ohlc.getDate(), ohlc.getLow());
            }
        }
        return v;
    }

    public TimeSpan getResolution() {
        return resolution;
    }
}
