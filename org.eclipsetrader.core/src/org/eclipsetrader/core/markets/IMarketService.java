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

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * @since 1.0
 */
public interface IMarketService {

    /**
     * Returns a possibly empty array of all known markets.
     *
     * @return the markets array
     */
    public IMarket[] getMarkets();

    /**
     * Returns the market instance associated with the given name.
     *
     * @param name - the market's name
     * @return the market instance or null if none found
     */
    public IMarket getMarket(String name);

    /**
     * Returns a possibly empty array of currently open markets.
     *
     * @return the markets array
     */
    public IMarket[] getOpenMarkets();

    /**
     * Returns a possibly empty array of markets that are open at the given time.
     *
     * @param time - the date and time
     * @return the markets array
     */
    public IMarket[] getOpenMarkets(Date time);

    /**
     * Adds a listener to the collection of listeners that receive notifications
     * when a market status change occurs.
     *
     * @param listener the listener to add.
     */
    public void addMarketStatusListener(IMarketStatusListener listener);

    /**
     * Removes a listener from the collection of listeners that receive notifications
     * when a market status change occurs.
     *
     * @param listener the listener to remove.
     */
    public void removeMarketStatusListener(IMarketStatusListener listener);

    /**
     * Gets the market associated with the given security.
     *
     * @param security the security.
     * @return the associated market, or <code>null</code> if the security doesn't belong to any market.
     */
    public IMarket getMarketForSecurity(ISecurity security);
}
