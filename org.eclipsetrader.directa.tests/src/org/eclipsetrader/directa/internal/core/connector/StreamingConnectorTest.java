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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.directa.internal.core.repository.IdentifiersList;

public class StreamingConnectorTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        new IdentifiersList();
    }

    public void testSubscribeSameSymbolResultInSameInstance() throws Exception {
        StreamingConnector connector = new StreamingConnector();
        IFeedSubscription subscription = connector.subscribe(new FeedIdentifier("PG", null));
        IFeedSubscription subscription2 = connector.subscribe(new FeedIdentifier("PG", null));
        assertSame(subscription, subscription2);
    }

    public void testSubscribeLevel2And1ResultInSameInstance() throws Exception {
        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription2 = (FeedSubscription) connector.subscribeLevel2("PG");
        assertNull(subscription2.getIdentifier());
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("PG", null));
        assertNotNull(subscription.getIdentifier());
        assertNotNull(subscription2.getIdentifier());
        assertSame(subscription2, subscription);
    }

    public void testFireLevel2And1Notifications() throws Exception {
        StreamingConnector connector = new StreamingConnector();
        final Set<String> notifications = new HashSet<String>();

        FeedSubscription subscription = (FeedSubscription) connector.subscribeLevel2("PG");
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                notifications.add("subscription");
            }
        });

        IFeedSubscription subscription2 = connector.subscribe(new FeedIdentifier("PG", null));
        subscription2.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                notifications.add("subscription2");
            }
        });

        subscription.addDelta(new QuoteDelta(subscription.getIdentifier(), new Integer(1), new Integer(2)));
        subscription.fireNotification();
        assertEquals(2, notifications.size());
    }
}
