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

package org.eclipsetrader.internal.ui.trading;

import org.eclipse.ui.IStartup;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class EarlyStartup implements IStartup {

    public EarlyStartup() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    @Override
    public void earlyStartup() {
        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IAlertService.class.getName());
        if (serviceReference != null) {
            IAlertService service = (IAlertService) context.getService(serviceReference);
            service.addAlertListener(new AlertNotificationListener());
            context.ungetService(serviceReference);
        }
    }
}
