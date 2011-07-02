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
 * Manages the subscription of a feed identifier to a connector.
 *
 * @since 1.0
 */
public interface ILevel2FeedSubscription {

    /**
     * Returns the identifier associated with this subscription.
     *
     * @return the identifier.
     */
    public IFeedIdentifier getIdentifier();

    /**
     * Disposes the receiver and all associated resources.
     */
    public void dispose();

    /**
     * Returns the current level II book.
     *
     * @return the level II book.
     */
    public IBook getBook();

    /**
     * Adds a listener to the collection of listeners that receive
     * notifications when a quote is updated.
     *
     * @param listener the listener to add.
     */
    public void addSubscriptionListener(ISubscriptionListener listener);

    /**
     * Removes a listener from the collection of listeners that receive
     * notifications when a quote is updated.
     *
     * @param listener the listener to remove.
     */
    public void removeSubscriptionListener(ISubscriptionListener listener);
}
