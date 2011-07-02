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

/**
 * Stock splits interface.
 *
 * @since 1.0
 */
public interface ISplit {

    /**
     * Gets the date at which the split occurred.
     *
     * @return the date.
     */
    public Date getDate();

    /**
     * Gets the quantity before the split.
     *
     * @return the quantity.
     */
    public Double getOldQuantity();

    /**
     * Gets the quantity after the split.
     *
     * @return the quantity.
     */
    public Double getNewQuantity();
}
