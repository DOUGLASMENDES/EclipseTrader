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

package org.eclipsetrader.ui.internal.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPrice;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IWatchListElement;
import org.eclipsetrader.ui.NullRealm;
import org.eclipsetrader.ui.internal.ats.ViewItem;

public class WatchListViewItem implements ViewItem {

    public static final String PROP_QUANTITY = "position";
    public static final String PROP_PRICE = "purchasePrice";

    private final IWatchListElement element;

    private final ISecurity security;

    private IPrice price;
    private ITrade trade;
    private IQuote quote;
    private ITodayOHL todayOHL;
    private ILastClose lastClose;
    private IBook book;

    private Long position;
    private Double purchasePrice;

    private final WritableMap observableValues = new WritableMap(NullRealm.getInstance(), String.class, Object.class);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public WatchListViewItem(IWatchListElement element) {
        this.element = element;
        this.security = element.getSecurity();
        this.position = element.getPosition();
        this.purchasePrice = element.getPurchasePrice();
    }

    public IWatchListElement getElement() {
        return element;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getValue(java.lang.String)
     */
    @Override
    public Object getValue(String name) {
        return observableValues.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#putValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void putValue(final String name, final Object value) {
        final Object oldValue = observableValues.get(name);
        if (oldValue != null && value != null && oldValue.equals(value)) {
            return;
        }

        observableValues.put(name, value);
        changeSupport.firePropertyChange(name, oldValue, value);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getParent()
     */
    @Override
    public ViewItem getParent() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.ats.ViewItem#getItems()
     */
    @Override
    public ObservableList getItems() {
        return null;
    }

    public ISecurity getSecurity() {
        return security;
    }

    public void setPriceData(Object obj) {
        if (obj instanceof IPrice) {
            price = (IPrice) obj;
        }
        if (obj instanceof ITrade) {
            trade = (ITrade) obj;
        }
        if (obj instanceof IQuote) {
            quote = (IQuote) obj;
        }
        if (obj instanceof ILastClose) {
            lastClose = (ILastClose) obj;
        }
        if (obj instanceof ITodayOHL) {
            todayOHL = (ITodayOHL) obj;
        }
        if (obj instanceof IBook) {
            book = (IBook) obj;
        }
    }

    public IPrice getPrice() {
        return price;
    }

    public void setPrice(IPrice price) {
        this.price = price;
    }

    public ITrade getTrade() {
        return trade;
    }

    public void setTrade(ITrade trade) {
        this.trade = trade;
    }

    public IQuote getQuote() {
        return quote;
    }

    public void setQuote(IQuote quote) {
        this.quote = quote;
    }

    public ITodayOHL getTodayOHL() {
        return todayOHL;
    }

    public void setTodayOHL(ITodayOHL todayOHL) {
        this.todayOHL = todayOHL;
    }

    public ILastClose getLastClose() {
        return lastClose;
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

    public Long getPosition() {
        return position;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (security != null) {
            Object obj = security.getAdapter(adapter);
            if (obj != null) {
                return obj;
            }
        }

        if (adapter.isAssignableFrom(Holding.class)) {
            return new Holding() {

                @Override
                public Long getPosition() {
                    return WatchListViewItem.this.position;
                }

                @Override
                public void setPosition(Long position) {
                    changeSupport.firePropertyChange(PROP_QUANTITY, WatchListViewItem.this.position, WatchListViewItem.this.position = position);
                }

                @Override
                public Double getPurchasePrice() {
                    return WatchListViewItem.this.purchasePrice;
                }

                @Override
                public void setPurchasePrice(Double purchasePrice) {
                    changeSupport.firePropertyChange(WatchListViewItem.PROP_PRICE, WatchListViewItem.this.purchasePrice, WatchListViewItem.this.purchasePrice = purchasePrice);
                }
            };
        }

        if (adapter.isAssignableFrom(IPrice.class)) {
            return price;
        }
        if (adapter.isAssignableFrom(ITrade.class)) {
            return trade;
        }
        if (adapter.isAssignableFrom(IQuote.class)) {
            return quote;
        }
        if (adapter.isAssignableFrom(ITodayOHL.class)) {
            return todayOHL;
        }
        if (adapter.isAssignableFrom(ILastClose.class)) {
            return lastClose;
        }
        if (adapter.isAssignableFrom(IBook.class)) {
            return book;
        }

        if (adapter.isAssignableFrom(element.getClass())) {
            return element;
        }

        Object result = element.getAdapter(adapter);
        if (result != null) {
            return result;
        }

        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        return null;
    }
}
