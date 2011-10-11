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

package org.eclipsetrader.core.instruments;

import java.util.Currency;

import org.eclipsetrader.core.feed.IDividend;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;

public class Stock extends Security implements IStock {

    private Currency currency;
    private IDividend[] dividends;

    protected Stock() {
    }

    public Stock(String name, IFeedIdentifier identifier, Currency currency) {
        super(name, identifier);
        this.currency = currency;
    }

    public Stock(IStore store, IStoreProperties storeProperties) {
        super(store, storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.IStock#getCurrency()
     */
    @Override
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.IStock#getDividends()
     */
    @Override
    public IDividend[] getDividends() {
        return dividends;
    }

    public void setDividends(IDividend[] dividends) {
        this.dividends = dividends;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.Security#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(Currency.class)) {
            return currency;
        }
        if (dividends != null && adapter.isAssignableFrom(dividends.getClass())) {
            return dividends;
        }
        return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.Security#getStoreProperties()
     */
    @Override
    public IStoreProperties getStoreProperties() {
        IStoreProperties storeProperties = super.getStoreProperties();

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IStock.class.getName());

        storeProperties.setProperty(IPropertyConstants.CURRENCY, currency);
        if (dividends != null) {
            storeProperties.setProperty(IPropertyConstants.DIVIDENDS, dividends);
        }

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.Security#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        super.setStoreProperties(storeProperties);

        this.currency = (Currency) storeProperties.getProperty(IPropertyConstants.CURRENCY);
        this.dividends = (IDividend[]) storeProperties.getProperty(IPropertyConstants.DIVIDENDS);
    }
}
