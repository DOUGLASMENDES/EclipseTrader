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

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradeSystemService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

    public static final String PLUGIN_ID = "org.eclipsetrader.core.ats";

    public static final String STRATEGIES_EXTENSION_ID = "org.eclipsetrader.core.ats.systems";

    private static Activator plugin;

    public static Activator getDefault() {
        return plugin;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        final TradeSystemService tradeSystemService = new TradeSystemService();
        context.registerService(new String[] {
            ITradeSystemService.class.getName(),
            TradeSystemService.class.getName()
        }, tradeSystemService, new Hashtable<String, Object>());
        //tradeSystemService.startUp();

        Platform.getAdapterManager().registerAdapters(tradeSystemService, ITradingSystem.class);
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
            if (service != null) {
                //service.shutDown();
            }
            context.ungetService(serviceReference);
        }

        context = null;
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
}
