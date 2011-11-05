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
 * Holds the trade informations.
 *
 * @since 1.0
 */
public interface ITrade extends IPrice {

    /**
     * Returns the size of the trade, if the data source makes this information
     * available.
     *
     * @return the size, or <code>null</code> if not available.
     */
    public Long getSize();

    /**
     * Returns the cumulative trade volume for the day.
     *
     * @return the cumulative volume, or <code>null</code> if not available.
     */
    public Long getVolume();
}
