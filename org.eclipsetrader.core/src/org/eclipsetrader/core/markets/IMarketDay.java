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

package org.eclipsetrader.core.markets;

import java.util.Date;

/**
 * @since 1.0
 */
public interface IMarketDay {

    /**
     * Returns the market open time, or null if the market is closed today.
     *
     * @return the open time or null
     */
    public Date getOpenTime();

    /**
     * Returns the market close time, or null if the market is closed today.
     *
     * @return the close time or null
     */
    public Date getCloseTime();

    /**
     * Returns wether the market is currently open.
     *
     * @return true if the market is open, false if closed
     */
    public boolean isOpen();

    /**
     * Returns wether the market is open at the given time.
     *
     * @return true if the market is open, false if closed
     */
    public boolean isOpen(Date time);

    /**
     * Returns the message associated with today, or null if the market doesn't
     * have any message.
     *
     * @return the message
     */
    public String getMessage();
}
