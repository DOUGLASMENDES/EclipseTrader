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

import java.util.ArrayList;
import java.util.List;

import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ITradingSystemContext;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

public class TradingSystemContext implements ITradingSystemContext {

    private final IBroker broker;
    private final IAccount account;
    private final PricingEnvironment pricingEnvironment;

    private final MarketPricingEnvironment marketPricingEnvironment;
    private final List<BarFactory> barFactory = new ArrayList<BarFactory>();

    public IBarFactoryListener barFactoryListener = new IBarFactoryListener() {

        @Override
        public void barOpen(BarFactoryEvent event) {
            pricingEnvironment.setBarOpen(event.security, new BarOpen(event.date, event.timeSpan, event.open));
        }

        @Override
        public void barClose(BarFactoryEvent event) {
            Bar bar = new Bar(event.date, event.timeSpan, event.open, event.high, event.low, event.close, event.volume);
            pricingEnvironment.setBar(event.security, bar);
        }
    };

    public TradingSystemContext(IStrategy strategy, IBroker broker, IAccount account, IMarketService marketService) {
        this.broker = broker;
        this.account = account;
        this.pricingEnvironment = new PricingEnvironment();

        marketPricingEnvironment = new MarketPricingEnvironment(marketService);
        marketPricingEnvironment.addSecurities(strategy.getInstruments());

        for (ISecurity security : strategy.getInstruments()) {
            for (TimeSpan timeSpan : strategy.getBarsTimeSpan()) {
                BarFactory factory = new BarFactory(security, timeSpan, marketPricingEnvironment);
                factory.addBarFactoryListener(barFactoryListener);
                barFactory.add(factory);
            }
        }
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
        marketPricingEnvironment.dispose();
    }
}
