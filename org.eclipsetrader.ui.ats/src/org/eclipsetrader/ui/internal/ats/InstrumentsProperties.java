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

package org.eclipsetrader.ui.internal.ats;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.views.SecuritySelectionControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class InstrumentsProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private SecuritySelectionControl providers;

    public InstrumentsProperties() {
        setTitle("Instruments");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        initializeDialogUnits(content);

        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        IRepositoryService repositoryService = bundleContext.getService(serviceReference);

        providers = new SecuritySelectionControl(content);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providers.setInput(repositoryService.getSecurities());

        bundleContext.ungetService(serviceReference);

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IStrategy strategy = (IStrategy) getElement().getAdapter(IStrategy.class);
        providers.setSelectedColumns(strategy.getInstruments());
        super.performDefaults();
    }

    protected void applyChanges() {
        ScriptStrategy strategy = (ScriptStrategy) getElement().getAdapter(ScriptStrategy.class);
        if (strategy != null) {
            strategy.setInstruments(providers.getSelection());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        if (getErrorMessage() != null) {
            setErrorMessage(null);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (getControl() != null) {
            applyChanges();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();
        SaveAdaptableHelper.save(getElement());
        super.performApply();
    }
}
