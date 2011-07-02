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

package org.eclipsetrader.directa.internal.core.connector;

import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription2;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;

public class FeedSubscription2 implements IFeedSubscription2 {

    private StreamingConnector connector;
    private FeedSubscription subscription;

    public FeedSubscription2(StreamingConnector connector, FeedSubscription subscription) {
        this.connector = connector;
        this.subscription = subscription;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription2#getBook()
     */
    @Override
    public IBook getBook() {
        return subscription.getBook();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
     */
    @Override
    public void addSubscriptionListener(ISubscriptionListener listener) {
        subscription.addSubscriptionListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
     */
    @Override
    public void dispose() {
        connector.disposeSubscription2(subscription, this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
     */
    @Override
    public IFeedIdentifier getIdentifier() {
        return subscription.getIdentifier();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
     */
    @Override
    public String getSymbol() {
        return subscription.getSymbol();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
     */
    @Override
    public ILastClose getLastClose() {
        return subscription.getLastClose();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
     */
    @Override
    public IQuote getQuote() {
        return subscription.getQuote();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
     */
    @Override
    public ITodayOHL getTodayOHL() {
        return subscription.getTodayOHL();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
     */
    @Override
    public ITrade getTrade() {
        return subscription.getTrade();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
     */
    @Override
    public void removeSubscriptionListener(ISubscriptionListener listener) {
        subscription.removeSubscriptionListener(listener);
    }
}
