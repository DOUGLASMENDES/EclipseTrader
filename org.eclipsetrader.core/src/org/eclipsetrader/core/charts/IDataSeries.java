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

import org.eclipse.core.runtime.IAdaptable;

/**
 * Implmentations of this interface represents series of data to be rendered
 * on a chart.
 *
 * @since 1.0
 */
public interface IDataSeries {

    public static final int NONE = 0;
    public static final int ABOVE = 1;
    public static final int BELOW = -1;

    /**
     * Gets the name of the receiver.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Gets a possible empty array of values.
     *
     * @return the values array.
     */
    public IAdaptable[] getValues();

    /**
     * Returns the size of the data series.
     * 
     * @return the size
     */
    public int size();

    /**
     * Returns wether this data series crosses another data series at the given value.
     * 
     * @param series the other data series
     * @param value the value to check
     * @return one of the constants <code>ABOVE</code>, <code>BELOW</code> or <code>NONE</code>
     */
    public int cross(IDataSeries series, IAdaptable value);

    /**
     * Gets the highest numeric value in this series.
     *
     * @return the highest value.
     */
    public IAdaptable getHighest();

    /**
     * Gets the lowest numeric value in this series.
     *
     * @return the lowest value.
     */
    public IAdaptable getLowest();

    /**
     * Gets the first value, in temporal order, in this series.
     *
     * @return the first value.
     */
    public IAdaptable getFirst();

    /**
     * Gets the last value, in temporal order, in this series.
     *
     * @return the last value.
     */
    public IAdaptable getLast();

    /**
     * Gets a series that is a subset of the receiver.
     *
     * @param first the first value to include.
     * @param last the last value to include.
     * @return the subset series.
     */
    public IDataSeries getSeries(IAdaptable first, IAdaptable last);

    /**
     * Gets a possible empty array of series that are direct childrens of the receiver.
     *
     * @return the childs series, or <code>null</code> if the receiver has no childrens.
     */
    public IDataSeries[] getChildren();

    /**
     * Sets the array of data series that are direct childrens of the receiver.
     *
     * @param childrens the child data series.
     */
    public void setChildren(IDataSeries[] childrens);

    /**
     * Accepts the visitor used to visit the receiver and all its childrens.
     *
     * @param visitor the visitor instance.
     */
    public void accept(IDataSeriesVisitor visitor);
}
