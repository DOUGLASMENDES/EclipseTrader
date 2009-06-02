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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.trading.TargetPrice;
import org.eclipsetrader.core.trading.IAlert;
import org.eclipsetrader.core.trading.IAlertService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TargetPriceWizard extends Wizard implements INewWizard {
	private TargetPriceWizardPage page;

	private ISecurity security;

	public TargetPriceWizard() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection.isEmpty())
			throw new IllegalStateException("Nothing selected");

		Object o = selection.getFirstElement();
		if (o instanceof IAdaptable)
			o = ((IAdaptable) o).getAdapter(ISecurity.class);

		if (o == null || !(o instanceof ISecurity))
			throw new IllegalStateException("Not a security");

		security = (ISecurity) o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		return "New Target Price";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
	 */
	@Override
	public Image getDefaultPageImage() {
		return Activator.getDefault().getImageRegistry().get(Activator.ALERT_WIZARD_IMAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(page = new TargetPriceWizardPage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IAlertService service = getAlertService();

		List<IAlert> list = new ArrayList<IAlert>(Arrays.asList(service.getAlerts(security)));

		TargetPrice alert = new TargetPrice();
		alert.setParameters(page.getParametersMap());
		list.add(alert);

		service.setAlerts(security, list.toArray(new IAlert[list.size()]));

		return true;
	}

	IAlertService getAlertService() {
		IAlertService service = null;

		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IAlertService.class.getName());
		if (serviceReference != null) {
			service = (IAlertService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}

		return service;
	}
}
