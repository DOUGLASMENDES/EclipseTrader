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

package org.eclipsetrader.repository.hibernate.internal.stores;

import java.util.Date;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.HistoryData;
import org.hibernate.Session;

public class IntradayHistoryStoreTest extends TestCase {

    HibernateRepository repository;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        repository = EasyMock.createNiceMock(HibernateRepository.class);
        Session session = EasyMock.createNiceMock(Session.class);
        org.easymock.EasyMock.expect(repository.getSession()).andStubReturn(session);
        EasyMock.replay(repository, session);
    }

    public void testFillHistory() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(null, repository);
        store.data.add(new HistoryData(null, new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 5L), TimeSpan.minutes(1)));
        store.data.add(new HistoryData(null, new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 4L), TimeSpan.minutes(1)));
        store.data.add(new HistoryData(null, new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 9L), TimeSpan.minutes(2)));
        store.fillHistory();
        assertEquals(2, store.bars.get(TimeSpan.minutes(1)).length);
        assertEquals(1, store.bars.get(TimeSpan.minutes(2)).length);
    }

    public void testPutHistoryData() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(null, repository);
        StoreProperties properties = new StoreProperties();
        properties.setProperty(TimeSpan.minutes(1).toString(), new IOHLC[] {
            new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 5L)
        });
        store.putProperties(properties, null);
        assertEquals(1, store.data.size());
    }

    public void testPutHistoryDataWithDifferentTimeSpan() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(null, repository);
        StoreProperties properties = new StoreProperties();
        properties.setProperty(TimeSpan.minutes(1).toString(), new IOHLC[] {
                new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 5L),
                new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 4L)
        });
        properties.setProperty(TimeSpan.minutes(2).toString(), new IOHLC[] {
            new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 9L)
        });
        store.putProperties(properties, null);
        assertEquals(3, store.data.size());
        assertEquals(2, store.bars.get(TimeSpan.minutes(1)).length);
        assertEquals(1, store.bars.get(TimeSpan.minutes(2)).length);
    }

    public void testDontReplaceExistingHistoryData() throws Exception {
        IntradayHistoryStore store = new IntradayHistoryStore(null, repository);
        StoreProperties properties = new StoreProperties();
        OHLC ohlc1 = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 5L);
        OHLC ohlc2 = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 15L);
        properties.setProperty(TimeSpan.minutes(1).toString(), new IOHLC[] {
            ohlc1
        });
        store.putProperties(properties, null);
        assertEquals(1, store.data.size());
        HistoryData data = store.data.get(0);

        properties.setProperty(TimeSpan.minutes(1).toString(), new IOHLC[] {
                ohlc1, ohlc2
        });
        store.putProperties(properties, null);
        assertEquals(2, store.data.size());
        assertSame(data, store.data.get(0));
    }
}
