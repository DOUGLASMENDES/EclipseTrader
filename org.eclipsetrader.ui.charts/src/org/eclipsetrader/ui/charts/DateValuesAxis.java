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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Implementation of an axis that holds <code>Date</code> values.
 *
 * @since 1.0
 */
public class DateValuesAxis implements IAxis {

    public double gridSize = 5.0;
    public int additionalSpace = 0;
    public boolean fillAvailableSpace;
    public int zoomFactor = 0;

    private List<Date> sortedList = new ArrayList<Date>();

    public DateValuesAxis() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#addValues(java.lang.Object[])
     */
    @Override
    public void addValues(Object[] values) {
        for (Object v : values) {
            Date value = null;

            if (v instanceof Date) {
                value = (Date) v;
            }
            if (v instanceof IAdaptable) {
                value = (Date) ((IAdaptable) v).getAdapter(Date.class);
            }

            if (value != null && !sortedList.contains(value)) {
                sortedList.add(value);
            }
        }

        Collections.sort(sortedList);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#clear()
     */
    @Override
    public void clear() {
        sortedList.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#computeSize(int)
     */
    @Override
    public int computeSize(int preferredSize) {
        if (fillAvailableSpace) {
            gridSize = (double) preferredSize / sortedList.size();
            return preferredSize;
        }
        else {
            return (int) (gridSize * sortedList.size() + gridSize * additionalSpace);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#mapToAxis(java.lang.Object)
     */
    @Override
    public int mapToAxis(Object value) {
        if (value instanceof Date) {
            int index = Collections.binarySearch(sortedList, (Date) value);
            if (index < 0) {
                index = -(index + 1);
            }
            return (int) (gridSize / 2 + index * gridSize);
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#mapToValue(int)
     */
    @Override
    public Object mapToValue(int position) {
        if (sortedList.size() == 0) {
            return null;
        }
        int index = (int) (position / gridSize);
        if (index < 0) {
            index = 0;
        }
        if (index >= sortedList.size()) {
            index = sortedList.size() - 1;
        }
        return sortedList.get(index);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getFirstValue()
     */
    @Override
    public Object getFirstValue() {
        return sortedList.size() != 0 ? sortedList.get(0) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getLastValue()
     */
    @Override
    public Object getLastValue() {
        return sortedList.size() != 0 ? sortedList.get(sortedList.size() - 1) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getValues()
     */
    @Override
    public Object[] getValues() {
        return sortedList.toArray(new Date[sortedList.size()]);
    }

    public int getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(int zoomFactor) {
        this.zoomFactor = zoomFactor;
        this.gridSize = zoomFactor != 0 ? 5.0 + 2.0 * zoomFactor : 5.0;
    }
}
