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

package org.eclipsetrader.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipsetrader.core.feed.IConnectorListener;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;

public class TestFeedConnector implements IFeedConnector {

    private String id;
    private String name;
    private Set<IFeedSubscription> subscriptions = new HashSet<IFeedSubscription>();

    public TestFeedConnector(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#connect()
     */
    @Override
    public void connect() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#disconnect()
     */
    @Override
    public void disconnect() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#subscribe(org.eclipsetrader.core.feed.IFeedIdentifier)
     */
    @Override
    public IFeedSubscription subscribe(final IFeedIdentifier identifier) {
        IFeedSubscription s = new IFeedSubscription() {

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#dispose()
             */
            @Override
            public void dispose() {
                subscriptions.remove(this);
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#addSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
             */
            @Override
            public void addSubscriptionListener(ISubscriptionListener listener) {
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getIdentifier()
             */
            @Override
            public IFeedIdentifier getIdentifier() {
                return identifier;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getSymbol()
             */
            @Override
            public String getSymbol() {
                return identifier.getSymbol();
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getLastClose()
             */
            @Override
            public ILastClose getLastClose() {
                return null;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getQuote()
             */
            @Override
            public IQuote getQuote() {
                return null;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getTodayOHL()
             */
            @Override
            public ITodayOHL getTodayOHL() {
                return null;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#getTrade()
             */
            @Override
            public ITrade getTrade() {
                return null;
            }

            /* (non-Javadoc)
             * @see org.eclipsetrader.core.feed.IFeedSubscription#removeSubscriptionListener(org.eclipsetrader.core.feed.ISubscriptionListener)
             */
            @Override
            public void removeSubscriptionListener(ISubscriptionListener listener) {
            }
        };
        subscriptions.add(s);
        return s;
    }

    public Set<IFeedSubscription> getSubscriptions() {
        return subscriptions;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#addConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void addConnectorListener(IConnectorListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IFeedConnector#removeConnectorListener(org.eclipsetrader.core.feed.IConnectorListener)
     */
    @Override
    public void removeConnectorListener(IConnectorListener listener) {
    }
}
