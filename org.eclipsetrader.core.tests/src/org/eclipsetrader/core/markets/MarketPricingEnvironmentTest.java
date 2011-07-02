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

import junit.framework.TestCase;

import org.eclipsetrader.core.TestFeedConnector;
import org.eclipsetrader.core.TestMarket;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

public class MarketPricingEnvironmentTest extends TestCase {

    private TestMarket market1;
    private TestMarket market2;
    private TestFeedConnector connector1;
    private TestFeedConnector connector2;
    private ISecurity security1;
    private ISecurity security2;
    private MarketPricingEnvironment environment;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security1 = new Security("Security1", new FeedIdentifier("id1", null));
        security2 = new Security("Security2", new FeedIdentifier("id2", null));

        connector1 = new TestFeedConnector("id1", "Connector1");
        connector2 = new TestFeedConnector("id2", "Connector2");

        market1 = new TestMarket("Market1");
        market1.setLiveFeedConnector(connector1);
        market1.addMembers(new ISecurity[] {
            security1
        });

        market2 = new TestMarket("Market2");
        market2.setLiveFeedConnector(connector2);
        market2.addMembers(new ISecurity[] {
            security2
        });

        environment = new MarketPricingEnvironment() {

            @Override
            protected IMarket getMarketsForSecurity(ISecurity security) {
                if (security == security1) {
                    return market1;
                }
                if (security == security2) {
                    return market2;
                }
                return null;
            }
        };
    }

    public void testAddSecurity() throws Exception {
        environment.addSecurities(new ISecurity[] {
            security1
        });
        assertNotNull(environment.getSubscriptionStatus(security1));
        assertEquals(1, connector1.getSubscriptions().size());
    }

    public void testRemoveSecurity() throws Exception {
        environment.addSecurities(new ISecurity[] {
            security1
        });
        environment.removeSecurities(new ISecurity[] {
            security1
        });
        assertNull(environment.getSubscriptionStatus(security1));
        assertEquals(0, connector1.getSubscriptions().size());
    }

    public void testChangeMarketConnector() throws Exception {
        environment.addSecurities(new ISecurity[] {
            security1
        });
        environment.handleMarketChanges(market1, IMarket.PROP_LIVE_FEED_CONNECTOR, connector1, connector2);
        assertNotNull(environment.getSubscriptionStatus(security1));
        assertEquals(0, connector1.getSubscriptions().size());
        assertEquals(1, connector2.getSubscriptions().size());
    }

    public void testSubscribeSecurity() throws Exception {
        environment.addSecurities(new ISecurity[] {
            security1
        });
        assertNotNull(environment.getSubscriptionStatus(security1));
        assertEquals(1, connector1.getSubscriptions().size());
        assertEquals(0, connector2.getSubscriptions().size());
    }

    public void testUnsubscribeSecurityFromMultipleMarkets() throws Exception {
        environment.addSecurities(new ISecurity[] {
            security1
        });
        environment.removeSecurities(new ISecurity[] {
            security1
        });
        assertNull(environment.getSubscriptionStatus(security1));
        assertEquals(0, connector1.getSubscriptions().size());
        assertEquals(0, connector2.getSubscriptions().size());
    }
}
