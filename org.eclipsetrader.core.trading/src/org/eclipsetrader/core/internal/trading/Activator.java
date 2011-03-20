/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.trading;

import java.util.Hashtable;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipsetrader.core.ats.ITradeSystem;
import org.eclipsetrader.core.ats.ITradeSystemService;
import org.eclipsetrader.core.internal.ats.TradeSystemService;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.trading.IAlertService;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends Plugin {
	public static final String PLUGIN_ID = "org.eclipsetrader.core.trading";

	public static final String BROKERS_EXTENSION_ID = "org.eclipsetrader.core.trading.brokers";
	public static final String ALERTS_EXTENSION_ID = "org.eclipsetrader.core.trading.alerts";
	public static final String STRATEGIES_EXTENSION_ID = "org.eclipsetrader.core.trading.systems";

	private static Activator plugin;

	private TradingServiceFactory tradingServiceFactory;
	private ServiceRegistration tradingServiceRegistration;
	private AlertService alertService;
	private ServiceRegistration alertServiceRegistration;

	private MarketBrokerAdapterFactory marketBrokerFactory;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		marketBrokerFactory = new MarketBrokerAdapterFactory(getStateLocation().append("market_brokers.xml").toFile());
		AdapterManager.getDefault().registerAdapters(marketBrokerFactory, IMarket.class);

		tradingServiceFactory = new TradingServiceFactory();
		tradingServiceRegistration = context.registerService(new String[] {
		    ITradingService.class.getName(), TradingService.class.getName()
		}, tradingServiceFactory, new Hashtable<Object, Object>());

		final TradeSystemService tradeSystemService = new TradeSystemService();
		context.registerService(new String[] {
		    ITradeSystemService.class.getName(), TradeSystemService.class.getName()
		}, tradeSystemService, new Hashtable<Object, Object>());
		tradeSystemService.startUp();

		Platform.getAdapterManager().registerAdapters(tradeSystemService, ITradeSystem.class);

		alertService = new AlertService();
		alertServiceRegistration = context.registerService(new String[] {
		    IAlertService.class.getName(), AlertService.class.getName()
		}, alertService, new Hashtable<Object, Object>());
		alertService.startUp();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		ServiceReference serviceReference = context.getServiceReference(TradeSystemService.class.getName());
		if (serviceReference != null) {
			TradeSystemService service = (TradeSystemService) context.getService(serviceReference);
			if (service != null)
				service.shutDown();
			context.ungetService(serviceReference);
		}

		alertServiceRegistration.unregister();
		alertService.shutDown();

		if (tradingServiceRegistration != null)
			tradingServiceRegistration.unregister();
		if (tradingServiceFactory != null)
			tradingServiceFactory.dispose();

		marketBrokerFactory.save(getStateLocation().append("market_brokers.xml").toFile());

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		if (plugin == null) {
			if (status.getException() != null)
				status.getException().printStackTrace();
			return;
		}
		plugin.getLog().log(status);
	}
}
