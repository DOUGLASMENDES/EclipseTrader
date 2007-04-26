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
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

public class StrategyWizard extends Wizard {
	TradingSystem system;

	StrategyPage strategyPage = new StrategyPage();

	MarketManagerPage marketManagerPage = new MarketManagerPage();

	SecuritySelectionPage securitySelectionPage = new SecuritySelectionPage();

	public StrategyWizard(TradingSystem system) {
		this.system = system;
		setWindowTitle("New Strategy Wizard");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(strategyPage);
		addPage(marketManagerPage);
		addPage(securitySelectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		Strategy strategy = new Strategy();

		IConfigurationElement element = (IConfigurationElement) ((IStructuredSelection) strategyPage.list.getSelection()).getFirstElement();
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
