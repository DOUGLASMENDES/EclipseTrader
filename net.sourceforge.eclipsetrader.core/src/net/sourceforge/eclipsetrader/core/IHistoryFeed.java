/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Interface for history and interday data feed plugins.
 */
public interface IHistoryFeed
{
    /**
     * Constant for 1 minute time interval.
     */
    public static final int INTERVAL_MINUTE = 0;
    /**
     * Constant for 1 day time interval.
     */
    public static final int INTERVAL_DAILY = 8;

    /**
     * Update the history data for a security using the specified time interval.<br>
     * Minute data must be loaded and saved using the <code>Security.getIntradayHistory()</code> method,
     * daily data must be loaded and saved using the <code>Security.getHistory()</code> method.
     * <p>Implementors should always download the smallest time interval available for each category (i.e.
     * if the data source doesn't provide 1 minute data it is safe to download 2, 5 or greater minute data).</p>
     * <p>If the data source doesn't provide data for the given category (i.e. 1 minute data was requested
     * but only daily data is available) do nothing.</p>
     * 
     * @param security the security to update
     * @param interval the time interval, specified with one of the INTERVAL_* constants
     */
    public void updateHistory(Security security, int interval);
}
