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

package org.eclipsetrader.ui.internal.markets;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketTime;
import org.eclipsetrader.core.markets.IMarketDay;
import org.eclipsetrader.ui.internal.UIActivator;

public class MarketLabelProviderTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        new UIActivator() {

            @Override
            protected void initializeImageRegistry(ImageRegistry reg) {
            }
        };
    }

    public void testCloseInLessThanOneMinute() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.MINUTE, -10);
        open.set(Calendar.SECOND, 0);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.MINUTE, 1);
        close.set(Calendar.SECOND, 0);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Closes in less than 1 minute", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testCloseInMinutes() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.MINUTE, -10);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.MINUTE, 30);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Closes in 30 minute(s)", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testCloseInHours() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.MINUTE, -10);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.HOUR, 1);
        close.add(Calendar.MINUTE, 30);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Closes in 1 hour(s) and 30 minute(s)", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testOpenInLessThanOneMinute() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.MINUTE, 1);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.HOUR, 5);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Opens in less than 1 minute", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testOpenInMinutes() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.MINUTE, 10);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.HOUR, 5);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Opens in 10 minute(s)", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testOpenInHours() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.HOUR, 1);
        open.add(Calendar.MINUTE, 10);
        Calendar close = Calendar.getInstance();
        close.add(Calendar.HOUR, 5);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Opens in 1 hour(s) and 10 minute(s)", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testLabelAtOpen() throws Exception {
        Calendar open = Calendar.getInstance();
        Calendar close = Calendar.getInstance();
        close.add(Calendar.HOUR_OF_DAY, 6);
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        assertEquals("Closes in 6 hour(s) and 0 minute(s)", new MarketLabelProvider().getColumnText(market, 2));
    }

    public void testLabelAtClose() throws Exception {
        Calendar open = Calendar.getInstance();
        open.add(Calendar.HOUR_OF_DAY, -6);
        Calendar close = Calendar.getInstance();
        Market market = new Market("Market", Arrays.asList(new MarketTime[] {
            new MarketTime(open.getTime(), close.getTime())
        }));
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        IMarketDay day = market.getNextDay();
        assertEquals(NLS.bind(Messages.MarketLabelProvider_OpenDate, new Object[] {
                dateFormat.format(day.getOpenTime()),
                timeFormat.format(day.getOpenTime()),
        }), new MarketLabelProvider().getColumnText(market, 2));
    }
}
