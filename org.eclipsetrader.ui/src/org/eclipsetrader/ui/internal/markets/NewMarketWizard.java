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

package org.eclipsetrader.ui.internal.markets;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewMarketWizard extends MarketWizard implements INewWizard {

	public NewMarketWizard() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(MarketService.class.getName());
		marketService = (MarketService) context.getService(serviceReference);
		context.ungetService(serviceReference);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.internal.markets.MarketWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();

		getMarketService().addMarket(getMarket());

		return result;
	}
}
