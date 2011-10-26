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

package org.eclipsetrader.core.feed;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface to get bar values (open, high, low and close prices) for a time period.
 *
 * @since 1.0
 */
public interface IBar extends IAdaptable {

    /**
     * Returns the starting time of the time period.
     *
     * @return the starting time.
     */
    public Date getDate();

    /**
     * Gets the time span.
     * 
     * @return the time span.
     */
    public TimeSpan getTimeSpan();

    /**
     * Returns the open price.
     *
     * @return the open price.
     */
    public Double getOpen();

    /**
     * Returns the highest price.
     *
     * @return the highest price.
     */
    public Double getHigh();

    /**
     * Returns the lowest price.
     *
     * @return the lowest price.
     */
    public Double getLow();

    /**
     * Returns the close price.
     *
     * @return the close price.
     */
    public Double getClose();

    /**
     * Returns the accumulated volume of the time period.
     *
     * @return the volume.
     */
    public Long getVolume();
}
