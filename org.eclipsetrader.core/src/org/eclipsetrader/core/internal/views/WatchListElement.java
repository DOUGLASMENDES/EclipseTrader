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

package org.eclipsetrader.core.internal.views;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IWatchListElement;

/**
 * Default implementation of the <code>IWatchListElement</code> interface.
 *
 * @since 1.0
 */
public class WatchListElement implements IWatchListElement, ISessionData, Cloneable {
	private ISecurity security;
	private Long position;
	private Double purchasePrice;
	private Date date;

	private ITrade trade;
	private IQuote quote;
	private ITodayOHL todayOHL;
	private ILastClose lastClose;

	private Map<Object, Object> sessionData = new HashMap<Object, Object>();

	protected WatchListElement() {
	}

	public WatchListElement(ISecurity security) {
	    this(security, null, null, null);
    }

	public WatchListElement(ISecurity security, Long position, Double purchasePrice) {
	    this(security, position, purchasePrice, null);
    }

	public WatchListElement(ISecurity security, Long position, Double purchasePrice, Date date) {
	    this.security = security;
	    this.position = position;
	    this.purchasePrice = purchasePrice;
	    this.date = date;
    }

	public WatchListElement(IHolding holding) {
	    this(holding.getSecurity(), holding.getPosition(), holding.getPurchasePrice(), holding.getDate());
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#getSecurity()
	 */
	public ISecurity getSecurity() {
		return security;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#getPosition()
	 */
	public Long getPosition() {
		return position;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#setPosition(java.lang.Long)
	 */
	public void setPosition(Long position) {
		this.position = position;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#getPurchasePrice()
	 */
	public Double getPurchasePrice() {
		return purchasePrice;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IWatchListElement#setPurchasePrice(java.lang.Double)
	 */
	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (security != null) {
			if (security.getIdentifier() != null && adapter.isAssignableFrom(security.getIdentifier().getClass()))
				return security.getIdentifier();
			if (adapter.isAssignableFrom(security.getClass()))
				return security;
		}
		if (adapter.isAssignableFrom(ITrade.class))
			return trade;
		if (adapter.isAssignableFrom(IQuote.class))
			return quote;
		if (adapter.isAssignableFrom(ITodayOHL.class))
			return todayOHL;
		if (adapter.isAssignableFrom(ILastClose.class))
			return lastClose;
		if (adapter.isAssignableFrom(getClass()))
			return this;
		return null;
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

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#getData(java.lang.Object)
     */
    public Object getData(Object key) {
	    return sessionData.get(key);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#setData(java.lang.Object, java.lang.Object)
     */
    public void setData(Object key, Object value) {
    	sessionData.put(key, value);
    }

	/* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	    return super.clone();
    }
}
