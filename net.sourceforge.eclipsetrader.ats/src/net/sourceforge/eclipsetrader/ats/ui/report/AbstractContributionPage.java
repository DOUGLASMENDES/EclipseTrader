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

public abstract class AbstractContributionPage extends AbstractReportPage {
	String description;

	Font boldFont;

	Table table;

	TableViewer viewer;

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

	public AbstractContributionPage(String title) {
		super(title);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.ui.report.AbstractReportPage#createControls(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControls(Composite parent) {
		Control control = super.createControls(parent);

		Font font = parent.getFont();
		FontData fontData = font.getFontData()[0];
		boldFont = new Font(parent.getDisplay(), fontData.getName(), fontData.getHeight(), SWT.BOLD);

		if (description != null) {
			Label label = getToolkit().createLabel(getForm().getBody(), description);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		}

		table = getToolkit().createTable(getForm().getBody(), SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("");
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Net Profit");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Gross Profit");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Gross Loss");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("% Profitable");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Avg. Trade");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Total Trades");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Winning Trades");
		tableColumn.setWidth(80);
		tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setText("Losing Trades");
		tableColumn.setWidth(80);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ContributionStatisticLabelProvider());
		viewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((ContributionStatistic) e1).name.compareTo(((ContributionStatistic) e2).name);
			}
		});
		viewer.setInput(new Object[0]);

		return control;
	}

	/**
	 * Updates the table rows background colors. 
	 */
	protected void updateColors() {
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++)
			items[i].setBackground((i & 1) == 0 ? evenBackground : oddBackground);
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
