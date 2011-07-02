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

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;

/**
 * Default implementation of <code>ICurrencyExchange</code>.
 *
 * @since 1.0
 */
public class CurrencyExchange extends Security implements ICurrencyExchange {

    private Currency from;
    private Currency to;
    private Double multiplier;

    protected CurrencyExchange() {
    }

    public CurrencyExchange(Currency from, Currency to, double multiplier) {
        this.from = from;
        this.to = to;
        this.multiplier = multiplier;
    }

    public CurrencyExchange(String name, IFeedIdentifier identifier, Currency from, Currency to, double multiplier) {
        super(name, identifier);
        this.from = from;
        this.to = to;
        this.multiplier = multiplier;
    }

    public CurrencyExchange(IStore store, IStoreProperties storeProperties) {
        super(store, storeProperties);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ICurrencyExchange#getFromCurrency()
     */
    @Override
    public Currency getFromCurrency() {
        return from;
    }

    public void setFromCurrency(Currency from) {
        this.from = from;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ICurrencyExchange#getToCurrency()
     */
    @Override
    public Currency getToCurrency() {
        return to;
    }

    public void setToCurrency(Currency to) {
        this.to = to;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.ICurrencyExchange#getMultiplier()
     */
    @Override
    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.Security#getStoreProperties()
     */
    @Override
    public IStoreProperties getStoreProperties() {
        IStoreProperties storeProperties = super.getStoreProperties();

        storeProperties.setProperty(IPropertyConstants.OBJECT_TYPE, ICurrencyExchange.class.getName());

        storeProperties.setProperty("from-currency", from);
        storeProperties.setProperty("to-currency", to);
        storeProperties.setProperty("multiplier", multiplier);

        return storeProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.instruments.Security#setStoreProperties(org.eclipsetrader.core.repositories.IStoreProperties)
     */
    @Override
    public void setStoreProperties(IStoreProperties storeProperties) {
        super.setStoreProperties(storeProperties);

        from = (Currency) storeProperties.getProperty("from-currency");
        to = (Currency) storeProperties.getProperty("to-currency");
        multiplier = (Double) storeProperties.getProperty("multiplier");
    }
}
