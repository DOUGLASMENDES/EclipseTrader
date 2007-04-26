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

import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class StatisticsPage {
	String title;

	CTabFolder tabFolder;

	GeneralPage statisticsPage = new GeneralPage();

	TradesPage tradesPage = new TradesPage();

	StrategiesPage strategiesPage = new StrategiesPage();

	SecuritiesPage securitiesPage = new SecuritiesPage();

	Backtest test;

	public StatisticsPage(String title) {
		this.title = title;
	}

	public void createPartControl(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("General");
		tabItem.setControl(statisticsPage.createControls(tabFolder));
		tabFolder.setSelection(tabItem);

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("Trades");
		tabItem.setControl(tradesPage.createControls(tabFolder));

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("Strategies");
		tabItem.setControl(strategiesPage.createControls(tabFolder));

		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("Securities");
		tabItem.setControl(securitiesPage.createControls(tabFolder));
	}

	public void setFocus() {
		tabFolder.setFocus();
	}

	public void setInput(Backtest test) {
		this.test = test;
		tradesPage.setInput(test);
		statisticsPage.setInput(test);
		strategiesPage.setInput(test);
		securitiesPage.setInput(test);
	}

	public Control getControl() {
		return tabFolder;
	}

	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return title.equals(((StatisticsPage) o).title);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return title.hashCode();
	}
}
