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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class History implements IHistory, IStoreObject {
	private ISecurity security;
	private IOHLC[] bars = new IOHLC[0];
	private TimeSpan timeSpan;

	private IOHLC highest;
	private IOHLC lowest;

	private IStore store;
	private IStoreProperties storeProperties;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	protected History() {
	}

	public History(ISecurity security, IOHLC[] bars) {
		this(security, bars, TimeSpan.days(1));
	}

	public History(ISecurity security, IOHLC[] bars, TimeSpan timeSpan) {
		setSecurity(security);
		setOHLC(bars);
	    this.timeSpan = timeSpan;
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
		return new History(security, l.toArray(new IOHLC[l.size()]), timeSpan);
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getSubset(java.util.Date, java.util.Date, org.eclipsetrader.core.feed.TimeSpan)
     */
    public IHistory getSubset(Date first, Date last, TimeSpan timeSpan) {
    	if (this.timeSpan != null && this.timeSpan.equals(timeSpan))
    		return getSubset(first, last);

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

    	List<IOHLC> l = new ArrayList<IOHLC>();

		if (store != null) {
    		IStore[] childStores = store.fetchChilds(null);
    		if (childStores != null) {
        		for (IStore childStore : childStores) {
        			IStoreProperties properties = childStore.fetchProperties(null);

    				Date barsDate = (Date) properties.getProperty(IPropertyConstants.BARS_DATE);
    				if ((first != null && barsDate.before(first)) || (last != null && barsDate.after(last)))
    					continue;

		    		IStore[] aggregateChilds = childStore.fetchChilds(null);
		    		if (aggregateChilds != null) {
    		    		for (IStore aggregateChild : aggregateChilds) {
    		    			properties = aggregateChild.fetchProperties(null);
    		    			if (timeSpan.equals(properties.getProperty(IPropertyConstants.TIME_SPAN))) {
    		    				IOHLC[] bars = (IOHLC[]) properties.getProperty(IPropertyConstants.BARS);
    		    			    if (bars != null)
    		    			    	l.addAll(Arrays.asList(bars));
    		    			}
    		    		}
		    		}
        		}
    		}
    	}

		if (l.size() == 0 && !TimeSpan.minutes(1).equals(timeSpan)) {
			IHistory temp = getSubset(first, last, TimeSpan.minutes(1));

			Date startDate = null, endDate = null;
			Double open = null, high = null, low = null, close = null;
			Long volume = 0L;

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
		}

		return new History(security, l.toArray(new IOHLC[l.size()]), timeSpan);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IHistory#getTimeSpan()
     */
    public TimeSpan getTimeSpan() {
	    return timeSpan;
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
		List<IOHLC> l = bars != null ? Arrays.asList(bars) : new ArrayList<IOHLC>();
		Collections.sort(l, new Comparator<IOHLC>() {
            public int compare(IOHLC o1, IOHLC o2) {
	            return o1.getDate().compareTo(o2.getDate());
            }
		});
		this.bars = l.toArray(new IOHLC[l.size()]);

		this.timeSpan = (TimeSpan) storeProperties.getProperty(IPropertyConstants.TIME_SPAN);

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
