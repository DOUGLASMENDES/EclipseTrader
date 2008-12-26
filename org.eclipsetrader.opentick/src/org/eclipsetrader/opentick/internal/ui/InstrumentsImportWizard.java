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

package org.eclipsetrader.opentick.internal.ui;

import java.util.Currency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.Exchange;
import org.eclipsetrader.opentick.internal.core.repository.IdentifiersList;
import org.eclipsetrader.opentick.internal.core.repository.Instrument;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.otfeed.event.InstrumentEnum;

public class InstrumentsImportWizard extends Wizard implements IImportWizard {
	private InstrumentsPage instrumentsPage;
	private MarketsPage marketsPage;

	public InstrumentsImportWizard() {
    	setWindowTitle("Import Instruments");
    	setDefaultPageImageDescriptor(OTActivator.imageDescriptorFromPlugin(OTActivator.PLUGIN_ID, "icons/wizban/import_wiz.png"));
    	setNeedsProgressMonitor(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
	    addPage(new ExchangePage());
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
            public IStatus run(IProgressMonitor monitor) throws Exception {
        		Exchange exchange = instrumentsPage.getExchange();
        		Instrument[] instruments = instrumentsPage.getInstruments();
        		Integer type = instrumentsPage.getType();

        		ISecurity[] security = new ISecurity[instruments.length];
        		for (int i = 0; i < instruments.length; i++) {
        			FeedProperties properties = new FeedProperties();
        			properties.setProperty(IdentifiersList.SYMBOL_PROPERTY, instruments[i].getCode());
        			properties.setProperty(IdentifiersList.EXCHANGE_PROPERTY, exchange.getCode());
        			properties.setProperty("org.eclipsetrader.yahoo.symbol", instruments[i].getCode());
        			FeedIdentifier identifier = new FeedIdentifier(instruments[i].getCode(), properties);

        			if (type == InstrumentEnum.STOCK.code)
            			security[i] = new Stock(!"".equals(instruments[i].getCompany()) ? instruments[i].getCompany(): instruments[i].getCode(), identifier, Currency.getInstance("USD"));
        			else
            			security[i] = new Security(!"".equals(instruments[i].getCompany()) ? instruments[i].getCompany(): instruments[i].getCode(), identifier);
        		}
        		repository.saveAdaptable(security);

        		IMarket[] markets = marketsPage.getSelectedMarkets();
				for (int i = 0; i < markets.length; i++)
					markets[i].addMembers(security);

        		return Status.OK_STATUS;
            }
		}, null);
		return true;
	}

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = OTActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}
}
