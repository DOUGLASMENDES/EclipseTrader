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

package org.eclipsetrader.internal.brokers.paper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PaperBrokerFactory implements IExecutableExtension, IExecutableExtensionFactory {

    private static PaperBroker instance;

    private IConfigurationElement config;

    private ServiceReference<IMarketService> marketServiceReference;
    private IMarketService marketService;
    private ServiceReference<IRepositoryService> repositoryServiceReference;
    private IRepositoryService repositoryService;

    public PaperBrokerFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    @Override
    public Object create() throws CoreException {
        if (instance == null) {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();

            marketServiceReference = context.getServiceReference(IMarketService.class);
            if (marketServiceReference != null) {
                marketService = context.getService(marketServiceReference);
            }

            repositoryServiceReference = context.getServiceReference(IRepositoryService.class);
            if (repositoryServiceReference != null) {
                repositoryService = context.getService(repositoryServiceReference);
            }

            String id = config.getAttribute("id");
            String name = config.getAttribute("name");

            instance = new PaperBroker(id, name, marketService, repositoryService);
            try {
                instance.load(Activator.getDefault().getStateLocation().append("monitors.xml").toFile());
            } catch (Exception e) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error loading monitors", e); //$NON-NLS-1$
                Activator.log(status);
            }
        }
        return instance;
    }

    public void dispose() {
        try {
            if (instance != null) {
                instance.save(Activator.getDefault().getStateLocation().append("monitors.xml").toFile());

                BundleContext context = Activator.getDefault().getBundle().getBundleContext();
                if (marketServiceReference != null) {
                    context.ungetService(marketServiceReference);
                }
                if (repositoryServiceReference != null) {
                    context.ungetService(repositoryServiceReference);
                }
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error saving monitors", e); //$NON-NLS-1$
            Activator.log(status);
        }
        instance = null;
    }
}
