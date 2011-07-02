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

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.Security;

public class PricingEnvironmentTest extends TestCase {

    private Security security = new Security("Test", null);

    public void testSetTradeAddsStatus() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        assertNull(env.getStatus(security));
        ITrade value = new Trade(null, 100.0, null, null);
        env.setTrade(security, value);
        assertSame(value, env.getStatus(security).trade);
    }

    public void testSetNullTrade() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setTrade(security, new Trade(null, 100.0, null, null));
        assertNotNull(env.getStatus(security).trade);
        env.setTrade(security, null);
        assertNull(env.getStatus(security).trade);
    }

    public void testSetNewTrade() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setTrade(security, new Trade(null, 90.0, null, null));
        ITrade value = new Trade(null, 100.0, null, null);
        env.setTrade(security, value);
        assertSame(value, env.getStatus(security).trade);
    }

    public void testSetQuoteAddsStatus() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        assertNull(env.getStatus(security));
        IQuote quote = new Quote(100.0, 101.0, null, null);
        env.setQuote(security, quote);
        assertSame(quote, env.getStatus(security).quote);
    }

    public void testSetNullQuote() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setQuote(security, new Quote(100.0, 101.0, null, null));
        assertNotNull(env.getStatus(security));
        env.setQuote(security, null);
        assertNull(env.getStatus(security).quote);
    }

    public void testSetNewQuote() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setQuote(security, new Quote(90.0, 91.0, null, null));
        IQuote quote = new Quote(100.0, 101.0, null, null);
        env.setQuote(security, quote);
        assertSame(quote, env.getStatus(security).quote);
    }

    public void testSetTodayOHLAddsStatus() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        assertNull(env.getStatus(security));
        ITodayOHL value = new TodayOHL(100.0, 110.0, 90.0);
        env.setTodayOHL(security, value);
        assertSame(value, env.getStatus(security).todayOHL);
    }

    public void testSetNullTodayOHL() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setTodayOHL(security, new TodayOHL(100.0, 110.0, 90.0));
        assertNotNull(env.getStatus(security).todayOHL);
        env.setTodayOHL(security, null);
        assertNull(env.getStatus(security).todayOHL);
    }

    public void testSetNewTodayOHL() throws Exception {
        PricingEnvironment env = new PricingEnvironment();
        env.setTodayOHL(security, new TodayOHL(90.0, 110.0, 90.0));
        ITodayOHL value = new TodayOHL(100.0, 110.0, 90.0);
        env.setTodayOHL(security, value);
        assertSame(value, env.getStatus(security).todayOHL);
    }
}
