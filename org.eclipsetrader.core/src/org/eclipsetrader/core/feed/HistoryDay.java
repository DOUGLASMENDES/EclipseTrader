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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class HistoryDay implements IHistory {

    private ISecurity security;
    private IOHLC[] bars = new IOHLC[0];
    private TimeSpan timeSpan;

    private IOHLC highest;
    private IOHLC lowest;

    private Map<Date, StoreObject> storeObjects = new TreeMap<Date, StoreObject>();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private class StoreObject implements IStoreObject {

        private IStore store;
        private IStoreProperties storeProperties;

        public StoreObject(IStore store, IStoreProperties storeProperties) {
            this.store = store;
            this.storeProperties = storeProperties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
         */
        @Override
        public IStore getStore() {
            return store;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
         */
        @Override
        public IStoreProperties getStoreProperties() {
            return storeProperties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
         */
        @Override
        public void setStore(IStore store) {
            this.store = store;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
         */
        @Override
        public void setStoreProperties(IStoreProperties storeProperties) {
            this.storeProperties = storeProperties;
        }
    }

    protected HistoryDay() {
    }

    public HistoryDay(ISecurity security, TimeSpan timeSpan) {
        this.security = security;
        this.timeSpan = timeSpan;
    }

    public HistoryDay(ISecurity security, TimeSpan timeSpan, IStore[] store, IStoreProperties[] storeProperties) {
        this.security = security;
        this.timeSpan = timeSpan;
        setStoreProperties(store, storeProperties);
    }

    public HistoryDay(ISecurity security, TimeSpan timeSpan, IOHLC[] bars) {
        this.security = security;
        this.timeSpan = timeSpan;
        this.bars = bars;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getAdjustedOHLC()
     */
    @Override
    public IOHLC[] getAdjustedOHLC() {
        return bars;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getFirst()
     */
    @Override
    public IOHLC getFirst() {
        return bars != null && bars.length != 0 ? bars[0] : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getHighest()
     */
    @Override
    public IOHLC getHighest() {
        return highest;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getLast()
     */
    @Override
    public IOHLC getLast() {
        return bars != null && bars.length != 0 ? bars[bars.length - 1] : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getLowest()
     */
    @Override
    public IOHLC getLowest() {
        return lowest;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getOHLC()
     */
    @Override
    public IOHLC[] getOHLC() {
        return bars;
    }

    public void setOHLC(IOHLC[] bars) {
        if (Arrays.equals(this.bars, bars)) {
            return;
        }

        IOHLC[] oldBars = this.bars;

        List<IOHLC> l = new ArrayList<IOHLC>(Arrays.asList(bars));
        Collections.sort(l, new Comparator<IOHLC>() {

            @Override
            public int compare(IOHLC o1, IOHLC o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        this.bars = l.toArray(new IOHLC[l.size()]);

        updateStoreObjects();
        updateRange();

        propertyChangeSupport.firePropertyChange(IPropertyConstants.BARS, oldBars, this.bars);
    }

    protected IStoreObject[] updateStoreObjects() {
        Set<StoreObject> updatedStoreObjects = new HashSet<StoreObject>();

        IRepository repository = null;
        if (storeObjects.size() != 0) {
            IStore store = storeObjects.values().iterator().next().getStore();
            if (store != null) {
                repository = store.getRepository();
            }
        }

        if (bars.length != 0) {
            Calendar c = Calendar.getInstance();
            int dayOfYear = -1;
            Date date = null;

            List<IOHLC> list = new ArrayList<IOHLC>(2048);
            for (IOHLC d : bars) {
                c.setTime(d.getDate());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);

                if (c.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                    if (list.size() != 0 && date != null) {
                        StoreObject object = storeObjects.get(date);
                        if (object == null) {
                            IStoreProperties properties = new StoreProperties();
                            properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
                            properties.setProperty(IPropertyConstants.SECURITY, security);
                            properties.setProperty(IPropertyConstants.BARS_DATE, date);
                            properties.setProperty(timeSpan.toString(), list.toArray(new IOHLC[list.size()]));
                            object = new StoreObject(repository != null ? repository.createObject() : null, properties);
                            storeObjects.put(date, object);
                            updatedStoreObjects.add(object);
                        }
                        else {
                            IStoreProperties properties = object.getStoreProperties();
                            properties.setProperty(timeSpan.toString(), list.toArray(new IOHLC[list.size()]));
                            object.setStoreProperties(properties);
                            updatedStoreObjects.add(object);
                        }

                        list = new ArrayList<IOHLC>(2048);
                    }
                    dayOfYear = c.get(Calendar.DAY_OF_YEAR);
                }
                list.add(d);
                date = c.getTime();
            }
            if (list.size() != 0 && date != null) {
                StoreObject object = storeObjects.get(date);
                if (object == null) {
                    IStoreProperties properties = new StoreProperties();
                    properties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
                    properties.setProperty(IPropertyConstants.SECURITY, security);
                    properties.setProperty(IPropertyConstants.BARS_DATE, date);
                    properties.setProperty(timeSpan.toString(), list.toArray(new IOHLC[list.size()]));
                    object = new StoreObject(repository != null ? repository.createObject() : null, properties);
                    storeObjects.put(date, object);
                    updatedStoreObjects.add(object);
                }
                else {
                    IStoreProperties properties = object.getStoreProperties();
                    properties.setProperty(timeSpan.toString(), list.toArray(new IOHLC[list.size()]));
                    object.setStoreProperties(properties);
                    updatedStoreObjects.add(object);
                }
            }
        }

        return updatedStoreObjects.toArray(new IStoreObject[updatedStoreObjects.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    protected void setSecurity(ISecurity security) {
        this.security = security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSplits()
     */
    @Override
    public ISplit[] getSplits() {
        return null;
    }

    public void setSplits(ISplit[] splits) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date)
     */
    @Override
    public IHistory getSubset(Date first, Date last) {
        List<IOHLC> l = new ArrayList<IOHLC>();
        for (IOHLC b : bars) {
            if ((first == null || !b.getDate().before(first)) && (last == null || !b.getDate().after(last))) {
                l.add(b);
            }
        }
        return new HistoryDay(security, timeSpan, l.toArray(new IOHLC[l.size()]));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public IHistory getSubset(Date first, Date last, TimeSpan aggregation) {
        if (this.timeSpan != null && this.timeSpan.equals(aggregation)) {
            return getSubset(first, last);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getDay(java.util.Date)
     */
    @Override
    public IHistory[] getDay(Date date) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getTimeSpan()
     */
    @Override
    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        if (adapter.isAssignableFrom(PropertyChangeSupport.class)) {
            return propertyChangeSupport;
        }

        if (adapter.isAssignableFrom(IStoreObject[].class)) {
            Collection<StoreObject> c = storeObjects.values();
            return c.toArray(new IStoreObject[c.size()]);
        }

        return null;
    }

    public void setStoreProperties(IStore[] store, IStoreProperties[] storeProperties) {
        Assert.isTrue(store.length == storeProperties.length, "IStore and IStoreProperties arrays must be of same size!");

        storeObjects.clear();

        if (storeProperties.length != 0) {
            this.security = (ISecurity) storeProperties[0].getProperty(IPropertyConstants.SECURITY);
        }

        List<IOHLC> l1 = new ArrayList<IOHLC>(2048);

        for (int i = 0; i < store.length; i++) {
            StoreObject object = new StoreObject(store[i], storeProperties[i]);
            Date date = (Date) storeProperties[i].getProperty(IPropertyConstants.BARS_DATE);
            storeObjects.put(date, object);

            IOHLC[] bars = (IOHLC[]) storeProperties[i].getProperty(timeSpan.toString());
            if (bars == null) {
                IOHLC[] minuteBars = getLowestTimespanBars(storeProperties[i]);
                if (minuteBars != null) {
                    Date startDate = null, endDate = null;
                    Double open = null, high = null, low = null, close = null;
                    Long volume = 0L;
                    Calendar c = Calendar.getInstance();

                    List<IOHLC> l = new ArrayList<IOHLC>();
                    for (IOHLC currentBar : minuteBars) {
                        if (startDate != null && !currentBar.getDate().before(endDate)) {
                            l.add(new OHLC(startDate, open, high, low, close, volume));
                            startDate = null;
                        }

                        if (startDate == null) {
                            c.setTime(currentBar.getDate());
                            startDate = c.getTime();
                            c.add(Calendar.MINUTE, timeSpan.getLength());
                            endDate = c.getTime();
                            open = high = low = close = null;
                            volume = 0L;
                        }

                        if (open == null) {
                            open = currentBar.getOpen();
                        }
                        high = high != null ? Math.max(high, currentBar.getHigh()) : currentBar.getHigh();
                        low = low != null ? Math.min(low, currentBar.getLow()) : currentBar.getLow();
                        close = currentBar.getClose();
                        volume += currentBar.getVolume();
                    }

                    if (startDate != null) {
                        l.add(new OHLC(startDate, open, high, low, close, volume));
                    }

                    bars = l.toArray(new IOHLC[l.size()]);
                }
            }
            if (bars != null) {
                l1.addAll(Arrays.asList(bars));
            }
        }

        Collections.sort(l1, new Comparator<IOHLC>() {

            @Override
            public int compare(IOHLC o1, IOHLC o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        IOHLC[] newBars = l1.toArray(new IOHLC[l1.size()]);
        if (Arrays.equals(this.bars, newBars)) {
            return;
        }

        IOHLC[] oldBars = this.bars;
        this.bars = newBars;

        updateRange();

        propertyChangeSupport.firePropertyChange(IPropertyConstants.BARS, oldBars, this.bars);
    }

    IOHLC[] getLowestTimespanBars(IStoreProperties storeProperties) {
        TimeSpan lowestTimeSpan = null;

        for (String name : storeProperties.getPropertyNames()) {
            TimeSpan propertyTimeSpan = TimeSpan.fromString(name);
            if (propertyTimeSpan != null && propertyTimeSpan.lowerThan(timeSpan)) {
                if (lowestTimeSpan == null || propertyTimeSpan.lowerThan(lowestTimeSpan)) {
                    lowestTimeSpan = propertyTimeSpan;
                }
            }
        }

        if (lowestTimeSpan != null) {
            return (IOHLC[]) storeProperties.getProperty(lowestTimeSpan.toString());
        }

        return null;
    }

    protected void updateRange() {
        highest = null;
        lowest = null;
        for (IOHLC b : bars) {
            if (highest == null || b.getHigh() > highest.getHigh()) {
                highest = b;
            }
            if (lowest == null || b.getLow() < lowest.getLow()) {
                lowest = b;
            }
        }
    }
}
