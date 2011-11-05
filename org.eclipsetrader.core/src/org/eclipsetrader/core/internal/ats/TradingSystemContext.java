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

package org.eclipsetrader.core.internal.ats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ITradingSystemContext;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBarOpen;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITodayOHL;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

public class TradingSystemContext implements ITradingSystemContext {

    private final IMarketService marketService;
    private final IBroker broker;
    private final IAccount account;
    private final PricingEnvironment pricingEnvironment;

    private Integer backfillSize;

    private final MarketPricingEnvironment marketPricingEnvironment;
    private final BarFactory barFactory;
    private boolean needsDailyBars;

    private Log logger = LogFactory.getLog(getClass());

    public IBarFactoryListener barFactoryListener = new IBarFactoryListener() {

        @Override
        public void barOpen(BarFactoryEvent event) {
            BarOpen barOpen = new BarOpen(event.date, event.timeSpan, event.open);
            logger.info(barOpen);
            pricingEnvironment.setBarOpen(event.security, barOpen);
        }

        @Override
        public void barClose(BarFactoryEvent event) {
            Bar bar = new Bar(event.date, event.timeSpan, event.open, event.high, event.low, event.close, event.volume);
            logger.info(bar);
            pricingEnvironment.setBar(event.security, bar);
        }
    };

    private final IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            IMarket market = marketService.getMarketForSecurity(event.getSecurity());
            if (market != null) {
                IMarketDay day = market.getToday();
                if (day != null && !day.isOpen()) {
                    return;
                }
            }
            for (PricingDelta delta : event.getDelta()) {
                if (delta.getNewValue() instanceof ITrade) {
                    pricingEnvironment.setTrade(event.getSecurity(), (ITrade) delta.getNewValue());
                }
                if (delta.getNewValue() instanceof IQuote) {
                    pricingEnvironment.setQuote(event.getSecurity(), (IQuote) delta.getNewValue());
                }
                if (delta.getNewValue() instanceof ITodayOHL) {
                    pricingEnvironment.setTodayOHL(event.getSecurity(), (ITodayOHL) delta.getNewValue());
                }
                if (delta.getNewValue() instanceof ILastClose) {
                    pricingEnvironment.setLastClose(event.getSecurity(), (ILastClose) delta.getNewValue());
                }
                if (delta.getNewValue() instanceof IBook) {
                    pricingEnvironment.setBook(event.getSecurity(), (IBook) delta.getNewValue());
                }
                if (needsDailyBars) {
                    if (delta.getNewValue() instanceof IBarOpen) {
                        IBarOpen bar = (IBarOpen) delta.getNewValue();
                        if (bar.getTimeSpan().getUnits() == TimeSpan.Units.Days && bar.getTimeSpan().getLength() == 1) {
                            pricingEnvironment.setBarOpen(event.getSecurity(), bar);
                        }
                    }
                    if (delta.getNewValue() instanceof IBar) {
                        IBar bar = (IBar) delta.getNewValue();
                        if (bar.getTimeSpan().getUnits() == TimeSpan.Units.Days && bar.getTimeSpan().getLength() == 1) {
                            pricingEnvironment.setBar(event.getSecurity(), bar);
                        }
                    }
                }
            }
            barFactory.pricingUpdate(event);
        }
    };

    public TradingSystemContext(IMarketService marketService, IStrategy strategy, IBroker broker, IAccount account) {
        this.marketService = marketService;
        this.broker = broker;
        this.account = account;

        pricingEnvironment = new PricingEnvironment();

        marketPricingEnvironment = new MarketPricingEnvironment(marketService);
        marketPricingEnvironment.addSecurities(strategy.getInstruments());
        marketPricingEnvironment.addPricingListener(pricingListener);

        barFactory = new BarFactory();
        for (ISecurity security : strategy.getInstruments()) {
            for (TimeSpan timeSpan : strategy.getBarsTimeSpan()) {
                if (timeSpan.getUnits() == TimeSpan.Units.Days && timeSpan.getLength() == 1) {
                    needsDailyBars = true;
                    continue;
                }
                if (timeSpan.getUnits() == TimeSpan.Units.Minutes) {
                    barFactory.add(security, timeSpan);
                }
            }
        }
        barFactory.addBarFactoryListener(barFactoryListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getBroker()
     */
    @Override
    public IBroker getBroker() {
        return broker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getAccount()
     */
    @Override
    public IAccount getAccount() {
        return account;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getPricingEnvironment()
     */
    @Override
    public IPricingEnvironment getPricingEnvironment() {
        return pricingEnvironment;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#dispose()
     */
    @Override
    public void dispose() {
        barFactory.dispose();
        marketPricingEnvironment.dispose();
    }

    public void setInitialBackfillSize(Integer backfillSize) {
        this.backfillSize = backfillSize;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getInitialBackfillSize()
     */
    @Override
    public int getInitialBackfillSize() {
        return backfillSize != null ? backfillSize.intValue() : 0;
    }
}
