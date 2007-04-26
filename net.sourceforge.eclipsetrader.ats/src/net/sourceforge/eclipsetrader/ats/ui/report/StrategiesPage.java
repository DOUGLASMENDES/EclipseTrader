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

package net.sourceforge.eclipsetrader.ats.ui.report;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.TableColumn;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;
import net.sourceforge.eclipsetrader.ats.core.internal.Trade;

public class StrategiesPage extends AbstractContributionPage {

	public StrategiesPage() {
		super("Strategies");
		setDescription("Individual strategy contribution");
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.ui.report.AbstractReportPage#setInput(net.sourceforge.eclipsetrader.ats.core.internal.Backtest)
	 */
	public void setInput(Backtest test) {
		Map stats = new HashMap();

		Trade[] trades = test.getTrades();
		for (int i = 0; i < trades.length; i++) {
			String name = trades[i].getStrategy().getName();
			if (name == null)
				name = ATSPlugin.getStrategyPluginName(trades[i].getStrategy().getPluginId());

			ContributionStatistic c = (ContributionStatistic) stats.get(name);
			if (c == null) {
				c = new ContributionStatistic(name);
				stats.put(c.name, c);
			}

			double amount = (trades[i].getExitPrice() * trades[i].getQuantity()) - (trades[i].getEnterPrice() * trades[i].getQuantity());
			if (amount > 0) {
				c.grossProfit += amount;
				c.winningTrades++;
			} else if (amount < 0) {
				c.grossLoss += amount;
				c.losingTrades++;
			}
			c.totalAmount += Math.abs(trades[i].getEnterPrice() * trades[i].getQuantity());
		}

		viewer.setInput(stats.values().toArray());

		TableColumn[] columns = viewer.getTable().getColumns();
		for (int i = 0; i < columns.length; i++)
			columns[i].pack();

		updateColors();

		getForm().reflow(true);
	}
}
