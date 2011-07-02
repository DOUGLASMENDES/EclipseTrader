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

package org.eclipsetrader.core.internal;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.ICurrencyService;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ICurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.core.repositories.RepositoryResourceDelta;

public class CurrencyService implements ICurrencyService {

    private IRepositoryService repositoryService;
    private MarketPricingEnvironment pricingEnvironment;
    private List<ICurrencyExchange> exchanges = new ArrayList<ICurrencyExchange>();

    private IRepositoryChangeListener repositoryListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            for (RepositoryResourceDelta delta : event.getDeltas()) {
                if (!(delta.getResource() instanceof ICurrencyExchange)) {
                    continue;
                }
                ICurrencyExchange exchange = (ICurrencyExchange) delta.getResource();
                if (delta.getKind() == RepositoryResourceDelta.ADDED) {
                    if (!exchanges.contains(exchange)) {
                        exchanges.add(exchange);
                    }
                }
                else if (delta.getKind() == RepositoryResourceDelta.REMOVED) {
                    exchanges.remove(exchange);
                }
            }
        }
    };

    public CurrencyService(IRepositoryService repositoryService, IMarketService marketService) {
        this.repositoryService = repositoryService;
        this.pricingEnvironment = new MarketPricingEnvironment(marketService);
    }

    protected CurrencyService(IRepositoryService repositoryService, MarketPricingEnvironment pricingEnvironment) {
        this.repositoryService = repositoryService;
        this.pricingEnvironment = pricingEnvironment;
    }

    public void startUp(IProgressMonitor monitor) throws Exception {
        for (ISecurity security : repositoryService.getSecurities()) {
            ICurrencyExchange exchange = (ICurrencyExchange) security.getAdapter(ICurrencyExchange.class);
            if (exchange != null) {
                exchanges.add(exchange);
                pricingEnvironment.addSecurity(security);
            }
        }
        repositoryService.addRepositoryResourceListener(repositoryListener);
    }

    public void shutDown(IProgressMonitor monitor) throws Exception {
        pricingEnvironment.dispose();
        repositoryService.removeRepositoryResourceListener(repositoryListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ICurrencyService#addExchange(org.eclipsetrader.core.instruments.ICurrencyExchange)
     */
    @Override
    public void addExchange(ICurrencyExchange exchange) {
        exchanges.add(exchange);

        ISecurity security = (ISecurity) exchange.getAdapter(ISecurity.class);
        if (security != null) {
            pricingEnvironment.addSecurity(security);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ICurrencyService#removeExchange(org.eclipsetrader.core.instruments.ICurrencyExchange)
     */
    @Override
    public void removeExchange(ICurrencyExchange exchange) {
        ISecurity security = (ISecurity) exchange.getAdapter(ISecurity.class);
        if (security != null) {
            pricingEnvironment.removeSecurity(security);
        }

        exchanges.remove(exchange);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ICurrencyService#getAvailableCurrencies()
     */
    @Override
    public Currency[] getAvailableCurrencies() {
        Set<Currency> set = new HashSet<Currency>();
        for (ICurrencyExchange xchg : exchanges) {
            set.add(xchg.getFromCurrency());
            set.add(xchg.getToCurrency());
        }
        return set.toArray(new Currency[set.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ICurrencyService#convert(org.eclipsetrader.core.trading.Cash, java.util.Currency)
     */
    @Override
    public Cash convert(Cash cash, Currency currency) {
        if (cash.getCurrency().equals(currency)) {
            return new Cash(cash.getAmount(), currency);
        }

        for (ICurrencyExchange xchg : exchanges) {
            if (xchg.getFromCurrency().equals(cash.getCurrency()) && xchg.getToCurrency().equals(currency)) {
                double multiplier = xchg.getMultiplier() != null ? xchg.getMultiplier() : 1.0;
                ITrade trade = pricingEnvironment.getTrade((ISecurity) xchg.getAdapter(ISecurity.class));
                if (trade != null) {
                    return new Cash(cash.getAmount() * trade.getPrice() * multiplier, currency);
                }
                ILastClose lastClose = pricingEnvironment.getLastClose((ISecurity) xchg.getAdapter(ISecurity.class));
                if (lastClose != null) {
                    return new Cash(cash.getAmount() * lastClose.getPrice() * multiplier, currency);
                }
            }
        }

        for (ICurrencyExchange xchg : exchanges) {
            if (xchg.getToCurrency().equals(cash.getCurrency()) && xchg.getFromCurrency().equals(currency)) {
                double multiplier = xchg.getMultiplier() != null ? xchg.getMultiplier() : 1.0;
                ITrade trade = pricingEnvironment.getTrade((ISecurity) xchg.getAdapter(ISecurity.class));
                if (trade != null) {
                    return new Cash(cash.getAmount() / trade.getPrice() / multiplier, currency);
                }
                ILastClose lastClose = pricingEnvironment.getLastClose((ISecurity) xchg.getAdapter(ISecurity.class));
                if (lastClose != null) {
                    return new Cash(cash.getAmount() / lastClose.getPrice() / multiplier, currency);
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.ICurrencyService#convert(org.eclipsetrader.core.trading.Cash, java.util.Currency, java.util.Date)
     */
    @Override
    public Cash convert(Cash cash, Currency currency, Date date) {
        if (cash.getCurrency().equals(currency)) {
            return new Cash(cash.getAmount(), currency);
        }

        for (ICurrencyExchange xchg : exchanges) {
            if (xchg.getFromCurrency().equals(cash.getCurrency()) && xchg.getToCurrency().equals(currency)) {
                double multiplier = xchg.getMultiplier() != null ? xchg.getMultiplier() : 1.0;
                ISecurity security = (ISecurity) xchg.getAdapter(ISecurity.class);

                IHistory history = repositoryService.getHistoryFor(security);
                history = history.getSubset(date, date);
                IOHLC[] ohlc = history.getOHLC();
                if (ohlc.length != 0) {
                    return new Cash(cash.getAmount() * ohlc[0].getClose() * multiplier, currency);
                }
            }
        }

        for (ICurrencyExchange xchg : exchanges) {
            if (xchg.getToCurrency().equals(cash.getCurrency()) && xchg.getFromCurrency().equals(currency)) {
                double multiplier = xchg.getMultiplier() != null ? xchg.getMultiplier() : 1.0;
                ISecurity security = (ISecurity) xchg.getAdapter(ISecurity.class);

                IHistory history = repositoryService.getHistoryFor(security);
                history = history.getSubset(date, date);
                IOHLC[] ohlc = history.getOHLC();
                if (ohlc.length != 0) {
                    return new Cash(cash.getAmount() / ohlc[0].getClose() / multiplier, currency);
                }
            }
        }

        return null;
    }
}
