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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.core.internal.CoreActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradingSystemsLauncher implements ILauncher, IExecutableExtension {

    private String id;
    private String name;

    public TradingSystemsLauncher() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#launch(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void launch(IProgressMonitor monitor) {
        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference<ITradingSystemService> serviceReference = context.getServiceReference(ITradingSystemService.class);
            if (serviceReference != null) {
                ITradingSystemService service = context.getService(serviceReference);
                service.start();
                context.ungetService(serviceReference);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error starting trading system service", e);
            CoreActivator.log(status);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ILauncher#terminate(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void terminate(IProgressMonitor monitor) {
        try {
            BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
            ServiceReference<ITradingSystemService> serviceReference = context.getServiceReference(ITradingSystemService.class);
            if (serviceReference != null) {
                ITradingSystemService service = context.getService(serviceReference);
                service.stop();
                context.ungetService(serviceReference);
            }
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, 0, "Error stopping trading system service", e);
            CoreActivator.log(status);
        }
    }
}
