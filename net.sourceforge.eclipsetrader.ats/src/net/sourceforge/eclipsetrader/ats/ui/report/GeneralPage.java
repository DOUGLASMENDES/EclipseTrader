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

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;
import net.sourceforge.eclipsetrader.ats.core.internal.Trade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class GeneralPage extends AbstractReportPage {
	Font boldFont;

	NumberFormat nf = NumberFormat.getInstance();

	Label strategies;

	Label securities;

	Label netProfit;

	Label grossProfit;

	Label grossLoss;

	Label timeInMarket;

	Label percentInMarket;

	public GeneralPage() {
		super("General");
		nf.setGroupingUsed(true);
		nf.setMinimumIntegerDigits(1);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.ui.report.AbstractReportPage#createControls(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControls(Composite parent) {
		Control control = super.createControls(parent);

		Font font = parent.getFont();
		FontData fontData = font.getFontData()[0];
		boldFont = new Font(parent.getDisplay(), fontData.getName(), fontData.getHeight(), SWT.BOLD);

		strategies = createLabel(getForm().getBody(), "Strategies", "0");
		securities = createLabel(getForm().getBody(), "Securities", "0");

		netProfit = createLabel(getForm().getBody(), "Net Profit", "0,00");
		createLabel(getForm().getBody(), "", "");

		grossProfit = createLabel(getForm().getBody(), "Gross Profit", "0,00");
		grossLoss = createLabel(getForm().getBody(), "Gross Loss", "0,00");

		createTimeAnalisysSection();

		return control;
	}

	protected void createTimeAnalisysSection() {
		Composite parent = createSection(getForm().getBody(), "Time Analisys");

		timeInMarket = createLabel(parent, "Time in Market", "0");
		createLabel(parent, "Max Positions", "0");

		percentInMarket = createLabel(parent, "Percent in Market", "0,00%");
		createLabel(parent, "Longest Flat Period", "0");

		createLabel(parent, "Average Time in Trades", "0");
		createLabel(parent, "Average between Trades", "0");
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.ui.report.AbstractReportPage#setInput(net.sourceforge.eclipsetrader.ats.core.internal.Backtest)
	 */
	public void setInput(Backtest test) {
		Set strategySet = new HashSet();
		Set securitySet = new HashSet();
		Trade[] trades = test.getTrades();

		int inMarket = 0;
		double profit = 0, loss = 0;

		for (int i = 0; i < trades.length; i++) {
			strategySet.add(trades[i].getStrategy());
			securitySet.add(trades[i].getSecurity());
			inMarket += trades[i].getBars();

			double amount = (trades[i].getExitPrice() * trades[i].getQuantity()) - (trades[i].getEnterPrice() * trades[i].getQuantity());
			if (amount > 0)
				profit += amount;
			else if (amount < 0)
				loss += amount;
		}
		strategies.setText(String.valueOf(strategySet.size()));
		securities.setText(String.valueOf(securitySet.size()));

		netProfit.setText(nf.format(profit + loss));
		grossProfit.setText(nf.format(profit));
		grossLoss.setText(nf.format(loss));

		timeInMarket.setText(String.valueOf(inMarket));
		percentInMarket.setText(nf.format((double) inMarket / test.getTotalBars() * 100.0));

		getForm().reflow(true);
	}
}
