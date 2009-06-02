/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.ui.trading;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.trading.Activator;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ResetTriggeredAlerts extends AbstractHandler {

	public ResetTriggeredAlerts() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection == null || selection.isEmpty())
			return null;
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();

		ServiceReference serviceReference = context.getServiceReference(IAlertService.class.getName());
		if (serviceReference != null) {
			IAlertService service = (IAlertService) context.getService(serviceReference);
			for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
				Object target = iter.next();
				if (target instanceof IAdaptable)
					target = ((IAdaptable) target).getAdapter(ISecurity.class);
				if (target instanceof ISecurity)
					service.resetTriggers((ISecurity) target);
			}
			context.ungetService(serviceReference);
		}

		return null;
	}
}
