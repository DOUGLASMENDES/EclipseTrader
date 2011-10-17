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
 * Data series visitor implementation used to build a subset of a data
 * series and all its childrens.
 *
 * @since 1.0
 */
public class DataSeriesSubsetVisitor implements IDataSeriesVisitor {

    private IAdaptable first;
    private IAdaptable last;
    private IDataSeries subset;

    private class DateWrapper implements IAdaptable {

        private Date value;

        public DateWrapper(Date value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (value != null && adapter.isAssignableFrom(value.getClass())) {
                return value;
            }
            return null;
        }
    }

    /**
     * Constructor.
     *
     * @param first the first element in the subset.
     * @param last the last element in the subset.
     */
    public DataSeriesSubsetVisitor(IAdaptable first, IAdaptable last) {
        this.first = first;
        this.last = last;
    }

    /**
     * Constructor.
     *
     * @param first the first date in the subset.
     * @param last the last date in the subset.
     */
    public DataSeriesSubsetVisitor(Date first, Date last) {
        this.first = new DateWrapper(first);
        this.last = new DateWrapper(last);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.core.IDataSeriesVisitor#visit(org.eclipsetrader.charts.core.IDataSeries)
     */
    @Override
    public boolean visit(IDataSeries data) {
        subset = data.getSeries(first, last);

        IDataSeries[] childs = data.getChildren();
        if (childs != null) {
            IDataSeries[] childSubsets = new IDataSeries[childs.length];
            for (int i = 0; i < childs.length; i++) {
                DataSeriesSubsetVisitor visitor = new DataSeriesSubsetVisitor(first, last);
                visitor.visit(childs[i]);
                childSubsets[i] = visitor.getSubset();
            }
            subset.setChildren(childSubsets);
        }

        return false;
    }

    /**
     * Returns the subset series.
     *
     * @return the subset series.
     */
    public IDataSeries getSubset() {
        return subset;
    }
}
