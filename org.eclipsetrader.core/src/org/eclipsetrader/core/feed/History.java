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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class History implements IHistory, IStoreObject {

    private ISecurity security;
    private IOHLC[] bars = new IOHLC[0];
    private ISplit[] splits = new ISplit[0];
    private TimeSpan timeSpan;

    private IOHLC highest;
    private IOHLC lowest;

    private IStore store;
    private IStoreProperties storeProperties;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private class Key {

        private Date first;
        private Date last;
        private TimeSpan timeSpan;

        public Key(Date first, Date last, TimeSpan timeSpan) {
            this.first = first;
            this.last = last;
            this.timeSpan = timeSpan;
        }

        public Date getFirst() {
            return first;
        }

        public Date getLast() {
            return last;
        }

        public boolean isInRange(Date date) {
            if (first != null && date.before(first)) {
                return false;
            }
            if (last != null && date.after(last)) {
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int hash = 13 * timeSpan.hashCode();
            if (first != null) {
                hash += 7 * first.hashCode();
            }
            if (last != null) {
                hash += 11 * last.hashCode();
            }
            return hash;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            return hashCode() == obj.hashCode();
        }
    }

    private Map<Key, WeakReference<HistoryDay>> historyMap = new HashMap<Key, WeakReference<HistoryDay>>();

    protected History() {
    }

    public History(ISecurity security, IOHLC[] bars) {
        this(security, bars, null, TimeSpan.days(1));
    }

    public History(ISecurity security, IOHLC[] bars, TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
        setSecurity(security);
        setOHLC(bars);
    }

    public History(ISecurity security, IOHLC[] bars, ISplit[] splits, TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
        setSecurity(security);
        setOHLC(bars);
        setSplits(splits);
    }

    public History(IStore store, IStoreProperties storeProperties) {
        setStore(store);
        setStoreProperties(storeProperties);
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
     * @see org.eclipsetrader.core.feed.IHistory#getFirst()
     */
    @Override
    public IOHLC getFirst() {
        return bars != null && bars.length != 0 ? bars[0] : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getLast()
     */
    @Override
    public IOHLC getLast() {
        return bars != null && bars.length != 0 ? bars[bars.length - 1] : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getHighest()
     */
    @Override
    public IOHLC getHighest() {
        return highest;
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

        updateRange();
        updateSubsets();

        propertyChangeSupport.firePropertyChange(IPropertyConstants.BARS, oldBars, this.bars);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date)
     */
    @Override
    public IHistory getSubset(Date first, Date last) {
        Key key = new Key(first, last, timeSpan);

        WeakReference<HistoryDay> reference = historyMap.get(key);
        HistoryDay history = reference != null ? reference.get() : null;
        if (history != null) {
            return history;
        }

        IOHLC[] subset = getOHLCSubset(first, last);
        history = new HistoryDay(security, timeSpan, subset);

        historyMap.put(key, new WeakReference<HistoryDay>(history));

        return history;
    }

    private IOHLC[] getOHLCSubset(Date first, Date last) {
        List<IOHLC> l = new ArrayList<IOHLC>();
        for (IOHLC b : bars) {
            if ((first == null || !b.getDate().before(first)) && (last == null || !b.getDate().after(last))) {
                l.add(b);
            }
        }
        return l.toArray(new IOHLC[l.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    @Override
    public IHistory getSubset(Date first, Date last, TimeSpan timeSpan) {
        if (this.timeSpan != null && this.timeSpan.equals(timeSpan)) {
            return getSubset(first, last);
        }

        Key key = new Key(first, last, timeSpan);

        WeakReference<HistoryDay> reference = historyMap.get(key);
        HistoryDay history = reference != null ? reference.get() : null;
        if (history != null) {
            return history;
        }

        Calendar c = Calendar.getInstance();
        if (first != null) {
            c.setTime(first);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            first = c.getTime();
        }
        if (last != null) {
            c.setTime(last);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            last = c.getTime();
        }

        List<IStore> storeList = new ArrayList<IStore>();
        List<IStoreProperties> propertyList = new ArrayList<IStoreProperties>();

        if (store != null) {
            IStore[] childStores = store.fetchChilds(null);
            if (childStores != null) {
                for (IStore childStore : childStores) {
                    IStoreProperties properties = childStore.fetchProperties(null);

                    Date barsDate = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
                    if (first != null && barsDate.before(first) || last != null && barsDate.after(last)) {
                        continue;
                    }

                    storeList.add(childStore);
                    propertyList.add(properties);
                }
            }
        }

        IStoreProperties[] properties = propertyList.toArray(new IStoreProperties[propertyList.size()]);
        history = new HistoryDay(security, timeSpan, storeList.toArray(new IStore[storeList.size()]), properties) {

            @Override
            protected IStoreObject[] updateStoreObjects() {
                IStoreObject[] storeObject = super.updateStoreObjects();

                Set<Entry<Key, WeakReference<HistoryDay>>> set = historyMap.entrySet();
                Entry<Key, WeakReference<HistoryDay>>[] entry = set.toArray(new Entry[set.size()]);

                Set<Key> updatedElements = new HashSet<Key>();
                TimeSpan skipTimeSpan = TimeSpan.days(1);

                for (int ii = 0; ii < storeObject.length; ii++) {
                    Date barsDate = (Date) storeObject[ii].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE);
                    for (int i = 0; i < entry.length; i++) {
                        HistoryDay element = entry[i].getValue().get();
                        Key key = entry[i].getKey();
                        if (element != null && element != this && !element.getTimeSpan().equals(skipTimeSpan)) {
                            if (!entry[i].getKey().isInRange(barsDate)) {
                                continue;
                            }
                            updatedElements.add(key);
                        }
                    }
                }

                for (Key key : updatedElements) {
                    HistoryDay element = historyMap.get(key).get();
                    if (element == null) {
                        continue;
                    }

                    Map<Date, IStore> storeList = new HashMap<Date, IStore>();
                    Map<Date, IStoreProperties> propertyList = new HashMap<Date, IStoreProperties>();

                    IStore[] childStores = store != null ? store.fetchChilds(null) : null;
                    if (childStores != null) {
                        for (IStore childStore : childStores) {
                            IStoreProperties properties = childStore.fetchProperties(null);

                            Date barsDate = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
                            if (!key.isInRange(barsDate)) {
                                continue;
                            }

                            storeList.put(barsDate, childStore);
                            propertyList.put(barsDate, properties);
                        }
                    }
                    for (int i = 0; i < storeObject.length; i++) {
                        Date barsDate = (Date) storeObject[i].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE);
                        if (!key.isInRange(barsDate)) {
                            continue;
                        }

                        storeList.put(barsDate, storeObject[i].getStore());
                        propertyList.put(barsDate, storeObject[i].getStoreProperties());
                    }

                    Collection<IStore> s = storeList.values();
                    Collection<IStoreProperties> p = propertyList.values();
                    element.setStoreProperties(s.toArray(new IStore[s.size()]), p.toArray(new IStoreProperties[p.size()]));
                }

                return storeObject;
            }
        };

        historyMap.put(key, new WeakReference<HistoryDay>(history));

        return history;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getDay(java.util.Date)
     */
    @Override
    public IHistory[] getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        date = c.getTime();

        IStore dayStore = null;
        IStoreProperties dayProperties = null;

        if (store != null) {
            IStore[] childStores = store.fetchChilds(null);
            if (childStores != null) {
                for (int i = 0; i < childStores.length; i++) {
                    IStoreProperties childProperties = childStores[i].fetchProperties(null);

                    Date barsDate = (Date) childProperties.getProperty(IPropertyConstants.BARS_DATE);
                    if (date.equals(barsDate)) {
                        dayStore = childStores[i];
                        dayProperties = childProperties;
                        break;
                    }
                }
            }
        }

        if (dayStore == null || dayProperties == null) {
            return new IHistory[0];
        }

        List<IHistory> l = new ArrayList<IHistory>();

        String[] propertyNames = dayProperties.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            Object value = dayProperties.getProperty(propertyNames[i]);
            if (!(value instanceof IOHLC[])) {
                continue;
            }
            TimeSpan timeSpan = TimeSpan.fromString(propertyNames[i]);
            if (timeSpan != null) {
                Key key = new Key(date, date, timeSpan);

                WeakReference<HistoryDay> reference = historyMap.get(key);
                HistoryDay history = reference != null ? reference.get() : null;
                if (history == null) {
                    IStore[] storeList = new IStore[] {
                        dayStore,
                    };
                    IStoreProperties[] propertiesList = new IStoreProperties[] {
                        dayProperties
                    };
                    history = createHistoryDay(storeList, propertiesList, timeSpan);
                    historyMap.put(key, new WeakReference<HistoryDay>(history));
                }
                l.add(history);
            }
        }

        return l.toArray(new IHistory[l.size()]);
    }

    @SuppressWarnings("unchecked")
    private HistoryDay createHistoryDay(IStore[] storeList, IStoreProperties[] propertiesList, TimeSpan timeSpan) {
        HistoryDay history = new HistoryDay(security, timeSpan, storeList, propertiesList) {

            @Override
            protected IStoreObject[] updateStoreObjects() {
                IStoreObject[] storeObject = super.updateStoreObjects();

                Set<Entry<Key, WeakReference<HistoryDay>>> set = historyMap.entrySet();
                Entry<Key, WeakReference<HistoryDay>>[] entry = set.toArray(new Entry[set.size()]);

                Set<Key> updatedElements = new HashSet<Key>();

                for (int ii = 0; ii < storeObject.length; ii++) {
                    TimeSpan timeSpan = (TimeSpan) storeObject[ii].getStoreProperties().getProperty(IPropertyConstants.TIME_SPAN);
                    Date barsDate = (Date) storeObject[ii].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE);
                    for (int i = 0; i < entry.length; i++) {
                        HistoryDay element = entry[i].getValue().get();
                        Key key = entry[i].getKey();
                        if (element != null && element != this && element.getTimeSpan().equals(timeSpan)) {
                            if (!entry[i].getKey().isInRange(barsDate)) {
                                continue;
                            }
                            updatedElements.add(key);
                        }
                    }
                }

                for (Key key : updatedElements) {
                    HistoryDay element = historyMap.get(key).get();
                    if (element == null) {
                        continue;
                    }

                    Map<Date, IStore> storeList = new HashMap<Date, IStore>();
                    Map<Date, IStoreProperties> propertyList = new HashMap<Date, IStoreProperties>();

                    IStore[] childStores = store != null ? store.fetchChilds(null) : null;
                    if (childStores != null) {
                        for (IStore childStore : childStores) {
                            IStoreProperties properties = childStore.fetchProperties(null);

                            Date barsDate = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
                            if (!key.isInRange(barsDate)) {
                                continue;
                            }

                            storeList.put(barsDate, childStore);
                            propertyList.put(barsDate, properties);
                        }
                    }
                    for (int i = 0; i < storeObject.length; i++) {
                        Date barsDate = (Date) storeObject[i].getStoreProperties().getProperty(IPropertyConstants.BARS_DATE);
                        if (!key.isInRange(barsDate)) {
                            continue;
                        }

                        storeList.put(barsDate, storeObject[i].getStore());
                        propertyList.put(barsDate, storeObject[i].getStoreProperties());
                    }

                    Collection<IStore> s = storeList.values();
                    Collection<IStoreProperties> p = propertyList.values();
                    element.setStoreProperties(s.toArray(new IStore[s.size()]), p.toArray(new IStoreProperties[p.size()]));
                }

                return storeObject;
            }
        };
        return history;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getTimeSpan()
     */
    @Override
    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSplits()
     */
    @Override
    public ISplit[] getSplits() {
        return splits;
    }

    public void setSplits(ISplit[] splits) {
        if (Arrays.equals(this.splits, splits)) {
            return;
        }
        ISplit[] oldValue = this.splits;
        this.splits = splits;
        propertyChangeSupport.firePropertyChange(IPropertyConstants.SPLITS, oldValue, this.splits);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getAdjustedOHLC()
     */
    @Override
    public IOHLC[] getAdjustedOHLC() {
        IDividend[] dividends = (IDividend[]) security.getAdapter(IDividend[].class);

        if ((dividends == null || dividends.length == 0) && (splits == null || splits.length == 0)) {
            return bars;
        }

        IOHLC[] l = new IOHLC[bars.length];
        for (int i = 0; i < l.length; i++) {
            double splitFactor = 1.0;
            if (splits != null) {
                for (int s = 0; s < splits.length; s++) {
                    if (bars[i].getDate().before(splits[s].getDate())) {
                        splitFactor *= splits[s].getNewQuantity() / splits[s].getOldQuantity();
                    }
                }
            }

            double cumulatedDividends = 0.0;
            if (dividends != null) {
                for (int d = 0; d < dividends.length; d++) {
                    if (bars[i].getDate().before(dividends[d].getExDate())) {
                        cumulatedDividends += dividends[d].getValue();
                    }
                }
            }

            l[i] = new OHLC(bars[i].getDate(), bars[i].getOpen() / splitFactor - cumulatedDividends, bars[i].getHigh() / splitFactor - cumulatedDividends, bars[i].getLow() / splitFactor - cumulatedDividends, bars[i].getClose() / splitFactor - cumulatedDividends, (long) (bars[i].getVolume() * splitFactor));
        }
        return l;
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

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
     */
    @Override
    public IStore getStore() {
        return store;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
     */
    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
     */
    @Override
    public IStoreProperties getStoreProperties() {
        if (storeProperties == null) {
            storeProperties = new StoreProperties();
        }

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

        storeProperties.setProperty(IPropertyConstants.SECURITY, security);
        storeProperties.setProperty(IPropertyConstants.BARS, bars);
        storeProperties.setProperty(IPropertyConstants.TIME_SPAN, timeSpan);
        storeProperties.setProperty(IPropertyConstants.SPLITS, splits);

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        this.storeProperties = storeProperties;

        this.security = (ISecurity) storeProperties.getProperty(IPropertyConstants.SECURITY);

        IOHLC[] bars = (IOHLC[]) storeProperties.getProperty(IPropertyConstants.BARS);
        List<IOHLC> l1 = bars != null ? Arrays.asList(bars) : new ArrayList<IOHLC>();
        Collections.sort(l1, new Comparator<IOHLC>() {

            @Override
            public int compare(IOHLC o1, IOHLC o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        this.bars = l1.toArray(new IOHLC[l1.size()]);

        this.timeSpan = (TimeSpan) storeProperties.getProperty(IPropertyConstants.TIME_SPAN);

        ISplit[] splits = (ISplit[]) storeProperties.getProperty(IPropertyConstants.SPLITS);
        List<ISplit> l2 = splits != null ? Arrays.asList(splits) : new ArrayList<ISplit>();
        Collections.sort(l2, new Comparator<ISplit>() {

            @Override
            public int compare(ISplit o1, ISplit o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        this.splits = l2.toArray(new ISplit[l2.size()]);

        updateRange();
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

    protected void updateSubsets() {
        for (Key key : historyMap.keySet()) {
            WeakReference<HistoryDay> reference = historyMap.get(key);
            HistoryDay history = reference.get();
            if (history == null) {
                continue;
            }
            if (history.getTimeSpan().equals(timeSpan)) {
                IOHLC[] subset = getOHLCSubset(key.getFirst(), key.getLast());
                history.setOHLC(subset);
            }
        }
    }
}
