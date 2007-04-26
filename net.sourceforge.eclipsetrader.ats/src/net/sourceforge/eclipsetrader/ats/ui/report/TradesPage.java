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

import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;
import net.sourceforge.eclipsetrader.ats.core.internal.Trade;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TradesPage extends AbstractReportPage {
	Font boldFont;

	Table table;

	TableViewer viewer;

	Label totalTrades;

	Label averageTrade;

	Label winningTotal;

	Label losingTotal;

	Label winningLargest;

	Label losingLargest;

	Label winningAverage;

	Label losingAverage;

	NumberFormat nf = NumberFormat.getInstance();

	/**
	 * Even rows foreground color.
	 */
	Color evenForeground = new Color(null, 0, 0, 0);

	/**
	 * Even rows background color.
	 */
	Color evenBackground = new Color(null, 255, 255, 255);

	/**
	 * Odd rows foreground color.
	 */
	Color oddForeground = new Color(null, 0, 0, 0);

	/**
	 * Odd rows background color.
	 */
	Color oddBackground = new Color(null, 240, 240, 240);

	public TradesPage() {
		super("Trades");
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

		createTradeAnalisysSection();

		table = getToolkit().createTable(getForm().getBody(), SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Symbol");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Security");
		tableColumn.setWidth(100);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Strategy");
		tableColumn.setWidth(170);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Quantity");
		tableColumn.setWidth(60);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Bars");
		tableColumn.setWidth(40);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Enter Price");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Enter Date");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Enter Message");
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Exit Price");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Exit Date");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Exit Message");
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Profit");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Return %");
		tableColumn.setWidth(50);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new TradeLabelProvider());
		viewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (((Trade) e1).getEnterDate().equals(((Trade) e2).getEnterDate()))
					return ((Trade) e1).getExitDate().compareTo(((Trade) e2).getExitDate());
				return ((Trade) e1).getEnterDate().compareTo(((Trade) e2).getEnterDate());
			}
		});
		viewer.setInput(new Object[0]);

		return control;
	}

	protected void createTradeAnalisysSection() {
		Composite parent = getForm().getBody();

		totalTrades = createLabel(parent, "Total Trades", "0");
		createLabel(parent, "Percent Profitable", "0,00%");

		averageTrade = createLabel(parent, "Average Trade", "0");
		createLabel(parent, "", "");

		createLabel(parent, "Standard Dev.", "0");
		createLabel(parent, "Coefficient", "0,00");

		Label label = getToolkit().createLabel(parent, "Winning Trades");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		label.setFont(boldFont);
		label = getToolkit().createLabel(parent, "Losing Trades");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		label.setFont(boldFont);

		winningTotal = createLabel(parent, "Total", "0");
		losingTotal = createLabel(parent, "Total", "0");

		winningLargest = createLabel(parent, "Largest", "0,00");
		losingLargest = createLabel(parent, "Largest", "0,00");

		winningAverage = createLabel(parent, "Average", "0,00");
		losingAverage = createLabel(parent, "Average", "0,00");
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.ui.report.AbstractReportPage#setInput(net.sourceforge.eclipsetrader.ats.core.internal.Backtest)
	 */
	public void setInput(Backtest test) {
		Trade[] trades = test.getTrades();
		double averageAmount = 0;

		int winning = 0, losing = 0;
		double winLargest = 0, loseLargest = 0;
		double winAverage = 0, loseAverage = 0;
		for (int i = 0; i < trades.length; i++) {
			averageAmount += Math.abs(trades[i].getEnterPrice() * trades[i].getQuantity());

			double amount = (trades[i].getExitPrice() * trades[i].getQuantity()) - (trades[i].getEnterPrice() * trades[i].getQuantity());
			if (amount > 0) {
				winning++;
				if (amount > winLargest)
					winLargest = amount;
				winAverage += amount;
			} else if (amount < 0) {
				losing++;
				if (amount < loseLargest)
					loseLargest = amount;
				loseAverage += amount;
			}
		}
		winAverage /= winning;
		loseAverage /= losing;

		totalTrades.setText(String.valueOf(trades.length));
		averageTrade.setText(nf.format(averageAmount / trades.length));

		winningTotal.setText(String.valueOf(winning));
		losingTotal.setText(String.valueOf(losing));
		winningLargest.setText(nf.format(winLargest));
		losingLargest.setText(nf.format(loseLargest));
		winningAverage.setText(nf.format(winAverage));
		losingAverage.setText(nf.format(loseAverage));

		viewer.setInput(trades);
		
		TableColumn[] columns = viewer.getTable().getColumns();
		for (int i = 0; i < columns.length; i++)
			columns[i].pack();
		
		updateColors();

		getForm().reflow(true);
	}

	/**
	 * Updates the table rows background colors. 
	 */
	protected void updateColors() {
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++)
			items[i].setBackground((i & 1) == 0 ? evenBackground : oddBackground);
	}
}
