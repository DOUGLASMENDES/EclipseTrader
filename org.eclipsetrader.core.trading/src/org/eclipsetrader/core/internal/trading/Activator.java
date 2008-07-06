package org.eclipsetrader.core.internal.trading;

import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.Order;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipsetrader.core.trading";

	public static final String BROKERS_EXTENSION_ID = "org.eclipsetrader.core.brokers";

	// The shared instance
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

    	IAdapterManager adapterManager = Platform.getAdapterManager();
    	adapterManager.registerAdapters(new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Object adaptableObject, Class adapterType) {
            	if (adaptableObject instanceof Order)
            		return ((Order) adaptableObject).getAdapter(adapterType);
	            return null;
            }

            @SuppressWarnings("unchecked")
            public Class[] getAdapterList() {
	            return new Class[] {
	            		IOrder.class,
	            		PropertyChangeSupport.class,
	            };
            }
    	}, Order.class);

		Job job = new Job("Trading Service Startup") {
            @Override
            public IStatus run(IProgressMonitor monitor) {
        		tradingService.startUp();
	            return Status.OK_STATUS;
            }
		};
		job.schedule(1000);
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
