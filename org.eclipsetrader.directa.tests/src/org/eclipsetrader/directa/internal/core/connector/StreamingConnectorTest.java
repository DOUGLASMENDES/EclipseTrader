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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedSubscription;
import org.eclipsetrader.core.feed.ISubscriptionListener;
import org.eclipsetrader.core.feed.Price;
import org.eclipsetrader.core.feed.QuoteDelta;
import org.eclipsetrader.core.feed.QuoteEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.directa.internal.core.messages.AstaApertura;
import org.eclipsetrader.directa.internal.core.messages.AstaChiusura;
import org.eclipsetrader.directa.internal.core.messages.HeaderRecord;
import org.eclipsetrader.directa.internal.core.messages.Util;
import org.eclipsetrader.directa.internal.core.repository.IdentifierType;
import org.eclipsetrader.directa.internal.core.repository.IdentifiersList;
import org.eclipsetrader.directa.internal.core.repository.PriceDataType;

public class StreamingConnectorTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        new IdentifiersList();
    }

    private void putFloat(float value, byte[] arr, int i) {
        int k = Float.floatToIntBits(value);

        int j = 0;
        for (int l = 0; l < 4; l++) {
            arr[i + l] = (byte) ((k >> j) & 0xFF);
            j += 8;
        }
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

    public void testProcessSnapshotData() throws Exception {
        Hashtable<String, Map<String, String>> hashTable = new Hashtable<String, Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("[L=]", "4.1400");
        map.put("[LC=]", "4.1400");
        map.put("[TLT]", "08:32:12");
        map.put("[DATA_ULT]", "20111104");
        map.put("[CV]", "555236");
        map.put("[LO]", "4.1100");
        map.put("[HI]", "4.1840");
        map.put("[OP1]", "4.4360");
        map.put("[LIE]", "4.3800");
        map.put("[BS1]", "0");
        map.put("[BP1]", "0.0000");
        map.put("[AS1]", "0");
        map.put("[AP1]", "0.0000");
        hashTable.put("F", map);

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));

        connector.processSnapshotData(new String[] {
            "F"
        }, hashTable);

        IdentifierType identifierType = subscription.getIdentifierType();
        PriceDataType priceData = identifierType.getPriceData();

        assertNull(subscription.getPrice());
        assertEquals(new Trade(priceData.getTime(), 4.1400, null, 555236L), subscription.getTrade());
    }

    public void testProcessPreOpenPriceData() throws Exception {
        Hashtable<String, Map<String, String>> hashTable = new Hashtable<String, Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("[L=]", "4.4020");
        map.put("[LC=]", "0.0000");
        map.put("[TLT]", "08:32:12");
        map.put("[DATA_ULT]", "0");
        map.put("[CV]", "0");
        map.put("[LO]", "0.0000");
        map.put("[HI]", "0.0000");
        map.put("[OP1]", "0.0000");
        map.put("[LIE]", "0.0000");
        map.put("[BS1]", "0");
        map.put("[BP1]", "0.0000");
        map.put("[AS1]", "0");
        map.put("[AP1]", "0.0000");
        hashTable.put("F", map);

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));

        connector.processSnapshotData(new String[] {
            "F"
        }, hashTable);

        IdentifierType identifierType = subscription.getIdentifierType();
        PriceDataType priceData = identifierType.getPriceData();

        assertEquals(new Price(priceData.getTime(), 4.4020), subscription.getPrice());
        assertEquals(new Trade(null, null, null, null), subscription.getTrade());
    }

    public void testEventsGeneratedWithAstaApertura() throws Exception {
        byte[] arr = new byte[20];
        putFloat(4.1200f, arr, 0);
        AstaApertura msg = new AstaApertura(arr, 0, 0);
        msg.head = new HeaderRecord();
        msg.head.key = "F";

        final List<QuoteDelta> events = new ArrayList<QuoteDelta>();

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                events.addAll(Arrays.asList(event.getDelta()));
            }
        });

        connector.processMessage(msg);
        subscription.fireNotification();

        assertEquals(1, events.size());

        assertEquals(new Price(new Date(msg.ora_aper), (double) Util.getFloat(arr, 0)), events.get(0).getNewValue());
    }

    public void testEventsGeneratedWithAstaChiusura() throws Exception {
        byte[] arr = new byte[8];
        putFloat(4.1500f, arr, 0);
        AstaChiusura msg = new AstaChiusura(arr, 0, 0);
        msg.head = new HeaderRecord();
        msg.head.key = "F";

        final List<QuoteDelta> events = new ArrayList<QuoteDelta>();

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                events.addAll(Arrays.asList(event.getDelta()));
            }
        });

        connector.processMessage(msg);
        subscription.fireNotification();

        assertEquals(1, events.size());

        assertEquals(new Price(new Date(msg.ora_chiu), (double) Util.getFloat(arr, 0)), events.get(0).getNewValue());
    }

    public void testGenerateBarOpenEventAtMarketOpen() throws Exception {
        byte[] arr = new byte[32];
        putFloat(4.1200f, arr, 0);
        putFloat(0.0f, arr, 12);
        putFloat(0.0f, arr, 20);
        putFloat(0.0f, arr, 24);
        org.eclipsetrader.directa.internal.core.messages.Price msg = new org.eclipsetrader.directa.internal.core.messages.Price(arr, 0, 0);
        msg.head = new HeaderRecord();
        msg.head.key = "F";

        final List<QuoteDelta> events = new ArrayList<QuoteDelta>();

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                events.addAll(Arrays.asList(event.getDelta()));
            }
        });

        connector.processMessage(msg);
        subscription.fireNotification();

        assertEquals(2, events.size());

        assertEquals(new BarOpen(new Date(msg.ora_ult), TimeSpan.days(1), (double) Util.getFloat(arr, 0)), events.get(1).getNewValue());
    }

    public void testDontGenerateBarOpenEventIfAlreadyOpen() throws Exception {
        byte[] arr = new byte[32];
        putFloat(4.1200f, arr, 0);
        putFloat(0.0f, arr, 12);
        putFloat(0.0f, arr, 20);
        putFloat(0.0f, arr, 24);
        org.eclipsetrader.directa.internal.core.messages.Price msg = new org.eclipsetrader.directa.internal.core.messages.Price(arr, 0, 0);
        msg.head = new HeaderRecord();
        msg.head.key = "F";

        final List<QuoteDelta> events = new ArrayList<QuoteDelta>();

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                events.addAll(Arrays.asList(event.getDelta()));
            }
        });

        PriceDataType priceData = subscription.getIdentifierType().getPriceData();
        priceData.setOpen(4.11);

        connector.processMessage(msg);
        subscription.fireNotification();

        assertEquals(1, events.size());

        assertEquals(new Trade(new Date(msg.ora_ult), (double) Util.getFloat(arr, 0), 0L, 0L), events.get(0).getNewValue());
    }

    public void testGenerateBarCloseEventAtMarketClose() throws Exception {
        byte[] arr1 = new byte[32];
        putFloat(4.1200f, arr1, 0);
        putFloat(0.0f, arr1, 12);
        putFloat(4.08f, arr1, 20);
        putFloat(4.20f, arr1, 24);
        org.eclipsetrader.directa.internal.core.messages.Price msg1 = new org.eclipsetrader.directa.internal.core.messages.Price(arr1, 0, 0);
        msg1.head = new HeaderRecord();
        msg1.head.key = "F";

        byte[] arr2 = new byte[8];
        putFloat(4.1500f, arr2, 0);
        AstaChiusura msg2 = new AstaChiusura(arr2, 0, 0);
        msg2.head = new HeaderRecord();
        msg2.head.key = "F";

        final List<QuoteDelta> events = new ArrayList<QuoteDelta>();

        StreamingConnector connector = new StreamingConnector();
        FeedSubscription subscription = (FeedSubscription) connector.subscribe(new FeedIdentifier("F", null));
        subscription.addSubscriptionListener(new ISubscriptionListener() {

            @Override
            public void quoteUpdate(QuoteEvent event) {
                events.addAll(Arrays.asList(event.getDelta()));
            }
        });

        PriceDataType priceData = subscription.getIdentifierType().getPriceData();
        priceData.setTime(new Date());
        priceData.setLast(4.15);
        priceData.setOpen(4.12);
        priceData.setHigh(4.20);
        priceData.setLow(4.08);
        priceData.setVolume(100000L);

        connector.processMessage(msg2);
        subscription.fireNotification();
        assertEquals(2, events.size());

        Calendar c = Calendar.getInstance();
        c.setTime(priceData.getTime());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        assertEquals(new Bar(c.getTime(), TimeSpan.days(1), 4.12, 4.20, 4.08, 4.15, 100000L), events.get(0).getNewValue());
    }
}
