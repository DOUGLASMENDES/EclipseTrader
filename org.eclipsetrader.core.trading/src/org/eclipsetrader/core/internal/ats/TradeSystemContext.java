/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.ats;

import org.eclipsetrader.core.ats.IBarFactory;
import org.eclipsetrader.core.ats.ITradeStrategy;
import org.eclipsetrader.core.ats.ITradeStrategyParameters;
import org.eclipsetrader.core.ats.ITradeSystem;
import org.eclipsetrader.core.ats.ITradeSystemContext;
import org.eclipsetrader.core.ats.ITradeSystemMonitor;
import org.eclipsetrader.core.ats.TradeStrategyParameters;
import org.eclipsetrader.core.ats.TradeSystemEvent;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.trading.Activator;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradeSystemContext implements ITradeSystemContext {
	private TradeSystemService service;
	private ITradeSystem system;

	private boolean active;
	private ISecurity instrument;
	private TimeSpan timeSpan;
	private ITradeStrategy strategy;

	private TradeStrategyParameters strategyParameters;

	private IBroker broker;
	private IMarketService marketService;
	private MarketPricingEnvironment pricingEnvironment;
	private IBarFactory barFactory;

	private ITradeSystemMonitor strategyMonitor;

	public TradeSystemContext(TradeSystemService service, ITradeSystem system) {
		this.service = service;
		this.system = system;

		this.active = system.isActive();
		this.instrument = system.getInstrument();
		this.timeSpan = system.getTimeSpan();
		this.strategy = system.getTradeStrategy();
		this.strategyParameters = new TradeStrategyParameters(system.getParameters());
	}

	public boolean isActive() {
    	return active;
    }

	public void setActive(boolean active) {
    	this.active = active;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#getSecurity()
     */
    public ISecurity getSecurity() {
	    return instrument;
    }

    public ITradeSystemMonitor start() {
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();

		ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
		if (serviceReference != null) {
			marketService = (IMarketService) context.getService(serviceReference);
			pricingEnvironment = new MarketPricingEnvironment(marketService);
			context.ungetService(serviceReference);
		}

		if (pricingEnvironment != null) {
			pricingEnvironment.addSecurity(instrument);
			barFactory = new BarFactory(instrument, timeSpan, pricingEnvironment);
		}

		strategyMonitor = strategy.start(this);

		return strategyMonitor;
    }

    public void stop() {
    	if (strategyMonitor != null) {
    		strategyMonitor.stop();
    		strategyMonitor = null;
    	}

    	if (pricingEnvironment != null) {
    		pricingEnvironment.dispose();
    		pricingEnvironment = null;
    	}

    	if (barFactory != null) {
    		barFactory.dispose();
    		barFactory = null;
    	}

    	if (service != null) {
        	TradeSystemEvent event = new TradeSystemEvent();
        	event.kind = TradeSystemEvent.KIND_STOPPED;
        	event.service = service;
        	event.tradeSystem = system;
        	event.tradeSystemContext = this;
        	service.notifyListeners(event);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#getPricingEnvironment()
     */
    public IPricingEnvironment getPricingEnvironment() {
	    return pricingEnvironment;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#getBarFactory()
     */
    public IBarFactory getBarFactory() {
	    return barFactory;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#getBroker()
     */
    public IBroker getBroker() {
	    return broker;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#getStrategyParameters()
     */
    public ITradeStrategyParameters getStrategyParameters() {
	    return strategyParameters;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#prepareOrder(org.eclipsetrader.core.trading.IOrderSide, java.lang.Long, java.lang.Double)
     */
    public IOrderMonitor prepareOrder(IOrderSide side, Long quantity, Double price) throws BrokerException {
    	Order order = new Order(null, IOrderType.Limit, side, instrument, quantity, price);
	    return broker.prepareOrder(order);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradeSystemContext#prepareOrder(org.eclipsetrader.core.trading.IOrderSide, java.lang.Long)
     */
    public IOrderMonitor prepareOrder(IOrderSide side, Long quantity) throws BrokerException {
    	Order order = new Order(null, side, instrument, quantity);
	    return broker.prepareOrder(order);
    }
}
