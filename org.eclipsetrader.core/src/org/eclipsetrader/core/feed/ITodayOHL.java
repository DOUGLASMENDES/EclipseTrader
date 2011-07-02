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

/**
 * Interface to get today's OHL values (open, high and low prices).
 *
 * @since 1.0
 */
public interface ITodayOHL {

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
}
