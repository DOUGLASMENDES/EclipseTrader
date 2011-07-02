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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class TickerViewItem implements IViewItem, ISessionData {

    private ISecurity security;

    private ITrade trade;
    private IQuote quote;
    private ILastClose lastClose;

    private IAdaptable[] values;
    private Map<Object, Object> sessionData = new HashMap<Object, Object>();

    public TickerViewItem(ISecurity security) {
        this.security = security;
        this.values = new IAdaptable[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#accept(org.eclipsetrader.core.views.IViewItemVisitor)
     */
    @Override
    public void accept(IViewItemVisitor visitor) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
     */
    @Override
    public int getItemCount() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getItems()
     */
    @Override
    public IViewItem[] getItems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getParent()
     */
    @Override
    public IViewItem getParent() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItem#getValues()
     */
    @Override
    public IAdaptable[] getValues() {
        return values;
    }

    public void setValues(IAdaptable[] values) {
        this.values = values;
    }

    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (security != null) {
            if (security.getIdentifier() != null && adapter.isAssignableFrom(security.getIdentifier().getClass())) {
                return security.getIdentifier();
            }
            if (adapter.isAssignableFrom(security.getClass())) {
                return security;
            }
        }

        if (adapter.isAssignableFrom(ITrade.class)) {
            return trade;
        }
        if (adapter.isAssignableFrom(IQuote.class)) {
            return quote;
        }
        if (adapter.isAssignableFrom(ILastClose.class)) {
            return lastClose;
        }

        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        return null;
    }

    public void setTrade(ITrade trade) {
        this.trade = trade;
    }

    public void setQuote(IQuote quote) {
        this.quote = quote;
    }

    public void setLastClose(ILastClose lastClose) {
        this.lastClose = lastClose;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#getData(java.lang.Object)
     */
    @Override
    public Object getData(Object key) {
        return sessionData.get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.ISessionData#setData(java.lang.Object, java.lang.Object)
     */
    @Override
    public void setData(Object key, Object value) {
        sessionData.put(key, value);
    }
}
