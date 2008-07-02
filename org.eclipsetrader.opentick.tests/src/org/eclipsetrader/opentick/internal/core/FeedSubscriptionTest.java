/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.opentick.internal.core;

import junit.framework.TestCase;

import org.eclipsetrader.opentick.internal.core.repository.IdentifierType;

public class FeedSubscriptionTest extends TestCase {

	public void testDisposeOnFeedConnector() throws Exception {
		FeedConnector connector = new FeedConnector();
		FeedSubscription subscription = new FeedSubscription(connector, new IdentifierType("ID", "X"));
		connector.symbolSubscriptions.put("ID", subscription);
		subscription.dispose();
		assertNull(connector.symbolSubscriptions.get("X:ID"));
    }

	public void testDisposeOnOTFeedConnector() throws Exception {
		FeedConnector connector = new FeedConnector();
		FeedSubscription subscription = new FeedSubscription(connector, new IdentifierType("ID", "X"));
		connector.symbolSubscriptions.put("ID", subscription);
		subscription.dispose();
		assertNull(connector.symbolSubscriptions.get("X:ID"));
    }

	public void testIncrementInstanceCount() throws Exception {
		FeedSubscription subscription = new FeedSubscription(null, new IdentifierType("ID", "X"));
		assertEquals(0, subscription.getInstanceCount());
		subscription.incrementInstanceCount();
		assertEquals(1, subscription.getInstanceCount());
	}

	public void testDecrementInstanceCount() throws Exception {
		FeedSubscription subscription = new FeedSubscription(null, new IdentifierType("ID", "X"));
		subscription.incrementInstanceCount();
		assertEquals(1, subscription.getInstanceCount());
		int count = subscription.decrementInstanceCount();
		assertEquals(0, count);
		assertEquals(0, subscription.getInstanceCount());
	}
}
