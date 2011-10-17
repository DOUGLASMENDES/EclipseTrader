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

package org.eclipsetrader.core.ats.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.feed.Trade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.ats.TradingSystem;
import org.eclipsetrader.core.repositories.IRepositoryService;

public class SimulationRunner {

    private final IRepositoryService repositoryService;
    private final IScriptStrategy strategy;

    private Date begin;
    private Date end;
    private SimulationReport report;

    private class BarData implements Comparable<BarData> {

        ISecurity security;
        TimeSpan timeSpan;
        IOHLC bar;

        public BarData(ISecurity security, TimeSpan timeSpan, IOHLC bar) {
            this.security = security;
            this.timeSpan = timeSpan;
            this.bar = bar;
        }

        @Override
        public int compareTo(BarData o) {
            return bar.getDate().compareTo(o.bar.getDate());
        }
    }

    public SimulationRunner(IRepositoryService repositoryService, IScriptStrategy strategy, Date begin, Date end) {
        this.repositoryService = repositoryService;
        this.strategy = strategy;
        this.begin = begin;
        this.end = end;
    }

    public void runWithProgress(IProgressMonitor monitor) throws Exception {
        TimeSpan[] barsTimeSpan = strategy.getBarsTimeSpan();
        if (barsTimeSpan == null || barsTimeSpan.length == 0) {
            barsTimeSpan = new TimeSpan[] {
                TimeSpan.days(1)
            };
        }

        List<BarData> dataSet = new ArrayList<BarData>();
        for (ISecurity security : strategy.getInstruments()) {
            if (monitor.isCanceled()) {
                return;
            }
            IHistory history = repositoryService.getHistoryFor(security);
            for (TimeSpan timeSpan : barsTimeSpan) {
                IHistory subHistory = history.getSubset(begin, end, timeSpan);
                for (IOHLC ohlc : subHistory.getOHLC()) {
                    dataSet.add(new BarData(security, timeSpan, ohlc));
                }
            }
        }
        Collections.sort(dataSet);

        PricingEnvironment pricingEnvironment = new PricingEnvironment();
        Broker broker = new Broker(pricingEnvironment);
        broker.connect();
        Account account = new Account();

        SimulationContext context = new SimulationContext(broker, account, pricingEnvironment);

        report = new SimulationReport(strategy, context, begin, end);

        TradingSystem tradingSystem = new TradingSystem(strategy);
        tradingSystem.start(context);

        for (Iterator<BarData> iter = dataSet.iterator(); iter.hasNext() && !monitor.isCanceled();) {
            BarData data = iter.next();
            pricingEnvironment.setTrade(data.security, new Trade(data.bar.getDate(), data.bar.getOpen(), 0L, 0L));
            pricingEnvironment.setBarOpen(data.security, new BarOpen(data.bar.getDate(), data.timeSpan, data.bar.getOpen()));
            pricingEnvironment.setTrade(data.security, new Trade(data.bar.getDate(), data.bar.getClose(), 0L, 0L));
            pricingEnvironment.setBar(data.security, new Bar(data.bar.getDate(), data.timeSpan, data.bar.getOpen(), data.bar.getHigh(), data.bar.getLow(), data.bar.getClose(), data.bar.getVolume()));
        }

        tradingSystem.stop();
        broker.disconnect();

        context.dispose();
    }

    public SimulationReport getReport() {
        return report;
    }
}
