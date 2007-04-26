/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem.wizards;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.db.Component;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

public class TradingSystemWizard extends Wizard {
	GeneralPage generalPage = new GeneralPage();

	StrategyPage strategyPage = new StrategyPage();

	MarketManagerPage marketManagerPage = new MarketManagerPage();

	SecuritySelectionPage securitySelectionPage = new SecuritySelectionPage();

	public TradingSystemWizard() {
		setWindowTitle("New Trading System Wizard");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(generalPage);
		addPage(strategyPage);
		addPage(marketManagerPage);
		addPage(securitySelectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		TradingSystem system = new TradingSystem();
		system.setName(generalPage.name.getText());
		system.setAccount((Account) ((IStructuredSelection) generalPage.account.getSelection()).getFirstElement());

		IConfigurationElement element = (IConfigurationElement) ((IStructuredSelection) generalPage.tradingProvider.getSelection()).getFirstElement();
		system.setTradingProviderId(element.getAttribute("id"));

		Strategy strategy = new Strategy();

		element = (IConfigurationElement) ((IStructuredSelection) strategyPage.list.getSelection()).getFirstElement();
		strategy.setPluginId(element.getAttribute("id"));

		element = marketManagerPage.getSelection();
		strategy.setMarketManager(new Component(element.getAttribute("id")));

		Object[] items = securitySelectionPage.getSelectedItems().toArray();
		for (int i = 0; i < items.length; i++)
			strategy.addSecurity((Security) items[i]);

		system.addStrategy(strategy);

		ATSPlugin.getRepository().save(system);

		return true;
	}
}
