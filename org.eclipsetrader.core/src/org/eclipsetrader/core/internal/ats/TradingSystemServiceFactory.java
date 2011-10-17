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

import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class TradingSystemServiceFactory implements ServiceFactory {

    private final IRepositoryService repositoryService;
    private TradingSystemService serviceInstance;

    public TradingSystemServiceFactory(IRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
     */
    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration) {
        if (serviceInstance == null) {
            BundleContext bundleContext = bundle.getBundleContext();
            ServiceReference<IMarketService> marketServiceReference = bundleContext.getServiceReference(IMarketService.class);
            IMarketService marketService = bundleContext.getService(marketServiceReference);
            serviceInstance = new TradingSystemService(repositoryService, marketService);
            try {
                serviceInstance.startUp();
            } catch (Exception e) {
                CoreActivator.log("Error starting market service", e);
            }
        }
        return serviceInstance;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, java.lang.Object)
     */
    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    }

    public void dispose() {
        if (serviceInstance != null) {
            try {
                serviceInstance.shutDown();
            } catch (Exception e) {
                CoreActivator.log("Error stopping market service", e);
            }
        }
    }
}
