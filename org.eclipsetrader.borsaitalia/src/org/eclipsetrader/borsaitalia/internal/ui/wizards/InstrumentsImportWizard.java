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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import java.util.Currency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.borsaitalia.internal.Activator;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class InstrumentsImportWizard extends Wizard implements IImportWizard {

    private InstrumentsPage instrumentsPage;
    private MarketsPage marketsPage;

    public InstrumentsImportWizard() {
        setWindowTitle(Messages.InstrumentsImportWizard_WindowTitle);
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/wizban/import_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        addPage(instrumentsPage = new InstrumentsPage());
        addPage(marketsPage = new MarketsPage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final IRepositoryService repository = getRepositoryService();
        repository.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                Instrument[] instruments = instrumentsPage.getInstruments();

                ISecurity[] security = new ISecurity[instruments.length];
                for (int i = 0; i < instruments.length; i++) {
                    FeedProperties properties = new FeedProperties();
                    properties.setProperty(Activator.PROP_CODE, instruments[i].getCode());
                    properties.setProperty(Activator.PROP_ISIN, instruments[i].getIsin());
                    properties.setProperty("org.eclipsetrader.yahoo.symbol", instruments[i].getCode() + ".MI"); //$NON-NLS-1$ //$NON-NLS-2$
                    FeedIdentifier identifier = new FeedIdentifier(instruments[i].getCode(), properties);

                    security[i] = new Stock(!"".equals(instruments[i].getCompany()) ? instruments[i].getCompany() : instruments[i].getCode(), identifier, Currency.getInstance("EUR")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                repository.saveAdaptable(security);

                IMarket[] markets = marketsPage.getSelectedMarkets();
                for (int i = 0; i < markets.length; i++) {
                    markets[i].addMembers(security);
                }

                return Status.OK_STATUS;
            }
        }, null);
        return true;
    }

    protected IRepositoryService getRepositoryService() {
        IRepositoryService service = null;
        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            service = (IRepositoryService) context.getService(serviceReference);
            context.ungetService(serviceReference);
        }
        return service;
    }
}
