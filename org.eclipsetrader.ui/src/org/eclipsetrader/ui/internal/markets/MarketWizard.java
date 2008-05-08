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

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.core.internal.markets.Market;

public class MarketWizard extends Wizard implements INewWizard {
	private GeneralWizardPage generalPage;
	private ConnectorsWizardPage connectorsPage;
	private Market market;

	public MarketWizard() {
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
    	setWindowTitle("New Market");
	    addPage(generalPage = new GeneralWizardPage());
	    addPage(connectorsPage = new ConnectorsWizardPage());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		market = new Market(generalPage.getMarketName(), Arrays.asList(generalPage.getSchedule()), generalPage.getTimeZone());
		market.setWeekDays(generalPage.getWeekDays());
	    market.setLiveFeedConnector(connectorsPage.getLiveFeedConnector());
		return true;
	}

	public Market getMarket() {
    	return market;
    }
}
