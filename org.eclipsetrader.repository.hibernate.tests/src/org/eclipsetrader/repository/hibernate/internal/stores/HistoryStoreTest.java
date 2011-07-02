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
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.HistoryData;
import org.hibernate.Session;

public class HistoryStoreTest extends TestCase {

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

    public void testDontReplaceExistingHistoryData() throws Exception {
        HistoryStore store = new HistoryStore(null, repository);
        StoreProperties properties = new StoreProperties();
        OHLC ohlc1 = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 5L);
        OHLC ohlc2 = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 15L);
        properties.setProperty(IPropertyConstants.BARS, new IOHLC[] {
            ohlc1
        });
        store.putProperties(properties, null);
        assertEquals(1, store.data.size());
        HistoryData data = store.data.get(0);

        properties.setProperty(IPropertyConstants.BARS, new IOHLC[] {
                ohlc1, ohlc2
        });
        store.putProperties(properties, null);
        assertEquals(2, store.data.size());
        assertSame(data, store.data.get(0));
    }
}
