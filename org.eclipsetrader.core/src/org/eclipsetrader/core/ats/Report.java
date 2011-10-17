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

package org.eclipsetrader.core.ats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.ITransaction;

public class Report {

    private final IStrategy strategy;
    private final ITradingSystemContext context;

    private final List<EquityData> equityData = new ArrayList<EquityData>();
    private final Map<ISecurity, List<IBar>> barsData = new HashMap<ISecurity, List<IBar>>();

    private class EquityData implements IAdaptable {

        final Date date;
        final Double amount;

        public EquityData(Date date, Double amount) {
            this.date = date;
            this.amount = amount;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(Date.class)) {
                return date;
            }
            if (adapter.isAssignableFrom(Double.class)) {
                return amount;
            }
            if (adapter.isAssignableFrom(getClass())) {
                return this;
            }
            return null;
        }
    }

    private final IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            List<IBar> bars = barsData.get(event.getSecurity());
            if (bars == null) {
                bars = new ArrayList<IBar>();
                barsData.put(event.getSecurity(), bars);
            }
            for (PricingDelta delta : event.getDelta()) {
                if (!(delta.getNewValue() instanceof IBar)) {
                    continue;
                }

                bars.add((IBar) delta.getNewValue());

                double amount = calculateCurrentEquity();
                if (amount == 0.0) {
                    continue;
                }
                if (equityData.size() != 0) {
                    EquityData lastData = equityData.get(equityData.size() - 1);
                    if (lastData.amount == amount) {
                        continue;
                    }
                }
                equityData.add(new EquityData(((IBar) delta.getNewValue()).getDate(), amount));
            }
        }
    };

    public Report(IStrategy strategy, ITradingSystemContext context) {
        this.strategy = strategy;
        this.context = context;

        context.getPricingEnvironment().addPricingListener(pricingListener);
    }

    public void dispose() {
        context.getPricingEnvironment().removePricingListener(pricingListener);
        equityData.clear();
    }

    public IStrategy getStrategy() {
        return strategy;
    }

    public DataSeries getEquityData() {
        DataSeries result = new DataSeries("Performance", equityData.toArray(new IAdaptable[equityData.size()]));
        result.setHighest(result.getHighest());
        result.setLowest(result.getLowest());
        return result;
    }

    double calculateCurrentEquity() {
        double result = context.getAccount().getBalance().getAmount();

        for (IPosition position : context.getAccount().getPositions()) {
            ITrade trade = context.getPricingEnvironment().getTrade(position.getSecurity());
            result += position.getQuantity() * trade.getPrice();
        }

        return result;
    }

    public List<ITransaction> getTradesData() {
        return Arrays.asList(context.getAccount().getTransactions());
    }

    public Map<ISecurity, List<IBar>> getBarsData() {
        return barsData;
    }
}
