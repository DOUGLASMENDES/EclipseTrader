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

package org.eclipsetrader.ui.internal.markets;

import java.util.Arrays;

import org.eclipse.jface.wizard.Wizard;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketService;

public class MarketWizard extends Wizard {

    GeneralWizardPage generalPage;
    ScheduleWizardPage schedulePage;
    ConnectorsWizardPage connectorsPage;

    MarketService marketService;

    Market market;

    protected MarketWizard() {
    }

    public MarketWizard(MarketService marketService) {
        this.marketService = marketService;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        setWindowTitle("New Market");
        addPage(generalPage = new GeneralWizardPage(marketService));
        addPage(schedulePage = new ScheduleWizardPage());
        addPage(connectorsPage = new ConnectorsWizardPage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        market = new Market(generalPage.getMarketName(), Arrays.asList(schedulePage.getSchedule()), schedulePage.getTimeZone());
        market.setWeekDays(schedulePage.getWeekDays());
        market.setLiveFeedConnector(connectorsPage.getLiveFeedConnector());
        return true;
    }

    public MarketService getMarketService() {
        return marketService;
    }

    public Market getMarket() {
        return market;
    }
}
