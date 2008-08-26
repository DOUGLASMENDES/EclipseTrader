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

package org.eclipsetrader.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.feed.FeedService;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.internal.repositories.RepositoryService;
import org.eclipsetrader.core.internal.trading.CurrencyService;
import org.eclipsetrader.core.internal.trading.TradingService;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.ICurrencyService;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoreActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipsetrader.core";

	// The extension points IDs
	public static final String ELEMENT_FACTORY_ID = "org.eclipsetrader.core.elementFactories";
	public static final String REPOSITORY_ID = "org.eclipsetrader.core.repositories";
	public static final String PROVIDERS_FACTORY_ID = "org.eclipsetrader.core.providers";
	public static final String BROKERS_EXTENSION_ID = "org.eclipsetrader.core.brokers";

	// Preferences IDs
	public static final String DEFAULT_CONNECTOR_ID = "DEFAULT_CONNECTOR";

	// The shared instance
	private static CoreActivator plugin;

	private Map<String, IRepositoryElementFactory> elementFactoryMap = new HashMap<String, IRepositoryElementFactory>();
	private Map<String, IDataProviderFactory> providersFactoryMap = new HashMap<String, IDataProviderFactory>();

	/**
	 * The constructor
	 */
	public CoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		RepositoryService repositoryService = new RepositoryService();
		context.registerService(new String[] { IRepositoryService.class.getName(), RepositoryService.class.getName() }, repositoryService, new Hashtable<Object,Object>());
		repositoryService.startUp();

		FeedService feedService = new FeedService();
		context.registerService(new String[] { IFeedService.class.getName(), FeedService.class.getName() }, feedService, new Hashtable<Object,Object>());
		feedService.startUp();

		TradingService tradingService = new TradingService();
		context.registerService(
				new String[] {
						ITradingService.class.getName(),
						TradingService.class.getName()
					},
				tradingService,
				new Hashtable<Object,Object>()
			);
		tradingService.startUp();

		MarketService marketService = new MarketService();
		context.registerService(new String[] { IMarketService.class.getName(), MarketService.class.getName() }, marketService, new Hashtable<Object,Object>());
		marketService.startUp(null);

		CurrencyService currencyService = new CurrencyService(repositoryService, (MarketPricingEnvironment) marketService.getPricingEnvironment());
		context.registerService(
				new String[] {
						ICurrencyService.class.getName(),
						CurrencyService.class.getName()
					},
				currencyService,
				new Hashtable<Object,Object>()
			);

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
				try {
					IDataProviderFactory factory = (IDataProviderFactory) configElements[j].createExecutableExtension("class");
					providersFactoryMap.put(strID, factory);
				} catch (Exception e) {
					Status status = new Status(Status.WARNING, PLUGIN_ID, 0, "Unable to create data provider factory with id " + strID, e);
					getLog().log(status);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		ServiceReference serviceReference = context.getServiceReference(TradingService.class.getName());
		if (serviceReference != null) {
			TradingService service = (TradingService) context.getService(serviceReference);
			if (service != null)
				service.shutDown();
			context.ungetService(serviceReference);
		}

		serviceReference = context.getServiceReference(MarketService.class.getName());
		if (serviceReference != null) {
			MarketService service = (MarketService) context.getService(serviceReference);
			if (service != null)
				service.shutDown(null);
			context.ungetService(serviceReference);
		}

		serviceReference = context.getServiceReference(FeedService.class.getName());
		if (serviceReference != null) {
			FeedService service = (FeedService) context.getService(serviceReference);
			if (service != null)
				service.shutDown();
			context.ungetService(serviceReference);
		}

		serviceReference = context.getServiceReference(RepositoryService.class.getName());
		if (serviceReference != null) {
			RepositoryService service = (RepositoryService) context.getService(serviceReference);
			if (service != null)
				service.shutDown();
			context.ungetService(serviceReference);
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CoreActivator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		if (plugin != null)
			plugin.getLog().log(status);
	}

	public IRepositoryElementFactory getElementFactory(String targetID) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ELEMENT_FACTORY_ID);
		if (extensionPoint == null)
			return null;

		synchronized(elementFactoryMap) {
			IRepositoryElementFactory factory = elementFactoryMap.get(targetID);

			if (factory == null) {
				IConfigurationElement targetElement = null;
				IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (targetID.equals(strID)) {
						targetElement = configElements[j];
						break;
					}
				}
				if (targetElement == null)
					return null;

				try {
					factory = (IRepositoryElementFactory) targetElement.createExecutableExtension("class");
					elementFactoryMap.put(targetID, factory);
				} catch (Exception e) {
					Status status = new Status(Status.WARNING, PLUGIN_ID, 0, "Unable to create factory with id " + targetID, e);
					getLog().log(status);
				}
			}

			return factory;
		}
	}

	public IDataProviderFactory getDataProviderFactory(String targetID) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
		if (extensionPoint == null)
			return null;

		synchronized(providersFactoryMap) {
			IDataProviderFactory factory = providersFactoryMap.get(targetID);

			if (factory == null) {
				IConfigurationElement targetElement = null;
				IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (targetID.equals(strID)) {
						targetElement = configElements[j];
						break;
					}
				}
				if (targetElement == null)
					return null;

				try {
					factory = (IDataProviderFactory) targetElement.createExecutableExtension("class");
					providersFactoryMap.put(targetID, factory);
				} catch (Exception e) {
					Status status = new Status(Status.WARNING, PLUGIN_ID, 0, "Unable to create data provider factory with id " + targetID, e);
					getLog().log(status);
				}
			}

			return factory;
		}
	}

	public IDataProviderFactory[] getDataProviderFactories() {
		synchronized(providersFactoryMap) {
			Collection<IDataProviderFactory> c = providersFactoryMap.values();
			return c.toArray(new IDataProviderFactory[c.size()]);
		}
	}

	public IFeedConnector getDefaultConnector() {
		BundleContext context = getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(FeedService.class.getName());
		if (serviceReference != null) {
			FeedService service = (FeedService) context.getService(serviceReference);
			context.ungetService(serviceReference);
			if (service != null)
				return service.getConnector(getPluginPreferences().getString(DEFAULT_CONNECTOR_ID));
		}
		return null;
	}
}
