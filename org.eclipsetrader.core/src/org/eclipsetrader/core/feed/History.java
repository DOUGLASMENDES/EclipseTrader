/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

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

	private Date rangeBegin;
	private Date rangeEnd;

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

		/* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
	        return 7 * first.hashCode() + 11 * last.hashCode() + 13 * timeSpan.hashCode();
        }

		/* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
        	if (!(obj instanceof Key))
        		return false;
	        return hashCode() == obj.hashCode();
        }
	}

	private Map<Key, HistoryDay> historyMap = new WeakHashMap<Key, HistoryDay>();

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

	public History(ISecurity security, IOHLC[] bars, Date rangeBegin, Date rangeEnd, TimeSpan timeSpan) {
		this.rangeBegin = rangeBegin;
		this.rangeEnd = rangeEnd;
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
	public ISecurity getSecurity() {
		return security;
	}

	protected void setSecurity(ISecurity security) {
    	this.security = security;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getFirst()
	 */
	public IOHLC getFirst() {
		return bars != null && bars.length != 0 ? bars[0] : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getLast()
	 */
	public IOHLC getLast() {
		return bars != null && bars.length != 0 ? bars[bars.length - 1] : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getHighest()
	 */
	public IOHLC getHighest() {
		return highest;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getLowest()
	 */
	public IOHLC getLowest() {
		return lowest;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getOHLC()
	 */
	public IOHLC[] getOHLC() {
		return bars;
	}

	public void setOHLC(IOHLC[] bars) {
		Object oldValue = this.bars;

		List<IOHLC> l = new ArrayList<IOHLC>(Arrays.asList(bars));
		Collections.sort(l, new Comparator<IOHLC>() {
            public int compare(IOHLC o1, IOHLC o2) {
	            return o1.getDate().compareTo(o2.getDate());
            }
		});
		this.bars = l.toArray(new IOHLC[l.size()]);

	    updateRange();

		propertyChangeSupport.firePropertyChange(IPropertyConstants.BARS, oldValue, this.bars);
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date)
	 */
	public IHistory getSubset(Date first, Date last) {
		List<IOHLC> l = new ArrayList<IOHLC>();
		for (IOHLC b : bars) {
			if ((first == null || !b.getDate().before(first)) && (last == null || !b.getDate().after(last)))
				l.add(b);
		}
		return new History(security, l.toArray(new IOHLC[l.size()]), first, last, timeSpan);
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    public IHistory getSubset(Date first, Date last, TimeSpan timeSpan) {
    	if (this.timeSpan != null && this.timeSpan.equals(timeSpan))
    		return getSubset(first, last);

    	Key key = new Key(first, last, timeSpan);

    	HistoryDay history = historyMap.get(key);
    	if (history != null)
    		return history;

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
    				if ((first != null && barsDate.before(first)) || (last != null && barsDate.after(last)))
    					continue;

    				storeList.add(childStore);
    				propertyList.add(properties);
        		}
    		}
    	}

    	history = new HistoryDay(security, timeSpan, storeList.toArray(new IStore[storeList.size()]), propertyList.toArray(new IStoreProperties[propertyList.size()]));

    	if (history.getOHLC().length == 0 && !TimeSpan.minutes(1).equals(timeSpan)) {
    		IHistory temp = new HistoryDay(security, TimeSpan.minutes(1), storeList.toArray(new IStore[storeList.size()]), propertyList.toArray(new IStoreProperties[propertyList.size()]));

			Date startDate = null, endDate = null;
			Double open = null, high = null, low = null, close = null;
			Long volume = 0L;

			List<IOHLC> l = new ArrayList<IOHLC>();
			for (IOHLC currentBar : temp.getOHLC()) {
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

				if (open == null)
					open = currentBar.getOpen();
				high = high != null ? Math.max(high, currentBar.getHigh()) : currentBar.getHigh();
				low = low != null ? Math.min(low, currentBar.getLow()) : currentBar.getLow();
				close = currentBar.getClose();
				volume += currentBar.getVolume();
			}

			if (startDate != null)
				l.add(new OHLC(startDate, open, high, low, close, volume));

			history.setOHLC(l.toArray(new IOHLC[l.size()]));
    	}

    	PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
    	if (propertyChangeSupport != null) {
    		propertyChangeSupport.addPropertyChangeListener("store_object", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                	IStoreObject storeObject = (IStoreObject) evt.getNewValue();
                	Date date = (Date) storeObject.getStoreProperties().getProperty(IPropertyConstants.BARS_DATE);

                	for (Entry<Key, HistoryDay> entry : historyMap.entrySet()) {
                		HistoryDay element = entry.getValue();
                		if (element != null) {
                			if (!date.before(entry.getKey().getFirst()) && !date.after(entry.getKey().getLast())) {
                	    		IStore[] childStores = store.fetchChilds(null);
                	    		if (childStores != null) {
                	    	    	List<IStore> storeList = new ArrayList<IStore>();
                	    	    	List<IStoreProperties> propertyList = new ArrayList<IStoreProperties>();

                	    	    	for (IStore childStore : childStores) {
                	        			IStoreProperties properties = childStore.fetchProperties(null);

                	    				Date barsDate = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
                	    				if (barsDate.before(entry.getKey().getFirst()) || barsDate.after(entry.getKey().getLast()))
                	    					continue;

                	    				storeList.add(childStore);
                	    				propertyList.add(properties);
                	        		}

                	    	    	element.setStoreProperties(storeList.toArray(new IStore[storeList.size()]), propertyList.toArray(new IStoreProperties[propertyList.size()]));
                	    		}
                			}
                		}
                	}
                }
    		});
    	}

		historyMap.put(key, history);

		return history;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getTimeSpan()
     */
    public TimeSpan getTimeSpan() {
	    return timeSpan;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSplits()
     */
    public ISplit[] getSplits() {
	    return splits;
    }

	public void setSplits(ISplit[] splits) {
		Object oldValue = this.splits;
    	this.splits = splits;
		propertyChangeSupport.firePropertyChange(IPropertyConstants.SPLITS, oldValue, this.splits);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getAdjustedOHLC()
     */
    public IOHLC[] getAdjustedOHLC() {
    	IDividend[] dividends = (IDividend[]) security.getAdapter(IDividend[].class);

    	if ((dividends == null || dividends.length == 0) && (splits == null || splits.length == 0))
    		return bars;

    	IOHLC[] l = new IOHLC[bars.length];
    	for (int i = 0; i < l.length; i++) {
    		double splitFactor = 1.0;
    		if (splits != null) {
        		for (int s = 0; s < splits.length; s++) {
                    if (bars[i].getDate().before(splits[s].getDate()))
                        splitFactor *= splits[s].getNewQuantity() / splits[s].getOldQuantity();
        		}
    		}

    		double cumulatedDividends = 0.0;
    		if (dividends != null) {
        		for (int d = 0; d < dividends.length; d++) {
        			if (bars[i].getDate().before(dividends[d].getExDate()))
        				cumulatedDividends += dividends[d].getValue();
        		}
    		}

    		l[i] = new OHLC(
            		bars[i].getDate(),
            		(bars[i].getOpen() / splitFactor) - cumulatedDividends,
            		(bars[i].getHigh() / splitFactor) - cumulatedDividends,
            		(bars[i].getLow() / splitFactor) - cumulatedDividends,
            		(bars[i].getClose() / splitFactor) - cumulatedDividends,
            		(long) (bars[i].getVolume() * splitFactor)
            	);
    	}
	    return l;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(getClass()))
			return this;

    	if (adapter.isAssignableFrom(PropertyChangeSupport.class))
    		return propertyChangeSupport;

    	return null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStore()
     */
    public IStore getStore() {
	    return store;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStore(org.eclipsetrader.core.repositories.IStore)
     */
    public void setStore(IStore store) {
    	this.store = store;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#getStoreProperties()
     */
    public IStoreProperties getStoreProperties() {
		if (storeProperties == null)
			storeProperties = new StoreProperties();

		storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());

		storeProperties.setProperty(IPropertyConstants.SECURITY, security);
		storeProperties.setProperty(IPropertyConstants.BARS, bars);
		storeProperties.setProperty(IPropertyConstants.TIME_SPAN, timeSpan);
		storeProperties.setProperty(IPropertyConstants.SPLITS, splits);

		if (bars != null && bars.length != 0)
			storeProperties.setProperty(IPropertyConstants.BARS_DATE, bars[0].getDate());

		return storeProperties;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IStoreObject#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    public void setStoreProperties(IStoreProperties storeProperties) {
	    this.storeProperties = storeProperties;

	    this.security = (ISecurity) storeProperties.getProperty(IPropertyConstants.SECURITY);

	    IOHLC[] bars = (IOHLC[]) storeProperties.getProperty(IPropertyConstants.BARS);
		List<IOHLC> l1 = bars != null ? Arrays.asList(bars) : new ArrayList<IOHLC>();
		Collections.sort(l1, new Comparator<IOHLC>() {
            public int compare(IOHLC o1, IOHLC o2) {
	            return o1.getDate().compareTo(o2.getDate());
            }
		});
		this.bars = l1.toArray(new IOHLC[l1.size()]);

		this.timeSpan = (TimeSpan) storeProperties.getProperty(IPropertyConstants.TIME_SPAN);

		ISplit[] splits = (ISplit[]) storeProperties.getProperty(IPropertyConstants.SPLITS);
		List<ISplit> l2 = splits != null ? Arrays.asList(splits) : new ArrayList<ISplit>();
		Collections.sort(l2, new Comparator<ISplit>() {
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
			if (highest == null || b.getHigh() > highest.getHigh())
				highest = b;
			if (lowest == null || b.getLow() < lowest.getLow())
				lowest = b;
		}
    }
}
