/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.markets;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewMarketAction extends Action {
	private Shell shell;

	public NewMarketAction(Shell shell) {
		super("Market");
		this.shell = shell;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
    	MarketWizard wizard = new MarketWizard();
    	WizardDialog dlg = new WizardDialog(shell, wizard);
    	dlg.setMinimumPageSize(450, 300);
    	if (dlg.open() == WizardDialog.OK) {
    		BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(MarketService.class.getName());
    		if (serviceReference != null) {
    			MarketService marketService = (MarketService) context.getService(serviceReference);
    			context.ungetService(serviceReference);
    			if (marketService != null)
    				marketService.addMarket(wizard.getMarket());
    		}
    	}
    }
}
