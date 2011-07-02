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

/**
 * Interface for objects representing an axis on a chart.
 *
 * @since 1.0
 */
public interface IAxis {

    /**
     * Clears the values associated with the receiver.
     */
    public void clear();

    /**
     * Add values to the receiver.
     *
     * @param values the values to add.
     */
    public void addValues(Object[] values);

    /**
     * Computes and returns the size in pixels of this axis.
     *
     * @param preferredSize the preferred size.
     * @return the axis size.
     */
    public int computeSize(int preferredSize);

    /**
     * Maps a value to a position on the axis.
     *
     * @param value the value to map.
     * @return the position.
     */
    public int mapToAxis(Object value);

    /**
     * Maps a position on the axis to a value.
     *
     * @param position the position to map.
     * @return the value.
     */
    public Object mapToValue(int position);

    /**
     * Returns the first value on the axis.
     *
     * @return the value.
     */
    public Object getFirstValue();

    /**
     * Returns the last value on the axis.
     *
     * @return the value.
     */
    public Object getLastValue();

    /**
     * Returns all values associated to the receiver.
     *
     * @return the values array.
     */
    public Object[] getValues();
}
