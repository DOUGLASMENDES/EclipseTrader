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

import java.net.URI;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.CurrencyExchange;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.core.views.IWatchList;

public class CurrencyServiceTest extends TestCase {

    private CurrencyExchange eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 1.0);
    private IOHLC[] history = new IOHLC[] {
            new OHLC(new Date(1000000), 1.4677, 1.4677, 1.4677, 1.4677, 0L),
            new OHLC(new Date(2000000), 1.4593, 1.4593, 1.4593, 1.4593, 0L),
    };

    private class TestRepositoryService implements IRepositoryService {

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#addRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
         */
        @Override
        public void addRepositoryResourceListener(IRepositoryChangeListener listener) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#deleteAdaptable(org.eclipse.core.runtime.IAdaptable[])
         */
        @Override
        public void deleteAdaptable(IAdaptable[] adaptables) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getFeedIdentifierFromSymbol(java.lang.String)
         */
        @Override
        public IFeedIdentifier getFeedIdentifierFromSymbol(String symbol) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getFeedIdentifiers()
         */
        @Override
        public IFeedIdentifier[] getFeedIdentifiers() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getHistoryFor(org.eclipsetrader.core.instruments.ISecurity)
         */
        @Override
        public IHistory getHistoryFor(ISecurity security) {
            if (security == eurusd) {
                return new History(security, history);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getRepositories()
         */
        @Override
        public IRepository[] getRepositories() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getRepository(java.lang.String)
         */
        @Override
        public IRepository getRepository(String scheme) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurities()
         */
        @Override
        public ISecurity[] getSecurities() {
            return new ISecurity[] {
                eurusd
            };
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurityFromName(java.lang.String)
         */
        @Override
        public ISecurity getSecurityFromName(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getSecurityFromURI(java.net.URI)
         */
        @Override
        public ISecurity getSecurityFromURI(URI uri) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromName(java.lang.String)
         */
        @Override
        public IWatchList getWatchListFromName(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchListFromURI(java.net.URI)
         */
        @Override
        public IWatchList getWatchListFromURI(URI uri) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getWatchLists()
         */
        @Override
        public IWatchList[] getWatchLists() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getTrades()
         */
        @Override
        public IHolding[] getTrades() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#moveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
         */
        @Override
        public void moveAdaptable(IAdaptable[] adaptables, IRepository repository) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#removeRepositoryResourceListener(org.eclipsetrader.core.repositories.IRepositoryChangeListener)
         */
        @Override
        public void removeRepositoryResourceListener(IRepositoryChangeListener listener) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInService(IRepositoryRunnable runnable, IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#runInService(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInService(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[])
         */
        @Override
        public void saveAdaptable(IAdaptable[] adaptables) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#saveAdaptable(org.eclipse.core.runtime.IAdaptable[], org.eclipsetrader.core.repositories.IRepository)
         */
        @Override
        public void saveAdaptable(IAdaptable[] adaptables, IRepository defaultRepository) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getObjectFromURI(java.net.URI)
         */
        @Override
        public IStoreObject getObjectFromURI(URI uri) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IRepositoryService#getAllObjects()
         */
        @Override
        public IStoreObject[] getAllObjects() {
            return null;
        }
    };

    private MarketPricingEnvironment pricingEnvironment = new MarketPricingEnvironment() {

        @Override
        public void addSecurity(ISecurity security) {
        }

        @Override
        public ITrade getTrade(ISecurity security) {
            if (security == eurusd) {
                return new Trade(null, 1.4677, null, null);
            }
            return null;
        }
    };

    public void testGetAvailableCurrencies() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Set<Currency> set = new HashSet<Currency>(Arrays.asList(service.getAvailableCurrencies()));
        assertTrue(set.contains(Currency.getInstance("EUR")));
        assertTrue(set.contains(Currency.getInstance("USD")));
        assertFalse(set.contains(Currency.getInstance("GBP")));
    }

    public void testDirectConvert() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"));
        assertEquals(1.4677, cash.getAmount());
        assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    public void testInverseConvert() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.4677, Currency.getInstance("USD")), Currency.getInstance("EUR"));
        assertEquals(1.0, cash.getAmount());
        assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    public void testConvertToUnknownCurrency() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("GBP"));
        assertNull(cash);
    }

    public void testConvertFromUnknownCurrency() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("GBP")), Currency.getInstance("USD"));
        assertNull(cash);
    }

    public void testConvertToSameCurrency() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("EUR"));
        assertEquals(1.0, cash.getAmount());
        assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    public void testDirectConvertWithMultiplier() throws Exception {
        eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100.0);
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"));
        assertEquals(146.77, cash.getAmount());
        assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    public void testInverseConvertWithMultiplier() throws Exception {
        eurusd = new CurrencyExchange(Currency.getInstance("EUR"), Currency.getInstance("USD"), 100.0);
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(146.77, Currency.getInstance("USD")), Currency.getInstance("EUR"));
        assertEquals(1.0, cash.getAmount());
        assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    public void testDirectConvertToDate() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"), new Date(2000000));
        assertEquals(1.4593, cash.getAmount());
        assertEquals(Currency.getInstance("USD"), cash.getCurrency());
    }

    public void testInverseConvertToDate() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.4593, Currency.getInstance("USD")), Currency.getInstance("EUR"), new Date(2000000));
        assertEquals(1.0, cash.getAmount());
        assertEquals(Currency.getInstance("EUR"), cash.getCurrency());
    }

    public void testConvertToUnknownDate() throws Exception {
        CurrencyService service = new CurrencyService(new TestRepositoryService(), pricingEnvironment);
        service.startUp(null);
        Cash cash = service.convert(new Cash(1.0, Currency.getInstance("EUR")), Currency.getInstance("USD"), new Date(3000000));
        assertNull(cash);
    }
}
