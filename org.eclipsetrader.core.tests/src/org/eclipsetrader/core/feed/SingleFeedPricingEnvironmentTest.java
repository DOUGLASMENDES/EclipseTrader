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

package org.eclipsetrader.core.feed;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

public class SingleFeedPricingEnvironmentTest extends TestCase {
	private IFeedConnector connector;
	private List<IFeedSubscription> subscriptions;

	/* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    	subscriptions = new ArrayList<IFeedSubscription>();
	    connector = new IFeedConnector() {
            public void connect() {
            }

            public void disconnect() {
            }

            public String getId() {
	            return "test.id";
            }

            public String getName() {
	            return "Test Connector";
            }

            public IFeedSubscription subscribe(final IFeedIdentifier identifier) {
            	IFeedSubscription s = new IFeedSubscription() {
                    public void addSubscriptionListener(ISubscriptionListener listener) {
                    }

                    public void dispose() {
                    	subscriptions.remove(this);
                    }

                    public IFeedIdentifier getIdentifier() {
	                    return identifier;
                    }

                    public ILastClose getLastClose() {
	                    return null;
                    }

                    public IQuote getQuote() {
	                    return null;
                    }

                    public ITodayOHL getTodayOHL() {
	                    return null;
                    }

                    public ITrade getTrade() {
	                    return null;
                    }

                    public void removeSubscriptionListener(ISubscriptionListener listener) {
                    }
            	};
            	subscriptions.add(s);
	            return s;
            }
	    };
    }

	/* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
    	subscriptions.clear();
    	connector = null;
    }

	public void testAddSecurity() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		Security sec = new Security("Sample", new FeedIdentifier("ID", null));
		environment.addSecurities(new ISecurity[] { sec });
		assertEquals(1, environment.securitiesMap.size());
		assertEquals(1, environment.identifiersMap.size());
		assertEquals(1, subscriptions.size());
    }

	public void testAddSameSecurityOnlyOnce() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		Security sec = new Security("Sample", new FeedIdentifier("ID", null));
		environment.addSecurities(new ISecurity[] { sec });
		environment.addSecurities(new ISecurity[] { sec });
		assertEquals(1, environment.securitiesMap.size());
		assertEquals(1, environment.identifiersMap.size());
		assertEquals(1, subscriptions.size());
    }

	public void testAddSameIdentifierOnlyOnce() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		FeedIdentifier id = new FeedIdentifier("ID", null);
		Security sec1 = new Security("Sample 1", id);
		Security sec2 = new Security("Sample 2", id);
		environment.addSecurities(new ISecurity[] { sec1 });
		environment.addSecurities(new ISecurity[] { sec2 });
		assertEquals(2, environment.securitiesMap.size());
		assertEquals(1, environment.identifiersMap.size());
		assertEquals(1, subscriptions.size());
    }

	public void testRemoveSecurity() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		Security sec = new Security("Sample", new FeedIdentifier("ID", null));
		environment.addSecurities(new ISecurity[] { sec });
		environment.removeSecurities(new ISecurity[] { sec });
		assertEquals(0, environment.securitiesMap.size());
		assertEquals(0, environment.identifiersMap.size());
		assertEquals(0, subscriptions.size());
    }

	public void testRemoveSameSecurityOnlyOnce() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		Security sec = new Security("Sample", new FeedIdentifier("ID", null));
		environment.addSecurities(new ISecurity[] { sec });
		environment.removeSecurities(new ISecurity[] { sec });
		assertEquals(0, environment.securitiesMap.size());
		assertEquals(0, environment.identifiersMap.size());
		assertEquals(0, subscriptions.size());
    }

	public void testRemoveIdentifierWhenAllSecuritiesAreRemoved() throws Exception {
		SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
		FeedIdentifier id = new FeedIdentifier("ID", null);
		Security sec1 = new Security("Sample 1", id);
		Security sec2 = new Security("Sample 2", id);
		environment.addSecurities(new ISecurity[] { sec1 });
		environment.addSecurities(new ISecurity[] { sec2 });
		environment.removeSecurities(new ISecurity[] { sec1 });
		assertEquals(1, environment.securitiesMap.size());
		assertEquals(1, environment.identifiersMap.size());
		assertEquals(1, subscriptions.size());
		environment.removeSecurities(new ISecurity[] { sec2 });
		assertEquals(0, environment.securitiesMap.size());
		assertEquals(0, environment.identifiersMap.size());
		assertEquals(0, subscriptions.size());
    }
}
