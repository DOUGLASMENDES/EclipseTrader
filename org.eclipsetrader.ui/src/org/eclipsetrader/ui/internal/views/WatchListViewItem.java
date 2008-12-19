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

package org.eclipsetrader.ui.internal.views;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IWatchListElement;

public class WatchListViewItem extends PlatformObject {
	private IWatchListElement reference;

	private ISecurity security;
	private Holding holding;

	private ITrade trade;
	private IQuote quote;
	private ITodayOHL todayOHL;
	private ILastClose lastClose;
	private IBook book;
	private MarketPricingEnvironment pricingEnvironment;

	private Map<String, IDataProvider> dataProviders = new HashMap<String, IDataProvider>();
	private Map<String, IAdaptable> values = new HashMap<String, IAdaptable>();
	private Map<String, Integer> updateTime = new HashMap<String, Integer>();

	public WatchListViewItem() {
	}

	public WatchListViewItem(IWatchListElement reference) {
	    this(reference.getSecurity(), reference.getPosition(), reference.getPurchasePrice(), reference.getDate());
	    this.reference = reference;
    }

	public WatchListViewItem(ISecurity security) {
	    this(security, null, null, null);
    }

	public WatchListViewItem(ISecurity security, Long position, Double purchasePrice) {
	    this(security, position, purchasePrice, null);
    }

	public WatchListViewItem(ISecurity security, Long position, Double purchasePrice, Date date) {
	    this.security = security;
	    this.holding = new Holding(security, position, purchasePrice, date);
    }

	public ISecurity getSecurity() {
		return security;
	}

	public Date getDate() {
		return holding != null ? holding.getDate() : null;
	}

	public Long getPosition() {
		return holding != null ? holding.getPosition() : null;
	}

	public Double getPurchasePrice() {
		return holding != null ? holding.getPurchasePrice() : null;
	}

	public IWatchListElement getReference() {
    	return reference;
    }

	public IDataProvider getDataProvider(String propertyName) {
		return dataProviders.get(propertyName);
	}

	public void setDataProvider(String propertyName, IDataProvider value) {
    	this.dataProviders.put(propertyName, value);
    }

	public void clearDataProvider(String propertyName) {
    	this.dataProviders.remove(propertyName);
    }

	public IAdaptable getValue(String propertyName) {
		return values.get(propertyName);
	}

	public void setValue(String propertyName, IAdaptable value) {
    	this.values.put(propertyName, value);
    }

	public void clearValue(String propertyName) {
		values.remove(propertyName);
		updateTime.remove(propertyName);
	}

	public String[] getValueProperties() {
		Set<String> s = values.keySet();
		return s.toArray(new String[s.size()]);
	}

	public Integer getUpdateTime(String propertyName) {
		return updateTime.get(propertyName);
	}

	public void setUpdateTime(String propertyName, Integer value) {
    	this.updateTime.put(propertyName, value);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (security != null) {
			Object obj = security.getAdapter(adapter);
			if (obj != null)
				return obj;
		}

		if (adapter.isAssignableFrom(Holding.class))
			return holding;

		if (adapter.isAssignableFrom(ITrade.class))
			return trade;
		if (adapter.isAssignableFrom(IQuote.class))
			return quote;
		if (adapter.isAssignableFrom(ITodayOHL.class))
			return todayOHL;
		if (adapter.isAssignableFrom(ILastClose.class))
			return lastClose;
		if (adapter.isAssignableFrom(IBook.class))
			return book;
		if (adapter.isAssignableFrom(MarketPricingEnvironment.class))
			return pricingEnvironment;

		if (adapter.isAssignableFrom(getClass()))
			return this;

		if (reference != null) {
			Object obj = reference.getAdapter(adapter);
			if (obj != null)
				return obj;
		}

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
	}

	public void setTrade(ITrade trade) {
    	this.trade = trade;
    }

	public void setQuote(IQuote quote) {
    	this.quote = quote;
    }

	public void setTodayOHL(ITodayOHL todayOHL) {
    	this.todayOHL = todayOHL;
    }

	public void setLastClose(ILastClose lastClose) {
    	this.lastClose = lastClose;
    }

	public IBook getBook() {
    	return book;
    }

	public void setBook(IBook book) {
    	this.book = book;
    }

	public MarketPricingEnvironment getPricingEnvironment() {
    	return pricingEnvironment;
    }

	public void setPricingEnvironment(MarketPricingEnvironment pricingEnvironment) {
    	this.pricingEnvironment = pricingEnvironment;
    }
}
