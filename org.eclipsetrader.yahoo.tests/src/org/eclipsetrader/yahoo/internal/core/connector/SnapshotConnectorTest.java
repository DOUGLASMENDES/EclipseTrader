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

package org.eclipsetrader.yahoo.internal.core.connector;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.yahoo.internal.core.repository.IdentifierType;
import org.eclipsetrader.yahoo.internal.core.repository.IdentifiersList;

public class SnapshotConnectorTest extends TestCase {

    SnapshotConnector connector;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        new IdentifiersList();
        connector = new SnapshotConnector();
    }

    public void testSubscribeWithoutProperties() throws Exception {
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("ID", null));
        assertSame(subscription, connector.symbolSubscriptions.get("ID"));
        assertTrue(connector.isSubscriptionsChanged());
        assertEquals(1, subscription.getInstanceCount());
    }

    public void testSubscribeWithProperties() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("ID", new FeedProperties());
        identifier.getProperties().setProperty("org.eclipsetrader.yahoo.symbol", "Y-ID");
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(identifier);
        assertNull(connector.symbolSubscriptions.get("ID"));
        assertSame(subscription, connector.symbolSubscriptions.get("Y-ID"));
        assertTrue(connector.isSubscriptionsChanged());
        assertEquals(1, subscription.getInstanceCount());
    }

    public void testDisposeSubcription() throws Exception {
        FeedSubscription subscription = new FeedSubscription(connector, new IdentifierType("ID"));
        connector.symbolSubscriptions.put("ID", subscription);
        connector.disposeSubscription(subscription);
        assertNull(connector.symbolSubscriptions.get("ID"));
        assertTrue(connector.isSubscriptionsChanged());
    }

    public void testUseSingleInstanceWithMultipleSubscriptions() throws Exception {
        FeedSubscription subscription1 = (FeedSubscription) connector.subscribe(new FeedIdentifier("ID", null));
        FeedSubscription subscription2 = (FeedSubscription) connector.subscribe(new FeedIdentifier("ID", null));
        assertSame(subscription1, subscription2);
        assertEquals(2, subscription1.getInstanceCount());
    }

    public void testDisposeSubcriptionOnLastInstance() throws Exception {
        FeedSubscription subscription = new FeedSubscription(connector, new IdentifierType("ID"));
        subscription.incrementInstanceCount();
        subscription.incrementInstanceCount();
        connector.symbolSubscriptions.put("ID", subscription);
        connector.disposeSubscription(subscription);
        assertNotNull(connector.symbolSubscriptions.get("ID"));
        assertFalse(connector.isSubscriptionsChanged());
        connector.disposeSubscription(subscription);
        assertNull(connector.symbolSubscriptions.get("ID"));
        assertTrue(connector.isSubscriptionsChanged());
    }

    public void testUpdateStreamWhenIdentifierChanges() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("ID", null);
        connector.subscribe(identifier);
        connector.setSubscriptionsChanged(false);
        identifier.setSymbol("NEWID");
        assertTrue(connector.isSubscriptionsChanged());
        assertNull(connector.symbolSubscriptions.get("ID"));
        assertNotNull(connector.symbolSubscriptions.get("NEWID"));
    }

    public void testUpdateStreamWhenIdentifierPropertyChanges() throws Exception {
        FeedIdentifier identifier = new FeedIdentifier("ID", new FeedProperties());
        identifier.getProperties().setProperty("org.eclipsetrader.yahoo.symbol", "Y-ID");
        connector.subscribe(identifier);
        connector.setSubscriptionsChanged(false);
        identifier.getProperties().setProperty("org.eclipsetrader.yahoo.symbol", "Y-NEWID");
        assertTrue(connector.isSubscriptionsChanged());
        assertNull(connector.symbolSubscriptions.get("ID"));
        assertNull(connector.symbolSubscriptions.get("Y-ID"));
        assertNotNull(connector.symbolSubscriptions.get("Y-NEWID"));
    }
}
