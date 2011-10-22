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

package org.eclipsetrader.core.internal.trading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.AbstractAlert;
import org.eclipsetrader.core.trading.AlertEvent;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertListener;

public class AlertServiceTest extends TestCase {

    public void testUnknownSecurityHasTriggeredAlerts() throws Exception {
        AlertService service = new AlertService();
        assertFalse(service.hasTriggeredAlerts(new Security("Test", null)));
    }

    public void testHasTriggeredAlertsWithEmptyList() throws Exception {
        ISecurity security = new Security("Test", null);

        AlertService service = new AlertService();
        service.triggeredMap.put(security, new ArrayList<IAlert>());

        assertFalse(service.hasTriggeredAlerts(security));
    }

    public void testFireSingleAlertEvent() throws Exception {
        IMarketService marketService = EasyMock.createNiceMock(IMarketService.class);
        EasyMock.expect(marketService.getMarkets()).andStubReturn(new IMarket[0]);
        EasyMock.replay(marketService);

        MarketPricingEnvironment pricingEnvironment = new MarketPricingEnvironment(marketService);

        ISecurity security = new Security("Test", null);
        IAlert alert = new AbstractAlert() {

            @Override
            public boolean isTriggered() {
                return true;
            }
        };

        PricingEvent pricingEvent = new PricingEvent(security, new PricingDelta[] {
            new PricingDelta(null, new Trade(10.0)),
            new PricingDelta(null, new Trade(10.1))
        });

        final List<AlertEvent> events = new ArrayList<AlertEvent>();

        AlertService service = new AlertService();
        service.pricingEnvironment = pricingEnvironment;
        service.map.put(security, Arrays.asList(new IAlert[] {
            alert
        }));
        service.addAlertListener(new IAlertListener() {

            @Override
            public void alertTriggered(AlertEvent event) {
                events.add(event);
            }
        });

        service.doPricingUpdate(pricingEvent);

        assertEquals(1, events.size());
    }
}
