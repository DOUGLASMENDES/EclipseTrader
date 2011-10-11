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

package org.eclipsetrader.ui.internal.ats.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.ats.Activator;
import org.eclipsetrader.ui.internal.views.SecuritySelectionControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class InstrumentsPage extends WizardPage {

    private SecuritySelectionControl providers;

    public InstrumentsPage() {
        super("instruments", "Instruments", null);
        setDescription("Sets the instruments");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        setControl(content);
        initializeDialogUnits(content);

        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        IRepositoryService repositoryService = bundleContext.getService(serviceReference);

        providers = new SecuritySelectionControl(content);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providers.setInput(repositoryService.getSecurities());

        bundleContext.ungetService(serviceReference);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        providers.getControl().setFocus();
        super.setVisible(visible);
    }

    public ISecurity[] getInstruments() {
        return providers.getSelection();
    }
}
