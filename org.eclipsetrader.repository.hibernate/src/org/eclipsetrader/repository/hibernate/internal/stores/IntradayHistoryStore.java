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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.repository.hibernate.HibernateRepository;
import org.eclipsetrader.repository.hibernate.internal.types.HistoryData;
import org.hibernate.Session;

@Entity
@DiscriminatorValue("day")
public class IntradayHistoryStore extends HistoryStore {

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Transient
    Map<TimeSpan, IOHLC[]> bars = new HashMap<TimeSpan, IOHLC[]>();

    public IntradayHistoryStore() {
    }

    public IntradayHistoryStore(ISecurity security, HibernateRepository repository) {
        super(security, repository);
    }

    public IntradayHistoryStore(ISecurity security, Date date, HibernateRepository repository) {
        super(security, repository);
        this.date = date;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStoreProperties fetchProperties(IProgressMonitor monitor) {
        StoreProperties properties = new StoreProperties() {

            @Override
            public String[] getPropertyNames() {
                fillHistory();
                Set<String> s = new HashSet<String>(Arrays.asList(super.getPropertyNames()));
                for (TimeSpan timeSpan : bars.keySet()) {
                    s.add(timeSpan.toString());
                }
                return s.toArray(new String[s.size()]);
            }

            @Override
            public Object getProperty(String name) {
                Object o = super.getProperty(name);
                if (o != null) {
                    return o;
                }
                TimeSpan timeSpan = TimeSpan.fromString(name);
                if (timeSpan != null) {
                    fillHistory();
                    return bars.get(timeSpan);
                }
                return null;
            }
        };

        properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

        properties.setProperty(IPropertyConstants.SECURITY, security);
        properties.setProperty(IPropertyConstants.BARS_DATE, date);

        return properties;
    }

    protected void fillHistory() {
        if (bars.size() == 0) {
            Map<TimeSpan, List<HistoryData>> map = new HashMap<TimeSpan, List<HistoryData>>();
            for (HistoryData ohlc : data) {
                List<HistoryData> h = map.get(ohlc.getTimeSpan());
                if (h == null) {
                    h = new ArrayList<HistoryData>(2048);
                    map.put(ohlc.getTimeSpan(), h);
                }
                h.add(ohlc);
            }
            for (TimeSpan timeSpan : map.keySet()) {
                List<HistoryData> h = map.get(timeSpan);
                this.bars.put(timeSpan, h.toArray(new IOHLC[h.size()]));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
        Session session = repository.getSession();

        this.security = (ISecurity) properties.getProperty(IPropertyConstants.SECURITY);
        this.date = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);

        List<HistoryData> h = new ArrayList<HistoryData>(2048);
        for (String name : properties.getPropertyNames()) {
            TimeSpan timeSpan = TimeSpan.fromString(name);
            if (timeSpan != null) {
                IOHLC[] ohlc = (IOHLC[]) properties.getProperty(name);
                for (int i = 0; i < ohlc.length; i++) {
                    h.add(new HistoryData(this, ohlc[i], timeSpan));
                }
            }
        }
        for (HistoryData ohlc : h) {
            if (!this.data.contains(ohlc)) {
                this.data.add(ohlc);
            }
        }
        for (Iterator<HistoryData> iter = this.data.iterator(); iter.hasNext();) {
            if (!h.contains(iter.next())) {
                iter.remove();
            }
        }

        this.bars.clear();
        fillHistory();

        session.save(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore#createChild()
     */
    @Override
    public IStore createChild() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore#toURI()
     */
    @Override
    public URI toURI() {
        try {
            return new URI(repository.getSchema(), HibernateRepository.URI_SECURITY_HISTORY_PART + "/" + new SimpleDateFormat("yyyyMMdd").format(date), id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
