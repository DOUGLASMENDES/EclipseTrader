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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.internal.markets.MarketTime;
import org.eclipsetrader.core.internal.markets.MarketTimeExclude;
import org.eclipsetrader.ui.internal.markets.MarketTimeElement.MarketTimeExcludeElement;

public class MarketTimeElementTest extends TestCase {

    public void testCreateFromMarketTime() throws Exception {
        MarketTimeElement element = new MarketTimeElement(new MarketTime(getTime(9, 0), getTime(16, 0), "Description"));
        assertEquals(getTime(9, 0), element.getOpenTime());
        assertEquals(getTime(16, 0), element.getCloseTime());
        assertEquals("Description", element.getDescription());
    }

    public void testCreateMarketTime() throws Exception {
        MarketTimeElement element = new MarketTimeElement(getTime(9, 0), getTime(16, 0));
        element.setDescription("Description");
        MarketTime marketTime = element.getMarketTime();
        assertEquals(getTime(9, 0), marketTime.getOpenTime());
        assertEquals(getTime(16, 0), marketTime.getCloseTime());
        assertEquals("Description", marketTime.getDescription());
    }

    public void testCreateExclusionListFromMarketTime() throws Exception {
        MarketTime marketTime = new MarketTime(getTime(9, 0), getTime(16, 0), "Description");
        marketTime.setExclude(new MarketTimeExclude[] {
            new MarketTimeExclude(getDate(2008, Calendar.APRIL, 24)),
        });
        MarketTimeElement element = new MarketTimeElement(marketTime);
        assertEquals(1, element.getExclude().size());
    }

    public void testCreateMarketTimeWithExclusionList() throws Exception {
        MarketTimeElement element = new MarketTimeElement(getTime(9, 0), getTime(16, 0));
        element.getExclude().add(new MarketTimeExcludeElement(getDate(2008, Calendar.APRIL, 24), getDate(2008, Calendar.APRIL, 24)));
        MarketTime marketTime = element.getMarketTime();
        assertEquals(1, marketTime.getExclude().length);
        assertEquals(getDate(2008, Calendar.APRIL, 24), marketTime.getExclude()[0].getFromDate());
        assertEquals(getDate(2008, Calendar.APRIL, 24), marketTime.getExclude()[0].getToDate());
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getDate(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
