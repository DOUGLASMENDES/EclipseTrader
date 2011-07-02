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

            @Override
            public void connect() {
            }

            @Override
            public void disconnect() {
            }

            @Override
            public String getId() {
                return "test.id";
            }

            @Override
            public String getName() {
                return "Test Connector";
            }

            @Override
            public IFeedSubscription subscribe(final IFeedIdentifier identifier) {
                IFeedSubscription s = new IFeedSubscription() {

                    @Override
                    public void addSubscriptionListener(ISubscriptionListener listener) {
                    }

                    @Override
                    public void dispose() {
                        subscriptions.remove(this);
                    }

                    @Override
                    public IFeedIdentifier getIdentifier() {
                        return identifier;
                    }

                    @Override
                    public String getSymbol() {
                        return identifier.getSymbol();
                    }

                    @Override
                    public ILastClose getLastClose() {
                        return null;
                    }

                    @Override
                    public IQuote getQuote() {
                        return null;
                    }

                    @Override
                    public ITodayOHL getTodayOHL() {
                        return null;
                    }

                    @Override
                    public ITrade getTrade() {
                        return null;
                    }

                    @Override
                    public void removeSubscriptionListener(ISubscriptionListener listener) {
                    }
                };
                subscriptions.add(s);
                return s;
            }

            @Override
            public void addConnectorListener(IConnectorListener listener) {
            }

            @Override
            public void removeConnectorListener(IConnectorListener listener) {
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
        environment.addSecurities(new ISecurity[] {
            sec
        });
        assertEquals(1, environment.securitiesMap.size());
        assertEquals(1, environment.identifiersMap.size());
        assertEquals(1, subscriptions.size());
    }

    public void testAddSameSecurityOnlyOnce() throws Exception {
        SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
        Security sec = new Security("Sample", new FeedIdentifier("ID", null));
        environment.addSecurities(new ISecurity[] {
            sec
        });
        environment.addSecurities(new ISecurity[] {
            sec
        });
        assertEquals(1, environment.securitiesMap.size());
        assertEquals(1, environment.identifiersMap.size());
        assertEquals(1, subscriptions.size());
    }

    public void testAddSameIdentifierOnlyOnce() throws Exception {
        SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
        FeedIdentifier id = new FeedIdentifier("ID", null);
        Security sec1 = new Security("Sample 1", id);
        Security sec2 = new Security("Sample 2", id);
        environment.addSecurities(new ISecurity[] {
            sec1
        });
        environment.addSecurities(new ISecurity[] {
            sec2
        });
        assertEquals(2, environment.securitiesMap.size());
        assertEquals(1, environment.identifiersMap.size());
        assertEquals(1, subscriptions.size());
    }

    public void testRemoveSecurity() throws Exception {
        SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
        Security sec = new Security("Sample", new FeedIdentifier("ID", null));
        environment.addSecurities(new ISecurity[] {
            sec
        });
        environment.removeSecurities(new ISecurity[] {
            sec
        });
        assertEquals(0, environment.securitiesMap.size());
        assertEquals(0, environment.identifiersMap.size());
        assertEquals(0, subscriptions.size());
    }

    public void testRemoveSameSecurityOnlyOnce() throws Exception {
        SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
        Security sec = new Security("Sample", new FeedIdentifier("ID", null));
        environment.addSecurities(new ISecurity[] {
            sec
        });
        environment.removeSecurities(new ISecurity[] {
            sec
        });
        assertEquals(0, environment.securitiesMap.size());
        assertEquals(0, environment.identifiersMap.size());
        assertEquals(0, subscriptions.size());
    }

    public void testRemoveIdentifierWhenAllSecuritiesAreRemoved() throws Exception {
        SingleFeedPricingEnvironment environment = new SingleFeedPricingEnvironment(connector);
        FeedIdentifier id = new FeedIdentifier("ID", null);
        Security sec1 = new Security("Sample 1", id);
        Security sec2 = new Security("Sample 2", id);
        environment.addSecurities(new ISecurity[] {
            sec1
        });
        environment.addSecurities(new ISecurity[] {
            sec2
        });
        environment.removeSecurities(new ISecurity[] {
            sec1
        });
        assertEquals(1, environment.securitiesMap.size());
        assertEquals(1, environment.identifiersMap.size());
        assertEquals(1, subscriptions.size());
        environment.removeSecurities(new ISecurity[] {
            sec2
        });
        assertEquals(0, environment.securitiesMap.size());
        assertEquals(0, environment.identifiersMap.size());
        assertEquals(0, subscriptions.size());
    }
}
