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

import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class CurrencyServiceFactory implements ServiceFactory {

    private CurrencyService serviceInstance;

    public CurrencyServiceFactory() {
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)
     */
    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration) {
        if (serviceInstance == null) {
            IRepositoryService repositoryService = null;
            IMarketService marketService = null;

            try {
                BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();

                ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
                if (serviceReference != null) {
                    repositoryService = (IRepositoryService) context.getService(serviceReference);
                    context.ungetService(serviceReference);
                }

                serviceReference = context.getServiceReference(IMarketService.class.getName());
                if (serviceReference != null) {
                    marketService = (IMarketService) context.getService(serviceReference);
                    context.ungetService(serviceReference);
                }

                serviceInstance = new CurrencyService(repositoryService, marketService);
                serviceInstance.startUp(null);
            } catch (Exception e) {
                CoreActivator.log("Error starting currency service", e);
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
                serviceInstance.shutDown(null);
            } catch (Exception e) {
                CoreActivator.log("Error stopping currency service", e);
            }
        }
    }
}
