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

package org.eclipsetrader.ui.internal.ats.monitor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemService;
import org.eclipsetrader.ui.internal.ats.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StartStrategyHandler extends AbstractHandler {

    public StartStrategyHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

        if (selection != null && !selection.isEmpty()) {
            BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference<ITradingSystemService> serviceReference = bundleContext.getServiceReference(ITradingSystemService.class);

            ITradingSystemService service = bundleContext.getService(serviceReference);

            for (Object target : selection.toList()) {
                if (target instanceof IAdaptable) {
                    target = ((IAdaptable) target).getAdapter(ITradingSystem.class);
                }
                if (target instanceof ITradingSystem) {
                    ITradingSystem tradingSystem = (ITradingSystem) target;
                    service.start(tradingSystem);
                }
            }

            bundleContext.ungetService(serviceReference);
        }

        return null;
    }
}
