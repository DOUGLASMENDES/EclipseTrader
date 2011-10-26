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

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.ICurrencyService;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.ats.TradingSystemService;
import org.eclipsetrader.core.internal.ats.TradingSystemServiceFactory;
import org.eclipsetrader.core.internal.feed.ConnectorOverrideAdapter;
import org.eclipsetrader.core.internal.feed.FeedService;
import org.eclipsetrader.core.internal.feed.FeedServiceFactory;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.internal.markets.MarketServiceFactory;
import org.eclipsetrader.core.internal.repositories.RepositoryService;
import org.eclipsetrader.core.internal.trading.AlertService;
import org.eclipsetrader.core.internal.trading.MarketBrokerAdapterFactory;
import org.eclipsetrader.core.internal.trading.TradingService;
import org.eclipsetrader.core.internal.trading.TradingServiceFactory;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryElementFactory;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IAlertService;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class CoreActivator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.core";

    // The extension points IDs
    public static final String ELEMENT_FACTORY_ID = "org.eclipsetrader.core.elementFactories";
    public static final String REPOSITORY_ID = "org.eclipsetrader.core.repositories";
    public static final String PROVIDERS_FACTORY_ID = "org.eclipsetrader.core.providers";
    public static final String BROKERS_EXTENSION_ID = "org.eclipsetrader.core.trading.brokers";
    public static final String ALERTS_EXTENSION_ID = "org.eclipsetrader.core.trading.alerts";
    public static final String STRATEGIES_EXTENSION_ID = "org.eclipsetrader.core.ats.systems";
    public static final String SCRIPTS_EXTENSION_ID = "org.eclipsetrader.core.ats.javascript";

    // Preferences IDs
    public static final String DEFAULT_CONNECTOR_ID = "DEFAULT_CONNECTOR";
    public static final String DEFAULT_BACKFILL_CONNECTOR_ID = "DEFAULT_BACKFILL_CONNECTOR";

    // The shared instance
    private static CoreActivator plugin;

    private RepositoryService repositoryService;
    private ServiceRegistration repositoryServiceRegistration;

    private FeedServiceFactory feedServiceFactory;
    private ServiceRegistration feedServiceRegistration;
    private MarketServiceFactory marketServiceFactory;
    private ServiceRegistration marketServiceRegistration;
    private CurrencyServiceFactory currencyServiceFactory;
    private ServiceRegistration currencyServiceRegistration;
    private TradingServiceFactory tradingServiceFactory;
    private ServiceRegistration tradingServiceRegistration;
    private AlertService alertService;
    private ServiceRegistration alertServiceRegistration;

    private MarketBrokerAdapterFactory marketBrokerFactory;
    private TradingSystemServiceFactory tradingSystemServiceFactory;
    private ServiceRegistration tradingSystemServiceRegistration;

    private Map<String, IRepositoryElementFactory> elementFactoryMap = new HashMap<String, IRepositoryElementFactory>();
    private Map<String, IDataProviderFactory> providersFactoryMap = new HashMap<String, IDataProviderFactory>();

    private ConnectorOverrideAdapter overrideAdapter;

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

        repositoryService = new RepositoryService();
        repositoryServiceRegistration = context.registerService(new String[] {
            IRepositoryService.class.getName(), RepositoryService.class.getName()
        }, repositoryService, new Hashtable<String, Object>());
        repositoryService.startUp();

        feedServiceFactory = new FeedServiceFactory();
        feedServiceRegistration = context.registerService(new String[] {
            IFeedService.class.getName(), FeedService.class.getName()
        }, feedServiceFactory, new Hashtable<String, Object>());

        marketServiceFactory = new MarketServiceFactory();
        marketServiceRegistration = context.registerService(new String[] {
            IMarketService.class.getName(), MarketService.class.getName()
        }, marketServiceFactory, new Hashtable<String, Object>());

        currencyServiceFactory = new CurrencyServiceFactory();
        currencyServiceRegistration = context.registerService(new String[] {
            ICurrencyService.class.getName(),
            CurrencyService.class.getName()
        }, currencyServiceFactory, new Hashtable<String, Object>());

        marketBrokerFactory = new MarketBrokerAdapterFactory(getStateLocation().append("market_brokers.xml").toFile());
        AdapterManager.getDefault().registerAdapters(marketBrokerFactory, IMarket.class);

        tradingServiceFactory = new TradingServiceFactory();
        tradingServiceRegistration = context.registerService(new String[] {
            ITradingService.class.getName(), TradingService.class.getName()
        }, tradingServiceFactory, new Hashtable<String, Object>());

        alertService = new AlertService();
        alertServiceRegistration = context.registerService(new String[] {
            IAlertService.class.getName(), AlertService.class.getName()
        }, alertService, new Hashtable<String, Object>());
        alertService.startUp();

        tradingSystemServiceFactory = new TradingSystemServiceFactory(repositoryService);
        tradingSystemServiceRegistration = context.registerService(new String[] {
            ITradingSystemService.class.getName(), TradingSystemService.class.getName()
        }, tradingSystemServiceFactory, new Hashtable<String, Object>());

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
                try {
                    IDataProviderFactory factory = (IDataProviderFactory) configElements[j].createExecutableExtension("class");
                    providersFactoryMap.put(strID, factory);
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Unable to create data provider factory with id " + strID, e);
                    getLog().log(status);
                }
            }
        }

        try {
            overrideAdapter = new ConnectorOverrideAdapter(getStateLocation().append("overrides.xml").toFile());
            AdapterManager.getDefault().registerAdapters(overrideAdapter, ISecurity.class);
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error reading override settings", e); //$NON-NLS-1$
            getLog().log(status);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        tradingSystemServiceRegistration.unregister();
        tradingSystemServiceFactory.dispose();

        alertServiceRegistration.unregister();
        alertService.shutDown();

        tradingServiceRegistration.unregister();
        tradingServiceFactory.dispose();

        marketBrokerFactory.save(getStateLocation().append("market_brokers.xml").toFile());

        try {
            if (overrideAdapter != null) {
                overrideAdapter.save(getStateLocation().append("overrides.xml").toFile());
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, "Error saving override settings", e); //$NON-NLS-1$
            getLog().log(status);
        }

        currencyServiceRegistration.unregister();
        currencyServiceFactory.dispose();

        marketServiceRegistration.unregister();
        marketServiceFactory.dispose();

        feedServiceRegistration.unregister();
        feedServiceFactory.dispose();

        repositoryServiceRegistration.unregister();
        repositoryService.shutDown();

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
        if (plugin == null) {
            if (status.getException() != null) {
                status.getException().printStackTrace();
            }
            return;
        }
        plugin.getLog().log(status);
    }

    public static void log(String message, Throwable throwable) {
        Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
        if (plugin == null) {
            if (status.getException() != null) {
                status.getException().printStackTrace();
            }
            return;
        }
        plugin.getLog().log(status);
    }

    public IRepositoryElementFactory getElementFactory(String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ELEMENT_FACTORY_ID);
        if (extensionPoint == null) {
            return null;
        }

        synchronized (elementFactoryMap) {
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
                if (targetElement == null) {
                    return null;
                }

                try {
                    factory = (IRepositoryElementFactory) targetElement.createExecutableExtension("class");
                    elementFactoryMap.put(targetID, factory);
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Unable to create factory with id " + targetID, e);
                    getLog().log(status);
                }
            }

            return factory;
        }
    }

    public IDataProviderFactory getDataProviderFactory(String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_FACTORY_ID);
        if (extensionPoint == null) {
            return null;
        }

        synchronized (providersFactoryMap) {
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
                if (targetElement == null) {
                    return null;
                }

                try {
                    factory = (IDataProviderFactory) targetElement.createExecutableExtension("class");
                    providersFactoryMap.put(targetID, factory);
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, "Unable to create data provider factory with id " + targetID, e);
                    getLog().log(status);
                }
            }

            return factory;
        }
    }

    public IDataProviderFactory[] getDataProviderFactories() {
        synchronized (providersFactoryMap) {
            Collection<IDataProviderFactory> c = providersFactoryMap.values();
            return c.toArray(new IDataProviderFactory[c.size()]);
        }
    }

    public IFeedConnector getDefaultConnector() {
        IFeedConnector connector = null;

        BundleContext context = getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
        if (serviceReference != null) {
            IFeedService service = (IFeedService) context.getService(serviceReference);
            if (service != null) {
                connector = service.getConnector(getPluginPreferences().getString(DEFAULT_CONNECTOR_ID));
            }
            context.ungetService(serviceReference);
        }

        return connector;
    }

    public IBackfillConnector getDefaultBackfillConnector() {
        IBackfillConnector connector = null;

        BundleContext context = getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
        if (serviceReference != null) {
            IFeedService service = (IFeedService) context.getService(serviceReference);
            if (service != null) {
                connector = service.getBackfillConnector(getPluginPreferences().getString(DEFAULT_BACKFILL_CONNECTOR_ID));
            }
            context.ungetService(serviceReference);
        }

        return connector;
    }
}
