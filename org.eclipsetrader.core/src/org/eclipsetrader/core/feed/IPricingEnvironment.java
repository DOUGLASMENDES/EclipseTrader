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

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Pricing environments provides methods to listen and get quotes from a
 * set of securities independently of the underlying feed.
 *
 * @since 1.0
 */
public interface IPricingEnvironment {

    /**
     * Adds a listener to the collection of listeners that receives notifications
     * when a pricing value is updated.
     *
     * @param listener the listener to add.
     */
    public void addPricingListener(IPricingListener listener);

    /**
     * Removes a listener from the collection of listeners that receives notifications
     * when a pricing value is updated.
     *
     * @param listener the listener to add.
     */
    public void removePricingListener(IPricingListener listener);

    /**
     * Returns the most recent trade value for a security.
     *
     * @param security the security.
     * @return the latest trade value.
     */
    public ITrade getTrade(ISecurity security);

    /**
     * Returns the most recent quote value for a security.
     *
     * @param security the security.
     * @return the latest quote value.
     */
    public IQuote getQuote(ISecurity security);

    /**
     * Returns the most recent today's OHL value for a security.
     *
     * @param security the security.
     * @return the latest OHL value.
     */
    public ITodayOHL getTodayOHL(ISecurity security);

    /**
     * Returns last close price.
     *
     * @param security the security.
     * @return the last close value.
     */
    public ILastClose getLastClose(ISecurity security);

    /**
     * Gets the level II book.
     *
     * @param security the security.
     * @return the level II book.
     */
    public IBook getBook(ISecurity security);

    /**
     * Disposes the receiver and all associated resources.
     */
    public void dispose();
}
