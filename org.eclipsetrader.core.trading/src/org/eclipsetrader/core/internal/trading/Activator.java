package org.eclipsetrader.core.internal.trading;

import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.ats.ITradeSystem;
import org.eclipsetrader.core.ats.ITradeSystemService;
import org.eclipsetrader.core.internal.ats.TradeSystemService;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.TradingServiceSchedulingRule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {
	public static final String PLUGIN_ID = "org.eclipsetrader.core.trading";

	public static final String BROKERS_EXTENSION_ID = "org.eclipsetrader.core.brokers";
	public static final String STRATEGIES_EXTENSION_ID = "org.eclipsetrader.core.trading.systems";

	private static Activator plugin;

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

		final TradingService tradingService = new TradingService();
		context.registerService(
				new String[] {
						ITradingService.class.getName(),
						TradingService.class.getName()
					},
				tradingService,
				new Hashtable<Object,Object>()
			);

		final TradeSystemService tradeSystemService = new TradeSystemService();
		context.registerService(
				new String[] {
						ITradeSystemService.class.getName(),
						TradeSystemService.class.getName()
					},
					tradeSystemService,
				new Hashtable<Object,Object>()
			);

		Platform.getAdapterManager().registerAdapters(tradeSystemService, ITradeSystem.class);

		Job job = new Job("Trading Service Startup") {
            @Override
            public IStatus run(IProgressMonitor monitor) {
        		tradingService.startUp();
        		tradeSystemService.startUp();
	            return Status.OK_STATUS;
            }
		};
		job.setRule(new TradingServiceSchedulingRule());
		job.schedule(1000);
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

		serviceReference = context.getServiceReference(TradingService.class.getName());
		if (serviceReference != null) {
			TradingService service = (TradingService) context.getService(serviceReference);
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
